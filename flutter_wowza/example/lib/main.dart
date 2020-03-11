import 'package:flutter/material.dart';
import 'package:flutter_wowza/gocoder/wowza_gocoder.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  WOWZCameraController controller = WOWZCameraController();

  bool flashLight = false;

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: SingleChildScrollView(
          child: Stack(
            children: <Widget>[
              SizedBox(
                height: 720,
                width: 1280,
                child: WOWZCameraView(
                  apiLicenseKey: "GOSK-9C47-010C-2895-D225-9FEF",
                  controller: controller,
                  hostAddress: "20ae97.entrypoint.cloud.wowza.com",
                  portNumber: 1935,
                  applicationName: "app-9887",
                  streamName: "eea812c0",
                  username: "client49777",
                  password: "59afa4d1",
                  scaleMode: ScaleMode.FILL_VIEW,
                  statusCallback: (status) {
                    print(
                        "status: ${status.mState} | ${status.isStarting()} | ${status.isReady()}");
                  },
                  broadcastStatusCallback: (broadcastStatus) {
                    print(
                        "status: ${broadcastStatus.state.toString()} | ${broadcastStatus.message}");
                  },
                ),
              ),
              Wrap(
                children: <Widget>[
                  _action('start preview', () {
                    controller.startPreview();
                  }),
                  _action('resume preview', () {
                    controller.continuePreview();
                  }),
                  _action('pause preview', () {
                    controller.pausePreview();
                  }),
                  _action('switch camera', () {
                    controller.switchCamera();
                  }),
                  _action('flashlight', () {
                    controller.flashLight(!flashLight);
                    flashLight = !flashLight;
                  }),
                  _action('Start Live Stream', () {
                    controller.startBroadcast();
                  }),
                  _action('End Live Stream', () {
                    controller.endBroadcast();
                  }),
                ],
              )
            ],
          ),
        ),
      ),
    );
  }

  _action(text, Function function) =>
      RaisedButton(child: Text(text), onPressed: function);
}
