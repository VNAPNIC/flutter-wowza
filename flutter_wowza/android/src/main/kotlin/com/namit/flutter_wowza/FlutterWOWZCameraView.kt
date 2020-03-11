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
import com.wowza.gocoder.sdk.api.WowzaGoCoder
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig
import com.wowza.gocoder.sdk.api.devices.WOWZAudioDevice
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView
import com.wowza.gocoder.sdk.api.geometry.WOWZSize
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
        PlatformView, MethodChannel.MethodCallHandler, WOWZBroadcastStatusCallback, WOWZStatusCallback, PluginRegistry.RequestPermissionsResultListener {

    //define callback interface
    interface PermissionCallbackInterface {
        fun onPermissionResult(result: Boolean)
    }

    private var mPermissionsGranted = false
    private var hasRequestedPermissions = false

    private val PERMISSIONS_REQUEST_CODE = 0x1

    private var mRequiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.READ_PHONE_STATE
    )

    private var callbackFunction: PermissionCallbackInterface? = null

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
        Log.i("FlutterWOWZCameraView", "Channel: method: ${call.method} | arguments: ${call.arguments}")

        val activeCamera = this.goCoderCameraView.camera

        when (call.method) {
            "api_licenseKey" -> goCoder = WowzaGoCoder.init(context, call.arguments.toString())

            "_host_address" -> goCoderBroadcastConfig?.hostAddress = call.arguments.toString()

            "port_number" -> goCoderBroadcastConfig?.portNumber = call.arguments.toString().toInt()

            "application_name" -> goCoderBroadcastConfig?.applicationName = call.arguments.toString()

            "stream_name" -> goCoderBroadcastConfig?.streamName = call.arguments.toString()

            "username" -> goCoderBroadcastConfig?.username = call.arguments.toString()

            "password" -> goCoderBroadcastConfig?.password = call.arguments.toString()

            "wowz_size" -> {
                val wowzSize = call.arguments.toString().split("/")
                if (wowzSize.size > 1)
                    goCoderCameraView.frameSize = WOWZSize(wowzSize[0].trim().toInt(), wowzSize[1].trim().toInt())
            }

            "_wowz_media_config" -> {

                val frame = when (call.arguments.toString()) {
                    "FRAME_SIZE_176x144" -> WOWZMediaConfig.FRAME_SIZE_176x144
                    "FRAME_SIZE_320x240" -> WOWZMediaConfig.FRAME_SIZE_320x240
                    "FRAME_SIZE_352x288" -> WOWZMediaConfig.FRAME_SIZE_352x288
                    "FRAME_SIZE_640x480" -> WOWZMediaConfig.FRAME_SIZE_640x480
                    "FRAME_SIZE_960x540" -> WOWZMediaConfig.FRAME_SIZE_960x540
                    "FRAME_SIZE_1280x720" -> WOWZMediaConfig.FRAME_SIZE_1280x720
                    "FRAME_SIZE_1440x1080" -> WOWZMediaConfig.FRAME_SIZE_1440x1080
                    "FRAME_SIZE_1920x1080" -> WOWZMediaConfig.FRAME_SIZE_1920x1080
                    "FRAME_SIZE_3840x2160" -> WOWZMediaConfig.FRAME_SIZE_3840x2160
                    else -> WOWZMediaConfig.FRAME_SIZE_640x480
                }
                goCoderBroadcastConfig?.set(frame)
            }

            "scale_mode" -> {
                val scale = when (call.arguments.toString()) {
                    "FILL_VIEW" -> WOWZMediaConfig.FILL_VIEW
                    else -> WOWZMediaConfig.RESIZE_TO_ASPECT
                }

                goCoderCameraView.scaleMode = scale
            }

            "start_preview" -> {
                if (goCoder != null) {
                    var videoIsInitialized = false
                    var audioIsInitialized = false
                    this.hasDevicePermissionToAccess(object : PermissionCallbackInterface {
                        override fun onPermissionResult(result: Boolean) {

                            Log.i("FlutterWOWZCameraView", "onPermissionResult $result")

                            if (result) {
                                // Initialize the camera preview
                                if (hasDevicePermissionToAccess(Manifest.permission.CAMERA)) {
                                    val availableCameras = goCoderCameraView.cameras
                                    // Ensure we can access to at least one camera
                                    if (availableCameras.isNotEmpty()) {
                                        // Set the video broadcaster in the broadcast config
                                        goCoderBroadcastConfig?.videoBroadcaster = goCoderCameraView
                                        videoIsInitialized = true
                                        Log.i("FlutterWOWZCameraView", "*** getOriginalFrameSizes - Get original frame size : ")
                                    } else {
                                        Log.i("FlutterWOWZCameraView", "Could not detect or gain access to any cameras on this device")
                                        goCoderBroadcastConfig?.isVideoEnabled = false
                                    }
                                }

                                if (hasDevicePermissionToAccess(Manifest.permission.RECORD_AUDIO)) {
                                    // Create an audio device instance for capturing and broadcasting audio
                                    goCoderAudioDevice = WOWZAudioDevice()
                                    // Set the audio broadcaster in the broadcast config
                                    goCoderBroadcastConfig?.audioBroadcaster = goCoderAudioDevice
                                    audioIsInitialized = true
                                }

                                if (videoIsInitialized && audioIsInitialized) {
                                    Log.i("FlutterWOWZCameraView", "startPreview")
                                    if (!goCoderCameraView.isPreviewing)
                                        goCoderCameraView.startPreview()
                                }
                            }
                        }
                    })

                }
            }

            "pause_preview" -> activeCamera?.pausePreview()

            "continue_preview" -> activeCamera.continuePreview()

            "stop_preview" -> goCoderCameraView.stopPreview()

            "switch_camera" -> goCoderCameraView.switchCamera()

            "flashlight" -> activeCamera.isTorchOn = call.arguments.toString().toBoolean()

            "fps" -> goCoderBroadcastConfig?.videoFramerate = call.arguments.toString().toInt()

            "bps" -> goCoderBroadcastConfig?.videoBitRate = call.arguments.toString().toInt()

            "khz" -> goCoderBroadcastConfig?.audioSampleRate = call.arguments.toString().toInt()

            "muted" -> goCoderAudioDevice?.isMuted = call.arguments.toString().toBoolean()

            "is_switch_camera_available" -> result.success(goCoderCameraView.isSwitchCameraAvailable)

            "is_initialized" -> result.success(WowzaGoCoder.isInitialized())

            "start_broadcast" -> goCoderBroadcaster?.startBroadcast(goCoderBroadcastConfig, this)

            "end_broadcast" -> goCoderBroadcaster?.endBroadcast(this)

            "on_pause" -> goCoderCameraView.onPause()

            "on_resume" -> goCoderCameraView.onResume()
        }
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
            Log.i("FlutterWOWZCameraView", "broadcast_status: ${statusMessage.toString()}")
        }
    }

    override fun onWZError(status: WOWZBroadcastStatus?) {
        // Display the status message using the U/I thread
        Handler(Looper.getMainLooper()).post {
            methodChannel.invokeMethod("broadcast_error", "{\"state\":\"${status?.state?.name}\",\"message\":\"${status?.lastError?.errorDescription}\"}")
            Log.i("FlutterWOWZCameraView", "broadcast_error: ${status?.lastError?.errorDescription}")
        }
    }

    // wowz_status
    override fun onWZStatus(status: WOWZStatus?) {
        // Display the status message using the U/I thread
        Handler(Looper.getMainLooper()).post {
            methodChannel.invokeMethod("wowz_status", status?.state)
            Log.i("FlutterWOWZCameraView", "wowz_status: ${status?.toString()}")
        }
    }

    override fun onWZError(status: WOWZStatus?) {
        // Display the status message using the U/I thread
        Handler(Looper.getMainLooper()).post {
            methodChannel.invokeMethod("wowz_serror", status?.state)
            Log.i("FlutterWOWZCameraView", "wowz_serror: ${status?.toString()}")
        }
    }

    private fun hasDevicePermissionToAccess(callback: PermissionCallbackInterface) {
        this.callbackFunction = callback
        var result = false

        if (goCoderBroadcaster != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                result = if (mRequiredPermissions.isNotEmpty()) hasDevicePermissionToAccess(mRequiredPermissions) else true

                if (!result && !hasRequestedPermissions) {
                    ActivityCompat.requestPermissions(registrar.activity(),
                            mRequiredPermissions,
                            PERMISSIONS_REQUEST_CODE)
                    hasRequestedPermissions = true
                }
            }
        }

        this.callbackFunction?.onPermissionResult(result)
    }

    private fun hasDevicePermissionToAccess(permissions: Array<String>): Boolean {
        var result = true

        if (goCoderBroadcaster != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (permission in permissions) {
                    if (ContextCompat.checkSelfPermission(registrar.activity(), permission) == PackageManager.PERMISSION_DENIED) {
                        result = false
                    }
                }
            }
        }

        Log.i("FlutterWOWZCameraView", "hasDevicePermissionToAccess $result")
        return result
    }

    private fun hasDevicePermissionToAccess(source: String): Boolean {
        if (goCoderBroadcaster != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(registrar.activity(), source) == PackageManager.PERMISSION_DENIED) {
                    return false
                }
            }
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
            }
        }
        this.callbackFunction?.onPermissionResult(mPermissionsGranted)
        return true
    }
}