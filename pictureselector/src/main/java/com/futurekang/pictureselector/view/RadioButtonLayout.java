package com.futurekang.pictureselector.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class RadioButtonLayout extends ConstraintLayout {

    /**
     * 静态数组保存
     */
    private static SparseArray<RadioButtonLayout> cacheArray;

    private RadioButton radioButton;

    public RadioButtonLayout(Context context) {
        super(context);
    }

    public RadioButtonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RadioButtonLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 将选中的添加到缓存中去
     */
    private void addCacheView() {
        if (cacheArray == null) {
            cacheArray = new SparseArray<>();
        }
        cancelSelection();
        cacheArray.put(this.hashCode(), RadioButtonLayout.this);
        radioButton.setChecked(true);
    }

    /**
     * 取消其他条目的选中项
     */
    private void cancelSelection() {
        for (int i = 0; i < cacheArray.size(); i++) {
            RadioButtonLayout layout = cacheArray.get(cacheArray.keyAt(i));
            if (RadioButtonLayout.this != layout) {
                RadioButton radioButton = layout.getRadioButton();
                radioButton.setChecked(false);
            }
        }
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.radioButton = getRadioButton();
    }

    /**
     * 获取View中的RadioButton
     *
     * @return
     */
    private RadioButton getRadioButton() {
        return findRadioButton(this);
    }

    /**
     * 递归查找
     *
     * @param group
     * @return
     */
    private RadioButton findRadioButton(ViewGroup group) {
        if (group != null) {
            for (int i = 0, j = group.getChildCount(); i < j; i++) {
                View child = group.getChildAt(i);
                if (child instanceof RadioButton) {
                    return (RadioButton) child;
                } else if (child instanceof ViewGroup) {
                    RadioButton result = findRadioButton((ViewGroup) child);
                    if (result != null)
                        return result;
                }
            }
        }
        throw new IllegalArgumentException("RadioLayout中未包含RadioButton");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private float dy;
    private float dx;
    private long downTime;

    private float MAX_DISTANCE = 100;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //如果有别的指头摸过了，那么就return false。
                // 这样后续的move..等事件也不会再来找这个View了。
                if (ev.getPointerId(ev.getActionIndex()) != 0) {
                    return false;
                }
                dy = ev.getY();
                dx = ev.getX();
                downTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                //判定是点击事件
                if (Math.abs(ev.getY() - dy) < MAX_DISTANCE
                        && Math.abs(ev.getX() - dx) < MAX_DISTANCE
                        && System.currentTimeMillis() - downTime < MAX_DISTANCE) {
                    if (onClickListener != null)
                        onClickListener.onClick(RadioButtonLayout.this);
                    addCacheView();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(ev.getY() - dy) < MAX_DISTANCE
                        && Math.abs(ev.getX() - dx) < MAX_DISTANCE
                        && System.currentTimeMillis() - downTime > MAX_DISTANCE * 10) {
                    if (onLongClickListener != null)
                        onLongClickListener.onLongClick(RadioButtonLayout.this);
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        this.onClickListener = l;
    }

    OnClickListener onClickListener;

    public OnClickListener getOnClickListener() {
        return onClickListener;
    }

    OnLongClickListener onLongClickListener;

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        this.onLongClickListener = l;
    }

    public OnLongClickListener getOnLongClickListener() {
        return onLongClickListener;
    }


}
