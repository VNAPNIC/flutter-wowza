part of gocoder;

/// The WOWZSize class represents a dimension with integer-based width and height values. For example:
///         WOWZSize frameSize = WOWZSize(1280, 720);
///         float aspectRatio = frameSize.aspectRatio();
class WOWZSize {
  WOWZSize(this.width, this.height);

  int width;
  int height;

  void _set(int width, int height) {
    this.width = width;
    this.height = height;
  }

  void set(WOWZSize other) {
    if (other != null) {
      this._set(other.width, other.height);
    }
  }
}

///  the H.264 profile level.
/// DOCS https://www.wowza.com/resources/gocodersdk/docs/api-reference-android/com/wowza/gocoder/sdk/api/h264/WOWZProfileLevel.html
class WOWZProfileLevel {
  WOWZProfileLevel(WOWZProfileLevel other) {
    this.set(other);
  }

  void set(WOWZProfileLevel other) {
    if (other != null || !other.validate()) {
      this.mProfile = other.mProfile;
      this.mLevel = other.mLevel;
    }
  }

  int mProfile;
  int mLevel;

  /// An identifier for the H.264 baseline profile.
  static final PROFILE_BASELINE = 1;

  /// An identifier for the H.264 main profile.
  static final PROFILE_MAIN = 3;

  /// An identifier for the H.264 high profile.
  static final PROFILE_HIGH = 4;

  /// Sets the profile level to PROFILE_LEVEL_AUTO to have the encoder select the best one for the profile specified.
  static final PROFILE_LEVEL_AUTO = 0;

  /// Identifiers for the various profile levels.
  static final PROFILE_LEVEL1 = 1;
  static final PROFILE_LEVEL1B = 2;
  static final PROFILE_LEVEL11 = 3;
  static final PROFILE_LEVEL12 = 4;
  static final PROFILE_LEVEL13 = 5;
  static final PROFILE_LEVEL2 = 6;
  static final PROFILE_LEVEL21 = 7;
  static final PROFILE_LEVEL22 = 8;
  static final PROFILE_LEVEL3 = 9;
  static final PROFILE_LEVEL31 = 10;
  static final PROFILE_LEVEL32 = 11;
  static final PROFILE_LEVEL4 = 12;
  static final PROFILE_LEVEL41 = 13;
  static final PROFILE_LEVEL42 = 14;
  static final PROFILE_LEVEL5 = 15;
  static final PROFILE_LEVEL51 = 16;

  bool validate() {
    switch (this.mProfile) {
      case 1:
      case 3:
      case 4:
        var result = false;
        switch (this.mLevel) {
          case 0:
          case 1:
          case 2:
          case 3:
          case 4:
          case 5:
          case 6:
          case 7:
          case 8:
          case 9:
          case 10:
          case 11:
          case 12:
          case 13:
          case 14:
          case 15:
          case 16:
            result = true;
            break;
          default:
            break;
        }
        return result;
      case 2:
      default:
        return false;
    }
  }
}

/// The WOWZStreamConfig class provides configuration properties for the streaming connection.
/// DOCS https://www.wowza.com/resources/gocodersdk/docs/api-reference-android/com/wowza/gocoder/sdk/api/configuration/WOWZStreamConfig.html
class WOWZStreamConfig extends WOWZMediaConfig {
  /// Specify the streaming server
  /// Use the WOWZPlayerConfig object to specify the connection properties to the streaming server
  WOWZStreamConfig(
      {this.hostAddress,
      this.applicationName,
      this.streamName,
      this.portNumber,
      this.username,
      this.password});

  /// Name or IP address of the streaming server.
  String hostAddress;

  /// Name of the live streaming application.
  String applicationName;

  /// target stream name.
  String streamName;

  /// Server connection port number.
  int portNumber;

  /// user name for source authentication
  String username;

  /// password for source authentication.
  String password;

  /// Updates the property values of this instance with the property values from the specified preset configuration.
  void set(WOWZMediaConfig preset) {
    if (preset != null) {
      super.set(preset);
    }
  }
}

