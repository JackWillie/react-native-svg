/*
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */


package com.horcrux.svg;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;

import javax.annotation.Nullable;

class GlyphContext {
    static final float DEFAULT_FONT_SIZE = 12f;
    private static final float DEFAULT_KERNING = 0f;
    private static final float DEFAULT_LETTER_SPACING = 0f;

    // Unique input attribute lists (only added if node sets a value)
    private final ArrayList<String[]> mXPositionsContext = new ArrayList<>();
    private final ArrayList<String[]> mYPositionsContext = new ArrayList<>();
    private final ArrayList<float[]> mRotationsContext = new ArrayList<>();
    private final ArrayList<float[]> mDeltaXsContext = new ArrayList<>();
    private final ArrayList<float[]> mDeltaYsContext = new ArrayList<>();

    // Unique index into attribute list (one per unique list)
    private final ArrayList<Integer> mXPositionIndices = new ArrayList<>();
    private final ArrayList<Integer> mYPositionIndices = new ArrayList<>();
    private final ArrayList<Integer> mRotationIndices = new ArrayList<>();
    private final ArrayList<Integer> mDeltaXIndices = new ArrayList<>();
    private final ArrayList<Integer> mDeltaYIndices = new ArrayList<>();

    // Index of unique context used (one per node push/pop)
    private final ArrayList<Integer> mXPositionsIndices = new ArrayList<>();
    private final ArrayList<Integer> mYPositionsIndices = new ArrayList<>();
    private final ArrayList<Integer> mRotationsIndices = new ArrayList<>();
    private final ArrayList<Integer> mDeltaXsIndices = new ArrayList<>();
    private final ArrayList<Integer> mDeltaYsIndices = new ArrayList<>();

    // Current stack (one per node push/pop)
    private final ArrayList<ReadableMap> mFontContext = new ArrayList<>();
    private final ArrayList<GroupShadowNode> mNodes = new ArrayList<>();

    // Cached per push context
    private double fontSize = DEFAULT_FONT_SIZE;

    // Current accumulated values
    private float mx;
    private float my;
    private float mr;
    private float mdx;
    private float mdy;

    // Current attribute list
    private float[] mRotations = new float[]{0};
    private float[] mDeltaXs = new float[]{};
    private float[] mDeltaYs = new float[]{};
    private String[] mXs = new String[]{};
    private String[] mYs = new String[]{};

    // Current attribute list index
    private int mXPositionsIndex;
    private int mYPositionsIndex;
    private int mRotationsIndex;
    private int mDeltaXsIndex;
    private int mDeltaYsIndex;

    // Current value index in current attribute list
    private int mXPositionIndex = -1;
    private int mYPositionIndex = -1;
    private int mRotationIndex = -1;
    private int mDeltaXIndex = -1;
    private int mDeltaYIndex = -1;

    // Stack length and last index
    private int mContextLength;
    private int top = -1;

    // Constructor parameters
    private final float mScale;
    private final float mWidth;
    private final float mHeight;

    GlyphContext(float scale, float width, float height) {
        mScale = scale;
        mWidth = width;
        mHeight = height;

        mXPositionsContext.add(mXs);
        mYPositionsContext.add(mYs);
        mDeltaXsContext.add(mDeltaXs);
        mDeltaYsContext.add(mDeltaYs);
        mRotationsContext.add(mRotations);

        mXPositionIndices.add(mXPositionIndex);
        mYPositionIndices.add(mYPositionIndex);
        mRotationIndices.add(mRotationIndex);
        mDeltaXIndices.add(mDeltaXIndex);
        mDeltaYIndices.add(mDeltaYIndex);

        pushIndices();
    }

    void pushContext(GroupShadowNode node, @Nullable ReadableMap font) {
        mFontContext.add(font);
        mNodes.add(node);
        mContextLength++;
        pushIndices();
        top++;

        fontSize = getFontSize();
    }

