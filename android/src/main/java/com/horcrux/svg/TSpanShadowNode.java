/*
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */


package com.horcrux.svg;


import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.facebook.react.uimanager.ReactShadowNode;
import com.facebook.react.uimanager.annotations.ReactProp;

import javax.annotation.Nullable;

import static android.graphics.PathMeasure.POSITION_MATRIX_FLAG;
import static android.graphics.PathMeasure.TANGENT_MATRIX_FLAG;

/**
 * Shadow node for virtual TSpan view
 */
class TSpanShadowNode extends TextShadowNode {
    private static final double tau = 2 * Math.PI;
    private static final double radToDeg = 360 / tau;

    private static final String FONTS = "fonts/";
    private static final String OTF = ".otf";
    private static final String TTF = ".ttf";

    private Path mCache;
    private @Nullable String mContent;
    private TextPathShadowNode textPath;

    @ReactProp(name = "content")
    public void setContent(@Nullable String content) {
        mContent = content;
        markUpdated();
    }

    @Override
    public void draw(Canvas canvas, Paint paint, float opacity) {
        if (mContent != null) {
            drawPath(canvas, paint, opacity);
        } else {
            clip(canvas, paint);
            drawGroup(canvas, paint, opacity);
        }
    }

    @Override
    protected void releaseCachedPath() {
        mCache = null;
    }

    @Override
    protected Path getPath(Canvas canvas, Paint paint) {
        if (mCache != null) {
            return mCache;
        }

        if (mContent == null) {
            return getGroupPath(canvas, paint);
        }

        setupTextPath();

        pushGlyphContext();
        mCache = getLinePath(mContent, paint);
        popGlyphContext();

        mCache.computeBounds(new RectF(), true);

        return mCache;
    }