/// The WOWZMediaConfig class provides configuration properties for video and audio capture and encoding.
/// DOCS https://www.wowza.com/resources/gocodersdk/docs/api-reference-android/com/wowza/gocoder/sdk/api/configuration/WOWZMediaConfig.html
class WOWZMediaConfig {
  /// The WOWZMediaConfig class provides configuration properties for video and audio capture and encoding.
  WOWZMediaConfig(
      {this.presetLabel,
      this.videoFrameWidth,
      this.videoFrameHeight,
      this.videoBitRate,
      this.videoFramerate,
      this.videoKeyFrameInterval,
      this.audioChannels,
      this.audioSampleRate,
      this.audioBitrate}) {
    this.videoFrameSize = WOWZSize(videoFrameWidth, videoFrameHeight);
  }

  /// Updates the property values of this instance with the property values from the specified instance.
  void set(WOWZMediaConfig other) {
    if (other != null) {
      this.videoFrameSize.set(other.videoFrameSize);

      this.videoBitRate = other.videoBitRate;
      this.videoFramerate = other.videoFramerate;
      this.videoKeyFrameInterval = other.videoKeyFrameInterval;
      this.videoRotation = other.videoRotation;
      if (other.videoProfileLevel == null) {
        this.videoProfileLevel = null;
      } else if (this.videoProfileLevel != null) {
        this.videoProfileLevel.set(other.videoProfileLevel);
      } else {
        this.videoProfileLevel = WOWZProfileLevel(other.videoProfileLevel);
      }
      this.vbeFrameRateLowBandwidthSkipCount =
          other.vbeFrameRateLowBandwidthSkipCount;
      this.vbeFrameBufferSizeMultiplier = other.vbeFrameBufferSizeMultiplier;
      this.vbeLowBandwidthScalingFactor = other.vbeLowBandwidthScalingFactor;
      this.abrEnabled = other.abrEnabled;
      this.audioChannels = other.audioChannels;
      this.audioSampleRate = other.audioSampleRate;
      this.audioBitrate = other.audioBitrate;
      this.videoEnabled = other.videoEnabled;
      this.hlsEnabled = other.hlsEnabled;
      this.audioEnabled = other.audioEnabled;
      this.isPlayback = other.isPlayback;
      this.hlsBackupUrl = other.hlsBackupUrl;
    }
  }

  /// The default value for the width of the video frame (640 pixels).
  static final DEFAULT_VIDEO_FRAME_WIDTH = 640;

  /// The default value for the height of the video frame (480 pixels).
  static final DEFAULT_VIDEO_FRAME_HEIGHT = 480;

  /// The default value for the video frame size, in pixels.
  static final WOWZSize DEFAULT_VIDEO_FRAME_SIZE = WOWZSize(640, 480);

  /// The default value for the video bitrate (1500 bps).
  static final DEFAULT_VIDEO_BITRATE = 1500;

  /// The default value for the video frame rate (30 fps).
  static final DEFAULT_VIDEO_FRAME_RATE = 30;

  /// The default value for the keyframe interval (30 frames).
  static final DEFAULT_VIDEO_KEYFRAME_INTERVAL = 30;

  /// The default value for the audio sample rate (44100 Hz).
  static final DEFAULT_AUDIO_SAMPLE_RATE = 44100;

  /// The default value for the audio bitrate (64000 bps).
  static final DEFAULT_AUDIO_BITRATE = 64000;

  ///  Supported sample rates at SUPPORTED_AUDIO_SAMPLE_RATES.
  ///  If an invalid rate is specified, the next-highest supported rate will be used instead.
  static final SUPPORTED_AUDIO_SAMPLE_RATES = {
    8000,
    11025,
    22050,
    44100,
    48000
  };
  static final RESIZE_TO_ASPECT = 1;

  /// An identifier for a preset configuration appropriate for Quarter CIF (QCIF):
  /// a video resolution of 176x144 pixels,
  /// a 280-kbps video bitrate, 30 fps,
  /// a keyframe interval of 30,
  /// a 44.1 kHz audio sample rate,
  /// and a 64-kbps audio bitrate.
  static final FRAME_SIZE_176x144 = WOWZMediaConfig(
      presetLabel: "QCIF",
      videoFrameWidth: 176,
      videoFrameHeight: 144,
      videoBitRate: 280,
      videoFramerate: 30,
      videoKeyFrameInterval: 30,
      audioChannels: 2,
      audioSampleRate: 44100,
      audioBitrate: 64000);

