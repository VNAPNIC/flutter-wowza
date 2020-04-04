import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter/services.dart';
import 'package:flutter_wowza/gocoder/wowza_gocoder.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _PlayerViewState createState() => _PlayerViewState();
}

class _PlayerViewState extends State<MyApp> {
  WOWZPlayerController controller = WOWZPlayerController();

  bool flashLight = false;

  @override
  void initState() {
    super.initState();
    controller.addListener(() {
      if (controller.value.event == Event.initialization) {
        final config = WOWZPlayerConfig();
        config.set(WOWZMediaConfig.FRAME_SIZE_640x480);
        config.hlsBackupUrl =
            "http://svc-lvanvato-cxtv-whio.cmgvideo.com/whio/2596k/index.m3u8";
        config.videoEnabled = true;
        config.audioEnabled = true;
        config.isPlayback = true;
        config.hlsEnabled = true;
        controller.play(config);
      }
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
                child: WOWZPlayerView(
                  androidLicenseKey: "GOSK-9C47-010C-2895-D225-9FEF",
                  iosLicenseKey: "GOSK-9C47-010C-A9B9-EB78-3FBD",
                  controller: controller,
                ),
              )
            ],
          ),
        ),
      ),
    );
  }
}

class _BroadcastState extends State<MyApp> {
  BroadcastController controller = BroadcastController();

  bool flashLight = false;

  final config = WOWZBroadcastConfig();

  @override
  void initState() {
    super.initState();
    controller.addListener(() {
      if (controller.value.event == Event.initialization) {
        config.set(WOWZMediaConfig.FRAME_SIZE_640x480);
        config.hostAddress = "xxx.xxx.xxx.xxx";
        config.portNumber = 1935;
        config.applicationName = "xxxxxx";
        config.streamName = "xxxxx";
        config.username = "xxxx";
        config.password = "xxxx";
        controller.setScaleMode(ScaleMode.FILL_VIEW);
      }
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
                  androidLicenseKey: "GOSK-9C47-010C-2895-D225-9FEF",
                  iosLicenseKey: "GOSK-9C47-010C-A9B9-EB78-3FBD",
                  controller: controller,
                ),
              ),
              Wrap(
                children: <Widget>[
                  _action('start preview', () {
                    controller.openCamera(config);
                  }),
                  _action('switch camera', () {
                    controller.switchCamera();
                  }),
                  _action('flashlight', () {
                    controller.flashLight(!flashLight);
                    flashLight = !flashLight;
                  }),
                  _action('Start Live Stream', () {
                    controller.startBroadcast(config);
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