    private Path getLinePath(String line, Paint paint) {
        final int length = line.length();
        final Path path = new Path();

        if (length == 0) {
            return path;
        }

        GlyphContext gc = getTextRootGlyphContext();
        FontData font = gc.getFont();
        applyTextPropertiesToPaint(paint, font);

        /*
            Determine the startpoint-on-the-path for the first glyph using attribute ‘startOffset’
            and property text-anchor.

            For text-anchor:start, startpoint-on-the-path is the point
            on the path which represents the point on the path which is ‘startOffset’ distance
            along the path from the start of the path, calculated using the user agent's distance
            along the path algorithm.

            For text-anchor:middle, startpoint-on-the-path is the point
            on the path which represents the point on the path which is [ ‘startOffset’ minus half
            of the total advance values for all of the glyphs in the ‘textPath’ element ] distance
            along the path from the start of the path, calculated using the user agent's distance
            along the path algorithm.

            For text-anchor:end, startpoint-on-the-path is the point on
            the path which represents the point on the path which is [ ‘startOffset’ minus the
            total advance values for all of the glyphs in the ‘textPath’ element ].

            Before rendering the first glyph, the horizontal component of the startpoint-on-the-path
            is adjusted to take into account various horizontal alignment text properties and
            attributes, such as a ‘dx’ attribute value on a ‘tspan’ element.
         */
        double offset;
        final TextAnchor textAnchor = font.textAnchor;
        final double textMeasure = paint.measureText(line);
        if (textAnchor == TextAnchor.start) {
            offset = 0;
        } else {
            if (textAnchor == TextAnchor.middle) {
                offset = -textMeasure / 2;
            } else {
                offset = -textMeasure;
            }
        }

        double distance = 0;
        PathMeasure pm = null;
        double renderMethodScaling = 1;
        if (textPath != null) {
            pm = new PathMeasure(textPath.getPath(), false);
            distance = pm.getLength();
            if (distance == 0) {
                return path;
            }
            final double size = gc.getFontSize();
            final String startOffset = textPath.getStartOffset();
            offset += PropHelper.fromRelative(startOffset, distance, 0, mScale, size);
            /*
            TextPathSpacing spacing = textPath.getSpacing();
            if (spacing == TextPathSpacing.auto) {
                // Hmm, what to do here?
                // https://svgwg.org/svg2-draft/text.html#TextPathElementSpacingAttribute
            }
            */
            final TextPathMethod method = textPath.getMethod();
            if (method == TextPathMethod.stretch) {
                renderMethodScaling = distance / textMeasure;
            }
        }

        /*
        *
        * Three properties affect the space between characters and words:
        *
        * ‘kerning’ indicates whether the user agent should adjust inter-glyph spacing
        * based on kerning tables that are included in the relevant font
        * (i.e., enable auto-kerning) or instead disable auto-kerning
        * and instead set inter-character spacing to a specific length (typically, zero).
        *
        * ‘letter-spacing’ indicates an amount of space that is to be added between text
        * characters supplemental to any spacing due to the ‘kerning’ property.
        *
        * ‘word-spacing’ indicates the spacing behavior between words.
        *
        *  Letter-spacing is applied after bidi reordering and is in addition to any word-spacing.
        *  Depending on the justification rules in effect, user agents may further increase
        *  or decrease the space between typographic character units in order to justify text.
        *
        * */
        String previous = "";
        double previousCharWidth = 0;
        double kerning = font.kerning;
        final double wordSpacing = font.wordSpacing;
        final double letterSpacing = font.letterSpacing;
        final boolean autoKerning = !font.manualKerning;

        final char[] chars = line.toCharArray();
        for (int index = 0; index < length; index++) {
            char currentChar = chars[index];
            String current = String.valueOf(currentChar);

            /*
                Determine the glyph's charwidth (i.e., the amount which the current text position
                advances horizontally when the glyph is drawn using horizontal text layout).
            */
            double charWidth = paint.measureText(current) * renderMethodScaling;

            /*
                For each subsequent glyph, set a new startpoint-on-the-path as the previous
                endpoint-on-the-path, but with appropriate adjustments taking into account
                horizontal kerning tables in the font and current values of various attributes
                and properties, including spacing properties (e.g. letter-spacing and word-spacing)
                and ‘tspan’ elements with values provided for attributes ‘dx’ and ‘dy’. All
                adjustments are calculated as distance adjustments along the path, calculated
                using the user agent's distance along the path algorithm.
            */

            if (autoKerning) {
                double bothCharsWidth = paint.measureText(previous + current) * renderMethodScaling;
                kerning = bothCharsWidth - previousCharWidth - charWidth;
                previousCharWidth = charWidth;
                previous = current;
            }

            boolean isWordSeparator = currentChar == ' ';
            double wordSpace = isWordSeparator ? wordSpacing : 0;
            double advance = charWidth + kerning + wordSpace + letterSpacing;

            double x = gc.nextX(advance);
            double y = gc.nextY();
            double dx = gc.nextDeltaX();
            double dy = gc.nextDeltaY();
            double r = gc.nextRotation();

            Matrix start = new Matrix();
            Matrix mid = new Matrix();
            Matrix end = new Matrix();

            float[] startPointMatrixData = new float[9];
            float[] endPointMatrixData = new float[9];

            double startpoint = offset + x + dx - charWidth;

            if (textPath != null) {
                /*
                    Determine the point on the curve which is charwidth distance along the path from
                    the startpoint-on-the-path for this glyph, calculated using the user agent's
                    distance along the path algorithm. This point is the endpoint-on-the-path for
                    the glyph.
                 */
                double endpoint = startpoint + charWidth;

                /*
                    Determine the midpoint-on-the-path, which is the point on the path which is
                    "halfway" (user agents can choose either a distance calculation or a parametric
                    calculation) between the startpoint-on-the-path and the endpoint-on-the-path.
                */
                double halfway = charWidth / 2;
                double midpoint = startpoint + halfway;

                //  Glyphs whose midpoint-on-the-path are off the path are not rendered.
                if (midpoint > distance) {
                    break;
                } else if (midpoint < 0) {
                    continue;
                }

                /*
                    Determine the glyph-midline, which is the vertical line in the glyph's
                    coordinate system that goes through the glyph's x-axis midpoint.

                    Position the glyph such that the glyph-midline passes through
                    the midpoint-on-the-path and is perpendicular to the line
                    through the startpoint-on-the-path and the endpoint-on-the-path.
                */
                assert pm != null;
                if (startpoint < 0 || endpoint > distance) {
                    /*
                        In the calculation above, if either the startpoint-on-the-path
                        or the endpoint-on-the-path is off the end of the path,
                        TODO then extend the path beyond its end points with a straight line
                        that is parallel to the tangent at the path at its end point
                        so that the midpoint-on-the-path can still be calculated.

                        TODO suggest change in wording of svg spec:
                        so that the midpoint-on-the-path can still be calculated
                        to
                        so that the angle of the glyph-midline to the x-axis can still be calculated
                    */
                    final int flags = POSITION_MATRIX_FLAG | TANGENT_MATRIX_FLAG;
                    pm.getMatrix((float) midpoint, mid, flags);
                } else {
                    pm.getMatrix((float) startpoint, start, POSITION_MATRIX_FLAG);
                    pm.getMatrix((float) midpoint, mid, POSITION_MATRIX_FLAG);
                    pm.getMatrix((float) endpoint, end, POSITION_MATRIX_FLAG);

                    start.getValues(startPointMatrixData);
                    end.getValues(endPointMatrixData);

                    double startX = startPointMatrixData[2];
                    double startY = startPointMatrixData[5];
                    double endX = endPointMatrixData[2];
                    double endY = endPointMatrixData[5];

                    double glyphMidlineAngle = Math.atan2(endY - startY, endX - startX) * radToDeg;

                    mid.preRotate((float) glyphMidlineAngle);
                }

            /*
                TODO alignment-baseline
                Align the glyph vertically relative to the midpoint-on-the-path based on property
                alignment-baseline and any specified values for attribute ‘dy’ on a ‘tspan’ element.
            */
                mid.preTranslate((float) -halfway, (float) dy);
                mid.preScale((float) renderMethodScaling, (float) renderMethodScaling);
                mid.postTranslate(0, (float) y);
            } else {
                mid.setTranslate((float) startpoint, (float) (y + dy));
            }

            mid.preRotate((float) r);

            Path glyph = new Path();
            paint.getTextPath(current, 0, 1, 0, 0, glyph);
            glyph.transform(mid);
            path.addPath(glyph);
        }

        return path;
    }

