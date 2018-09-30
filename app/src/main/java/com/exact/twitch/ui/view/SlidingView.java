package com.exact.twitch.ui.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

import com.google.android.exoplayer2.ui.PlayerView;

public class SlidingView extends RelativeLayout {

    private ViewDragHelper viewDragHelper;

    private View dragView;
    private View secondView;

    private int topBound;
    private int bottomBound;
    private int minimizedTop;
    private int minimizedLeft;
    private int minimizeThreshold;

    private int marginRight = 100;
    private int marginBottom = 400;

    float startX = 0;
    float startY = 0;
    float endX = 0;
    float endY = 0;
    long duration = 0;

    private int controllerVisibility;

    public SlidingView(Context context) {
        super(context);
    }

    public SlidingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeAttributes(attrs);
    }

    public SlidingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeAttributes(attrs);
    }

    private void initializeAttributes(AttributeSet attrs) {

    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        if (childCount != 2) {
            throw new IllegalArgumentException();
        }
        dragView = getChildAt(0);
        secondView = getChildAt(1);
        if (dragView instanceof PlayerView) {
            ((PlayerView) dragView).setControllerVisibilityListener(visibility -> controllerVisibility = visibility);
        }
        SlidingCallback callback = new SlidingCallback();
        viewDragHelper = ViewDragHelper.create(this, 1f,callback);
        dragView.post(() -> {
            topBound = getPaddingTop();
            bottomBound = (int) (dragView.getHeight() / 1.5);
            minimizedLeft = getWidth() - dragView.getWidth() - marginRight;
            minimizedTop = 400;
            minimizeThreshold = dragView.getHeight() / 3;
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            viewDragHelper.cancel();
            return false;
        }
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if (!isViewMinimized() && dragView instanceof PlayerView && isClicked(event)) {
//            performClick();
//            return true;
//        }
//        viewDragHelper.processTouchEvent(event);
//        if (isViewMinimized() && isClicked(event)) {
//            maximize();
//        }

        Animation animation = new ScaleAnimation(1f, 0.1f, 1f, 0f);
        animation.start();
        return true;
    }

    @Override
    public boolean performClick() {
        PlayerView playerView = (PlayerView) dragView;
        if (controllerVisibility == VISIBLE) {
            playerView.hideController();
        } else if (controllerVisibility == GONE) {
            playerView.showController();
        }
        return super.performClick();
    }

    private boolean isClicked(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                endX = event.getX();
                endY = event.getY();
                duration = event.getEventTime() - event.getDownTime();
                break;
        }
        return startX == endX && startY == endY && duration >= 0 && duration < 300;
    }

    public void maximize() {
        secondView.setVisibility(VISIBLE);
        smoothSlideTo(0f);
        MarginLayoutParams margingParams = new MarginLayoutParams(dragView.getLayoutParams());
        margingParams.bottomMargin = 0;
        margingParams.rightMargin = 0;
        ViewGroup.LayoutParams params = new LayoutParams(margingParams);
        dragView.setLayoutParams(params);
    }

    public void minimize() {
        secondView.setVisibility(GONE);
        smoothSlideTo(1f);
        MarginLayoutParams margingParams = new MarginLayoutParams(dragView.getLayoutParams());
        margingParams.bottomMargin = marginBottom;
        margingParams.rightMargin = marginRight;
        ViewGroup.LayoutParams params = new LayoutParams(margingParams);
        dragView.setLayoutParams(params);
    }

    private boolean smoothSlideTo(float slideOffset) {
        int left = (int) (slideOffset * minimizedLeft);
        int top = (int) (slideOffset * minimizedTop);
        if (viewDragHelper.smoothSlideViewTo(dragView, left, top)) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
    }

    private void closeToRight() {
        if (viewDragHelper.smoothSlideViewTo(dragView, dragView.getLeft() + dragView.getWidth(), dragView.getTop())) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void closeToLeft() {
        if (viewDragHelper.smoothSlideViewTo(dragView, -dragView.getWidth(), dragView.getTop())) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    int left;
    int top;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (isInEditMode()) {
            super.onLayout(changed, l, t, r, b);
        } else {
            dragView.layout(l, t, r, dragView.getHeight());
            dragView.setY(top);
            if (!isViewMinimized()) {
                secondView.layout(l, dragView.getHeight(), r, b );
                secondView.setY(dragView.getHeight());

            }
        }
    }


    private boolean isViewAtBottom() {
        return dragView.getTop() == minimizedTop;
    }

    private boolean isViewAtRight() {
        return dragView.getLeft() == minimizedLeft;
    }

    private boolean isViewMinimized() {
        return isViewAtBottom() && isViewAtRight();
    }

    private boolean isFullscreen() {
        return dragView.getLeft() == 0;
    }

    private class SlidingCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return child == dragView;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            return !isFullscreen() ? child.getTop() : Math.min(Math.max(top, topBound), bottomBound); //new top
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            return isViewAtBottom() && child.getLeft() != 0 ? left : child.getLeft(); //new left
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            return super.getViewHorizontalDragRange(child);
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            SlidingView.this.top = top;
            SlidingView.this.left = left;
            if (!isFullscreen() && !isViewMinimized()) {
                float dragOffset = (float) top / changedView.getHeight();
                dragView.setScaleX(1 - dragOffset / 2);
                dragView.setScaleY(1 - dragOffset / 2);
                secondView.setTranslationY(top);
            } else if (isFullscreen()) {
                secondView.setTranslationY(top);
            }
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            if (isFullscreen()) {
                if (releasedChild.getTop() >= minimizeThreshold) {
                    minimize();
                    secondView.layout(0, 0, 0, 0);
                } else {
                    releasedChild.setTop(0);
                    secondView.setTranslationY(0);
                }
            } else {
                if (xvel > 1500) {
                    closeToRight();
                } else if (xvel < -1500) {
                    closeToLeft();
                } else {
                    if (!isViewMinimized()) {
                        minimize();
                    }
                }
            }
        }
    }
}
