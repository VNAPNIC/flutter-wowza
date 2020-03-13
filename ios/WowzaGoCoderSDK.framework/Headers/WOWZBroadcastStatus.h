//
//  WOWZBroadcastStatus.h
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
//

#import <Foundation/Foundation.h>

/*!
 @discussion WOWZBroadcastStatusNewBitrateKey is the entry in the WOWZBroadcastStatus class data dictionary for the new bitrate value, represented as an NSNumber.
 */
extern NSString * __nonnull const WOWZBroadcastStatusNewBitrateKey;

/*!
 @discussion WOWZBroadcastStatusPreviousBitrateKey is the entry in the WOWZBroadcastStatus class data dictionary for the previous bitrate value, represented as an NSNumber.
 */
extern NSString * __nonnull const  WOWZBroadcastStatusPreviousBitrateKey;

/*!
 @class WOWZBroadcastStatus
 @discussion WOWZBroadcastStatus is a thread-safe class for working with SDK component state and error properties.
 Client applications typically don't have any need for creating a WOWZBroadcastStatus object.
 */
@interface WOWZBroadcastStatus : NSObject <NSMutableCopying, NSCopying>

/*!
 *  @typedef WOWZBroadcastState
 *  @constant WOWZBroadcastStateIdle The broadcasting component or session is idle.
 *  @constant WOWZBroadcastStateReady The broadcasting component or session is ready or has been initialized.
 *  @constant WOWZBroadcastStateBroadcasting The broadcasting component or session is broadcasting.
 *  @discussion A collection of constants that describe the state of the broadcasting component or session.
 */
typedef NS_ENUM(NSUInteger, WOWZBroadcastState) {
    WOWZBroadcastStateIdle = 0,
    WOWZBroadcastStateReady,
    WOWZBroadcastStateBroadcasting
};

/*!
 *  @typedef WOWZBroadcastEvent
 *  @constant WOWZBroadcastEventNone No event.
 *  @constant WOWZBroadcastEventLowBandwidth If network bandwidth is insufficient for the specified video-broadcast settings, the encoder may attempt to compensate by sending a reduced bitrate or frame rate.
 *  @constant WOWZBroadcastEventBitrateReduced Sent when the encoder reduces the stream bitrate to compensate for low-bandwidth conditions. Changing the bitrate adds WOWZBroadcastStatusNewBitrateKey and WOWZBroadcastStatusPreviousBitrateKey,
 *  represented as NSNumber values, to the WOWZBroadcastStatus class data dictionary.
 *  @constant WOWZBroadcastEventBitrateIncreased Sent when the encoder increases the bitrate after having previously reduced it. The bitrate will never
 *  increase beyond the original bitrate specified in the configuration settings for the streaming session. Changing the bitrate adds WOWZBroadcastStatusNewBitrateKey and WOWZBroadcastStatusPreviousBitrateKey,
 *  represented as NSNumber values, to the WOWZBroadcastStatus class data dictionary.
 *  @constant WOWZBroadcastEventEncoderPaused Sent when the encoder stops sending frames and waits for queued frames to catch up. Typically, the encoder pauses while it's reducing the bitrate to compensate for constrained network bandwidth.
 *  @constant WOWZBroadcastEventEncoderResumed Sent when a previously paused encoder resumes.
 *  @discussion A collection of constants that describe the component event.
 */
typedef NS_ENUM(NSUInteger, WOWZBroadcastEvent) {
    WOWZBroadcastEventNone = 0,
    WOWZBroadcastEventLowBandwidth,
    WOWZBroadcastEventBitrateReduced,
    WOWZBroadcastEventBitrateIncreased,
    WOWZBroadcastEventEncoderPaused,
    WOWZBroadcastEventEncoderResumed
};

#pragma mark - Properties

/*!
 *  The state of the broadcast session.
 */
@property (nonatomic) WOWZBroadcastState state;

/*!
 *  The event used by the broadcast session.
 */
@property (nonatomic) WOWZBroadcastEvent event;

/*!
 *  The last error reported by the broadcast session.
 */
@property (nonatomic, strong, nullable) NSError * error;

/*!
 *  Data related to the status. May be null.
 */
@property (nonatomic, strong, nullable) NSDictionary * data;

