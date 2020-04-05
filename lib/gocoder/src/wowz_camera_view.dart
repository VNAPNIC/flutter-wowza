part of gocoder;

enum BroadcastState {
  READY,
  BROADCASTING,
  IDLE
}

BroadcastStatus broadcastStatusFromJson(String str) =>
    BroadcastStatus.fromJson(json.decode(str));

String broadcastStatusToJson(BroadcastStatus data) => json.encode(data.toJson());

class BroadcastStatus {
  BroadcastState state;
  String message;

  BroadcastStatus({this.state, this.message});

  factory BroadcastStatus.fromJson(Map<String, dynamic> json) =>
      BroadcastStatus(
          state: BroadcastState.values.firstWhere(
                  (type) => type.toString() == "BroadcastState." + json["state"]),
          message: json["message"]);

  Map<String, dynamic> toJson() =>
      {"state": state.toString(), "message": message};
}

enum ScaleMode {
  /// Scale the camera preview to fit within the screen area. Letterboxing may be applied to maintain the aspect ratio.
  RESIZE_TO_ASPECT,

  /// Scale the camera preview to fill the entire screen area. The preview may be cropped to maintain the aspect ratio.
  FILL_VIEW
}

class BroadcastControllerValue {
  BroadcastControllerValue({this.event, this.value});

  final Event event;

  final dynamic value;
}

/// Channel controller
class BroadcastController extends Controller<BroadcastControllerValue> {
  BroadcastController() : super(BroadcastControllerValue());

  bool _initialization;

  void _setInitialization() {
    _initialization = true;
    value = BroadcastControllerValue(event: Event.initialization);
  }

  bool isInitialization() => _initialization;


  /// Updates the property values of this instance with the property values from the specified preset configuration.
  void set(WOWZBroadcastConfig preset) {
    if (preset != null) {
      _config = preset;
      _channel?.invokeMethod(_push_config, json.encode(_config.toDataMap()));
      debugPrint("WOWZBroadcastConfig: ${json.encode(_config.toDataMap())}");
    } else {
      debugPrint("WOWZBroadcastConfig is not allowed to null!!!");
    }
  }

  WOWZBroadcastConfig _config;

  openCamera(WOWZBroadcastConfig preset) {
    if (preset != null) {
      _config = preset;
      debugPrint(
          "WOWZBroadcastConfig config: ${json.encode(_config.toDataMap())}");
      _channel?.invokeMethod(_open_camera, json.encode(_config.toDataMap()));
    } else {
      debugPrint("WOWZBroadcastConfig is not allowed to null!!!");
    }
  }

  stopCamera() {
    _channel?.invokeMethod(_stop_camera, "");
  }

  startBroadcast(WOWZBroadcastConfig preset) {
    if (preset != null) {
      _config = preset;
      debugPrint(
          "WOWZBroadcastConfig config: ${json.encode(_config.toDataMap())}");
      _channel?.invokeMethod(
          _start_broadcast, json.encode(_config.toDataMap()));
    } else {
      debugPrint("WOWZBroadcastConfig is not allowed to null!!!");
    }
  }

  endBroadcast() {
    _channel?.invokeMethod(_end_broadcast, "");
  }

  switchCamera() {
    _channel?.invokeMethod(_switch_camera, "");
  }

  flashLight(bool flashLight) {
    _channel?.invokeMethod(_flash_light, "$flashLight");
  }

  setScaleMode(ScaleMode scaleMode) {
    _channel?.invokeMethod(_scale_mode, scaleMode.toString());
  }
}

class WOWZBroadcastConfig extends WOWZStreamConfig {
  WOWZBroadcastConfig({this.logLevel});

  int logLevel;
  bool abrActive;

