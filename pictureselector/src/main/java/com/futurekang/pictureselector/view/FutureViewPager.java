package com.futurekang.pictureselector.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class FutureViewPager extends ViewPager {

    boolean clickable;
    private GestureDetector mDetector;

    public FutureViewPager(@NonNull Context context) {
        super(context);
        init();
    }

    public FutureViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mDetector = new GestureDetector(getContext(), mGestureListener);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void setClickable(boolean clickable) {
        super.setClickable(clickable);
        this.clickable = clickable;
    }

    private OnClickListener onClickListener;

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent ev) {
            if (isClickable() && onClickListener != null) {
                onClickListener.onClick(FutureViewPager.this);
            }
            return super.onSingleTapConfirmed(ev);
        }
    };
}
