//
//  WOWZCameraView.swift
//  Runner
//
//  Created by NamIT on 3/10/20.
//  Copyright © 2020 The Chromium Authors. All rights reserved.
//

import Foundation
import UIKit
import WowzaGoCoderSDK

public class FlutterWOWZCameraView : NSObject, FlutterPlatformView,WOWZBroadcastStatusCallback{
    
    var cameraView: WOWZCameraPreview?
    // The top-level GoCoder API interface
    var goCoder: WowzaGoCoder?
    // The broadcast configuration settings
    let goCoderConfig: WowzaConfig = WowzaConfig()

    let channel: FlutterMethodChannel
    let controller: FlutterViewController
    let frame:CGRect
    let viewId: Int64
    
    let uiView: UIView
    
    public func view() -> UIView {
        return uiView
    }

    
    public init(_ frame: CGRect,viewId: Int64,channel: FlutterMethodChannel,controller: FlutterViewController, args: Any?) {
        self.channel = channel
        self.controller = controller
        self.frame = frame
        self.viewId = viewId
        uiView = UIView(frame: frame)
        super.init()
        
        WowzaGoCoder.setLogLevel(.default)
        
        channel.setMethodCallHandler({
            (call: FlutterMethodCall, result:FlutterResult) -> Void in
            
            let argument = call.arguments as? String ?? ""
            print("Channel -> method: " + call.method + " | arguments: " + argument)
            
            switch call.method {
            case "api_licenseKey":
                WowzaGoCoder.registerLicenseKey(argument)
                break;
            case "host_address":
                self.goCoderConfig.hostAddress = argument
                break;
            case "port_number":
                self.goCoderConfig.portNumber = UInt(Int(String(argument)) ?? 0)
                break;
            case "application_name":
                self.goCoderConfig.applicationName = argument
                break;
            case "stream_name":
                self.goCoderConfig.streamName = argument
                break;
            case "username":
                self.goCoderConfig.username = argument
                break;
            case "password":
                self.goCoderConfig.password = argument
                break;
            case "wowz_size":
                let cgSize = argument.split(separator: "/")
                if cgSize.count > 1{
                    self.goCoderConfig.videoWidth = UInt(Int(String(cgSize[0])) ?? 640)
                    self.goCoderConfig.videoHeight = UInt(Int(String(cgSize[1])) ?? 400)
                }
                break;
            case "wowz_media_config":
                switch argument{
                case "WOWZMediaConfig.FRAME_SIZE_176x144":
                    print("IOS cannot support 176x144")
                    break
                case "WOWZMediaConfig.FRAME_SIZE_320x240":
                    print("IOS cannot support 320x240")
                    break
                case "WOWZMediaConfig.FRAME_SIZE_352x288":
                    self.goCoderConfig.load(WOWZFrameSizePreset.preset352x288)
                    break
                case "WOWZMediaConfig.FRAME_SIZE_640x480":
                    self.goCoderConfig.load(WOWZFrameSizePreset.preset640x480)
                    break
                case "WOWZMediaConfig.FRAME_SIZE_960x540":
                    print("IOS cannot support 960x540")
                    break
                case "WOWZMediaConfig.FRAME_SIZE_1280x720":
                    self.goCoderConfig.load(WOWZFrameSizePreset.preset1280x720)
                    break
                case "WOWZMediaConfig.FRAME_SIZE_1440x1080":
                    print("IOS cannot support 1440x1080")
                    break
                case "WOWZMediaConfig.FRAME_SIZE_1920x1080":
                    self.goCoderConfig.load(WOWZFrameSizePreset.preset1920x1080)
                    break
                case "WOWZMediaConfig.FRAME_SIZE_3840x2160":
                    self.goCoderConfig.load(WOWZFrameSizePreset.preset1920x1080)
                    break
                default:
                     self.goCoderConfig.load(WOWZFrameSizePreset.preset640x480)
                    break
                }
                
                break;
            case "scale_mode":
                switch argument {
                case "ScaleMode.FILL_VIEW":
                    self.goCoderConfig.broadcastScaleMode = WOWZBroadcastScaleMode.aspectFill
                    break
                default:
                    self.goCoderConfig.broadcastScaleMode = WOWZBroadcastScaleMode.aspectFit
                }
                break;
                case "fps":
                    self.goCoderConfig.videoFrameRate  = UInt(Int(String(argument)) ?? 30)
                break;
                case "bps":
                    self.goCoderConfig.videoBitrate = UInt(Int(String(argument)) ?? 1500)
                break;
                case "khz":
                    self.goCoderConfig.audioSampleRate = UInt(Int(String(argument)) ?? 44100)
                break;
            case "init_go_coder":
                if let goCoder = WowzaGoCoder.sharedInstance(){
                            self.goCoder = goCoder
                            print("gocoder is init!")
                            WowzaGoCoder.requestPermission(for: .camera, response: { (permission) in
                                print("Camera permission is: \(permission == .authorized ? "authorized" : "denied")")
                            })

                            WowzaGoCoder.requestPermission(for: .microphone, response: { (permission) in
                                print("Microphone permission is: \(permission == .authorized ? "authorized" : "denied")")
                            })
                            self.goCoder?.config = self.goCoderConfig
                            self.goCoder?.cameraView = self.uiView
                            self.cameraView = self.goCoder?.cameraPreview
                    }
                break
            case "start_preview":
                WowzaGoCoder.requestPermission(for: .camera, response: { (permission) in
                    print("Camera permission is: \(permission == .authorized ? "authorized" : "denied")")
                })

                WowzaGoCoder.requestPermission(for: .microphone, response: { (permission) in
                    print("Microphone permission is: \(permission == .authorized ? "authorized" : "denied")")
                })
                self.cameraView?.start()
                break;
            case "pause_preview":
                self.cameraView?.stop()
                break;
            case "continue_preview":
                WowzaGoCoder.requestPermission(for: .camera, response: { (permission) in
                print("Camera permission is: \(permission == .authorized ? "authorized" : "denied")")
                })

                WowzaGoCoder.requestPermission(for: .microphone, response: { (permission) in
                print("Microphone permission is: \(permission == .authorized ? "authorized" : "denied")")
                })
                self.cameraView?.start()
                break;
            case "switch_camera":
                self.cameraView?.switchCamera()
                break;
            case "flashlight_on":
                self.cameraView?.camera?.isTorchOn = true
                break;
            case "flashlight_off":
                self.cameraView?.camera?.isTorchOn = false
                break;
            case "muted_on":
                self.goCoder?.isAudioMuted  = true
                break;
            case "muted_off":
                self.goCoder?.isAudioMuted  = false
                break;
            case "is_switch_camera_available":
                result(self.cameraView?.isSwitchCameraAvailable(for: self.goCoderConfig))
                break;
            case "is_initialized":
                result(self.goCoder?.isStreaming)
                break;
            case "start_broadcast":
                WowzaGoCoder.requestPermission(for: .camera, response: { (permission) in
                print("Camera permission is: \(permission == .authorized ? "authorized" : "denied")")
                })

                WowzaGoCoder.requestPermission(for: .microphone, response: { (permission) in
                print("Microphone permission is: \(permission == .authorized ? "authorized" : "denied")")
                })
                self.goCoder?.startStreaming(self)
                break;
            case "end_broadcast":
                self.goCoder?.endStreaming(self)
                break;
            case "on_pause":
                self.cameraView?.stop()
                break;
            case "on_resume":
                WowzaGoCoder.requestPermission(for: .camera, response: { (permission) in
                print("Camera permission is: \(permission == .authorized ? "authorized" : "denied")")
                })

                WowzaGoCoder.requestPermission(for: .microphone, response: { (permission) in
                print("Microphone permission is: \(permission == .authorized ? "authorized" : "denied")")
                })
                self.cameraView?.start()
                break;
            default:
                break;
            }
        })
    }
    
