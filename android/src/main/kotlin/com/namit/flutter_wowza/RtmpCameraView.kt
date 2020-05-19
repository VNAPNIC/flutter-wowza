package com.namit.flutter_wowza

import android.app.Activity
import android.hardware.camera2.*
import android.media.CamcorderProfile
import android.media.ImageReader
import android.media.MediaRecorder
import android.util.Size
import com.pedro.rtplibrary.util.BitrateAdapter
import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.view.OrientationEventListener
import android.view.SurfaceView
import androidx.annotation.RequiresApi
import com.pedro.rtplibrary.rtmp.RtmpCamera2
import net.ossrs.rtmp.ConnectCheckerRtmp
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class RtmpCameraView internal
constructor(val activity: Activity,
            private val surfaceView: SurfaceView,
            private val invokeMessages: InvokeMessages,
            private val cameraName: String?,
            private val resolutionPreset: String?,
            val enableAudio: Boolean) : ConnectCheckerRtmp   {

    private val cameraManager: CameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var rtmpCamera: RtmpCamera2? = RtmpCamera2(surfaceView,this)

    private val orientationEventListener: OrientationEventListener
    private val isFrontFacing: Boolean
    private val sensorOrientation: Int
    private val captureSize: Size
    private val previewSize: Size
    private val streamSize: Size
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var pictureImageReader: ImageReader? = null
    private var imageStreamReader: ImageReader? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var mediaRecorder: MediaRecorder? = null
    private var recordingVideo = false
    private var recordingRtmp = false
    private val recordingProfile: CamcorderProfile
    private var currentOrientation = OrientationEventListener.ORIENTATION_UNKNOWN

    private var bitrateAdapter: BitrateAdapter? = null

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
        checkNotNull(cameraName) { "No cameraName available!" }

        orientationEventListener.enable()
        val characteristics = cameraManager.getCameraCharacteristics(cameraName)
        sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
        isFrontFacing = characteristics.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_FRONT
        val preset = ResolutionPreset.valueOf(resolutionPreset!!)
        recordingProfile = CameraUtils.getBestAvailableCamcorderProfileForResolutionPreset(cameraName, preset)
        captureSize = Size(recordingProfile.videoFrameWidth, recordingProfile.videoFrameHeight)
        previewSize = CameraUtils.computeBestPreviewSize(cameraName, preset)
        streamSize = Size(recordingProfile.videoFrameWidth, recordingProfile.videoFrameHeight)
    }

    /**
     * ConnectCheckerRtmp
     */

    override fun onAuthSuccessRtmp() {}

    override fun onNewBitrateRtmp(bitrate: Long) {
        bitrateAdapter?.setMaxBitrate(bitrate.toInt())
    }

    override fun onConnectionSuccessRtmp() {
        rtmpCamera?.let {
            bitrateAdapter = BitrateAdapter(BitrateAdapter.Listener { bitrate -> it.setVideoBitrateOnFly(bitrate) })
            bitrateAdapter?.setMaxBitrate(it.bitrate)
        }
    }

    override fun onConnectionFailedRtmp(reason: String) {
        rtmpCamera?.let {
            // Retry first.
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
        activity.runOnUiThread {
            invokeMessages.send(EventType.ERROR, "Auth error")
        }
    }

    override fun onDisconnectRtmp() {
        rtmpCamera?.stopStream()
        activity.runOnUiThread {
            invokeMessages.send(EventType.RTMP_STOPPED, "Disconnected")
        }
    }

    /**
     * Event
     */

    private fun closeCaptureSession() {
        cameraCaptureSession?.run {
            close()
        }
        cameraCaptureSession = null
    }

    fun close() {
        closeCaptureSession()
        cameraDevice?.run {
            close()
        }
        cameraDevice = null

        pictureImageReader?.run {
            close()
        }
        pictureImageReader = null

        imageStreamReader?.run {
            close()
        }
        imageStreamReader = null

        mediaRecorder?.run {
            reset()
            release()

        }
        mediaRecorder = null

        rtmpCamera?.run {
            stopStream()
        }
        rtmpCamera = null
        bitrateAdapter = null
    }

    fun dispose() {
        close()
        orientationEventListener.disable()
    }
}