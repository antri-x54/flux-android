package org.nikanikoo.flux.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class CustomDrawerLayout extends DrawerLayout {

    private static final float SWIPE_THRESHOLD = 100f; // минимальное расстояние для свайпа
    private static final float SWIPE_VELOCITY_THRESHOLD = 100f; // минимальная скорость свайпа
    
    private float startX;
    private float startY;
    private boolean isSwipeDetected = false;

    public CustomDrawerLayout(Context context) {
        super(context);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                isSwipeDetected = false;
                break;
                
            case MotionEvent.ACTION_MOVE:
                if (!isSwipeDetected) {
                    float deltaX = ev.getX() - startX;
                    float deltaY = ev.getY() - startY;
                    
                    // Проверяем, что это горизонтальный свайп слева направо
                    if (Math.abs(deltaX) > Math.abs(deltaY) && deltaX > SWIPE_THRESHOLD) {
                        // Проверяем, что свайп начался с левой части экрана (первые 50% ширины)
                        if (startX < getWidth() * 0.5f && !isDrawerOpen(GravityCompat.START)) {
                            isSwipeDetected = true;
                            openDrawer(GravityCompat.START);
                            return true;
                        }
                    }
                }
                break;
        }
        
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Если мы перехватили событие для открытия drawer, обрабатываем его
        if (isSwipeDetected) {
            return true;
        }
        return super.onTouchEvent(ev);
    }
}