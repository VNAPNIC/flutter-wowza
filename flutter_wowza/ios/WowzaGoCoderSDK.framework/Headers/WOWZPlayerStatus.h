//
//  WOWZPlayerStatus.h
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
 @class WOWZPlayerStatus
 @discussion WOWZPlayerStatus is a thread-safe class for working with SDK component state and error properties.
 Client applications typically don't have any need for creating a WOWZPlayerStatus object.
 */
@interface WOWZPlayerStatus : NSObject <NSMutableCopying, NSCopying>

/*!
 *  @typedef WOWZPlayerState
 *  @constant WOWZPlayerStateIdle The playback component or session is idle.
 *  @constant WOWZPlayerStateConnecting The playback component or session is starting.
 *  @constant WOWZPlayerStatePlaying The playback component or session is playing.
 *  @constant WOWZPlayerStateStopping The playback component or session is shutting down.
 *  @constant WOWZPlayerStateBuffering The playback component or session is buffering.
 *  @constant WOWZPlayerStatePaused The playback component or session is paused.
 *  @discussion A collection of constants that describe the state of the playback component or session.
 */
typedef NS_ENUM(NSUInteger, WOWZPlayerState) {
    WOWZPlayerStateIdle = 0,
    WOWZPlayerStateConnecting,
    WOWZPlayerStatePlaying,
    WOWZPlayerStateStopping,
    WOWZPlayerStateBuffering,
    WOWZPlayerStatePaused
};

/*!
 *  @typedef WOWZPlayerEvent
 *  @discussion A collection of constants that describe the component event.
 */
typedef NS_ENUM(NSUInteger, WOWZPlayerEvent) {
    WOWZPlayerEventNone = 0
};

#pragma mark - Properties

/*!
 *  The state of the playback session.
 */
@property (nonatomic) WOWZPlayerState state;

/*!
 *  The event used by the playback session.
 */
@property (nonatomic) WOWZPlayerEvent event;

/*!
 *  The last error reported by the playback session.
 */
@property (nonatomic, strong, nullable) NSError * error;

/*!
 *  Data related to the status. May be null.
 */
@property (nonatomic, strong, nullable) NSDictionary * data;

#pragma mark - Class Methods

/*!
 *  Returns a WOWZPlayerStatus object with a specified state.
 *
 *  @param aState The state to use to initialize the object.
 *
 *  @return An initialized WOWZPlayerStatus object.
 */
+ (nonnull instancetype) statusWithState:(WOWZPlayerState)aState;

/*!
 *  Returns a WOWZPlayerStatus object with a specified state and error.
 *
 *  @param aState The state to use to initialize the object.
 *  @param aError The error to use to initialize the object.
 *
 *  @return An initialized WOWZPlayerStatus object.
 */
+ (nonnull instancetype) statusWithStateAndError:(WOWZPlayerState)aState aError:(nonnull NSError *)aError;

/*!
 *  Returns a WOWZPlayerStatus object with a specified event.
 *
 *  @param event The WOWZPlayerEvent to use to initialize the object.
 *
 *  @return An initialized WOWZPlayerStatus object.
 */
+ (nonnull instancetype) statusWithEvent:(WOWZPlayerEvent)event;

/*!
 *  Returns a WOWZPlayerStatus object with a specified state and event.
 *
 *  @param aState The state to use to initialize the object.
 *  @param event The WOWZPlayerEvent to use to initialize the object.
 *
 *  @return An initialized WOWZPlayerStatus object.
 */
+ (nonnull instancetype) statusWithState:(WOWZPlayerState)aState event:(WOWZPlayerEvent)event;

#pragma mark - Instance Methods

/*!
 *  Initializes a WOWZPlayerStatus object with a specified state.
 *
 *  @param aState The state to use to initialize the object.
 *
 *  @return An initialized WOWZPlayerStatus object.
 */
- (nonnull instancetype) initWithState:(WOWZPlayerState)aState;

/*!
 *  Initializes a WOWZPlayerStatus object with a specified state and error.
 *
 *  @param aState The state to use to initialize the object.
 *  @param aError The error to use to initialize the object.
 *
 *  @return An initialized WOWZPlayerStatus object.
 */
- (nonnull instancetype) initWithStateAndError:(WOWZPlayerState)aState aError:(nonnull NSError *)aError;

/*!
 *  Initializes a WOWZPlayerStatus object with a specified event.
 *
 *  @param event The WOWZPlayerEvent to use to initialize the object.
 *
 *  @return An initialized WOWZPlayerStatus object.
 */
- (nonnull instancetype) initWithEvent:(WOWZPlayerEvent)event;

/*!
 *  Initializes a WOWZPlayerStatus object with a specified state and event.
 *
 *  @param aState The state to use to initialize the object.
 *  @param event The WOWZPlayerEvent to use to initialize the object.
 *
 *  @return An initialized WOWZPlayerStatus object.
 */
- (nonnull instancetype) initWithState:(WOWZPlayerState)aState event:(WOWZPlayerEvent)event;

/*!
 *  Reinitializes a WOWZPlayerStatus object, clearing all errors, events, and data values and setting the state to WOWZPlayerStateIdle.
 */
- (void) resetStatus;

/*!
 *  Reinitializes a WOWZPlayerStatus object, clearing all errors, events, and data values and setting the state to the specified value.
 *
 *  @param aState The state to use to initialize the object.
 */
- (void) resetStatusWithState:(WOWZPlayerState)aState;

/*!
 *  See if the state equals WOWZPlayerStateIdle.
 *
 *  @return True if the state is WOWZPlayerStateIdle; false otherwise.
 */
@property (readonly, nonatomic) BOOL isIdle;

/*!
 *  See if the state equals WOWZPlayerStateConnecting.
 *`
 *  @return True if the state is WOWZPlayerStateConnecting; false otherwise.
 */
@property (readonly, nonatomic) BOOL isConnecting;

/*!
 *  See if the state equals WOWZPlayerStatePlaying.
 *
 *  @return True if the state is WOWZPlayerStatePlaying; false otherwise.
 */
@property (readonly, nonatomic) BOOL isPlaying;

/*!
 *  See if the state equals WOWZPlayerStatePaused.
 *
 *  @return True if the state is WOWZPlayerStatePaused; false otherwise.
 */
@property (readonly, nonatomic) BOOL isPaused;

/*!
 *  See if the state equals WOWZPlayerStateStopping.
 *
 *  @return True if the state is WOWZPlayerStateStopping; false otherwise.
 */
@property (readonly, nonatomic) BOOL isStopping;

/*!
 *  See if the error is non-null.
 *
 *  @return True if the error is non-null; false otherwise.
 */
@property (readonly, nonatomic) BOOL hasError;

@end
