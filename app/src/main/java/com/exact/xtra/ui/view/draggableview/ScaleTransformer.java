package com.exact.xtra.ui.view.draggableview;

import android.view.View;

/**
 * Transformer extension created to scale the view instead of resize it as the other
 * implementation does. This implementation is based on Nineoldanroids library to scale
 * the view.
 *
 * @author Pedro Vicente Gómez Sánchez
 */
class ScaleTransformer extends Transformer {

    ScaleTransformer(View view, View parent) {
        super(view, parent);
    }

    /**
     * Uses Nineoldandroids to change the scale.
     *
     * @param verticalDragOffset used to calculate the new scale.
     */
    @Override public void updateScale(float verticalDragOffset) {
        getView().setScaleX(1 - verticalDragOffset / getXScaleFactor());
        getView().setScaleY(1 - verticalDragOffset / getYScaleFactor());
    }

    /**
     * Uses Nineoldandroids to change the position of the view.
     *
     * @param verticalDragOffset used to calculate the new position.
     */
    @Override public void updatePosition(float verticalDragOffset) {
        getView().setPivotX(getView().getWidth() - getMarginRight());
        getView().setPivotY(getView().getHeight() - getMarginBottom());
    }

    /**
     * @return true if the right corner of the view matches with the parent view width.
     */
    @Override public boolean isViewAtRight() {
        return getView().getRight() == getParentView().getWidth();
    }

    /**
     * @return true if the bottom corner of the view matches with the parent view height.
     */
    @Override public boolean isViewAtBottom() {
        return getView().getBottom() == getParentView().getHeight();
    }

    /**
     * @return true if the left position of the view is to the left of sixty percent of the parent
     * width.
     */
    @Override public boolean isNextToLeftBound() {
        return (getView().getRight() - getMarginRight()) < getParentView().getWidth() * 0.6;
    }

    /**
     * @return true if the right position of the view is to the right of the one hundred twenty five
     * five percent of the parent view width.
     */
    @Override public boolean isNextToRightBound() {
        return (getView().getRight() - getMarginRight()) > getParentView().getWidth() * 1.25;
    }

    /**
     * @return min view height taking into account the configured margin.
     */
    @Override public int getMinHeightPlusMargin() {
        return getView().getHeight();
    }

    /**
     * @return min view width.
     */
    @Override public int getMinWidthPlusMarginRight() {
        return getOriginalWidth();
    }

}