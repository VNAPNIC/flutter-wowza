part of gocoder;

const _camera_view_channel = 'flutter_wowza';

enum BroadcastState {
  READY,
  BROADCASTING,
  IDLE,
  IDLE_ERROR,
  READY_ERROR,
  BROADCASTING_ERROR
}

WOWZBroadcastStatus wowzBroadcastStatusFromJson(String str) =>
    WOWZBroadcastStatus.fromJson(json.decode(str));

String clientToJson(WOWZBroadcastStatus data) => json.encode(data.toJson());

class WOWZBroadcastStatus {
  BroadcastState state;
  String message;
  bool isError = false;

  WOWZBroadcastStatus({this.state, this.message});

  factory WOWZBroadcastStatus.fromJson(Map<String, dynamic> json) =>
      WOWZBroadcastStatus(
          state: BroadcastState.values.firstWhere(
              (type) => type.toString() == "BroadcastState." + json["state"]),
          message: json["message"]);

  Map<String, dynamic> toJson() =>
      {"state": state.toString(), "message": message};
}

enum CameraLensDirection { front, back, external }

/// Affect the quality of video recording and image capture:
///
/// If a preset is not available on the camera being used a preset of lower quality will be selected automatically.
enum ResolutionPreset {
  /// 352x288 on iOS, 240p (320x240) on Android
  low,

  /// 480p (640x480 on iOS, 720x480 on Android)
  medium,

  /// 720p (1280x720)
  high,

  /// 1080p (1920x1080)
  veryHigh,

  /// 2160p (3840x2160)
  ultraHigh,

  /// The highest resolution available.
  max,
}

/// Returns the resolution preset as a String.
String serializeResolutionPreset(ResolutionPreset resolutionPreset) {
  switch (resolutionPreset) {
    case ResolutionPreset.max:
      return 'max';
    case ResolutionPreset.ultraHigh:
      return 'ultraHigh';
    case ResolutionPreset.veryHigh:
      return 'veryHigh';
    case ResolutionPreset.high:
      return 'high';
    case ResolutionPreset.medium:
      return 'medium';
    case ResolutionPreset.low:
      return 'low';
  }
  throw ArgumentError('Unknown ResolutionPreset value');
}


CameraLensDirection _parseCameraLensDirection(String string) {
  switch (string) {
    case 'front':
      return CameraLensDirection.front;
    case 'back':
      return CameraLensDirection.back;
    case 'external':
      return CameraLensDirection.external;
  }
  throw ArgumentError('Unknown CameraLensDirection value');
}

typedef WOWZBroadcastStatusCallback = Function(WOWZBroadcastStatus);

abstract class OnWOWZBroadcastStatusCallback {
  void onWZStatus(WOWZBroadcastStatus status);

  void onWZError(WOWZBroadcastStatus status);
}

class WOWZCameraView extends StatefulWidget {
  WOWZCameraView(
      {@required this.controller,
      @required this.androidLicenseKey,
      @required this.iosLicenseKey,
      this.broadcastStatusCallback});

  @override
  _WOWZCameraViewState createState() => _WOWZCameraViewState();

  final WOWZCameraController controller;

  final WOWZBroadcastStatusCallback broadcastStatusCallback;

  final String androidLicenseKey;
  final String iosLicenseKey;
}

class _WOWZCameraViewState extends State<WOWZCameraView> with AutomaticKeepAliveClientMixin{
  var _viewId = 0;
  MethodChannel _channel;

  @override
  void initState() {
    super.initState();
    widget.controller?.addListener(() {
      if (widget.controller != null && widget.controller.value != null) {
        print('controller event: ${widget.controller.value.event}');
        switch (widget.controller.value.event) {
          case _flashlight:
            if (defaultTargetPlatform == TargetPlatform.android)
              _channel?.invokeMethod(
                  widget.controller.value.event, widget.controller.value.value);
            else
              _channel?.invokeMethod(widget.controller.value.value
                  ? _flashlightOn
                  : _flashlightOff);
            break;
          case _muted:
            if (defaultTargetPlatform == TargetPlatform.android)
              _channel?.invokeMethod(
                  widget.controller.value.event, widget.controller.value.value);
            else
              _channel?.invokeMethod(
                  widget.controller.value.value ? _mutedOn : _mutedOff);
            break;
          case _startPreview:
          case _startPreview:
          case _stopPreview:
          case _pausePreview:
          case _continuePreview:
          case _isSwitchCameraAvailable:
          case _onPause:
          case _onResume:
          case _switchCamera:
          case _fps:
          case _bps:
          case _khz:
          case _startBroadcast:
          case _endBroadcast:
            _channel?.invokeMethod(
                widget.controller.value.event, widget.controller.value.value);
            break;
          default:
            break;
        }
      }
    });
  }

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
                '$defaultTargetPlatform is not yet supported by the text_view plugin');
  }

  _onPlatformViewCreated(int viewId) {
    if (_viewId != viewId || _channel == null) {

      _viewId = viewId;
      _channel = MethodChannel("${_camera_view_channel}_$viewId");
      widget.controller?._setChannel(_channel);

      _channel.setMethodCallHandler((call) async {
        print('wowz: status: ${call.arguments}');
      });

      // license key gocoder sdk
      _channel.invokeMethod(
          _apiLicenseKey,
          (defaultTargetPlatform == TargetPlatform.android)
              ? widget.androidLicenseKey
              : widget.iosLicenseKey);

      if (widget.controller.lazyInitialization) {
        widget.controller.resetConfig();
      }

    }
  }

  @override
  bool get wantKeepAlive => true;
}