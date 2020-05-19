package com.namit.flutter_wowza

import android.text.TextUtils
import io.flutter.plugin.common.MethodChannel
import java.util.*

enum class EventType {
    ERROR, CAMERA_CLOSING, RTMP_CONNECTED, RTMP_STOPPED, RTMP_RETRY
}

class InvokeMessages(private val methodChannel: MethodChannel) {

    fun sendCameraClosingEvent() {
        send(EventType.CAMERA_CLOSING, null)
    }

    fun send(eventType: EventType, description: String?) {
        val type: String = eventType.toString().toLowerCase(Locale.getDefault())
        val desc: String? = description
        methodChannel.invokeMethod(type,desc)
    }
}