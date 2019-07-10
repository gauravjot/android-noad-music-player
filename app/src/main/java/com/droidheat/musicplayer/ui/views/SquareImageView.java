package com.droidheat.musicplayer.ui.views;

import android.content.Context;
import android.util.AttributeSet;

public class SquareImageView extends android.support.v7.widget.AppCompatImageView {

    public SquareImageView(final Context context) {
        super(context);
    }

    public SquareImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(final Context context, final AttributeSet attrs,
                           final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if ((widthMeasureSpec < getMaxWidth()) && (getMaxWidth() != Integer.MAX_VALUE || getMaxHeight() != Integer.MAX_VALUE)) {
            if (getMaxWidth() > getMaxHeight()) {
                setMeasuredDimension(getMaxHeight(), getMaxHeight());
            } else {
                setMeasuredDimension(getMaxWidth(), getMaxWidth());
            }
        } else {
            setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //Snap to width
        }
    }



    @Override
    public void requestLayout() {
        super.requestLayout();
    }
}