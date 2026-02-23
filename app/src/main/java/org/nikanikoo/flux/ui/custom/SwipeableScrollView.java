package org.nikanikoo.flux.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.core.widget.NestedScrollView;

/**
 * Custom NestedScrollView that allows horizontal swipe detection
 * while still supporting vertical scrolling.
 */
public class SwipeableScrollView extends NestedScrollView {
    
    private float startX;
    private float startY;
    private static final int SWIPE_THRESHOLD = 100;
    private OnSwipeListener swipeListener;
    
    public interface OnSwipeListener {
        void onSwipeLeft();
        void onSwipeRight();
    }
    
    public SwipeableScrollView(Context context) {
        super(context);
    }
    
    public SwipeableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public SwipeableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    public void setOnSwipeListener(OnSwipeListener listener) {
        this.swipeListener = listener;
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                break;
                
            case MotionEvent.ACTION_MOVE:
                float diffX = ev.getX() - startX;
                float diffY = ev.getY() - startY;
                
                // If horizontal movement is greater than vertical, intercept
                if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > SWIPE_THRESHOLD / 2) {
                    return true;
                }
                break;
        }
        
        return super.onInterceptTouchEvent(ev);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (swipeListener == null) {
            return super.onTouchEvent(ev);
        }
        
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                return true;
                
            case MotionEvent.ACTION_UP:
                float endX = ev.getX();
                float endY = ev.getY();
                float diffX = endX - startX;
                float diffY = endY - startY;
                
                // Detect horizontal swipe
                if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > SWIPE_THRESHOLD) {
                    if (diffX < 0) {
                        // Swipe left
                        swipeListener.onSwipeLeft();
                    } else {
                        // Swipe right
                        swipeListener.onSwipeRight();
                    }
                    return true;
                }
                break;
        }
        
        return super.onTouchEvent(ev);
    }
}
