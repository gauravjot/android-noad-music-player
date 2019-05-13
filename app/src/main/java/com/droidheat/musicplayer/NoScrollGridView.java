package com.droidheat.musicplayer;

import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;
import android.content.Context;

public class NoScrollGridView extends GridView
{

    boolean expanded = false;

    public NoScrollGridView(Context context)
    {
        super(context);
    }

    public NoScrollGridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public NoScrollGridView(Context context, AttributeSet attrs,
                            int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public boolean isExpanded()
    {
        return expanded;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        // HACK! TAKE THAT ANDROID!
        if (isExpanded())
        {
            // Calculate entire height by providing a very large height hint.
            // But do not use the highest 2 bits of this integer; those are
            // reserved for the MeasureSpec mode.
            int expandSpec = MeasureSpec.makeMeasureSpec(
                    Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);

            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = getMeasuredHeight();
        }
        else
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setExpanded(boolean expanded)
    {
        this.expanded = expanded;
    }
}