    void pushContext(TextShadowNode node, @Nullable ReadableMap font, @Nullable ReadableArray rotate, @Nullable ReadableArray deltaX, @Nullable ReadableArray deltaY, @Nullable String positionX, @Nullable String positionY, boolean resetPosition) {
        if (resetPosition) {
            reset();
        }

        if (positionX != null) {
            mXPositionsIndex++;
            mXPositionIndex = -1;
            mXPositionIndices.add(mXPositionIndex);
            mXs = positionX.trim().split("\\s+");
            mXPositionsContext.add(mXs);
        }

        if (positionY != null) {
            mYPositionsIndex++;
            mYPositionIndex = -1;
            mYPositionIndices.add(mYPositionIndex);
            mYs = positionY.trim().split("\\s+");
            mYPositionsContext.add(mYs);
        }

        if (rotate != null && rotate.size() != 0) {
            mRotationsIndex++;
            mRotationIndex = -1;
            mRotationIndices.add(mRotationIndex);
            mRotations = getFloatArrayFromReadableArray(rotate);
            mRotationsContext.add(mRotations);
        }

        if (deltaX != null && deltaX.size() != 0) {
            mDeltaXsIndex++;
            mDeltaXIndex = -1;
            mDeltaXIndices.add(mDeltaXIndex);
            mDeltaXs = getFloatArrayFromReadableArray(deltaX);
            mDeltaXsContext.add(mDeltaXs);
        }

        if (deltaY != null && deltaY.size() != 0) {
            mDeltaYsIndex++;
            mDeltaYIndex = -1;
            mDeltaYIndices.add(mDeltaYIndex);
            mDeltaYs = getFloatArrayFromReadableArray(deltaY);
            mDeltaYsContext.add(mDeltaYs);
        }

        mFontContext.add(font);
        mNodes.add(node);
        mContextLength++;
        pushIndices();
        top++;

        fontSize = getFontSize();
    }

    private void pushIndices() {
        mXPositionsIndices.add(mXPositionsIndex);
        mYPositionsIndices.add(mYPositionsIndex);
        mRotationsIndices.add(mRotationsIndex);
        mDeltaXsIndices.add(mDeltaXsIndex);
        mDeltaYsIndices.add(mDeltaYsIndex);
    }

    private void reset() {
        mXPositionsIndex = mYPositionsIndex = mRotationsIndex = mDeltaXsIndex = mDeltaYsIndex = 0;
        mXPositionIndex = mYPositionIndex = mRotationIndex = mDeltaXIndex = mDeltaYIndex = -1;
        mx = my = mr = mdx = mdy = 0;
    }

    void popContext() {
        mContextLength--;
        top--;

        mFontContext.remove(mContextLength);
        mNodes.remove(mContextLength);

        mXPositionsIndices.remove(mContextLength);
        mYPositionsIndices.remove(mContextLength);
        mRotationsIndices.remove(mContextLength);
        mDeltaXsIndices.remove(mContextLength);
        mDeltaYsIndices.remove(mContextLength);

        int x = mXPositionsIndex;
        int y = mYPositionsIndex;
        int r = mRotationsIndex;
        int dx = mDeltaXsIndex;
        int dy = mDeltaYsIndex;

        mXPositionsIndex = mXPositionsIndices.get(mContextLength);
        mYPositionsIndex = mYPositionsIndices.get(mContextLength);
        mRotationsIndex = mRotationsIndices.get(mContextLength);
        mDeltaXsIndex = mDeltaXsIndices.get(mContextLength);
        mDeltaYsIndex = mDeltaYsIndices.get(mContextLength);

        if (x != mXPositionsIndex) {
            mXPositionsContext.remove(x);
            mXs = mXPositionsContext.get(mXPositionsIndex);
            mXPositionIndex = mXPositionIndices.get(mXPositionsIndex);
        }
        if (y != mYPositionsIndex) {
            mYPositionsContext.remove(y);
            mYs = mYPositionsContext.get(mYPositionsIndex);
            mYPositionIndex = mYPositionIndices.get(mYPositionsIndex);
        }
        if (r != mRotationsIndex) {
            mRotationsContext.remove(r);
            mRotations = mRotationsContext.get(mRotationsIndex);
            mRotationIndex = mRotationIndices.get(mRotationsIndex);
        }
        if (dx != mDeltaXsIndex) {
            mDeltaXsContext.remove(dx);
            mDeltaXs = mDeltaXsContext.get(mDeltaXsIndex);
            mDeltaXIndex = mDeltaXIndices.get(mDeltaXsIndex);
        }
        if (dy != mDeltaYsIndex) {
            mDeltaYsContext.remove(dy);
            mDeltaYs = mDeltaYsContext.get(mDeltaYsIndex);
            mDeltaYIndex = mDeltaYIndices.get(mDeltaYsIndex);
        }
    }

    float nextX(float glyphWidth) {
        for (int index = mXPositionsIndex; index >= 0; index--) {
            int positionIndex = mXPositionIndices.get(index);
            mXPositionIndices.set(index, positionIndex + 1);
        }

        int nextIndex = mXPositionIndex + 1;
        if (nextIndex < mXs.length) {
            mdx = 0;
            mXPositionIndex = nextIndex;
            String val = mXs[nextIndex];
            mx = PropHelper.fromRelativeToFloat(val, mWidth, 0, mScale, fontSize);
        }

        mx += glyphWidth;

        return mx;
    }

