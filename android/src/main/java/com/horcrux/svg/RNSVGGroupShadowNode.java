/**
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */


package com.horcrux.svg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Region;
import android.util.Log;

/**
 * Shadow node for virtual RNSVGGroup view
 */
public class RNSVGGroupShadowNode extends RNSVGVirtualNode {

    @Override
    public boolean isVirtual() {
        return true;
    }

    public void draw(Canvas canvas, Paint paint, float opacity) {
        opacity *= mOpacity;
        if (opacity > MIN_OPACITY_FOR_DRAW) {
            saveAndSetupCanvas(canvas);

            clip(canvas, paint);

            for (int i = 0; i < getChildCount(); i++) {
                RNSVGVirtualNode child = (RNSVGVirtualNode) getChildAt(i);
                child.draw(canvas, paint, opacity);
                child.markUpdateSeen();
            }

            restoreCanvas(canvas);
        }
    }
}
