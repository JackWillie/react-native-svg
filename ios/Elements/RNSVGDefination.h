/**
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

#import "RNSVGNode.h"

/**
 * RNSVG defination are implemented as abstract UIViews for all elements inside Defs.
 */

@interface RNSVGDefination : RNSVGNode

- (void)renderTo:(CGContextRef)context;

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event;

@end