    public func onWOWZStatus(_ status: WOWZBroadcastStatus!) {
        print("status -----------> " + String(status.state.rawValue))
        switch status.state {
        case WOWZBroadcastState.idle:
            let ms = "{\"state\":\"IDLE\",\"message\":\"" + String(status.state.rawValue) + "\"}"
            channel.invokeMethod("broadcast_status", arguments: ms)
            break
        case WOWZBroadcastState.broadcasting:
            let ms = "{\"state\":\"BROADCASTING\",\"message\":\"" + String(status.state.rawValue) + "\"}"
            channel.invokeMethod("broadcast_status", arguments: ms)
            break
        case WOWZBroadcastState.ready:
            let ms = "{\"state\":\"READY\",\"message\":\"" + String(status.state.rawValue) + "\"}"
            channel.invokeMethod("broadcast_status", arguments: ms)
            break
        default:
            break
        }
    }
    
    public func onWOWZError(_ status: WOWZBroadcastStatus!) {
        print("status -----------> " + String(status.state.rawValue))
        switch status.state {
        case WOWZBroadcastState.idle:
            let ms = "{\"state\":\"IDLE\",\"message\":\"" + String(status.state.rawValue) + "\"}"
            channel.invokeMethod("broadcast_error", arguments: ms)
            break
        case WOWZBroadcastState.broadcasting:
            let ms = "{\"state\":\"BROADCASTING\",\"message\":\"" + String(status.state.rawValue) + "\"}"
            channel.invokeMethod("broadcast_error", arguments: ms)
            break
        case WOWZBroadcastState.ready:
            let ms = "{\"state\":\"READY\",\"message\":\"" + String(status.state.rawValue) + "\"}"
            channel.invokeMethod("broadcast_error", arguments: ms)
            break
        default:
            break
        }
    }
}

extension String {
    var boolValue: Bool {
        return (self as NSString).boolValue
    }
}