    float nextY() {
        for (int index = mYPositionsIndex; index >= 0; index--) {
            int positionIndex = mYPositionIndices.get(index);
            mYPositionIndices.set(index, positionIndex + 1);
        }

        int nextIndex = mYPositionIndex + 1;
        if (nextIndex < mYs.length) {
            mdy = 0;
            mYPositionIndex = nextIndex;
            String val = mYs[nextIndex];
            my = PropHelper.fromRelativeToFloat(val, mHeight, 0, mScale, fontSize);
        }

        return my;
    }

    float nextRotation() {
        for (int index = mRotationsIndex; index >= 0; index--) {
            int rotationIndex = mRotationIndices.get(index);
            mRotationIndices.set(index, rotationIndex + 1);
        }

        int nextIndex = mRotationIndex + 1;
        if (nextIndex < mRotations.length) {
            mRotationIndex = Math.min(mRotationIndex + 1, mRotations.length - 1);
            mr = mRotations[mRotationIndex];
        }

        return mr;
    }

    float nextDeltaX() {
        for (int index = mDeltaXsIndex; index >= 0; index--) {
            int deltaIndex = mDeltaXIndices.get(index);
            mDeltaXIndices.set(index, deltaIndex + 1);
        }

        int nextIndex = mDeltaXIndex + 1;
        if (nextIndex < mDeltaXs.length) {
            mDeltaXIndex = nextIndex;
            float val = mDeltaXs[nextIndex];
            mdx += val * mScale;
        }

        return mdx;
    }

    float nextDeltaY() {
        for (int index = mDeltaYsIndex; index >= 0; index--) {
            int deltaIndex = mDeltaYIndices.get(index);
            mDeltaYIndices.set(index, deltaIndex + 1);
        }

        int nextIndex = mDeltaYIndex + 1;
        if (nextIndex < mDeltaYs.length) {
            mDeltaYIndex = nextIndex;
            float val = mDeltaYs[nextIndex];
            mdy += val * mScale;
        }

        return mdy;
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

    ReadableMap getFont() {
        WritableMap map = Arguments.createMap();

        map.putDouble("letterSpacing", DEFAULT_LETTER_SPACING);
        map.putBoolean("isKerningValueSet", false);
        map.putDouble("kerning", DEFAULT_KERNING);
        map.putDouble("fontSize", fontSize);

        boolean letterSpacingSet = false;
        boolean fontFamilySet = false;
        boolean fontWeightSet = false;
        boolean fontStyleSet = false;
        boolean kerningSet = false;

        for (int index = top; index >= 0; index--) {
            ReadableMap font = mFontContext.get(index);

            if (!fontFamilySet && font.hasKey("fontFamily")) {
                String fontFamily = font.getString("fontFamily");
                map.putString("fontFamily", fontFamily);
                fontFamilySet = true;
            }

            if (!kerningSet && font.hasKey("kerning")) {
                float kerning = Float.valueOf(font.getString("kerning"));
                map.putBoolean("isKerningValueSet", true);
                map.putDouble("kerning", kerning);
                kerningSet = true;
            }

            if (!letterSpacingSet && font.hasKey("letterSpacing")) {
                float letterSpacing = Float.valueOf(font.getString("letterSpacing"));
                map.putDouble("letterSpacing", letterSpacing);
                letterSpacingSet = true;
            }

            if (!fontWeightSet && font.hasKey("fontWeight")) {
                String fontWeight = font.getString("fontWeight");
                map.putString("fontWeight", fontWeight);
                fontWeightSet = true;
            }

            if (!fontStyleSet && font.hasKey("fontStyle")) {
                String fontStyle = font.getString("fontStyle");
                map.putString("fontStyle", fontStyle);
                fontStyleSet = true;
            }

            if (fontFamilySet && kerningSet && letterSpacingSet && fontWeightSet && fontStyleSet) {
                break;
            }
        }

        return map;
    }

    private float[] getFloatArrayFromReadableArray(ReadableArray readableArray) {
        int size = readableArray.size();
        float[] floats = new float[size];
        for (int i = 0; i < size; i++) {
            switch (readableArray.getType(i)) {
                case String:
                    String val = readableArray.getString(i);
                    floats[i] = (float) (Float.valueOf(val.substring(0, val.length() - 2)) * fontSize);
                    break;

                case Number:
                    floats[i] = (float) readableArray.getDouble(i);
                    break;
            }
        }
        return floats;
    }
}
