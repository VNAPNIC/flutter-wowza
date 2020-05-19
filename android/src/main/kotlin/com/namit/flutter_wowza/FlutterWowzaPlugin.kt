package com.namit.flutter_wowza

import io.flutter.plugin.common.PluginRegistry.Registrar

/** FlutterWowzaPlugin */
object FlutterWowzaPlugin {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
        registrar.platformViewRegistry().registerViewFactory(
                CAMERA_VIEW_CHANNEL, WOWZCameraViewFactory(registrar.messenger(),registrar))
    }
}
