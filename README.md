# The project is redeveloping so that it doesn't depend on Wowza gocoder SDK

#### Wowza Player, Wowza GoCoder SDK, & the technology powering the Ultra Low Latency service in Wowza Streaming Cloud will no longer be available January 5, 2021. [Learn more](https://info.wowza.com/product-notification-april-2020) .




# flutter_wowza

The project is based on [Wowza GoCoder SDK](https://www.wowza.com/docs/wowza-gocoder-sdk)

Learn more about using GoCoder here
[Android](https://www.wowza.com/docs/how-to-build-a-basic-app-with-gocoder-sdk-for-android) | 
[IOS](https://www.wowza.com/docs/how-to-build-a-basic-app-with-gocoder-sdk-for-ios)

Get the Free License [Free License](https://www.wowza.com/products/gocoder/sdk/license)

Connect the Wowza GoCoder encoding app to Wowza Streaming Engine
[Docs](https://www.wowza.com/docs/how-to-connect-the-wowza-gocoder-encoding-app-to-wowza-streaming-engine) | 
[Youtube](https://www.youtube.com/watch?v=M860TAk4hWA&fbclid=IwAR2kdg_TIUvCDAt1if3zWBe4O_wSzdSwPTV-_J71VYN4BHQb1wWakPNvUb8)

<img src="https://raw.githubusercontent.com/VNAPNIC/flutter-wowza/master/resouces/mobile.jpg" data-canonical-src="https://raw.githubusercontent.com/VNAPNIC/flutter-wowza/master/resouces/mobile.jpg" width="400" />

## Usage
Add `flutter-wowza` as a [dependency in your pubspec.yaml file](https://flutter.io/using-packages/).

### iOS

Add the following entry to your _Info.plist_ file, located in `<project root>/ios/Runner/Info.plist`:

```xml
<key>NSCameraUsageDescription</key>
<string>The camera will be used to capture video for live streaming</string>
<key>NSMicrophoneUsageDescription</key>
<string>The microphone will be used to capture live audio for streaming</string>
<key>UIRequiredDeviceCapabilities</key>
<array>
   <string>armv7</string>
</array>
<key>io.flutter.embedded_views_preview</key>
<true/>
```

### Android

Ensure the following permission is present in your Android Manifest file, located in `<project root>/android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.FLASHLIGHT" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-feature android:name="android.hardware.camera.any" android:required="false" />
<uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />
<uses-feature android:name="android.hardware.camera.flash" android:required="false" />
<uses-feature android:name="android.hardware.microphone" android:required="false" />
<uses-feature android:glEsVersion="0x00020000" android:required="true" />
```
## Example

```dart
WOWZCameraView(
    androidLicenseKey: "GOSK-9C47-010C-2895-D225-9FEF",
    iosLicenseKey: "GOSK-9C47-010C-A9B9-EB78-3FBD",
    controller: controller,
    statusCallback: (status) {
        print("status: ${status.mState} | ${status.isStarting()} | ${status.isReady()}");
    },
    broadcastStatusCallback: (broadcastStatus) {
        print("status: ${broadcastStatus.state.toString()} | ${broadcastStatus.message}");
    },
)
```

Control live streams

```dart
WOWZCameraController controller = WOWZCameraController();
```

configuration wowza

```dart
controller.setWOWZConfig(
              hostAddress: "xxx.xxx.xxx.xxx",
              portNumber: 1935,
              applicationName: "xxxxxx",
              streamName: "xxxxx",
              username: "xxxx",
              password: "xxxx",
              scaleMode: ScaleMode.FILL_VIEW
          );
```

## Functionality supported

[WOWZMediaConfig](https://www.wowza.com/resources/gocodersdk/docs/api-reference-android/com/wowza/gocoder/sdk/api/configuration/WOWZMediaConfig.html)

| Feature | Android | iOS |
| :-------------: | :-------------:| :-----: |
| WOWZMediaConfig.FRAME_SIZE_176x144 | :heavy_check_mark: |  | 
| WOWZMediaConfig.FRAME_SIZE_320x240 | :heavy_check_mark: |  |
| WOWZMediaConfig.FRAME_SIZE_352x288 | :heavy_check_mark: | :heavy_check_mark: |
| WOWZMediaConfig.FRAME_SIZE_640x480 | :heavy_check_mark: | :heavy_check_mark: |
| WOWZMediaConfig.FRAME_SIZE_960x540 | :heavy_check_mark: |  |
| WOWZMediaConfig.FRAME_SIZE_1280x720 | :heavy_check_mark: | :heavy_check_mark: |
| WOWZMediaConfig.FRAME_SIZE_1440x1080 | :heavy_check_mark: |  |
| WOWZMediaConfig.FRAME_SIZE_1920x1080 | :heavy_check_mark: | :heavy_check_mark: |
| WOWZMediaConfig.FRAME_SIZE_3840x2160 | :heavy_check_mark: | :heavy_check_mark: |

[scaleMode](https://www.wowza.com/resources/gocodersdk/docs/1.0/api-docs-android/com/wowza/gocoder/sdk/api/geometry/WZCropDimensions.html)

| Feature | Android | iOS |
| :-------------: | :-------------:| :-----: |
| ScaleMode.FILL_VIEW | :heavy_check_mark: | :heavy_check_mark: | 
| ScaleMode.RESIZE_TO_ASPECT | :heavy_check_mark: | :heavy_check_mark: |

| IOS | Android | Flutter
| :-------------: | :-------------:| :-------------:|
| hostAddress | hostAddress | hostAddress |
| portNumber | portNumber | portNumber |
| applicationName | applicationName | applicationName |
| streamName | streamName | streamName |
| username | username | username |
| password | password | password |
| videoFrameRate | videoFrameRate | fps |
| videoBitrate | videoBitrate | bps |
| audioSampleRate | audioSampleRate | khz |
| cameraView.start() | WOWZCameraView.startPreview() | startPreview |
| cameraView.stop() | WOWZCameraView.stopPreview() | stopPreview |
| cameraView.start() | WOWZCameraView.continuePreview() | continuePreview |
| cameraView.stop() | WOWZCameraView.pausePreview() | pausePreview |
| cameraView.switchCamera() | WOWZCameraView.switchCamera() | switchCamera |
| cameraView.camera.isTorchOn | WOWZCameraView.camera.isTorchOn | flashLight |
| GoCoder.isAudioMuted | WOWZAudioDevice.isMuted | muted |
| cameraView.isSwitchCameraAvailable | WOWZCameraView.isSwitchCameraAvailable | isSwitchCameraAvailable |
| GoCoder.isStreaming | WowzaGoCoder.isInitialized | isInitialized |
| GoCoder.startStreaming | WOWZBroadcast.startBroadcast | startBroadcast |
| GoCoder.endStreaming | WOWZBroadcast.endBroadcast | endBroadcast |

### Project that is still being developed 
