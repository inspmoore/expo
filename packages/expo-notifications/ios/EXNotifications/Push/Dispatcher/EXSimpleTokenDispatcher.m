// Copyright 2019-present 650 Industries. All rights reserved.

#import "EXSimpleTokenDispatcher.h"
#import <EXNotifications/EXOnTokenChangeListener.h>
#import <EXNotifications/EXEngine.h>

@interface EXSimpleTokenDispatcher ()

@property (strong) NSUserDefaults *defaults;

@property (strong) NSMutableDictionary<NSString*,id<EXOnTokenChangeListener>> *listeners;

@property (strong) id<EXEngine> engine;

@end

@implementation EXSimpleTokenDispatcher

- (instancetype)initWithEngine:(id<EXEngine>)engine
{
  if (self = [super init])
  {
    _defaults = [[NSUserDefaults alloc] initWithSuiteName:@"expo.host.notifications.bare"];
    _listeners = [NSMutableDictionary new];
  }
  return self;
}

- (void)onNewToken:(NSData *)token
{
  NSString *tokenStringFormat = [[[[token description]
                                  stringByReplacingOccurrencesOfString: @"<" withString: @""]
                                  stringByReplacingOccurrencesOfString: @">" withString: @""]
                                  stringByReplacingOccurrencesOfString: @" " withString: @""];
  
  NSString *lastToken = [_defaults stringForKey:@"token"];
  if (![lastToken isEqualToString:tokenStringFormat])
  {
    [_defaults setObject:tokenStringFormat forKey:@"token"];
    [_engine sendTokenToServer:tokenStringFormat];
    
    for (NSString *key in [_listeners allKeys]) {
      id<EXOnTokenChangeListener> listener = _listeners[key];
      [listener onTokenChange:[_engine generateTokenForAppId:tokenStringFormat withToken:key]];
    }
  }
}

- (void)registerForPushTokenWithAppId:(NSString*)appId onTokenChangeListener:(id<EXOnTokenChangeListener>)onTokenChangeListener
{
  [self maybeRegisterForNotifications];
  
  NSString *token = [_defaults stringForKey:@"token"];
  NSString *lastAppIdToken = [_defaults stringForKey:appId];
  if (token != lastAppIdToken) {
    [_defaults setObject:token forKey:appId];
    [onTokenChangeListener onTokenChange:[_engine generateTokenForAppId:appId withToken:token]];
  }
  
  [_listeners setObject:onTokenChangeListener forKey:appId];
}

- (void)unregisterWithAppId:(NSString*)appId
{
  [_listeners removeObjectForKey:appId];
}

- (void)maybeRegisterForNotifications
{
  if ([_listeners count] == 0) {
    BOOL registered = [[UIApplication sharedApplication] isRegisteredForRemoteNotifications];
    if (!registered) {
      [[UIApplication sharedApplication] registerForRemoteNotifications];
    }
  }
}

@end
