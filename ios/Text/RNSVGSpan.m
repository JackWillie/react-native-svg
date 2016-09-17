/**
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */


#import "RNSVGSpan.h"
#import "RNSVGBezierPath.h"

@implementation RNSVGSpan

- (CGPathRef)getPath:(CGContextRef)context
{
    [self setBoundingBox:CGContextGetClipBoundingBox(context)];
    CGMutablePathRef path = CGPathCreateMutable();
    
    if (![self.content isEqualToString:@""]) {
        // Create a dictionary for this font
        CFDictionaryRef attributes = (__bridge CFDictionaryRef)@{
                                                                 (NSString *)kCTFontAttributeName: (__bridge id)self.font,
                                                                 (NSString *)kCTForegroundColorFromContextAttributeName: @YES
                                                                 };
        
        CFStringRef string = (__bridge CFStringRef)self.content;
        CFAttributedStringRef attrString = CFAttributedStringCreate(kCFAllocatorDefault, string, attributes);
        CTLineRef line = CTLineCreateWithAttributedString(attrString);
        CFRelease(attrString);
        
        CGMutablePathRef linePath = [self setLinePath:line];
        
        // Set up text frame with font metrics
        CGFloat size = CTFontGetSize(self.font);
        CGFloat px = self.px ? [self getWidthRelatedValue:self.px] : 0;
        CGFloat py = self.py ? [self getHeightRelatedValue:self.py] : 0;
        
        CGAffineTransform offset = CGAffineTransformMakeTranslation(px, size + py);
        
        CGPathAddPath(path, &offset, linePath);
        CGPathRelease(linePath);
    }
    
    return (CGPathRef)CFAutorelease(path);
}

- (CGMutablePathRef)setLinePath:(CTLineRef)line
{
    CGAffineTransform upsideDown = CGAffineTransformMakeScale(1.0, -1.0);
    CGMutablePathRef path = CGPathCreateMutable();
    
    CFArrayRef glyphRuns = CTLineGetGlyphRuns(line);
    CTRunRef run = CFArrayGetValueAtIndex(glyphRuns, 0);
    
    CFIndex runGlyphCount = CTRunGetGlyphCount(run);
    CGPoint positions[runGlyphCount];
    CGGlyph glyphs[runGlyphCount];
    
    // Grab the glyphs, positions, and font
    CTRunGetPositions(run, CFRangeMake(0, 0), positions);
    CTRunGetGlyphs(run, CFRangeMake(0, 0), glyphs);
    CFDictionaryRef attributes = CTRunGetAttributes(run);
    
    CTFontRef runFont = CFDictionaryGetValue(attributes, kCTFontAttributeName);
    
    for(CFIndex i = 0; i < runGlyphCount; ++i) {
        CGPathRef letter = CTFontCreatePathForGlyph(runFont, glyphs[i], nil);
        CGPoint point = positions[i];
        
        if (letter) {
            CGAffineTransform transform;
            
            transform = CGAffineTransformTranslate(upsideDown, point.x, point.y);
            
            
            CGPathAddPath(path, &transform, letter);
        }
        
        CGPathRelease(letter);
    }
    
    return path;
}


@end
