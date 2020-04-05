package com.namit.flutter_wowza

import android.util.Log
import io.flutter.plugin.common.PluginRegistry.Registrar

const val CAMERA_VIEW_ID = "flutter_wowza_camera"
const val PLAYER_VIEW_ID = "flutter_wowza_player"

/** FlutterWowzaPlugin */
object FlutterWowzaPlugin {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
        Log.i("FlutterWowzaPlugin", "registrar")
        registrar.platformViewRegistry().registerViewFactory(
                CAMERA_VIEW_ID, WOWZCameraViewFactory(registrar.messenger(),registrar))
        registrar.platformViewRegistry().registerViewFactory(
                PLAYER_VIEW_ID, WOWZPlayerViewFactory(registrar.messenger(),registrar))
    }
}
