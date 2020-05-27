package com.namit.flutter_wowza

import android.annotation.SuppressLint
import android.app.Activity
import android.hardware.camera2.*
import com.pedro.rtplibrary.util.BitrateAdapter
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraManager
import android.media.CamcorderProfile
import android.os.Build
import android.util.Size
import android.view.OrientationEventListener
import android.view.TextureView
import androidx.annotation.RequiresApi
import com.pedro.rtplibrary.rtmp.RtmpCamera2
import io.flutter.plugin.common.MethodChannel
import net.ossrs.rtmp.ConnectCheckerRtmp
import java.util.HashMap
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class RtmpCameraView internal
constructor(val activity: Activity,
            private val invokeMessages: InvokeMessages,
            private val mTextureView: TextureView,
            private val surface: SurfaceTexture?,
            private val width: Int,
            private val height: Int) : ConnectCheckerRtmp {

    val tag = "RtmpCameraView"

    private val cameraManager: CameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var rtmpCamera: RtmpCamera2? = RtmpCamera2(mTextureView, this@RtmpCameraView)
    private var cameraDevice: CameraDevice? = null

    private val orientationEventListener: OrientationEventListener
    private var currentOrientation = OrientationEventListener.ORIENTATION_UNKNOWN

    private var isFrontFacing: Boolean? = null
    private var sensorOrientation: Int? = null
    private var captureSize: Size? = null
    private var previewSize: Size? = null
    private var streamSize: Size? = null
    private var enableAudio: Boolean = false
    private var cameraName: String? = null

    private var bitrateAdapter: BitrateAdapter? = null
    private var recordingProfile: CamcorderProfile? = null

    init {
        orientationEventListener = object : OrientationEventListener(activity.applicationContext) {
            override fun onOrientationChanged(i: Int) {
                if (i == ORIENTATION_UNKNOWN) {
                    return
                }
                // Convert the raw deg angle to the nearest multiple of 90.
                currentOrientation = (i / 90.0).roundToInt().toInt() * 90
            }
        }
        orientationEventListener.enable()
    }

    fun initialize(cameraName: String?, resolutionPreset: String?, enableAudio: Boolean) {
        this@RtmpCameraView.enableAudio = enableAudio
        cameraName?.let {
            this@RtmpCameraView.cameraName = cameraName
            val characteristics = cameraManager.getCameraCharacteristics(cameraName)
            sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
            isFrontFacing = characteristics.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_FRONT

            resolutionPreset?.let { resolutionPreset ->
                val preset = ResolutionPreset.valueOf(resolutionPreset)
                recordingProfile = CameraUtils.getBestAvailableCamcorderProfileForResolutionPreset(cameraName, preset)
                recordingProfile?.let { recordingProfile ->
                    captureSize = Size(recordingProfile.videoFrameWidth, recordingProfile.videoFrameHeight)
                    previewSize = CameraUtils.computeBestPreviewSize(cameraName, preset)
                    streamSize = Size(recordingProfile.videoFrameWidth, recordingProfile.videoFrameHeight)
                }
            }
        }
    }

    override fun onAuthSuccessRtmp() {
        io.flutter.Log.i(tag, "auth success rtmp!")
    }

    override fun onConnectionSuccessRtmp() {
        io.flutter.Log.i(tag, "Connection Success Rtmp!")
        bitrateAdapter = BitrateAdapter(BitrateAdapter.Listener { bitrate -> rtmpCamera?.setVideoBitrateOnFly(bitrate) })
        rtmpCamera?.bitrate?.let { bitrateAdapter?.setMaxBitrate(it) }
    }

    override fun onNewBitrateRtmp(bitrate: Long) {
        io.flutter.Log.i(tag, "New Bitrate Rtmp: $bitrate")
        bitrateAdapter?.setMaxBitrate(bitrate.toInt())
    }

    override fun onConnectionFailedRtmp(reason: String) {
        io.flutter.Log.e(tag, "Connection failed Rtmp: $reason")
        rtmpCamera?.let {
            if (it.reTry(5000, reason)) {
                activity.runOnUiThread {
                    invokeMessages.send(EventType.RTMP_RETRY, reason)
                }
            } else {
                it.stopStream()
                activity.runOnUiThread {
                    invokeMessages.send(EventType.RTMP_STOPPED, "Failed retry")
                }
            }
        }

    }

    override fun onAuthErrorRtmp() {
        io.flutter.Log.e(tag, "Auth Error Rtmp!")
        activity.runOnUiThread {
            invokeMessages.send(EventType.ERROR, "Auth error")
        }
    }

    override fun onDisconnectRtmp() {
        io.flutter.Log.w(tag, "Disconnect Rtmp!")
        rtmpCamera?.stopStream()
        activity.runOnUiThread {
            invokeMessages.send(EventType.RTMP_STOPPED, "Disconnected")
        }
    }

    @SuppressLint("MissingPermission")
    @Throws(CameraAccessException::class)
    fun open(result: MethodChannel.Result) {
        cameraName?.let {cameraName->
            cameraManager.openCamera(
                    cameraName,
                    object : CameraDevice.StateCallback() {
                        override fun onOpened(device: CameraDevice) {
                            cameraDevice = device
                            try {
                                rtmpCamera?.startPreview()
                            } catch (e: CameraAccessException) {
                                result.error("CameraAccess", e.message, null)
                                close()
                                return
                            }
                            previewSize?.let {previewSize->
                                val reply: MutableMap<String, Any> = HashMap()
                                reply["textureId"] = mTextureView.id
                                reply["previewWidth"] = previewSize.width
                                reply["previewHeight"] = previewSize.height
                                result.success(reply)
                            }
                        }

                        override fun onClosed(camera: CameraDevice) {
                            invokeMessages.sendCameraClosingEvent()
                            super.onClosed(camera)
                        }

                        override fun onDisconnected(device: CameraDevice) {
                            close()
                            invokeMessages.send(EventType.ERROR, "The camera was disconnected.")
                        }

                        override fun onError(device: CameraDevice, errorCode: Int) {
                            close()
                            val errorDescription: String = when (errorCode) {
                                ERROR_CAMERA_IN_USE -> "The camera device is in use already."
                                ERROR_MAX_CAMERAS_IN_USE -> "Max cameras in use"
                                ERROR_CAMERA_DISABLED -> "The camera device could not be opened due to a device policy."
                                ERROR_CAMERA_DEVICE -> "The camera device has encountered a fatal error"
                                ERROR_CAMERA_SERVICE -> "The camera service has encountered a fatal error."
                                else -> "Unknown camera error"
                            }
                            invokeMessages.send(EventType.ERROR, errorDescription)
                        }

                    },
                    null)
        }
    }

    fun startStreaming(url: String?, result: MethodChannel.Result) {

    }

    fun stopVideoStreaming(result: MethodChannel.Result) {

    }

    fun pauseVideoStreaming(result: MethodChannel.Result) {

    }

    fun resumeVideoStreaming(result: MethodChannel.Result) {

    }

    fun close() {
            cameraDevice?.close()
            cameraDevice = null
            rtmpCamera?.stopStream()
            rtmpCamera = null
            bitrateAdapter = null
    }

    fun dispose() {
        close()
        surface?.release()
        orientationEventListener.disable()
    }
}