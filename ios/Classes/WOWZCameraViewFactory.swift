import Foundation

public class WOWZCameraViewFactory : NSObject, FlutterPlatformViewFactory{
    let messenger : FlutterBinaryMessenger
    let controller : FlutterViewController
    
    public init(messenger: FlutterBinaryMessenger,controller: FlutterViewController) {
        self.messenger = messenger
        self.controller = controller
    }
    
    public func create(withFrame frame: CGRect, viewIdentifier viewId: Int64, arguments args: Any?) -> FlutterPlatformView {
        let chanel = FlutterMethodChannel(
            name: "flutter_wowza_" + String(viewId),
            binaryMessenger: self.messenger
        )
        
        return FlutterWOWZCameraView(frame,viewId: viewId,channel: chanel,controller: controller,args: args)
    }
    
    public func createArgsCodec() -> FlutterMessageCodec & NSObjectProtocol {
        return FlutterStandardMessageCodec.sharedInstance()
    }
}
