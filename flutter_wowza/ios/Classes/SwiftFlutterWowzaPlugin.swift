import Flutter
import UIKit

public class SwiftFlutterWowzaPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
     let controller =
           (UIApplication.shared.delegate?.window??.rootViewController) as! FlutterViewController;
           let factory = WOWZCameraViewFactory(messenger: registrar.messenger(),controller:controller)
           registrar.register(factory, withId: "flutter_wowza")
  }
}
