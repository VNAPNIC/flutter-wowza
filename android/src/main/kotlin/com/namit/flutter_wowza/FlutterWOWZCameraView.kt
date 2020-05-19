package com.namit.flutter_wowza

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.os.Build
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.pedro.rtplibrary.rtsp.RtspCamera1
import com.pedro.rtplibrary.rtsp.RtspCamera2
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.platform.PlatformView

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class FlutterWOWZCameraView internal
constructor(private val context: Context?, private val registrar: PluginRegistry.Registrar,
            private val methodChannel: MethodChannel, id: Int?, params: Map<String, Any>?) :
        PlatformView, MethodChannel.MethodCallHandler,
        PluginRegistry.RequestPermissionsResultListener,
        SurfaceHolder.Callback{

    val TAG = "FlutterWOWZCameraView"

    private var mPermissionsGranted = false
    private var hasRequestedPermissions = false
    var videoIsInitialized = false
    var audioIsInitialized = false

    private var surfaceView: SurfaceView = SurfaceView(context)
    private var rtspCamera: RtmpCameraView? = null

    private val PERMISSIONS_REQUEST_CODE = 0x1

    private var mRequiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    init {
        registrar.addRequestPermissionsResultListener(this)
        methodChannel.setMethodCallHandler(this)
    }

    override fun getView(): View {
        val params = surfaceView.layoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        surfaceView.layoutParams = params
        return surfaceView
    }

    override fun dispose() {
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        context?.let {context->
            when (call.method) {
                "availableCameras" -> try {
                    result.success(CameraUtils.getAvailableCameras(context))
                } catch (e: Exception) {
                    handleException(e, result)
                }

                "initialize" -> {

                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Throws(CameraAccessException::class)
    private fun instantiateCamera(call: MethodCall, result: MethodChannel) {
        val cameraName = call.argument<String>("cameraName")
        val resolutionPreset = call.argument<String>("resolutionPreset")
        val enableAudio = call.argument<Boolean>("enableAudio")!!
        val invokeMessages = InvokeMessages(result)
        rtspCamera = RtmpCameraView(registrar.activity(),surfaceView,invokeMessages,cameraName,resolutionPreset,enableAudio)
    }

    /**
     * SurfaceHolder.Callback
     */
    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        Log.i(TAG, "surfaceChanged")
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        Log.i(TAG, "surfaceDestroyed")
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        Log.i(TAG, "surfaceCreated")
    }

    // We move catching CameraAccessException out of onMethodCall because it causes a crash
    // on plugin registration for sdks incompatible with Camera2 (< 21). We want this plugin to
    // to be able to compile with <21 sdks for apps that want the camera and support earlier version.
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun handleException(exception: Exception, result: MethodChannel.Result) {
        if (exception is CameraAccessException) {
            result.error("CameraAccess", exception.message, null)
        }
        throw (exception as RuntimeException)
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
//        if (goCoderBroadcaster != null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                for (permission in permissions) {
//                    if (ContextCompat.checkSelfPermission(registrar.activity(), permission) == PackageManager.PERMISSION_DENIED) {
//                        result = false
//                    }
//                }
//            }
//        } else {
//            Log.e("FlutterWOWZCameraView", "goCoderBroadcaster is null!")
//            result = false
//        }
        return result
    }

    private fun checkPermissionToAccess(source: String): Boolean {
//        if (goCoderBroadcaster != null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (ContextCompat.checkSelfPermission(registrar.activity(), source) == PackageManager.PERMISSION_DENIED) {
//                    return false
//                }
//            }
//        } else {
//            Log.e("FlutterWOWZCameraView", "goCoderBroadcaster is null!")
//            return false
//        }
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

        Log.i("FlutterWOWZCameraView", "onRequestPermissionsResult  has requested: $hasRequestedPermissions")
        return true
    }

    private fun onPermissionResult(mPermissionsGranted: Boolean) {
//        if (goCoder != null) {
//            if (mPermissionsGranted) {
//                // Initialize the camera preview
//                if (requestPermissionToAccess(Manifest.permission.CAMERA)) {
//                    if(!videoIsInitialized) {
//                        val availableCameras = goCoderCameraView.cameras
//                        // Ensure we can access to at least one camera
//                        if (availableCameras.isNotEmpty()) {
//                            // Set the video broadcaster in the broadcast config
//                            goCoderBroadcastConfig?.videoBroadcaster = goCoderCameraView
//                            videoIsInitialized = true
//                            Log.i("FlutterWOWZCameraView", "*** getOriginalFrameSizes - Get original frame size : ")
//                        } else {
//                            Log.e("FlutterWOWZCameraView", "Could not detect or gain access to any cameras on this device")
//                            goCoderBroadcastConfig?.isVideoEnabled = false
//                        }
//                    }
//                } else {
//                    Log.e("FlutterWOWZCameraView", "Exception Fail to connect to camera service. I checked camera permission in Settings")
//                }
//
//                if (requestPermissionToAccess(Manifest.permission.RECORD_AUDIO)) {
//                    if(!audioIsInitialized) {
//                        // Create an audio device instance for capturing and broadcasting audio
//                        goCoderAudioDevice = WOWZAudioDevice()
//                        // Set the audio broadcaster in the broadcast config
//                        goCoderBroadcastConfig?.audioBroadcaster = goCoderAudioDevice
//                        audioIsInitialized = true
//                    }
//                } else {
//                    Log.e("FlutterWOWZCameraView", "Exception Fail to connect to record audio service. I checked camera permission in Settings")
//                }
//
//                if (videoIsInitialized && audioIsInitialized) {
//                    Log.i("FlutterWOWZCameraView", "startPreview")
//                    if (!goCoderCameraView.isPreviewing)
//                        goCoderCameraView.startPreview()
//                }
//            }
//        } else {
//            Log.e("FlutterWOWZCameraView", "goCoder is null!, Please check the license key GoCoder SDK, maybe your license key GoCoder SDK is wrong!")
//        }
    }
}