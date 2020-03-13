//
//  WOWZVideoEncoderSink.h
//  WowzaGoCoderSDK
//
//  © 2007 – 2019 Wowza Media Systems, LLC. All rights reserved.
//
//  The above copyright notice and this permission notice shall be
//  included in all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
//  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
//  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
//  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
//  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
//  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
//  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
//  OTHER DEALINGS IN THE SOFTWARE.

#import "WOWZMediaSink.h"

/*!
 @protocol WOWZVideoEncoderSink
 @discussion WOWZVideoEncoderSink defines a protocol for callbacks that occur when video frames are encoded.
 */
@protocol WOWZVideoEncoderSink <WOWZMediaSink>

@required

/*!
 Called for each frame of encoded video.
 @param data
 The frame's encoded video data.
 */
- (void) videoFrameWasEncoded:(nonnull CMSampleBufferRef)data;


@optional

/*!
 DEPRECATED Use the WOWZBroadcastEventBitrateReduced and WOWZBroadcastEventBitrateIncreased constants in the WOWZBroadcastStatus class's WOWZBroadcastEvent typedef instead.
 Called when the sink bitrate is reduced due to inability to keep up with encoded frames.
 @param newBitrate
 The new (updated) bitrate.
 @param previousBitrate
 The previous bitrate.
 */
- (void) videoBitrateDidChange:(NSUInteger)newBitrate previousBitrate:(NSUInteger)previousBitrate __attribute__((deprecated));

@end