  /// An identifier for a preset configuration appropriate for Quarter Video Graphics Array (QVGA):
  /// a video resolution of 320x240 pixels,
  /// a 280-kbps video bitrate, 30 fps,
  /// a keyframe interval of 30,
  /// a 44.1 kHz audio sample rate,
  /// and a 64-kbps audio bitrate.
  static final FRAME_SIZE_320x240 = WOWZMediaConfig(
      presetLabel: "QVGA",
      videoFrameWidth: 320,
      videoFrameHeight: 240,
      videoBitRate: 280,
      videoFramerate: 30,
      videoKeyFrameInterval: 30,
      audioChannels: 2,
      audioSampleRate: 44100,
      audioBitrate: 64000);

  /// An identifier for a preset configuration appropriate for Common Intermediate Format (CIF):
  /// a video resolution of 352x288 pixels,
  /// a 1-Mbps video bitrate, 30 fps,
  /// a keyframe interval of 30,
  /// a 44.1 kHz audio sample rate,
  /// and a 64-kbps audio bitrate.
  static final FRAME_SIZE_352x288 = WOWZMediaConfig(
      presetLabel: "CIF",
      videoFrameWidth: 352,
      videoFrameHeight: 288,
      videoBitRate: 1000,
      videoFramerate: 30,
      videoKeyFrameInterval: 30,
      audioChannels: 2,
      audioSampleRate: 44100,
      audioBitrate: 64000);

  /// An identifier for a preset configuration appropriate for VGA displays:
  /// a video resolution of 640x480 pixels,
  /// a 1.5-Mbps video bitrate, 30 fps,
  /// a keyframe interval of 30,
  /// a 44.1 kHz audio sample rate,
  /// and a 64-kbps audio bitrate.
  static final FRAME_SIZE_640x480 = WOWZMediaConfig(
      presetLabel: "VGA",
      videoFrameWidth: 640,
      videoFrameHeight: 480,
      videoBitRate: 1500,
      videoFramerate: 30,
      videoKeyFrameInterval: 30,
      audioChannels: 2,
      audioSampleRate: 44100,
      audioBitrate: 64000);

  /// An identifier for a preset configuration appropriate for high-bandwidth connections:
  /// a video resolution of 960x540 pixels,
  /// a 1.5-Bbps video bitrate, 30 fps,
  /// a keyframe interval of 30,
  /// a 44.1 kHz audio sample rate,
  /// and a 64-kbps audio bitrate.
  static final FRAME_SIZE_960x540 = WOWZMediaConfig(
      videoFrameWidth: 960,
      videoFrameHeight: 540,
      videoBitRate: 1500,
      videoFramerate: 30,
      videoKeyFrameInterval: 30,
      audioChannels: 2,
      audioSampleRate: 44100,
      audioBitrate: 64000);

  /// An identifier for a preset configuration representing 720p HD video:
  /// a video resolution of 1280x720 pixels,
  /// a 3.75-Mbps video bitrate, 30 fps,
  /// a keyframe interval of 30,
  /// a 44.1 kHz audio sample rate,
  /// and a 64-kbps audio bitrate.
  static final FRAME_SIZE_1280x720 = WOWZMediaConfig(
      presetLabel: "720p",
      videoFrameWidth: 1280,
      videoFrameHeight: 720,
      videoBitRate: 3750,
      videoFramerate: 30,
      videoKeyFrameInterval: 30,
      audioChannels: 2,
      audioSampleRate: 44100,
      audioBitrate: 64000);

  /// An identifier for a preset configuration representing 1080i video:
  /// a video resolution of 1440x1080 pixels,
  /// a 5-Mbps video bitrate, 30 fps,
  /// a keyframe interval of 30,
  /// a 44.1 kHz audio sample rate,
  /// and a 64-kbps audio bitrate.
  static final FRAME_SIZE_1440x1080 = WOWZMediaConfig(
      presetLabel: "1080i",
      videoFrameWidth: 1440,
      videoFrameHeight: 1080,
      videoBitRate: 5000,
      videoFramerate: 30,
      videoKeyFrameInterval: 30,
      audioChannels: 2,
      audioSampleRate: 44100,
      audioBitrate: 64000);

