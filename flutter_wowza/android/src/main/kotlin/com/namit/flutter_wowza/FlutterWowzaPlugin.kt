package com.namit.flutter_wowza

import io.flutter.plugin.common.PluginRegistry.Registrar

/** FlutterWowzaPlugin */
object FlutterWowzaPlugin {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
        registrar.platformViewRegistry().registerViewFactory(
                "flutter_wowza", WOWZCameraViewFactory(registrar.messenger(),registrar))
    }
}