    private void applyTextPropertiesToPaint(Paint paint, FontData font) {
        AssetManager assetManager = getThemedContext().getResources().getAssets();

        double fontSize = font.fontSize * mScale;

        boolean isBold = font.fontWeight == FontWeight.Bold;
        boolean isItalic = font.fontStyle == FontStyle.italic;

        boolean underlineText = false;
        boolean strikeThruText = false;

        TextDecoration decoration = font.textDecoration;
        if (decoration == TextDecoration.Underline) {
            underlineText = true;
        } else if (decoration == TextDecoration.LineThrough) {
            strikeThruText = true;
        }

        int fontStyle;
        if (isBold && isItalic) {
            fontStyle = Typeface.BOLD_ITALIC;
        } else if (isBold) {
            fontStyle = Typeface.BOLD;
        } else if (isItalic) {
            fontStyle = Typeface.ITALIC;
        } else {
            fontStyle = Typeface.NORMAL;
        }

        Typeface typeface = null;
        final String fontFamily = font.fontFamily;
        try {
            String path = FONTS + fontFamily + OTF;
            typeface = Typeface.createFromAsset(assetManager, path);
        } catch (Exception ignored) {
            try {
                String path = FONTS + fontFamily + TTF;
                typeface = Typeface.createFromAsset(assetManager, path);
            } catch (Exception ignored2) {
                try {
                    typeface = Typeface.create(fontFamily, fontStyle);
                } catch (Exception ignored3) {
                }
            }
        }

        // NB: if the font family is null / unsupported, the default one will be used
        paint.setTypeface(typeface);
        paint.setTextSize((float) fontSize);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setUnderlineText(underlineText);
        paint.setStrikeThruText(strikeThruText);
    }

    private void setupTextPath() {
        ReactShadowNode parent = getParent();

        while (parent != null) {
            if (parent.getClass() == TextPathShadowNode.class) {
                textPath = (TextPathShadowNode) parent;
                break;
            } else if (!(parent instanceof TextShadowNode)) {
                break;
            }

            parent = parent.getParent();
        }
    }
}
