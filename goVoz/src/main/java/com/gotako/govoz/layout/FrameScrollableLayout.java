package com.gotako.govoz.layout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class FrameScrollableLayout extends FrameLayout {

    public FrameScrollableLayout(@NonNull Context context) {
        super(context);
    }

    public FrameScrollableLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FrameScrollableLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (super.canScrollVertically(direction)) {
            return true;
        }
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).canScrollVertically(direction)) {
                return true;
            }
        }
        return false;
    }
}