#pragma mark - Class Methods

/*!
 *  Returns a WOWZBroadcastStatus object with a specified state.
 *
 *  @param aState The state to use to initialize the object.
 *
 *  @return An initialized WOWZBroadcastStatus object.
 */
+ (nonnull instancetype) statusWithState:(WOWZBroadcastState)aState;

/*!
 *  Returns a WOWZBroadcastStatus object with a specified state and error.
 *
 *  @param aState The state to use to initialize the object.
 *  @param aError The error to use to initialize the object.
 *
 *  @return An initialized WOWZBroadcastStatus object.
 */
+ (nonnull instancetype) statusWithStateAndError:(WOWZBroadcastState)aState aError:(nonnull NSError *)aError;

/*!
 *  Returns a WOWZBroadcastStatus object with a specified event.
 *
 *  @param event The WOWZBroadcastEvent to use to initialize the object.
 *
 *  @return An initialized WOWZBroadcastStatus object.
 */
+ (nonnull instancetype) statusWithEvent:(WOWZBroadcastEvent)event;

/*!
 *  Returns a WOWZBroadcastStatus object with a specified state and event.
 *
 *  @param aState The state to use to initialize the object.
 *  @param event The WOWZBroadcastEvent to use to initialize the object.
 *
 *  @return An initialized WOWZBroadcastStatus object.
 */
+ (nonnull instancetype) statusWithState:(WOWZBroadcastState)aState event:(WOWZBroadcastEvent)event;

#pragma mark - Instance Methods

/*!
 *  Initializes a WOWZBroadcastStatus object with a specified state.
 *
 *  @param aState The state to use to initialize the object.
 *
 *  @return An initialized WOWZBroadcastStatus object.
 */
- (nonnull instancetype) initWithState:(WOWZBroadcastState)aState;

/*!
 *  Initializes a WOWZBroadcastStatus object with a specified state and error.
 *
 *  @param aState The state to use to initialize the object.
 *  @param aError The error to use to initialize the object.
 *
 *  @return An initialized WOWZBroadcastStatus object.
 */
- (nonnull instancetype) initWithStateAndError:(WOWZBroadcastState)aState aError:(nonnull NSError *)aError;

/*!
 *  Initializes a WOWZBroadcastStatus object with a specified event.
 *
 *  @param event The WOWZBroadcastEvent to use to initialize the object.
 *
 *  @return An initialized WOWZBroadcastStatus object.
 */
- (nonnull instancetype) initWithEvent:(WOWZBroadcastEvent)event;

/*!
 *  Initializes a WOWZBroadcastStatus object with a specified state and event.
 *
 *  @param aState The state to use to initialize the object.
 *  @param event The WOWZBroadcastEvent to use to initialize the object.
 *
 *  @return An initialized WOWZBroadcastStatus object.
 */
- (nonnull instancetype) initWithState:(WOWZBroadcastState)aState event:(WOWZBroadcastEvent)event;

/*!
 *  Reinitializes a WOWZBroadcastStatus object, clearing all errors, events, and data values and setting the state to WOWZBroadcastStateIdle.
 */
- (void) resetStatus;

/*!
 *  Reinitializes a WOWZBroadcastStatus object, clearing all errors, events, and data values and setting the state to the specified value.
 *
 *  @param aState The state to use to initialize the object.
 */
- (void) resetStatusWithState:(WOWZBroadcastState)aState;

/*!
 *  See if the state equals WOWZBroadcastStateIdle.
 *
 *  @return True if the state is WOWZBroadcastStateIdle; false otherwise.
 */
@property (readonly, nonatomic) BOOL isIdle;

/*!
 *  See if the state equals WOWZBroadcastStateReady.
 *
 *  @return True if the state is WOWZBroadcastStateReady; false otherwise.
 */
@property (readonly, nonatomic) BOOL isReady;

/*!
 *  See if the state equals WOWZBroadcastStateBroadcasting.
 *
 *  @return True if the state is WOWZBroadcastStateBroadcasting; false otherwise.
 */
@property (readonly, nonatomic) BOOL isBroadcasting;


/*!
 *  See if the error is non-null.
 *
 *  @return True if the error is non-null; false otherwise.
 */
@property (readonly, nonatomic) BOOL hasError;

@end
