/**
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

#import <Foundation/Foundation.h>
#import "RNSVGGroup.h"
#import "RNSVGTextAnchor.h"

@interface RNSVGText : RNSVGGroup

@property (nonatomic, assign) RNSVGTextAnchor textAnchor;
@property (nonatomic, assign) NSArray<NSNumber *> *deltaX;
@property (nonatomic, assign) NSArray<NSNumber *> *deltaY;
@property (nonatomic, strong) NSString *positionX;
@property (nonatomic, strong) NSString *positionY;
@property (nonatomic, assign) NSDictionary *font;

@property (nonatomic, assign) CGFloat offsetX;
@property (nonatomic, assign) CGFloat offsetY;

@end
