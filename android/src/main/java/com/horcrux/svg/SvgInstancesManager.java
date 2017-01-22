package com.horcrux.svg;

import android.util.SparseArray;

import javax.annotation.Nullable;

public class SvgInstancesManager {
    private static final SparseArray<SvgViewShadowNode> mTagToShadowNode = new SparseArray<>();
    private static final SparseArray<SvgView> mTagToSvgView = new SparseArray<>();

    static void registerShadowNode(SvgViewShadowNode shadowNode) {
        mTagToShadowNode.put(shadowNode.getReactTag(), shadowNode);
    }

    static void registerSvgView(SvgView svg) {
        mTagToSvgView.put(svg.getId(), svg);
    }

    static void unregisterInstance(int tag) {
        mTagToShadowNode.remove(tag);
        mTagToSvgView.remove(tag);
    }

    static @Nullable SvgView getSvgViewByTag(int tag) {
        return mTagToSvgView.get(tag);
    }

    static @Nullable SvgViewShadowNode getShadowNodeByTag(int tag) {
        return mTagToShadowNode.get(tag);
    }
}
