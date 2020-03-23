import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter/services.dart';
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
    SchedulerBinding.instance.addPostFrameCallback((_) {
      controller.setWOWZConfig(
          hostAddress: "xxx.xxx.xxx.xxx",
          portNumber: 1935,
          applicationName: "xxxxxx",
          streamName: "xxxxx",
          username: "xxxx",
          password: "xxxx",
          scaleMode: ScaleMode.FILL_VIEW);
    });
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
                  androidLicenseKey: "GOSK-xxxx-xxxx-xxxx-xxxx-xxxx",
                  iosLicenseKey: "GOSK-xxxx-xxxx-xxxx-xxxx-xxxx",
                  controller: controller,
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
