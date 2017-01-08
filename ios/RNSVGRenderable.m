/**
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

#import "RNSVGRenderable.h"
#import "RNSVGPercentageConverter.h"

@implementation RNSVGRenderable
{
    NSMutableDictionary *_originProperties;
    NSArray *_lastMergedList;
    RNSVGPercentageConverter *_widthConverter;
    RNSVGPercentageConverter *_heightConverter;
    CGFloat _contextWidth;
    CGFloat _contextHeight;
    CGRect _boundingBox;
}

- (id)init
{
    if (self = [super init]) {
        _fillOpacity = 1;
        _strokeOpacity = 1;
        _strokeWidth = 1;
    }
    return self;
}

- (void)setFill:(RNSVGBrush *)fill
{
    if (fill == _fill) {
        return;
    }
    [self invalidate];
    _fill = fill;
}

- (void)setFillOpacity:(CGFloat)fillOpacity
{
    if (fillOpacity == _fillOpacity) {
        return;
    }
    [self invalidate];
    _fillOpacity = fillOpacity;
}

- (void)setFillRule:(RNSVGCGFCRule)fillRule
{
    if (fillRule == _fillRule) {
        return;
    }
    [self invalidate];
    _fillRule = fillRule;
}

- (void)setStroke:(RNSVGBrush *)stroke
{
    if (stroke == _stroke) {
        return;
    }
    [self invalidate];
    _stroke = stroke;
}

- (void)setStrokeOpacity:(CGFloat)strokeOpacity
{
    if (strokeOpacity == _strokeOpacity) {
        return;
    }
    [self invalidate];
    _strokeOpacity = strokeOpacity;
}

- (void)setStrokeWidth:(CGFloat)strokeWidth
{
    if (strokeWidth == _strokeWidth) {
        return;
    }
    [self invalidate];
    _strokeWidth = strokeWidth;
}

- (void)setStrokeLinecap:(CGLineCap)strokeLinecap
{
    if (strokeLinecap == _strokeLinecap) {
        return;
    }
    [self invalidate];
    _strokeLinecap = strokeLinecap;
}

- (void)setStrokeJoin:(CGLineJoin)strokeLinejoin
{
    if (strokeLinejoin == _strokeLinejoin) {
        return;
    }
    [self invalidate];
    _strokeLinejoin = strokeLinejoin;
}

- (void)setStrokeMiterlimit:(CGFloat)strokeMiterlimit
{
    if (strokeMiterlimit == _strokeMiterlimit) {
        return;
    }
    [self invalidate];
    _strokeMiterlimit = strokeMiterlimit;
}

- (void)setStrokeDasharray:(RNSVGCGFloatArray)strokeDasharray
{
    if (strokeDasharray.array == _strokeDasharray.array) {
        return;
    }
    if (_strokeDasharray.array) {
        free(_strokeDasharray.array);
    }
    [self invalidate];
    _strokeDasharray = strokeDasharray;
}

- (void)setStrokeDashoffset:(CGFloat)strokeDashoffset
{
    if (strokeDashoffset == _strokeDashoffset) {
        return;
    }
    [self invalidate];
    _strokeDashoffset = strokeDashoffset;
}

- (void)setHitArea:(CGPathRef)hitArea
{
    if (hitArea == _hitArea) {
        return;
    }
    
    [self invalidate];
    CGPathRelease(_hitArea);
    _hitArea = CGPathRetain(hitArea);
}

- (void)setPropList:(NSArray<NSString *> *)propList
{
    if (propList == _propList) {
        return;
    }
    _attributeList = [propList copy];
    _propList = propList;
    [self invalidate];
}

- (void)dealloc
{
    CGPathRelease(_hitArea);
    if (_strokeDasharray.array) {
        free(_strokeDasharray.array);
    }
}

- (void)renderTo:(CGContextRef)context
{
    // This needs to be painted on a layer before being composited.
    CGContextSaveGState(context);
    CGContextConcatCTM(context, self.matrix);
    CGContextSetAlpha(context, self.opacity);
    
    [self beginTransparencyLayer:context];
    [self renderLayerTo:context];
    [self endTransparencyLayer:context];

    CGContextRestoreGState(context);
}

// hitTest delagate
- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event
{
    return [self hitTest:point withEvent:event withTransform:CGAffineTransformMakeRotation(0)];
}

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event withTransform:(CGAffineTransform)transform
{
    if (self.active) {
        if (!event) {
            self.active = NO;
        }
        return self;
    }
    
    CGAffineTransform matrix = CGAffineTransformConcat(self.matrix, transform);
    
    CGPathRef hitArea = CGPathCreateCopyByTransformingPath(self.hitArea, &matrix);
    BOOL contains = CGPathContainsPoint(hitArea, nil, point, NO);
    CGPathRelease(hitArea);
    
    if (contains) {
        CGPathRef clipPath = [self getClipPath];
        
        if (!clipPath) {
            return self;
        } else {
            CGPathRef transformedClipPath = CGPathCreateCopyByTransformingPath(clipPath, &matrix);
            BOOL result = CGPathContainsPoint(transformedClipPath, nil, point, self.clipRule == kRNSVGCGFCRuleEvenodd);
            CGPathRelease(transformedClipPath);
            return result ? self : nil;
        }
    } else {
        return nil;
    }
}

- (void)setBoundingBox:(CGRect)boundingBox
{
    _boundingBox = boundingBox;
    _widthConverter = [[RNSVGPercentageConverter alloc] initWithRelativeAndOffset:boundingBox.size.width offset:0];
    _heightConverter = [[RNSVGPercentageConverter alloc] initWithRelativeAndOffset:boundingBox.size.height offset:0];
}

- (CGFloat)getWidthRelatedValue:(NSString *)string
{
    return [_widthConverter stringToFloat:string];
}

- (CGFloat)getHeightRelatedValue:(NSString *)string
{
    return [_heightConverter stringToFloat:string];
}

- (CGFloat)getContextWidth
{
    return CGRectGetWidth(_boundingBox);
}

- (CGFloat)getContextHeight
{
    return CGRectGetHeight(_boundingBox);
}

- (CGFloat)getContextX
{
    return CGRectGetMinX(_boundingBox);
}

- (CGFloat)getContextY
{
    return CGRectGetMinY(_boundingBox);
}

- (void)mergeProperties:(__kindof RNSVGNode *)target mergeList:(NSArray<NSString *> *)mergeList
{

    [self mergeProperties:target mergeList:mergeList inherited:NO];
}

- (void)mergeProperties:(__kindof RNSVGNode *)target mergeList:(NSArray<NSString *> *)mergeList inherited:(BOOL)inherited
{
    _lastMergedList = mergeList;
    
    if (mergeList.count == 0) {
        return;
    }
    
    NSMutableArray* attributeList = [self.propList mutableCopy];
    
    _originProperties = [[NSMutableDictionary alloc] init];
    
    for (NSString *key in mergeList) {
        if (inherited) {
            if (![attributeList containsObject:key]) {
                [attributeList addObject:key];
                [_originProperties setValue:[self valueForKey:key] forKey:key];
                [self setValue:[target valueForKey:key] forKey:key];
            }
        } else {
            [_originProperties setValue:[self valueForKey:key] forKey:key];
            [self setValue:[target valueForKey:key] forKey:key];
        }
    }
    
    _attributeList = [attributeList copy];
}

- (void)resetProperties
{
    for (NSString *key in _lastMergedList) {
        [self setValue:[_originProperties valueForKey:key] forKey:key];
    }
    _attributeList = [_propList copy];
}

- (void)renderLayerTo:(CGContextRef)context
{
    // abstract
}

@end
