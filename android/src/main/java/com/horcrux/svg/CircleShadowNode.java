/*
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */


package com.horcrux.svg;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.facebook.react.bridge.Dynamic;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.uimanager.annotations.ReactProp;

/**
 * Shadow node for virtual Circle view
 */
class CircleShadowNode extends RenderableShadowNode {

    private String mCx;
    private String mCy;
    private String mR;

    @ReactProp(name = "cx")
    public void setCx(Dynamic cx) {
        if (cx.getType() == ReadableType.String) {
            mCx = cx.asString();
        } else {
            mCx = String.valueOf(cx.asDouble());
        }
        markUpdated();
    }

    @ReactProp(name = "cy")
    public void setCy(Dynamic cy) {
        if (cy.getType() == ReadableType.String) {
            mCy = cy.asString();
        } else {
            mCy = String.valueOf(cy.asDouble());
        }
        markUpdated();
    }

    @ReactProp(name = "r")
    public void setR(Dynamic r) {
        if (r.getType() == ReadableType.String) {
            mR = r.asString();
        } else {
            mR = String.valueOf(r.asDouble());
        }
        markUpdated();
    }

    @Override
    protected Path getPath(Canvas canvas, Paint paint) {
        Path path = new Path();

        double cx = relativeOnWidth(mCx);
        double cy = relativeOnHeight(mCy);

        double r;
        if (PropHelper.isPercentage(mR)) {
            r = relativeOnOther(mR);
        } else {
            r = Double.parseDouble(mR) * mScale;
        }

        path.addCircle((float) cx, (float) cy, (float) r, Path.Direction.CW);
        return path;
    }
}
