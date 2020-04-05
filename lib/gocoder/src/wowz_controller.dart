part of gocoder;

enum Event { initialization }

class ControllerValue {
  ControllerValue({this.event, this.value});

  final String event;

  final dynamic value;
}

/// Channel controller
abstract class Controller<T> extends ValueNotifier<T> {

  Controller(T value) : super(value);

  MethodChannel _channel;

  _setChannel(MethodChannel channel) {
    _channel = channel;
  }
}
