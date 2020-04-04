package com.namit.flutter_wowza

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wowza.gocoder.sdk.api.WowzaGoCoder
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig
import com.wowza.gocoder.sdk.api.h264.WOWZProfileLevel
import com.wowza.gocoder.sdk.api.logging.WOWZLog
import com.wowza.gocoder.sdk.api.logging.WOWZLogger
import com.wowza.gocoder.sdk.api.player.WOWZPlayerConfig
import com.wowza.gocoder.sdk.api.player.WOWZPlayerView
import com.wowza.gocoder.sdk.api.status.WOWZPlayerStatus
import com.wowza.gocoder.sdk.api.status.WOWZPlayerStatusCallback
import com.wowza.gocoder.sdk.support.status.WOWZStatus
import com.wowza.gocoder.sdk.support.status.WOWZStatusCallback
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.platform.PlatformView

class MLog : WOWZLogger() {
    override fun warn(tag: String?, message: String?) {
        super.warn(tag, message)
        Log.w(tag, message)
    }

    override fun info(tag: String?, message: String?) {
        super.info(tag, message)
        Log.i(tag, message)
    }

    override fun error(tag: String?, message: String?) {
        super.error(tag, message)
        Log.e(tag, message)
    }

    override fun error(tag: String?, message: String?, th: Throwable?) {
        super.error(tag, message, th)
        Log.e(tag, message)
    }

    override fun error(tag: String?, th: Throwable?) {
        super.error(tag, th)
        Log.e(tag, th?.message)
    }

    override fun verbose(tag: String?, message: String?) {
        super.verbose(tag, message)
        Log.v(tag, message)
    }

    override fun debug(tag: String?, message: String?) {
        super.debug(tag, message)
        Log.d(tag, message)
    }
}

class FlutterWOWZPlayerView internal
constructor(private val context: Context?, private val registrar: PluginRegistry.Registrar,
            private val methodChannel: MethodChannel, id: Int?, params: Map<String, Any>?) :
        PlatformView, MethodChannel.MethodCallHandler, WOWZPlayerStatusCallback {

    private val TAG = FlutterWOWZPlayerView::class.java.simpleName

    // Make WOWZPlayerView
    var playerView: WOWZPlayerView = WOWZPlayerView(context)

    // The top-level GoCoder API interface
    private var goCoder: WowzaGoCoder? = null

    val streamPlayerConfig = WOWZPlayerConfig()

    init {
        Log.i("FlutterWOWZPlayerView", "init")
        methodChannel.setMethodCallHandler(this)
    }

    override fun getView(): View {
        val param = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        playerView.layoutParams = param
        playerView.invalidate()
        return playerView
    }

    override fun dispose() {
        playerView.stop()
        playerView.clear()
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        Log.i("FlutterWOWZPlayerView", "method: ${call.method} | arguments: ${call.arguments}")
        playerView.logLevel = 2
        WOWZLog.LOGGING_ENABLED = true
        WOWZLog.registerLogger(MLog())

        when (call.method) {
            "api_licenseKey" -> goCoder = WowzaGoCoder.init(context, call.arguments.toString())
            "push_config" -> {
                streamPlayerConfig.set(makeConfig(call.arguments as String?))
            }
            "play_video" -> {
                playerView.play(makeConfig(call.arguments as String?), this)

                Log.i("FlutterWOWZPlayerView", "${(streamPlayerConfig as WOWZMediaConfig).toString()}")
            }
            "stop_video" -> {
                playerView.stop()
            }
            "volume" -> {
                if (call.arguments is Int)
                    playerView.volume = call.arguments as Int
            }
            "mute" -> {
                playerView.mute(call.arguments == "true")
            }
            "is_ready_to_play" -> {
                result.success(playerView.isReadyToPlay)
            }
            "duration" -> {
                result.success(playerView.duration)
            }
            "current_time" -> {
                result.success(playerView.currentTime)
            }
        }
    }

    private fun makeConfig(dataMap: String?): WOWZPlayerConfig? {
        val config = WOWZPlayerConfig()

        dataMap?.let { json ->
            val retMap: Map<String, Any> = Gson().fromJson(
                    json, object : TypeToken<HashMap<String?, Any?>?>() {}.type
            )
            if (retMap["PreRollBufferDurationMs"] != null)
                config.preRollBufferDuration = retMap["PreRollBufferDurationMs"] as Float

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

    // WOWZPlayerStatusCallback
    override fun onWZStatus(status: WOWZPlayerStatus?) {
        // Display the status message using the U/I thread
        Handler(Looper.getMainLooper()).post {
            methodChannel.invokeMethod("player_status", "{\"state\":\"${status?.state?.name}\",\"message\":\"${status?.toString()}\"}")
            Log.i("FlutterWOWZPlayerView", "WOWZPlayerStatus Status: ${status?.state?.name}")
        }
    }

    override fun onWZError(status: WOWZPlayerStatus?) {
        Handler(Looper.getMainLooper()).post {
            methodChannel.invokeMethod("player_error", "{\"state\":\"${status?.state?.name}\",\"message\":\"${status?.toString()}\"}")
            Log.e("FlutterWOWZPlayerView", "WOWZPlayerStatus Error: ${status?.state?.name} | ${status?.lastError?.toString()}")
        }
    }
}