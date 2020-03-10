part of gocoder;

const _camera_view_channel = 'flutter_wowza_camera_view';

enum BroadcastState { READY, BROADCASTING, IDLE }

enum FrameSize {
  FRAME_SIZE_176x144,
  FRAME_SIZE_320x240,
  FRAME_SIZE_352x288,
  FRAME_SIZE_640x480,
  FRAME_SIZE_960x540,
  FRAME_SIZE_1280x720,
  FRAME_SIZE_1440x1080,
  FRAME_SIZE_1920x1080,
  FRAME_SIZE_3840x2160
}

enum ScaleMode {
  /// Scale the camera preview to fit within the screen area. Letterboxing may be applied to maintain the aspect ratio.
  RESIZE_TO_ASPECT,

  /// Scale the camera preview to fill the entire screen area. The preview may be cropped to maintain the aspect ratio.
  FILL_VIEW
}

const _startPreview = "start_preview";
const _stopPreview = "stop_preview";
const _pausePreview = "pause_preview";
const _continuePreview = "continue_preview";

const _onPause = "on_pause";
const _onResume = "on_resume";

const _isSwitchCameraAvailable = "is_switch_camera_available";
const _switchCamera = "switch_camera";

class WOWZCameraController extends ValueNotifier<CameraControllerValue> {
  MethodChannel _channel;

  _setChannel(MethodChannel channel) {
    _channel = channel;
  }

  WOWZCameraController() : super(CameraControllerValue(null));

  /// Starts the camera preview display.
  startPreview() {
    value = CameraControllerValue(_startPreview);
  }

  /// Stops the camera preview display.
  stopPreview() {
    value = CameraControllerValue(_stopPreview);
  }

  pausePreview() {
    value = CameraControllerValue(_pausePreview);
  }

  continuePreview() {
    value = CameraControllerValue(_continuePreview);
  }

  onPause() {
    value = CameraControllerValue(_onPause);
  }

  onResume() {
    value = CameraControllerValue(_onResume);
  }

  switchCamera() {
    value = CameraControllerValue(_switchCamera);
  }

  Future<bool> isSwitchCameraAvailable() async {
    return await _channel?.invokeMethod(_isSwitchCameraAvailable);
  }
}

class WOWZCameraView extends StatefulWidget {
  WOWZCameraView(
      {@required this.controller,
      @required this.apiLicenseKey,
      @required this.hostAddress,
      @required this.portNumber,
      @required this.applicationName,
      @required this.streamName,
      this.username,
      this.password,
      this.frameSize,
      this.scaleMode = ScaleMode.RESIZE_TO_ASPECT});

  @override
  _WOWZCameraViewState createState() => _WOWZCameraViewState();

  final WOWZCameraController controller;

  final String apiLicenseKey;

  // Set the connection properties for the target Wowza Streaming Engine server or Wowza Streaming Cloud live stream
  final String hostAddress;
  final int portNumber;
  final String applicationName;
  final String streamName;

  //authentication
  final String username;
  final String password;

  final WOWZSize frameSize;
  final ScaleMode scaleMode;
}

class _WOWZCameraViewState extends State<WOWZCameraView> {
  var _viewId = 0;
  MethodChannel _channel;

  @override
  void initState() {
    super.initState();
    widget.controller?.addListener(() {
      if (widget.controller != null && widget.controller.value != null) {
        print('controller event: ${widget.controller.value.event}');
        switch (widget.controller.value.event) {
          case _startPreview:
          case _startPreview:
          case _stopPreview:
          case _pausePreview:
          case _continuePreview:
          case _isSwitchCameraAvailable:
          case _onPause:
          case _onResume:
          case _switchCamera:
            _channel?.invokeMethod(widget.controller.value.event);
            break;
          default:
            break;
        }
      }
    });
  }

  @override
  Widget build(BuildContext context) {
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
    if (_viewId != viewId) {
      _viewId = viewId;
      _channel = null;
      _channel = MethodChannel("${_camera_view_channel}_$viewId");
      _channel.setMethodCallHandler((call) async {
        switch (call.method) {
          case 'state':
            _state(BroadcastState.values.firstWhere((type) =>
                type.toString() == "BroadcastState." + call.arguments));
            break;
          case 'error':
            break;
        }
      });
      widget.controller?._setChannel(_channel);
    }
  }

  void _state(BroadcastState values) {
    switch (values) {
      case BroadcastState.IDLE:
        break;
      case BroadcastState.READY:
        break;
      case BroadcastState.BROADCASTING:
        break;
      default:
        break;
    }
  }
}

@immutable
class CameraControllerValue {
  CameraControllerValue(this.event);

  final String event;
}

@immutable
class WOWZSize {
  WOWZSize(this.width, this.height);

  final int width;
  final int height;
}
