/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
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

            if (mClipPath != null) {
                canvas.clipPath(mClipPath, Region.Op.REPLACE);
            }

            for (int i = 0; i < getChildCount(); i++) {
                RNSVGVirtualNode child = (RNSVGVirtualNode) getChildAt(i);
                child.draw(canvas, paint, opacity);
                child.markUpdateSeen();
            }

            restoreCanvas(canvas);
        }
    }
}
