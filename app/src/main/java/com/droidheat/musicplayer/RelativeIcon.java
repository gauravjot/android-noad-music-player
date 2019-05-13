package com.droidheat.musicplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class RelativeIcon extends RelativeLayout {

    public RelativeIcon(final Context context) {
        super(context);
    }

    public RelativeIcon(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeIcon(final Context context, final AttributeSet attrs,
                        final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //Snap to width

    }

    @Override
    public void requestLayout() {
        super.requestLayout();
    }
}