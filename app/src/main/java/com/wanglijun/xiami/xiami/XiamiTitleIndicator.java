package com.wanglijun.xiami.xiami;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * ViewPager上面的标签选项卡
 * <p>
 * Created by wanglijun on 2018/4/27.
 */

public class XiamiTitleIndicator extends RelativeLayout {
    public interface OnTitleSelectListener {
        void onTitleSelect(int position);
    }

    private OnTitleSelectListener onTitleSelectListener;

    public void setOnTitleSelectListener(OnTitleSelectListener onTitleSelectListener) {
        this.onTitleSelectListener = onTitleSelectListener;
    }

    private static final String TAG = "XiamiTitleIndicator";
    /**
     * 文字颜色的模式,一种是黑色的文字,一种是白色的文字
     */
    public static final int TEXT_COLOR_MODE_DARK = 0;
    public static final int TEXT_COLOR_MODE_LIGHT = 1;
    /**
     * 标签数组
     */
    private List<String> titles;
    /**
     * 最大状态下文字的默认大小
     */
    private static final float MAX_TITLE_TEXT_SIZE = 30;//sp
    /**
     * 最小状态下文字的默认大小
     */
    private static final float MIN_TITLE_TEXT_SIZE = 12;//sp
    /**
     * 波浪线的默认颜色
     */
    private static final int LINE_COLOR = Color.RED;
    /**
     * 波浪线的默认宽度
     */
    private static final float LINE_WIDTH = 1.0f;//dp
    /**
     * 波浪线颜色
     */
    private int lineColor;
    /**
     * 波浪线宽度
     */
    private int lineWidth;
    private Paint linePaint;
    /**
     * 所有波浪线加上横线的Path,最后一把绘制
     */
    private Path totalPath = new Path();
    /**
     * 每个标题下面的波浪线
     */
    private Path[] wavePaths;
    /**
     * 当前的索引值
     */
    private int titleIndex = 0;//当前索引
    /**
     * 文字的margin
     */
    private int margin;
    /**
     * 最大状态下文字的宽度和高度,宽度用于,进行获取缩放值使用,高度用于定死整个控件的高度,避免在重重绘的时候RelativeLayout高度重新计算
     */
    private int maxTextViewWidth;
    private int maxTextViewHeight;
    private static final int WAVE_DIVIDE_COUNT = 10;

    public XiamiTitleIndicator(Context context) {
        super(context, null);
    }

