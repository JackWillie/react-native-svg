/*
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */


package com.horcrux.svg;

import android.graphics.PointF;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class GlyphContext {
    static final float DEFAULT_FONT_SIZE = 12f;
    private static final float DEFAULT_KERNING = 0f;
    private static final float DEFAULT_LETTER_SPACING = 0f;

    private final ArrayList<ArrayList<String>> mXPositionsContext = new ArrayList<>();
    private final ArrayList<ArrayList<String>> mYPositionsContext = new ArrayList<>();
    private final ArrayList<ArrayList<Float>> mRotationsContext = new ArrayList<>();
    private final ArrayList<ArrayList<Float>> mDeltaXsContext = new ArrayList<>();
    private final ArrayList<ArrayList<Float>> mDeltaYsContext = new ArrayList<>();
    private final ArrayList<ReadableMap> mFontContext = new ArrayList<>();
    private final ArrayList<Float> mRotationContext = new ArrayList<>();
    private final ArrayList<GroupShadowNode> mNodes = new ArrayList<>();
    private @Nonnull final PointF mCurrentPosition = new PointF();
    private @Nonnull final PointF mCurrentDelta = new PointF();
    private ArrayList<Float> mRotations = new ArrayList<>();
    private ArrayList<Float> mDeltaXs = new ArrayList<>();
    private ArrayList<Float> mDeltaYs = new ArrayList<>();
    private ArrayList<String> mXs = new ArrayList<>();
    private ArrayList<String> mYs = new ArrayList<>();
    private int mContextLength;
    private float mRotation;
    private final float mHeight;
    private final float mWidth;
    private final float mScale;
    private int top = -1;

    GlyphContext(float scale, float width, float height) {
        mHeight = height;
        mWidth = width;
        mScale = scale;
    }

    void pushContext(GroupShadowNode node, @Nullable ReadableMap font) {
        mRotationsContext.add(mRotations);
        mRotationContext.add(mRotation);
        mDeltaXsContext.add(mDeltaXs);
        mDeltaYsContext.add(mDeltaYs);
        mXPositionsContext.add(mXs);
        mYPositionsContext.add(mYs);
        mFontContext.add(font);
        mNodes.add(node);
        mContextLength++;
        top++;
    }

    void pushContext(TextShadowNode node, @Nullable ReadableMap font, @Nullable ReadableArray rotate, @Nullable ReadableArray deltaX, @Nullable ReadableArray deltaY, @Nullable String positionX, @Nullable String positionY, boolean resetLocation) {
        if (resetLocation) {
            reset();
        }

        if (positionX != null) {
            mXs = new ArrayList<>(Arrays.asList(positionX.trim().split("\\s+")));
        }

        if (positionY != null) {
            mYs = new ArrayList<>(Arrays.asList(positionY.trim().split("\\s+")));
        }

        ArrayList<Float> rotations = getFloatArrayListFromReadableArray(rotate);
        if (rotations.size() != 0) {
            mRotation = rotations.get(0);
            mRotations = rotations;
        }

        ArrayList<Float> deltaXs = getFloatArrayListFromReadableArray(deltaX);
        if (deltaXs.size() != 0) {
            mDeltaXs = deltaXs;
        }

        ArrayList<Float> deltaYs = getFloatArrayListFromReadableArray(deltaY);
        if (deltaYs.size() != 0) {
            mDeltaYs = deltaYs;
        }

        mRotationsContext.add(mRotations);
        mRotationContext.add(mRotation);
        mDeltaXsContext.add(mDeltaXs);
        mDeltaYsContext.add(mDeltaYs);
        mXPositionsContext.add(mXs);
        mYPositionsContext.add(mYs);
        mFontContext.add(font);
        mNodes.add(node);
        mContextLength++;
        top++;
    }

    private void reset() {
        mCurrentDelta.x = mCurrentDelta.y = mRotation = 0;
        mCurrentPosition.x = mCurrentPosition.y = 0;
    }

    void popContext() {
        mContextLength--;
        top--;

        mXPositionsContext.remove(mContextLength);
        mYPositionsContext.remove(mContextLength);

        mRotationsContext.remove(mContextLength);
        mRotationContext.remove(mContextLength);

        mDeltaXsContext.remove(mContextLength);
        mDeltaYsContext.remove(mContextLength);

        mFontContext.remove(mContextLength);
        mNodes.remove(mContextLength);

        if (mContextLength != 0) {
            mRotations = mRotationsContext.get(top);
            mRotation = mRotationContext.get(top);
            mDeltaXs = mDeltaXsContext.get(top);
            mDeltaYs = mDeltaYsContext.get(top);
            mXs = mXPositionsContext.get(top);
            mYs = mYPositionsContext.get(top);
        } else {
            reset();
        }
    }

    PointF getNextGlyphPoint(float glyphWidth) {
        mCurrentPosition.x = getGlyphPosition(true);
        mCurrentPosition.y = getGlyphPosition(false);
        mCurrentPosition.x += glyphWidth;
        return mCurrentPosition;
    }

    PointF getNextGlyphDelta() {
        mCurrentDelta.x = getNextDelta(true);
        mCurrentDelta.y = getNextDelta(false);
        return mCurrentDelta;
    }

    float getNextGlyphRotation() {
        List<Float> context = mRotations;

        if (context.size() != 0) {
            mRotation = context.remove(0);
            mRotationContext.set(top, mRotation);
        }

        for (int index = top - 1; index >= 0; index--) {
            List<Float> rotations = mRotationsContext.get(index);

            if (context != rotations && rotations.size() != 0) {
                float val = rotations.remove(0);
                mRotationContext.set(index, val);
            }

            context = rotations;
        }

        return mRotation;
    }

    private float getNextDelta(boolean isX) {
        ArrayList<ArrayList<Float>> lists = isX ? mDeltaXsContext : mDeltaYsContext;
        float delta = isX ? mCurrentDelta.x : mCurrentDelta.y;
        List<Float> context = isX ? mDeltaXs : mDeltaYs;

        if (context.size() != 0) {
            float val = context.remove(0);
            delta += val * mScale;
        }

        for (int index = top - 1; index >= 0; index--) {
            List<Float> deltas = lists.get(index);

            if (context != deltas && deltas.size() != 0) {
                deltas.remove(0);
            }

            context = deltas;
        }

        return delta;
    }

    private float getGlyphPosition(boolean isX) {
        ArrayList<ArrayList<String>> lists = isX ? mXPositionsContext : mYPositionsContext;
        float value = isX ? mCurrentPosition.x : mCurrentPosition.y;
        List<String> context = isX ? mXs : mYs;

        if (context.size() != 0) {
            String val = context.remove(0);

            float relative = isX ? mWidth : mHeight;
            value = PropHelper.fromRelativeToFloat(val, relative, 0, mScale, getFontSize());
            if (isX) {
                mCurrentDelta.x = 0;
            } else {
                mCurrentDelta.y = 0;
            }
        }

        for (int index = top - 1; index >= 0; index--) {
            List<String> positions = lists.get(index);

            if (context != positions && positions.size() != 0) {
                positions.remove(0);
            }

            context = positions;
        }

        return value;
    }

    float getWidth() {
        return mWidth;
    }

    float getHeight() {
        return mHeight;
    }

    double getFontSize() {
        for (int index = top; index >= 0; index--) {
            ReadableMap font = mFontContext.get(index);

            if (mFontContext.get(index).hasKey("fontSize")) {
                return font.getDouble("fontSize");
            }
        }

        if (top > -1) {
            return mNodes.get(0).getFontSizeFromParentContext();
        }

        return DEFAULT_FONT_SIZE;
    }

    ReadableMap getGlyphFont() {
        float letterSpacing = DEFAULT_LETTER_SPACING;
        float fontSize = (float) getFontSize();
        float kerning = DEFAULT_KERNING;

        boolean letterSpacingSet = false;
        boolean kerningSet = false;

        String fontFamily = null;
        String fontWeight = null;
        String fontStyle = null;

        for (int index = top; index >= 0; index--) {
            ReadableMap font = mFontContext.get(index);

            if (fontFamily == null && font.hasKey("fontFamily")) {
                fontFamily = font.getString("fontFamily");
            }

            if (!kerningSet && font.hasKey("kerning")) {
                kerning = Float.valueOf(font.getString("kerning"));
                kerningSet = true;
            }

            if (!letterSpacingSet && font.hasKey("letterSpacing")) {
                letterSpacing = Float.valueOf(font.getString("letterSpacing"));
                letterSpacingSet = true;
            }

            if (fontWeight == null && font.hasKey("fontWeight")) {
                fontWeight = font.getString("fontWeight");
            }

            if (fontStyle == null && font.hasKey("fontStyle")) {
                fontStyle = font.getString("fontStyle");
            }

            if (fontFamily != null && kerningSet && letterSpacingSet && fontWeight != null && fontStyle != null) {
                break;
            }
        }

        WritableMap map = Arguments.createMap();

        map.putBoolean("isKerningValueSet", kerningSet);
        map.putDouble("letterSpacing", letterSpacing);
        map.putString("fontFamily", fontFamily);
        map.putString("fontWeight", fontWeight);
        map.putString("fontStyle", fontStyle);
        map.putDouble("fontSize", fontSize);
        map.putDouble("kerning", kerning);

        return map;
    }

    private ArrayList<Float> getFloatArrayListFromReadableArray(ReadableArray readableArray) {
        ArrayList<Float> arrayList = new ArrayList<>();

        if (readableArray != null) {
            for (int i = 0; i < readableArray.size(); i++) {
                switch (readableArray.getType(i)) {
                    case String:
                        String val = readableArray.getString(i);
                        arrayList.add((float) (Float.valueOf(val.substring(0, val.length() - 2)) * getFontSize()));
                        break;

                    case Number:
                        arrayList.add((float) readableArray.getDouble(i));
                        break;
                }
            }
        }

        return arrayList;
    }
}
