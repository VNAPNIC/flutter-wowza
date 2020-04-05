package com.namit.flutter_wowza

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wowza.gocoder.sdk.api.WowzaGoCoder
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig
import com.wowza.gocoder.sdk.api.devices.WOWZAudioDevice
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView
import com.wowza.gocoder.sdk.api.geometry.WOWZSize
import com.wowza.gocoder.sdk.api.h264.WOWZProfileLevel
import com.wowza.gocoder.sdk.api.player.WOWZPlayerConfig
import com.wowza.gocoder.sdk.api.status.WOWZBroadcastStatus
import com.wowza.gocoder.sdk.api.status.WOWZBroadcastStatusCallback
import com.wowza.gocoder.sdk.support.status.WOWZStatus
import com.wowza.gocoder.sdk.support.status.WOWZStatusCallback
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.platform.PlatformView


class FlutterWOWZCameraView internal
constructor(private val context: Context?, private val registrar: PluginRegistry.Registrar,
            private val methodChannel: MethodChannel, id: Int?, params: Map<String, Any>?) :
        PlatformView, MethodChannel.MethodCallHandler,
        WOWZBroadcastStatusCallback, WOWZStatusCallback,
        PluginRegistry.RequestPermissionsResultListener {

    private val TAG = FlutterWOWZCameraView::class.java.simpleName

    private var mPermissionsGranted = false
    private var hasRequestedPermissions = false
    private var videoIsInitialized = false
    private var audioIsInitialized = false

    private val PERMISSIONS_REQUEST_CODE = 0x1

    private var mRequiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.READ_PHONE_STATE
    )

    // Make WOWZCameraView
    private val goCoderCameraView: WOWZCameraView = WOWZCameraView(context)

    // The top-level GoCoder API interface
    private var goCoder: WowzaGoCoder? = null

    // The GoCoder SDK audio device
    private var goCoderAudioDevice: WOWZAudioDevice? = null

    // The GoCoder SDK broadcaster
    private var goCoderBroadcaster: WOWZBroadcast? = null

    // The broadcast configuration settings
    private var goCoderBroadcastConfig: WOWZBroadcastConfig? = null

    init {
        registrar.addRequestPermissionsResultListener(this)

        methodChannel.setMethodCallHandler(this)
        // Create a broadcaster instance
        goCoderBroadcaster = WOWZBroadcast()
        // Create a configuration instance for the broadcaster
        goCoderBroadcastConfig = WOWZBroadcastConfig()
    }

    override fun getView(): View {
        return goCoderCameraView
    }

    override fun dispose() {
        goCoderBroadcaster?.endBroadcast(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        Log.i(TAG, "Channel: method: ${call.method} | arguments: ${call.arguments}")

        val activeCamera = this.goCoderCameraView.camera

        when (call.method) {
            "api_licenseKey" -> {
                if (requestPermissionToAccess())
                    onPermissionResult(true)
                goCoder = WowzaGoCoder.init(context, call.arguments.toString())
            }
            "push_config" -> {
                if (requestPermissionToAccess())
                    onPermissionResult(true)
                goCoderBroadcastConfig?.set(makeConfig(call.arguments as String?))
                Log.i("FlutterWOWZCameraView", "${(goCoderBroadcastConfig as WOWZMediaConfig).toString()}")
            }
            "start_broadcast" -> {
                goCoderBroadcaster?.startBroadcast(makeConfig(call.arguments as String?), this)
                Log.i("FlutterWOWZCameraView", "${(goCoderBroadcastConfig as WOWZMediaConfig).toString()}")
            }
            "end_broadcast" -> {
                goCoderBroadcaster?.endBroadcast(this)
            }
            "open_camera" -> {
                if (requestPermissionToAccess()) {
                    if (videoIsInitialized && audioIsInitialized && !goCoderCameraView.isPreviewing)
                        goCoderCameraView.startPreview(makeConfig(call.arguments as String?))
                    else
                        onPermissionResult(true)
                }
            }
            "stop_camera" -> {
                goCoderCameraView.stopPreview()
            }
            "switch_camera" -> {
                goCoderCameraView.switchCamera()
            }
            "flash_light" -> {
                activeCamera.isTorchOn = call.arguments == "true"
            }

            "scale_mode" -> {
                val scale = when (call.arguments.toString()) {
                    "ScaleMode.FILL_VIEW" -> WOWZMediaConfig.FILL_VIEW
                    else -> WOWZMediaConfig.RESIZE_TO_ASPECT
                }

                goCoderCameraView.scaleMode = scale
            }
        }
    }

    private fun makeConfig(dataMap: String?): WOWZBroadcastConfig? {
        val config = WOWZBroadcastConfig()

        dataMap?.let { json ->
            val retMap: Map<String, Any> = Gson().fromJson(
                    json, object : TypeToken<HashMap<String?, Any?>?>() {}.type
            )
            if (retMap["abrActive"] != null)
                config.isABREnabled = (retMap["abrActive"] as String) == "true"

            // Stream  config
            if (retMap["hostAddress"] != null)
                config.hostAddress = retMap["hostAddress"] as String
            if (retMap["applicationName"] != null)
                config.applicationName = retMap["applicationName"] as String
            if (retMap["streamName"] != null)
                config.streamName = retMap["streamName"] as String
            if (retMap["portNumber"] != null)
                config.portNumber = (retMap["portNumber"] as Double).toInt()
            if (retMap["username"] != null)
                config.username = retMap["username"] as String
            if (retMap["password"] != null)
                config.password = retMap["password"] as String

            // player config
            if (retMap["isPlayback"] != null)
                config.isPlayback = (retMap["isPlayback"] as String) == "true"
            if (retMap["hlsEnabled"] != null)
                config.isHLSEnabled = (retMap["hlsEnabled"] as String) == "true"
            if (retMap["hlsBackupUrl"] != null)
                config.hlsBackupURL = retMap["hlsBackupUrl"] as String

            // video
            if (retMap["videoEnabled"] != null)
                config.isVideoEnabled = (retMap["videoEnabled"] as String) == "true"
            if (retMap["videoFrameWidth"] != null)
                config.videoFrameWidth = (retMap["videoFrameWidth"] as Double).toInt()
            if (retMap["videoFrameHeight"] != null)
                config.videoFrameHeight = (retMap["videoFrameHeight"] as Double).toInt()
            if (retMap["videoBitRate"] != null)
                config.videoBitRate = (retMap["videoBitRate"] as Double).toInt()
            if (retMap["videoFramerate"] != null)
                config.videoFramerate = (retMap["videoFramerate"] as Double).toInt()
            if (retMap["videoKeyFrameInterval"] != null)
                config.videoKeyFrameInterval = (retMap["videoKeyFrameInterval"] as Double).toInt()
            if (retMap["videoProfile"] != null)
                if (retMap["videoProfileLevel"] != null) {
                    config.videoProfileLevel = WOWZProfileLevel((retMap["videoProfile"] as Double).toInt(), (retMap["videoProfileLevel"] as Double).toInt())
                } else {
                    config.videoProfileLevel = WOWZProfileLevel((retMap["videoProfile"] as Double).toInt())
                }

            // audio
            if (retMap["audioEnabled"] != null)
                config.isVideoEnabled = (retMap["audioEnabled"] as String) == "true"
            if (retMap["audioChannels"] != null)
                config.audioChannels = (retMap["audioChannels"] as Double).toInt()
            if (retMap["audioSampleRate"] != null)
                config.audioSampleRate = (retMap["audioSampleRate"] as Double).toInt()
            if (retMap["audioBitrate"] != null)
                config.audioBitRate = (retMap["audioBitrate"] as Double).toInt()

            //abr
            if (retMap["abrEnabled"] != null)
                config.isABREnabled = (retMap["abrEnabled"] as String) == "true"
            if (retMap["vbeFrameBufferSizeMultiplier"] != null)
                config.frameBufferSizeMultiplier = (retMap["vbeFrameBufferSizeMultiplier"] as Double).toInt()
            if (retMap["vbeFrameRateLowBandwidthSkipCount"] != null)
                config.frameRateLowBandwidthSkipCount = (retMap["vbeFrameRateLowBandwidthSkipCount"] as Double).toInt()
            return config
        }
        config.resetToDefaults()
        return config
    }

    override fun onWZStatus(status: WOWZBroadcastStatus?) {
        // A successful status transition has been reported by the GoCoder SDK
        val statusMessage = StringBuffer("Broadcast status: ")

        when (status?.state) {
            WOWZBroadcastStatus.BroadcastState.READY -> {
                statusMessage.append("Ready to begin broadcasting")
            }
            WOWZBroadcastStatus.BroadcastState.BROADCASTING -> {
                statusMessage.append("Broadcast is active")
            }
            WOWZBroadcastStatus.BroadcastState.IDLE -> {
                statusMessage.append("The broadcast is stopped")
            }
            else -> return
        }
        // Display the status message using the U/I thread
        Handler(Looper.getMainLooper()).post {
            methodChannel.invokeMethod("broadcast_status", "{\"state\":\"${status?.state?.name}\",\"message\":\"${statusMessage.toString()}\"}")
            Log.i(TAG, "broadcast_status: ${statusMessage.toString()}")
        }
    }

    override fun onWZError(status: WOWZBroadcastStatus?) {
        // Display the status message using the U/I thread
        Handler(Looper.getMainLooper()).post {
            methodChannel.invokeMethod("broadcast_error", "{\"state\":\"${status?.state?.name}\",\"message\":\"${status?.lastError?.errorDescription}\"}")
            Log.i(TAG, "broadcast_error: ${status?.lastError?.errorDescription}")
        }
    }

    // wowz_status
    override fun onWZStatus(status: WOWZStatus?) {
        // Display the status message using the U/I thread
    }

    override fun onWZError(status: WOWZStatus?) {
        // Display the status message using the U/I thread
    }

    private fun requestPermissionToAccess(): Boolean {
        var result = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            result = if (mRequiredPermissions.isNotEmpty()) checkPermissionToAccess(mRequiredPermissions) else true

            if (!result && !hasRequestedPermissions) {
                ActivityCompat.requestPermissions(registrar.activity(),
                        mRequiredPermissions,
                        PERMISSIONS_REQUEST_CODE)
                hasRequestedPermissions = true
            }
        }
        return result
    }

    private fun requestPermissionToAccess(source: String): Boolean {
        var result = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            result = if (mRequiredPermissions.isNotEmpty()) checkPermissionToAccess(source) else true
            if (!result && !hasRequestedPermissions) {
                ActivityCompat.requestPermissions(registrar.activity(),
                        mRequiredPermissions,
                        PERMISSIONS_REQUEST_CODE)
                hasRequestedPermissions = true
            }
        }
        return result
    }

    private fun checkPermissionToAccess(permissions: Array<String>): Boolean {
        var result = true
        if (goCoderBroadcaster != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (permission in permissions) {
                    if (ContextCompat.checkSelfPermission(registrar.activity(), permission) == PackageManager.PERMISSION_DENIED) {
                        result = false
                    }
                }
            }
        } else {
            Log.e(TAG, "goCoderBroadcaster is null!")
            result = false
        }
        return result
    }

    private fun checkPermissionToAccess(source: String): Boolean {
        if (goCoderBroadcaster != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(registrar.activity(), source) == PackageManager.PERMISSION_DENIED) {
                    return false
                }
            }
        } else {
            Log.e(TAG, "goCoderBroadcaster is null!")
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?): Boolean {
        mPermissionsGranted = true
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                for (grantResult in grantResults!!) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mPermissionsGranted = false
                    }
                }
                hasRequestedPermissions = false
                onPermissionResult(mPermissionsGranted)
            }
        }

        Log.i(TAG, "onRequestPermissionsResult  has requested: $hasRequestedPermissions")
        return true
    }

    private fun onPermissionResult(mPermissionsGranted: Boolean) {
        if (goCoder != null) {
            if (mPermissionsGranted) {
                // Initialize the camera preview
                if (requestPermissionToAccess(Manifest.permission.CAMERA)) {
                    if (!videoIsInitialized) {
                        val availableCameras = goCoderCameraView.cameras
                        // Ensure we can access to at least one camera
                        if (availableCameras.isNotEmpty()) {
                            // Set the video broadcaster in the broadcast config
                            goCoderBroadcastConfig?.videoBroadcaster = goCoderCameraView
                            videoIsInitialized = true
                            Log.i(TAG, "*** getOriginalFrameSizes - Get original frame size : ")
                        } else {
                            Log.e(TAG, "Could not detect or gain access to any cameras on this device")
                            goCoderBroadcastConfig?.isVideoEnabled = false
                        }
                    }
                } else {
                    Log.e(TAG, "Exception Fail to connect to camera service. I checked camera permission in Settings")
                }

                if (requestPermissionToAccess(Manifest.permission.RECORD_AUDIO)) {
                    if (!audioIsInitialized) {
                        // Create an audio device instance for capturing and broadcasting audio
                        goCoderAudioDevice = WOWZAudioDevice()
                        // Set the audio broadcaster in the broadcast config
                        goCoderBroadcastConfig?.audioBroadcaster = goCoderAudioDevice
                        audioIsInitialized = true
                    }
                } else {
                    Log.e(TAG, "Exception Fail to connect to record audio service. I checked camera permission in Settings")
                }

                if (videoIsInitialized && audioIsInitialized) {
                    Log.i(TAG, "startPreview")
                    if (!goCoderCameraView.isPreviewing)
                        goCoderCameraView.startPreview()
                }
            }
        } else {
            Log.e(TAG, "goCoder is null!, Please check the license key GoCoder SDK, maybe your license key GoCoder SDK is wrong!")
        }
    }
}