    public XiamiTitleIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        margin = ViewUtil.dp2px(context, XiamiTitleMargin.XIAMI_TITLE_MARGIN);
        lineWidth = ViewUtil.dp2px(context, LINE_WIDTH);
        lineColor = LINE_COLOR;
        /**
         * 初始化画笔
         */
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(lineColor);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(lineWidth);
        /**
         * 设置一下执行ViewGroup的onDraw
         * 除了setWillNotDraw,也可以使用setBackgroudColor(Color.TRANSPARENT),因为在调用设置背景的时候,ViewGroup会开启这个开关
         */
        setWillNotDraw(false);
    }


    /**
     * 设置当前的标题以及选中的位置
     *
     * @param titles
     * @param position
     */
    public void setTitles(List<String> titles, int position) {
        this.titles = titles;
        wavePaths = new Path[titles.size()];
        removeAllViews();
        titleIndex = position;
        for (int i = 0; i < titles.size(); i++) {
            TextView textView = new TextView(getContext());
            /**
             * 为什么设置的id不直接用i,因为-1到22倍RelativeLayout使用作常量了,在addRule的时候就会有意想不到的效果,所以只要不使用这几个整型就行了
             */
            textView.setId(i + 100);
            textView.setText(titles.get(i));
            textView.setTextSize(titleIndex == i ? MAX_TITLE_TEXT_SIZE : MIN_TITLE_TEXT_SIZE);
            /**
             * 所有TextView包裹内容
             */
            RelativeLayout.LayoutParams params = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            /**
             * 靠底部排列
             */
            params.addRule(ALIGN_PARENT_BOTTOM);
            /**
             * 后一个在前一个的右边
             */
            if (i > 0) {
                params.addRule(RIGHT_OF, i - 1 + 100);
            }
            if (i != titleIndex) {
                textView.setTypeface(Typeface.DEFAULT);
            } else {
                textView.setTypeface(Typeface.DEFAULT_BOLD);
                /**
                 * 这里先用一个伪造的足够大的MeasureSpec来测量下这个最大状态下的TextView看看它有多大,为什么要右移2位,因为MeasureSpec前两位是决定模式使用的
                 */
                int w_m = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
                int h_m = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
                textView.measure(w_m, h_m);
                maxTextViewWidth = textView.getMeasuredWidth();
                maxTextViewHeight = textView.getMeasuredHeight();
            }
            params.setMargins(margin, margin, margin, margin);
            textView.setLayoutParams(params);
            final int finalI = i;
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTitleClick(finalI);
                }
            });
            addView(textView);
        }
    }


    /**
     * 设置文字的颜色
     *
     * @param mode
     */
    public void setTextColorMode(int mode) {
        if (titles == null) {
            return;
        }
        for (int i = 0; i < titles.size(); i++) {
            TextView textView = (TextView) getChildAt(i);
            if (mode == TEXT_COLOR_MODE_DARK) {
                textView.setTextColor(Color.BLACK);
            } else {
                textView.setTextColor(Color.WHITE);
            }
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /**
         * 当前是保证AT_MOST情况下的,不要使用EXACTLY.AT_MOST看起来效果是最好的
         */
        int h_m = MeasureSpec.makeMeasureSpec(maxTextViewHeight + margin * 2, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, h_m);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (titles == null) {
            return;
        }
        /**
         * 找到波浪线绘制的中线
         */
        int lineY = getMeasuredHeight() - margin / 2;
        int left = 0;
        int right = getMeasuredWidth();
        /**
         * 重置这个大的path
         *
         */
        totalPath.reset();
        totalPath.moveTo(left, lineY);
        /**
         *主要是用直线把波浪线连接起来
         */
        for (int i = 0; i < titles.size(); i++) {
            View view = getChildAt(i);
            int l = view.getLeft();
            int r = view.getRight();
            totalPath.lineTo(l, lineY);
            Path path = wavePaths[i];
            if (path == null) {
                path = new Path();
            }
            generateWavePath(path, l, r);
            totalPath.addPath(path);
        }
        totalPath.lineTo(right, lineY);
        canvas.drawPath(totalPath, linePaint);
    }

    /**
     * 生成文字下方的小波浪的Path
     *
     * @param path
     * @param left
     * @param right
     */
    private void generateWavePath(Path path, int left, int right) {
        int length = right - left;
        float scale = length * 1.f / maxTextViewWidth;
        int height = (int) (margin * scale);
        /**
         * 先找到中心线位置
         */
        int middleY = getMeasuredHeight() - margin / 2;
        int top = middleY - height / 2;
        int bottom = top + height;
        /**
         * 把这个长度分成8等分
         */
        int singleWidth = (right - left) / 10;
        path.reset();
        path.moveTo(left, middleY);
        /**
         * 这里的代码看起来很啰嗦,是的
         */
        for (int i = 1; i < WAVE_DIVIDE_COUNT; i++) {
            if ((i & 1) == 1) {
                path.lineTo(left + singleWidth * i, top);
            } else {
                path.lineTo(left + singleWidth * i, bottom);
            }
        }
        path.lineTo(right, middleY);
    }

    /**
     * 用于选中某个标签后文字放大的动画
     */
    private ValueAnimator timeValueAnimator;

    /**
     * TextView被点击的时候调用
     *
     * @param position
     */
    public void onTitleClick(int position) {
        setCurrentItem(position);
        if (onTitleSelectListener != null) {
            onTitleSelectListener.onTitleSelect(titleIndex);
        }
    }

    /**
     * ViewPager选中一页的时候调用
     *
     * @param position
     */
    public void setCurrentItem(int position) {
        if (getChildCount() == 0) {
            return;
        }
        /**
         * 之前的动画如果没执行完,让它结束
         */
        if (timeValueAnimator != null && timeValueAnimator.isRunning()) {
            timeValueAnimator.end();
        }
        if (position == titleIndex) {
            return;
        }
        int lastIndex = titleIndex;
        final TextView preTextView = (TextView) getChildAt(lastIndex);
        final TextView curTextView = (TextView) getChildAt(position);
        timeValueAnimator = ValueAnimator.ofFloat(0.f, 1.f);
        timeValueAnimator.setDuration(300);
        timeValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                /**
                 * 选中的这个放大,之前的那个缩小
                 */
                float preSize = MIN_TITLE_TEXT_SIZE + (MAX_TITLE_TEXT_SIZE - MIN_TITLE_TEXT_SIZE) * (1 - f);
                float curSize = MIN_TITLE_TEXT_SIZE + (MAX_TITLE_TEXT_SIZE - MIN_TITLE_TEXT_SIZE) * f;

                if (f >= 0.5f) {
                    preTextView.setTypeface(Typeface.DEFAULT);
                } else {
                    curTextView.setTypeface(Typeface.DEFAULT_BOLD);
                }
                preTextView.setTextSize(preSize);
                curTextView.setTextSize(curSize);
            }
        });
        timeValueAnimator.start();
        titleIndex = position;
    }
}
