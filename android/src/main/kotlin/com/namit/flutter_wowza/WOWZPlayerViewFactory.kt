package com.namit.flutter_wowza

import android.content.Context
import android.util.Log
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class WOWZPlayerViewFactory(private val messenger: BinaryMessenger, private val registrar: PluginRegistry.Registrar) : PlatformViewFactory(StandardMessageCodec.INSTANCE){

    override fun create(context: Context?, viewId: Int, args: Any?): PlatformView {
        Log.i("WOWZPlayerViewFactory", "create  $viewId")

        val methodChannel = MethodChannel(messenger, "${PLAYER_VIEW_ID}_$viewId")
        val params = args?.let { args as? Map<String, Any> }
        return FlutterWOWZPlayerView(context,registrar, methodChannel, viewId, params)
    }

}