  Map toDataMap() {
    final map = {};
    // just for WOWZBroadcastConfig
    map["logLevel"] = logLevel;
    map["abrActive"] = "$abrActive";

    // common config
    map["presetLabel"] = presetLabel;
    map["hostAddress"] = hostAddress;
    map["applicationName"] = applicationName;
    map["streamName"] = streamName;
    map["portNumber"] = portNumber;
    map["username"] = username;
    map["password"] = password;

    map["isPlayback"] = "$isPlayback";
    map["hlsEnabled"] = "$hlsEnabled";
    map["hlsBackupUrl"] = hlsBackupUrl;

    map["videoEnabled"] = "$videoEnabled";
    if (videoEnabled) {
      map["videoFrameWidth"] = videoFrameWidth;
      map["videoFrameHeight"] = videoFrameHeight;
      map["videoBitRate"] = videoBitRate;
      map["videoFramerate"] = videoFramerate;
      map["videoKeyFrameInterval"] = videoKeyFrameInterval;
      if (videoProfileLevel != null) {
        map["videoProfile"] = videoProfileLevel.mProfile;
        map["videoProfileLevel"] = videoProfileLevel.mLevel;
      }
    }

    map["audioEnabled"] = "$audioEnabled";
    if (audioEnabled) {
      map["audioChannels"] = audioChannels;
      map["audioSampleRate"] = audioSampleRate;
      map["audioBitrate"] = audioBitrate;
    }

    map["abrEnabled"] = "$abrEnabled";
    if (abrEnabled) {
      map["vbeFrameBufferSizeMultiplier"] = vbeFrameBufferSizeMultiplier;
      map["vbeFrameRateLowBandwidthSkipCount"] =
          vbeFrameRateLowBandwidthSkipCount;
    }

    return map;
  }
}

typedef BroadcastStatusCallback = Function(BroadcastStatus);
typedef BroadcastErrorCallback = Function(BroadcastStatus);

class WOWZCameraView extends StatefulWidget {
  WOWZCameraView(
      {@required this.androidLicenseKey,
      @required this.iosLicenseKey,
      @required this.controller,
      this.broadcastStatusCallback,
      this.broadcastErrorCallback,
      this.wantKeepAlive = false});

  final bool wantKeepAlive;

  final BroadcastController controller;
  final BroadcastStatusCallback broadcastStatusCallback;
  final BroadcastStatusCallback broadcastErrorCallback;

  final String androidLicenseKey;
  final String iosLicenseKey;

  @override
  _WOWZCameraViewState createState() => _WOWZCameraViewState();
}

class _WOWZCameraViewState extends State<WOWZCameraView>
    with AutomaticKeepAliveClientMixin {
  var _viewId = 0;
  MethodChannel _channel;

  @override
  Widget build(BuildContext context) {
    super.build(context);
    return (defaultTargetPlatform == TargetPlatform.android)
        ? AndroidView(
            viewType: _camera_view_channel,
            onPlatformViewCreated: _onPlatformViewCreated,
          )
        : (defaultTargetPlatform == TargetPlatform.iOS)
            ? UiKitView(
                viewType: _camera_view_channel,
                onPlatformViewCreated: _onPlatformViewCreated,
              )
            : Text(
                '$defaultTargetPlatform is not yet supported by the flutter_wowza plugin');
  }

  _onPlatformViewCreated(int viewId) {
    if (_viewId != viewId || _channel == null) {
      debugPrint(
          'WOWZCameraView: MethodChannel: ${_player_view_channel}_$viewId');

      _channel = MethodChannel("${_player_view_channel}_$viewId");
      widget.controller?._setChannel(_channel);

      _channel.setMethodCallHandler((call) async {
        debugPrint(
            'WOWZCameraView: method: ${call.method} | arguments: ${call.arguments}');
        switch (call.method) {
          case _broadcast_status:
            widget.broadcastStatusCallback(
                broadcastStatusFromJson(call.arguments));
            break;
          case _broadcast_error:
            widget.broadcastErrorCallback(
                broadcastStatusFromJson(call.arguments));
            break;
        }
      });

      _channel.invokeMethod(
          _apiLicenseKey,
          (defaultTargetPlatform == TargetPlatform.android)
              ? widget.androidLicenseKey
              : widget.iosLicenseKey);

      widget.controller?._setInitialization();
    }
  }

  @override
  bool get wantKeepAlive => widget.wantKeepAlive;
}
