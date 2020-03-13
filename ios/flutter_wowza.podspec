#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# pod update
#
Pod::Spec.new do |s|
  s.name             = 'flutter_wowza'
  s.version          = '0.0.1'
  s.summary          = 'Wowza GoCoder SDK plugin for Flutter Android&#x2F;IOS '
  s.description      = <<-DESC
Wowza GoCoder SDK plugin for Flutter Android&#x2F;IOS 
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  s.platform = :ios, '8.0'
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'VALID_ARCHS' => 'armv7 arm64 x86_64' }
  s.static_framework = true
  s.swift_version = '5.0'

  s.preserve_paths = 'WowzaGoCoderSDK.framework'
  s.xcconfig = { 'OTHER_LDFLAGS' => '-framework WowzaGoCoderSDK' }
  s.vendored_frameworks = 'WowzaGoCoderSDK.framework'

end