  /// An identifier for a preset configuration representing 1080p HD video:
  /// a video resolution of 1920x1080 pixels,
  /// a 5-Mbps video bitrate, 30 fps,
  /// a keyframe interval of 30,
  /// a 44.1 kHz audio sample rate,
  /// and a 64-kbps audio bitrate.
  static final FRAME_SIZE_1920x1080 = WOWZMediaConfig(
      presetLabel: "1080p",
      videoFrameWidth: 1920,
      videoFrameHeight: 1080,
      videoBitRate: 5000,
      videoFramerate: 30,
      videoKeyFrameInterval: 30,
      audioChannels: 2,
      audioSampleRate: 44100,
      audioBitrate: 64000);

  /// An identifier for a preset configuration representing 4K Ultra HD video:
  /// a video resolution of 3840x2160 pixels,
  /// an 8-Mbps video bitrate, 30 fps,
  /// a keyframe interval of 30,
  /// a 44.1 kHz audio sample rate,
  /// and a 64-kbps audio bitrate.
  static final FRAME_SIZE_3840x2160 = WOWZMediaConfig(
      presetLabel: "4k UHD",
      videoFrameWidth: 3840,
      videoFrameHeight: 2160,
      videoBitRate: 8000,
      videoFramerate: 30,
      videoKeyFrameInterval: 30,
      audioChannels: 2,
      audioSampleRate: 44100,
      audioBitrate: 64000);

  String presetLabel;

  /// Sets the video bitrate.
  int videoBitRate;

  /// Sets the video frame rate.
  int videoFramerate;

  /// Sets the keyframe interval, which is the number of frames between keyframes.
  int videoKeyFrameInterval;

  /// Sets the number of audio channels.
  int audioChannels;

  /// Sets the the audio sample rate.
  /// Supported sample rates at SUPPORTED_AUDIO_SAMPLE_RATES.
  /// If an invalid rate is specified, the next-highest supported rate will be used instead.
  int audioSampleRate;

  /// Sets the audio bitrate.
  int audioBitrate;

  /// Specifies whether the config is used for playback.
  bool isPlayback = false;

  /// Set to true to force the player to use Apple HLS instead of the default playback protocol.
  bool hlsEnabled = false;

  /// The .m3u8 playlist URL to use as a fallback.
  String hlsBackupUrl;

  /// To enable or disable audio for the stream, use the setAudioEnabled method in the WOWZPlayerConfig class.
  bool audioEnabled;

  /// To enable or disable video, use the setVideoEnabled method in the WOWZPlayerConfig class.
  bool videoEnabled;

  /// Sets the video frame size using the values from the specified instance.
  /// Frame width should always be greater than frame height (landscape orientation).
  /// If the specified frame size isn't landscape, the width and height will be swapped so that the frame is landscape oriented.
  WOWZSize videoFrameSize;

  /// Sets the video frame height; should be less than the frame width.
  int videoFrameHeight;

  /// Sets the video frame width; should be greater than the frame height.
  int videoFrameWidth;

  /// Enables adaptive bitrate (ABR) streaming.
  bool abrEnabled = false;

  /// Sets the rotation angle of the video source,
  /// with 0 being the natural landscape orientation and 90 being the natural portrait orientation. 180 and 270,
  /// respectively, are the inverted landscape and portrait orientations.
  int videoRotation;

  /// The value by which to multiply the video frame rate to determine the number of frames to buffer before the encoder starts to throttle bitrate and/or frame rate.
  int vbeFrameBufferSizeMultiplier;

  /// The number of frames to skip when the encoder can't keep up with the broadcaster.
  int vbeFrameRateLowBandwidthSkipCount;

  /// The factor by which to scale the bitrate in low-bandwidth conditions.
  int vbeLowBandwidthScalingFactor;

  /// Sets the H.264 profile.
  WOWZProfileLevel videoProfileLevel;
}
