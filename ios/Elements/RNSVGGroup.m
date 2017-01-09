/**
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

#import "RNSVGGroup.h"

@implementation RNSVGGroup

- (void)renderLayerTo:(CGContextRef)context
{
    [self clip:context];
    [self renderGroupTo:context];
}

- (void)renderGroupTo:(CGContextRef)context
{
    RNSVGSvgView* svg = [self getSvgView];
    [self traverseSubviews:^(RNSVGNode *node) {
        if (node.responsible && !svg.responsible) {
            svg.responsible = YES;
        }
        
        [node mergeProperties:self mergeList:self.attributeList inherited:YES];
        [node renderTo:context];
        
        if ([node isKindOfClass: [RNSVGRenderable class]]) {
            RNSVGRenderable *renderable = node;
            [self concatLayoutBoundingBox:[renderable getLayoutBoundingBox]];
        }
        return YES;
    }];
}

- (void)renderPathTo:(CGContextRef)context
{
    [super renderLayerTo:context];
}

- (void)concatLayoutBoundingBox:(CGRect)boundingBox
{
    [self setLayoutBoundingBox:CGRectUnion(boundingBox, [self getLayoutBoundingBox])];
}

- (CGPathRef)getPath:(CGContextRef)context
{
    CGMutablePathRef __block path = CGPathCreateMutable();
    [self traverseSubviews:^(RNSVGNode *node) {
        CGAffineTransform transform = node.matrix;
        CGPathAddPath(path, &transform, [node getPath:context]);
        return YES;
    }];

    return (CGPathRef)CFAutorelease(path);
}

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event withTransform:(CGAffineTransform)transform
{
    CGAffineTransform matrix = CGAffineTransformConcat(self.matrix, transform);

    CGPathRef clip = [self getClipPath];
    if (clip) {
        CGPathRef transformedClipPath = CGPathCreateCopyByTransformingPath(clip, &matrix);
        BOOL insideClipPath = CGPathContainsPoint(clip, nil, point, self.clipRule == kRNSVGCGFCRuleEvenodd);
        CGPathRelease(transformedClipPath);
        
        if (!insideClipPath) {
            return nil;
        }
        
    }

    for (RNSVGNode *node in [self.subviews reverseObjectEnumerator]) {
        if ([node isKindOfClass:[RNSVGNode class]]) {
            if (event) {
                node.active = NO;
            } else if (node.active) {
                return node;
            }

            UIView *view = [node hitTest: point withEvent:event withTransform:matrix];

            if (view) {
                node.active = YES;
                if (node.responsible || (node != view)) {
                    return view;
                } else {
                    return self;
                }
            }
        }
    }
    return nil;
}

- (void)saveDefinition
{
    if (self.name) {
        RNSVGSvgView* svg = [self getSvgView];
        [svg defineTemplate:self templateName:self.name];
    }

    [self traverseSubviews:^(RNSVGNode *node) {
        [node saveDefinition];
        return YES;
    }];

}

- (void)resetProperties
{
    [self traverseSubviews:^(RNSVGNode *node) {
        [node resetProperties];
        return YES;
    }];
}

@end
