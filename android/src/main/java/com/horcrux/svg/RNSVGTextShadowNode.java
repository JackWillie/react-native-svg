/**
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */


package com.horcrux.svg;

import javax.annotation.Nullable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.annotations.ReactProp;

/**
 * Shadow node for virtual RNSVGText view
 */
public class RNSVGTextShadowNode extends RNSVGPathShadowNode {

    private static final String PROP_LINES = "lines";

    private static final String PROP_FONT = "font";
    private static final String PROP_FONT_FAMILY = "fontFamily";
    private static final String PROP_FONT_SIZE = "fontSize";
    private static final String PROP_FONT_STYLE = "fontStyle";
    private static final String PROP_FONT_WEIGHT = "fontWeight";

    private static final int DEFAULT_FONT_SIZE = 12;

    private static final int TEXT_ALIGNMENT_CENTER = 2;
    private static final int TEXT_ALIGNMENT_LEFT = 0;
    private static final int TEXT_ALIGNMENT_RIGHT = 1;

    private @Nullable ReadableMap mFrame;
    private int mTextAlignment = TEXT_ALIGNMENT_LEFT;
    private Path mPath;

    @ReactProp(name = "frame")
    public void setFrame(@Nullable ReadableMap frame) {
        mFrame = frame;
    }

    @ReactProp(name = "alignment", defaultInt = TEXT_ALIGNMENT_LEFT)
    public void setAlignment(int alignment) {
        mTextAlignment = alignment;
    }

    @ReactProp(name = "path")
    public void setPath(@Nullable ReadableArray textPath) {
        float[] pathData = PropHelper.toFloatArray(textPath);
        Path path = new Path();
        mPath = super.createPath(pathData, path);
        markUpdated();
    }

    @Override
    public void draw(Canvas canvas, Paint paint, float opacity) {
        if (mFrame == null) {
            return;
        }
        opacity *= mOpacity;
        if (opacity <= MIN_OPACITY_FOR_DRAW) {
            return;
        }
        if (!mFrame.hasKey(PROP_LINES)) {
            return;
        }
        ReadableArray linesProp = mFrame.getArray(PROP_LINES);
        if (linesProp == null || linesProp.size() == 0) {
            return;
        }

        // only set up the canvas if we have something to draw
        saveAndSetupCanvas(canvas);
        clip(canvas, paint);

        String[] lines = new String[linesProp.size()];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = linesProp.getString(i);
        }
        String text = TextUtils.join("\n", lines);

        Rect bound = new Rect();
        paint.getTextBounds(text, 0, text.length(), bound);
        RectF box = new RectF(bound);
        if (setupStrokePaint(paint, opacity, box)) {
            applyTextPropertiesToPaint(paint);
            if (mPath == null) {
                canvas.drawText(text, 0, -paint.ascent(), paint);
            } else {
                canvas.drawTextOnPath(text, mPath, 0, 0, paint);
            }
        }
        if (setupFillPaint(paint, opacity, box)) {
            applyTextPropertiesToPaint(paint);
            if (mPath == null) {
                canvas.drawText(text, 0, -paint.ascent(), paint);
            } else {
                canvas.drawTextOnPath(text, mPath, 0, 0, paint);
            }
        }
        restoreCanvas(canvas);
        markUpdateSeen();
    }

    private void applyTextPropertiesToPaint(Paint paint) {
        int alignment = mTextAlignment;
        switch (alignment) {
            case TEXT_ALIGNMENT_LEFT:
                paint.setTextAlign(Paint.Align.LEFT);
                break;
            case TEXT_ALIGNMENT_RIGHT:
                paint.setTextAlign(Paint.Align.RIGHT);
                break;
            case TEXT_ALIGNMENT_CENTER:
                paint.setTextAlign(Paint.Align.CENTER);
                break;
        }
        if (mFrame != null) {
            if (mFrame.hasKey(PROP_FONT)) {
                ReadableMap font = mFrame.getMap(PROP_FONT);
                if (font != null) {
                    float fontSize = DEFAULT_FONT_SIZE;
                    if (font.hasKey(PROP_FONT_SIZE)) {
                        fontSize = (float) font.getDouble(PROP_FONT_SIZE);
                    }
                    paint.setTextSize(fontSize * mScale);
                    boolean isBold =
                        font.hasKey(PROP_FONT_WEIGHT) && "bold".equals(font.getString(PROP_FONT_WEIGHT));
                    boolean isItalic =
                        font.hasKey(PROP_FONT_STYLE) && "italic".equals(font.getString(PROP_FONT_STYLE));
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
                    // NB: if the font family is null / unsupported, the default one will be used
                    paint.setTypeface(Typeface.create(font.getString(PROP_FONT_FAMILY), fontStyle));
                }
            }
        }
    }
}
