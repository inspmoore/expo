// Copyright 2015-present 650 Industries. All rights reserved.

#import "NSData+EXRemoteNotifications.h"

NS_ASSUME_NONNULL_BEGIN

@implementation NSData (EXRemoteNotifications)

- (NSString *)apnsTokenString
{
  // if ios-13 change impl
  NSCharacterSet *brackets = [NSCharacterSet characterSetWithCharactersInString:@"<>"];
  return [[[self description] stringByTrimmingCharactersInSet:brackets] stringByReplacingOccurrencesOfString:@" " withString:@""];
}

@end

NS_ASSUME_NONNULL_END
