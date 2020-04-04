part of gocoder;

enum PlayerState {
  IDLE,
  CONNECTING,
  BUFFERING,
  STOPPING,
  PLAYING,
  PAUSING
}

PlayerBroadcastStatus playerStatusFromJson(String str) =>
    PlayerBroadcastStatus.fromJson(json.decode(str));

String playerStatusToJson(PlayerBroadcastStatus data) =>
    json.encode(data.toJson());

class PlayerBroadcastStatus {
  PlayerState state;
  String message;

  PlayerBroadcastStatus({this.state, this.message});

  factory PlayerBroadcastStatus.fromJson(Map<String, dynamic> json) =>
      PlayerBroadcastStatus(
          state: PlayerState.values.firstWhere(
                  (type) =>
              type.toString() == "BroadcastState." + json["state"]),
          message: json["message"]);

  Map<String, dynamic> toJson() =>
      {"state": state.toString(), "message": message};
}

class PlayerControllerValue {
  PlayerControllerValue({this.event, this.value});

  final Event event;

  final dynamic value;
}

/// Channel controller
class WOWZPlayerController extends Controller<PlayerControllerValue> {
  WOWZPlayerController() : super(PlayerControllerValue());

  bool _initialization;

  void _setInitialization() {
    _initialization = true;
    value = PlayerControllerValue(event: Event.initialization);
  }

  bool isInitialization() => _initialization;

  void set(WOWZPlayerConfig preset) {
    if (preset != null) {
      _config = preset;
      debugPrint("WOWZPlayerConfig: ${json.encode(_config.toDataMap())}");

      _channel?.invokeMethod(_push_config, json.encode(_config.toDataMap()));
    } else {
      debugPrint("WOWZPlayerConfig is not allowed to null!!!");
    }
  }

  /// Starts playback using the specified stream configuration. The onStatus()
  /// method of the status callback will be invoked with the state set to WOWZState.RUNNING when complete.
  void play(WOWZPlayerConfig preset) {
    if (preset != null) {
      _config = preset;
      debugPrint("WOWZPlayerConfig: ${json.encode(_config.toDataMap())}");
      _channel?.invokeMethod(_play_video, json.encode(_config.toDataMap()));
    } else {
      debugPrint("WOWZPlayerConfig is not allowed to null!!!");
    }
  }

  /// Stops playback.
  void stop() {
    _channel?.invokeMethod(_stop_video, "");
  }

  /// Sets the volume level.
  void setVolume(int volume) {
    _channel?.invokeMethod(_volume, volume);
  }

  /// Sets the player's audio track to mute.
  void mute(bool isMuted) {
    _channel?.invokeMethod(_mute, "$isMuted");
  }

  /// Checks whether the player is in a state that allows playback to start.
  Future<bool> isReadyToPlay() async {
    return await _channel?.invokeMethod(_is_ready_to_play);
  }

  /// Gets the duration of a video-on-demand stream.
  Future<int> getDuration() async {
    return await _channel?.invokeMethod(_duration);
  }

  /// Gets the timecode of the most recent frame displayed by the player.
  Future<int> getCurrentTime() async {
    return await _channel?.invokeMethod(_current_time);
  }

  WOWZPlayerConfig _config;
}

/// The WOWZStreamConfig class provides configuration properties for the streaming connection.
/// DOCS https://www.wowza.com/resources/gocodersdk/docs/api-reference-android/com/wowza/gocoder/sdk/api/player/WOWZPlayerConfig.html
class WOWZPlayerConfig extends WOWZStreamConfig {
  /// Creates and initializes an instance using the property values from the specified instance.
  WOWZPlayerConfig({String hostAddress,
    String applicationName,
    String streamName,
    int portNumber})
      : super(
      hostAddress: hostAddress,
      applicationName: applicationName,
      streamName: streamName,
      portNumber: portNumber);

  /// Updates the property values of this instance with the property values from the specified preset configuration.
  void set(WOWZMediaConfig preset) {
    if (preset != null) {
      super.set(preset);
    }
  }

  int _mPreRollBufferDurationMs;

  int getPreRollBufferDuration() {
    return this._mPreRollBufferDurationMs ~/ 1000.0;
  }

  int getPreRollBufferDurationMillis() {
    return this._mPreRollBufferDurationMs;
  }

  void setPreRollBufferDuration(double preRollBufferDuration) {
    if (preRollBufferDuration >= 0.0) {
      this._mPreRollBufferDurationMs = (preRollBufferDuration * 1000.0).round();
    } else {
      this._mPreRollBufferDurationMs = 0;
    }
  }

  Map toDataMap() {
    final map = {};
    map["PreRollBufferDurationMs"] = _mPreRollBufferDurationMs;
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
    if (videoEnabled == true) {
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
    if (audioEnabled == true) {
      map["audioChannels"] = audioChannels;
      map["audioSampleRate"] = audioSampleRate;
      map["audioBitrate"] = audioBitrate;
    }

    map["abrEnabled"] = "$abrEnabled";
    if (abrEnabled == true) {
      map["vbeFrameBufferSizeMultiplier"] = vbeFrameBufferSizeMultiplier;
      map["vbeFrameRateLowBandwidthSkipCount"] =
          vbeFrameRateLowBandwidthSkipCount;
    }

    return map;
  }
}

typedef PlayerStatusCallback = Function(PlayerBroadcastStatus);

typedef PlayerErrorCallback = Function(PlayerBroadcastStatus);

/// Player View
class WOWZPlayerView extends StatefulWidget {
  WOWZPlayerView({@required this.androidLicenseKey,
    @required this.iosLicenseKey,
    @required this.controller,
    this.playerStatusCallback,
    this.playerErrorCallback,
    this.wantKeepAlive = false});

  final bool wantKeepAlive;

  final WOWZPlayerController controller;
  final PlayerStatusCallback playerStatusCallback;
  final PlayerStatusCallback playerErrorCallback;

  final String androidLicenseKey;
  final String iosLicenseKey;

  @override
  _WOWZPlayerViewState createState() => _WOWZPlayerViewState();
}

class _WOWZPlayerViewState extends State<WOWZPlayerView>
    with AutomaticKeepAliveClientMixin {
  var _viewId = 0;
  MethodChannel _channel;

  @override
  Widget build(BuildContext context) {
    super.build(context);
    return (defaultTargetPlatform == TargetPlatform.android)
        ? AndroidView(
      viewType: _player_view_channel,
      onPlatformViewCreated: _onPlatformViewCreated,
    )
        : (defaultTargetPlatform == TargetPlatform.iOS)
        ? UiKitView(
      viewType: _player_view_channel,
      onPlatformViewCreated: _onPlatformViewCreated,
    )
        : Text(
        '$defaultTargetPlatform is not yet supported by the flutter_wowza plugin');
  }

  _onPlatformViewCreated(int viewId) {
    if (_viewId != viewId || _channel == null) {
      debugPrint(
          'WOWZPlayerView: MethodChannel: ${_player_view_channel}_$viewId');

      _channel = MethodChannel("${_player_view_channel}_$viewId");
      widget.controller?._setChannel(_channel);


      _channel.setMethodCallHandler((call) async {
        debugPrint(
            'WOWZPlayerView: method: ${call.method} | arguments: ${call
                .arguments}');
        switch (call.method) {
          case _player_status:
            widget.playerStatusCallback(
                playerStatusFromJson(call.arguments));
            break;
          case _player_error:
            widget.playerErrorCallback(
                playerStatusFromJson(call.arguments));
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
