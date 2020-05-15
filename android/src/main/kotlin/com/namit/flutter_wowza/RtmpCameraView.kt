package com.namit.flutter_wowza

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.CamcorderProfile
import android.media.ImageReader
import android.media.MediaRecorder
import android.util.Size
import android.view.Surface
import com.pedro.rtplibrary.util.BitrateAdapter
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.view.TextureRegistry.SurfaceTextureEntry
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.view.OrientationEventListener
import android.view.View
import androidx.annotation.RequiresApi
import com.pedro.rtplibrary.rtmp.RtmpCamera2
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.platform.PlatformView
import net.ossrs.rtmp.ConnectCheckerRtmp;
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class RtmpCameraView internal
constructor(  val activity: Activity?,
              val flutterTexture: SurfaceTextureEntry,
              val dartMessenger: DartMessenger,
              val cameraName: String,
              val resolutionPreset: String?,
              val enableAudio: Boolean) : ConnectCheckerRtmp   {

    private val cameraManager: CameraManager
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
    private var rtmpCamera: RtmpCamera2? = null
    private var bitrateAdapter: BitrateAdapter? = null

    init {
        checkNotNull(activity) { "No activity available!" }
        cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
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

    override fun onAuthSuccessRtmp() {
        TODO("Not yet implemented")
    }

    override fun onNewBitrateRtmp(bitrate: Long) {
        TODO("Not yet implemented")
    }

    override fun onConnectionSuccessRtmp() {
        TODO("Not yet implemented")
    }

    override fun onConnectionFailedRtmp(reason: String) {
        TODO("Not yet implemented")
    }

    override fun onAuthErrorRtmp() {
        TODO("Not yet implemented")
    }

    override fun onDisconnectRtmp() {
        TODO("Not yet implemented")
    }
}