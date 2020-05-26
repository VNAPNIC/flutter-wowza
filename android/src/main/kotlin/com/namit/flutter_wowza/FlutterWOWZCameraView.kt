package com.namit.flutter_wowza

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.os.Build
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.pedro.rtplibrary.rtsp.RtspCamera1
import com.pedro.rtplibrary.rtsp.RtspCamera2
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.platform.PlatformView

private const val PERMISSIONS_REQUEST_CODE = 0x1

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class FlutterWOWZCameraView internal
constructor(private val context: Context?, private val registrar: PluginRegistry.Registrar,
            private val methodChannel: MethodChannel, id: Int?, params: Map<String, Any>?) :
        PlatformView, MethodChannel.MethodCallHandler,
        PluginRegistry.RequestPermissionsResultListener,
        TextureView.SurfaceTextureListener {

    val tag = "FlutterWOWZCameraView"

    private var mTextureView: TextureView = TextureView(context)
    private var rtspCamera: RtmpCameraView? = null

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
        return mTextureView
    }

    override fun dispose() {
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        context?.let { context ->
            when (call.method) {
                "availableCameras" -> try {
                    result.success(CameraUtils.getAvailableCameras(context))
                } catch (e: Exception) {
                    handleException(e, result)
                }
                "initialize" -> {
                    mTextureView.surfaceTextureListener = this@FlutterWOWZCameraView
                    rtspCamera?.close()

                }
                // Camera
                "switchCamera" -> {
                }
                "flashLight" -> {
                }
                // Streaming control
                "startBroadcasting" -> {
                    io.flutter.Log.i(tag, call.arguments.toString())
                    rtspCamera?.startStreaming(call.argument("url"), result)
                }
                "pauseBroadcasting" -> {
                }
                "resumeBroadcasting" -> {
                }
                "stopBroadcasting" -> {
                }
                // dispose
                "dispose" -> {
                    rtspCamera?.dispose()
                    result.success(null)
                }
                else -> {
                }
            }
        }

    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        instantiateCamera()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        // Ignored, the Camera does all the work for us
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        // Update your view here!
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        rtspCamera?.dispose()
        return true
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Throws(CameraAccessException::class)
    private fun instantiateCamera(surface: SurfaceTexture?, width: Int, height: Int) {
        rtspCamera = RtmpCameraView(registrar.activity(), surface)
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

    // Request permissions
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?): Boolean {
        TODO("Not yet implemented")
    }
}