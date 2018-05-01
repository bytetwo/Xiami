package com.wanglijun.xiami;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by wanglijun on 2018/4/29.
 */

public class TestViewGroup extends ViewGroup {
    public TestViewGroup(Context context) {
        super(context);

        TextView textView=new TextView(context);
        textView.setText("fdsggdgfdgfdgdfgdfgdfgdfgdfgdfgdfgfd");

        addView(textView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec,heightMeasureSpec);
        setMeasuredDimension(200,200);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        TextView textView= (TextView) getChildAt(0);
        textView.layout(0,0,200,200);
    }
}
