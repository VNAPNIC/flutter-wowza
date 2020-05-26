package com.namit.flutter_wowza

import android.app.Activity
import android.hardware.camera2.*
import android.media.CamcorderProfile
import android.media.ImageReader
import android.media.MediaRecorder
import android.util.Size
import com.pedro.rtplibrary.util.BitrateAdapter
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraManager
import android.os.Build
import android.view.OrientationEventListener
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import androidx.annotation.RequiresApi
import com.pedro.rtplibrary.rtmp.RtmpCamera2
import io.flutter.plugin.common.MethodChannel
import net.ossrs.rtmp.ConnectCheckerRtmp
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class RtmpCameraView internal
constructor(val activity: Activity,
            private val mTextureView: TextureView,
            private val surfaceView: SurfaceTexture,
            private val invokeMessages: InvokeMessages,
            private val cameraName: String?,
            private val resolutionPreset: String?,
            val enableAudio: Boolean) : ConnectCheckerRtmp {

    private val cameraManager: CameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var rtmpCamera: RtmpCamera2? = RtmpCamera2(mTextureView, this)

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
    private val camcorderProfile: CamcorderProfile
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
        camcorderProfile = CameraUtils.getBestAvailableCamcorderProfileForResolutionPreset(cameraName, preset)
        captureSize = Size(camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight)
        previewSize = CameraUtils.computeBestPreviewSize(cameraName, preset)
        streamSize = Size(camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight)
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

    private val mediaOrientation: Int
        get() {
            val sensorOrientationOffset = if (currentOrientation == OrientationEventListener.ORIENTATION_UNKNOWN) 0 else if (isFrontFacing) -currentOrientation else currentOrientation
            return (sensorOrientationOffset + sensorOrientation + 360) % 360
        }

    @Throws(CameraAccessException::class)
    fun startPreview() {
        createCaptureSession(CameraDevice.TEMPLATE_PREVIEW, pictureImageReader!!.surface)
    }

    @Throws(CameraAccessException::class)
    private fun createCaptureSession(templateType: Int, vararg surfaces: Surface) {
        createCaptureSession(templateType, null, *surfaces)
    }

    @Throws(CameraAccessException::class)
    private fun createCaptureSession(
            templateType: Int, onSuccessCallback: Runnable?, vararg surfaces: Surface) {
        // Close any existing capture session.
        closeCaptureSession()

        // Create a new capture builder.
        captureRequestBuilder = cameraDevice!!.createCaptureRequest(templateType)

        // Build Flutter surface to render to
        val surfaceTexture = flutterTexture.surfaceTexture()
        surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)
        val flutterSurface = Surface(surfaceView)
        captureRequestBuilder!!.addTarget(flutterSurface)
        val remainingSurfaces = listOf(*surfaces)
        if (templateType != CameraDevice.TEMPLATE_PREVIEW) {
            // If it is not preview mode, add all surfaces as targets.
            for (surface in remainingSurfaces) {
                captureRequestBuilder!!.addTarget(surface)
            }
        }

        // Prepare the callback
        val callback: CameraCaptureSession.StateCallback = object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                try {
                    if (cameraDevice == null) {
                        dartMessenger.send(
                                DartMessenger.EventType.ERROR, "The camera was closed during configuration.")
                        return
                    }
                    cameraCaptureSession = session
                    captureRequestBuilder!!.set(
                            CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                    cameraCaptureSession!!.setRepeatingRequest(captureRequestBuilder!!.build(), null, null)
                    onSuccessCallback?.run()
                } catch (e: CameraAccessException) {
                    dartMessenger.send(DartMessenger.EventType.ERROR, e.message)
                } catch (e: IllegalStateException) {
                    dartMessenger.send(DartMessenger.EventType.ERROR, e.message)
                } catch (e: IllegalArgumentException) {
                    dartMessenger.send(DartMessenger.EventType.ERROR, e.message)
                }
            }

            override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                dartMessenger.send(
                        DartMessenger.EventType.ERROR, "Failed to configure camera session.")
            }
        }

        // Collect all surfaces we want to render to.
        val surfaceList: MutableList<Surface> = ArrayList()
        surfaceList.add(flutterSurface)
        surfaceList.addAll(remainingSurfaces)
        // Start the session
        cameraDevice!!.createCaptureSession(surfaceList, callback, null)
    }

    private fun closeCaptureSession() {
        cameraCaptureSession?.close()
        cameraCaptureSession = null
    }

    @Throws(IOException::class)
    private fun prepareRtmpPublished(url: String) {
        rtmpCamera?.prepareAudio()
        rtmpCamera?.prepareVideo(
                streamSize.width,
                streamSize.height,
                2,
                1200 * 1024,
                false,
                mediaOrientation)
    }


    fun startStreaming(url: String?, result: MethodChannel.Result) {
        if (url == null) {
            result.error("fileExists", "Must specify a url.", null)
            return
        }
        try {
            prepareRtmpPublished(url)
            recordingRtmp = true
            result.success(null)
        } catch (e: CameraAccessException) {
            result.error("videoRecordingFailed", e.message, null)
        } catch (e: IOException) {
            result.error("videoRecordingFailed", e.message, null)
        }
    }

    fun stopVideoStreaming(result: MethodChannel.Result) {
        if (!recordingRtmp) {
            result.success(null)
            return
        }
        try {
            recordingRtmp = false
            rtmpCamera?.stopStream()
            startPreview()
            result.success(null)
        } catch (e: CameraAccessException) {
            result.error("videoStreamingFailed", e.message, null)
        } catch (e: IllegalStateException) {
            result.error("videoStreamingFailed", e.message, null)
        }
    }

    fun pauseVideoStreaming(result: MethodChannel.Result) {
        if (!recordingRtmp) {
            result.success(null)
            return
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                rtmpCamera?.stopStream()
            } else {
                result.error("videoStreamingFailed", "pauseVideoStreaming requires Android API +24.", null)
                return
            }
        } catch (e: IllegalStateException) {
            result.error("videoStreamingFailed", e.message, null)
            return
        }
        result.success(null)
    }

    fun resumeVideoStreaming(url: String?, result: MethodChannel.Result) {
        if (!recordingRtmp) {
            result.success(null)
            return
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                rtmpCamera?.startStream()
            } else {
                result.error(
                        "videoStreamingFailed", "resumeVideoStreaming requires Android API +24.", null)
                return
            }
        } catch (e: IllegalStateException) {
            result.error("videoStreamingFailed", e.message, null)
            return
        }
        result.success(null)
    }

    fun close() {
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