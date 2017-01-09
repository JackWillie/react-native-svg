/**
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

#import <Foundation/Foundation.h>
#import <CoreText/CoreText.h>
#import "RNSVGText.h"
#import "RNSVGBezierPath.h"

@interface RNSVGTextPath : RNSVGText

@property (nonatomic, strong) NSString *href;

- (RNSVGBezierPath *)getBezierPath;

@end
