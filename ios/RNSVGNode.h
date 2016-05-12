/**
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "RNSVGCGFCRule.h"

/**
 * RNSVG nodes are implemented as empty UIViews but this is just an implementation detail to fit
 * into the existing view management. They should also be shadow views and painted on a background
 * thread.
 */

@interface RNSVGNode : UIView

@property (nonatomic, assign) CGRect rect;
@property (nonatomic, assign) CGFloat opacity;
@property (nonatomic, assign) RNSVGCGFCRule clipRule;
@property (nonatomic, assign) CGPathRef clipPath; // convert clipPath="M0,0 L0,10 L10,10z" into path
@property (nonatomic, strong) NSString *clipPathId; // use clipPath="url(#clip)" as ClipPath

- (void)invalidate;
- (void)renderTo:(CGContextRef)context;

/**
 * renderTo will take opacity into account and draw renderLayerTo off-screen if there is opacity
 * specified, then composite that onto the context. renderLayerTo always draws at opacity=1.
 * @abstract
 */
- (void)renderLayerTo:(CGContextRef)context;

/**
 * clip node by clipPath or clipPathId.
 */
- (void)clip:(CGContextRef)context;

/**
 * define <ClipPath></ClipPath> content as clipPath template.
 */
- (void)defineClipPath:(CGPathRef)clipPath clipPathId:(NSString *)clipPathId;

/**
 * getPath will return the path inside node as a ClipPath.
 */
- (CGPathRef)getPath: (CGContextRef) context;

@end
