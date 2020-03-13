part of gocoder;

@immutable
class CameraControllerValue {
  CameraControllerValue({this.event, this.value});

  final String event;

  final dynamic value;
}

class WOWZCameraController extends ValueNotifier<CameraControllerValue> {
  MethodChannel _channel;

  _setChannel(MethodChannel channel) {
    _channel = channel;
  }

  WOWZCameraController() : super(CameraControllerValue());

  /// Starts the camera preview display.
  startPreview() {
    value = CameraControllerValue(event: _startPreview);
  }

  /// Stops the camera preview display.
  stopPreview() {
    value = CameraControllerValue(event: _stopPreview);
  }

  pausePreview() {
    value = CameraControllerValue(event: _pausePreview);
  }

  continuePreview() {
    value = CameraControllerValue(event: _continuePreview);
  }

  onPause() {
    value = CameraControllerValue(event: _onPause);
  }

  onResume() {
    value = CameraControllerValue(event: _onResume);
  }

  switchCamera() {
    value = CameraControllerValue(event: _switchCamera);
  }

  flashLight(bool enable) {
    value = CameraControllerValue(event: _flashlight, value: enable);
  }

  Future<bool> isSwitchCameraAvailable() async {
    return await _channel?.invokeMethod(_isSwitchCameraAvailable);
  }

  setFps(int fps) {
    value = CameraControllerValue(event: _fps, value: fps);
  }

  setVideoBitrate(int bps) {
    value = CameraControllerValue(event: _bps, value: bps);
  }

  setAudioRate(int khz) {
    value = CameraControllerValue(event: _khz, value: khz);
  }

  setMuted(bool muted) {
    value = CameraControllerValue(event: _muted, value: muted);
  }

  // WOWZBroadcast
  startBroadcast() {
    value = CameraControllerValue(event: _startBroadcast);
  }

  endBroadcast() {
    value = CameraControllerValue(event: _endBroadcast);
  }

  Future<bool> isInitialized() async {
    return await _channel?.invokeMethod(_isInitialized);
  }
}
