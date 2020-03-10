import 'package:flutter/material.dart';
import 'package:flutter_wowza/gocoder/wowza_gocoder.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  WOWZCameraController controller = WOWZCameraController();

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
        body: Column(
          children: <Widget>[
            WOWZCameraView(
              apiLicenseKey: "GOSK-xxxx-xxxx-xxxx-xxxx-xxxx",
              controller: controller,
              hostAddress: "xxx.xxx.xxx.xxx.xxx",
              portNumber: 1935,
              applicationName: "app-xxxx",
              streamName: "xxxxxxx",
            ),
            RaisedButton(onPressed: () {
              controller.startPreview();
            })
          ],
        ),
      ),
    );
  }
}
