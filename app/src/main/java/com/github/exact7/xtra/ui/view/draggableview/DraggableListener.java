package com.github.exact7.xtra.ui.view.draggableview;

/**
 * Listener created to be notified when some drag actions are performed over DraggablePanel or
 * DraggableView instances.
 *
 * @author Pedro Vicente Gómez Sánchez.
 */
public interface DraggableListener {

    /**
     * Called when the view is maximized.
     */
    void onMaximized();

    /**
     * Called when the view is minimized.
     */
    void onMinimized();

    /**
     * Called when the view is closed to the left.
     */
    void onClosedToLeft();

    /**
     * Called when the view is closed to the right.
     */
    void onClosedToRight();

    void onMoved(float left, float top);
}