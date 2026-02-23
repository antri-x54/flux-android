package org.nikanikoo.flux.ui.custom;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class SwipeToCloseHelper {
    
    public interface OnSwipeListener {
        void onSwipeStart();
        void onSwipeProgress(float progress, float translationY);
        void onSwipeEnd(boolean shouldClose);
    }
    
    private final OnSwipeListener listener;
    private final int touchSlop;
    private final float dismissThreshold;
    
    private float initialY;
    private float initialX;
    private boolean isTracking = false;
    private boolean canStartSwipe = true;
    
    public SwipeToCloseHelper(View view, OnSwipeListener listener) {
        this.listener = listener;
        this.touchSlop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();
        this.dismissThreshold = 0.3f; // 30% экрана для закрытия
    }
    
    public void setCanStartSwipe(boolean canStartSwipe) {
        this.canStartSwipe = canStartSwipe;
    }
    
    public boolean onTouchEvent(MotionEvent event, View containerView) {
        float deltaY, deltaX;
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialY = event.getRawY();
                initialX = event.getRawX();
                isTracking = false;
                return false;
                
            case MotionEvent.ACTION_MOVE:
                if (!canStartSwipe) {
                    return false;
                }
                
                deltaY = event.getRawY() - initialY;
                deltaX = event.getRawX() - initialX;
                
                // Проверяем, является ли это вертикальным жестом
                if (!isTracking) {
                    if (Math.abs(deltaY) > touchSlop && Math.abs(deltaY) > Math.abs(deltaX) * 1.5f) {
                        isTracking = true;
                        if (listener != null) {
                            listener.onSwipeStart();
                        }
                    }
                }
                
                if (isTracking) {
                    float progress = Math.abs(deltaY) / (containerView.getHeight() * dismissThreshold);
                    progress = Math.min(1f, progress);
                    
                    if (listener != null) {
                        listener.onSwipeProgress(progress, deltaY);
                    }
                    return true;
                }
                return false;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isTracking) {
                    deltaY = event.getRawY() - initialY;
                    boolean shouldClose = Math.abs(deltaY) > containerView.getHeight() * dismissThreshold;
                    
                    if (listener != null) {
                        listener.onSwipeEnd(shouldClose);
                    }
                    
                    isTracking = false;
                    return true;
                }
                return false;
        }
        
        return false;
    }
}