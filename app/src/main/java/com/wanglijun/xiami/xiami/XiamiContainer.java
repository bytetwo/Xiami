package com.wanglijun.xiami.xiami;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * 用于ViewPager子View的父布局,有两种布局的方式,一种是直接放一个ScrollView,
 * 一种是上面放一个Banner下面一个ScrollView,(目前只做了对SCrollView 的支持,没有做ListView或者RecyclerView)
 * 这个布局的所有操作基于xml文件中只放一个或者两个view
 * <p>
 * <p>
 * Created by wanglijun on 2018/4/28.
 */

public class XiamiContainer extends ViewGroup {
    private static final String TAG = "XiamiContainer";
    /**
     * 控件的宽和高
     */
    private int containerWidth;
    private int containerHeight;
    /**
     * 第一个子View的topMargin和bottomMargin,方便后面的调用
     */
    private int firstViewMarginTop;
    private int firstViewMarginBottom;
    /**
     * 第一个View的区域高度+topMargin+bottomMargin
     */
    private int firstViewRange;
    /**
     * 如果是两个View的情况,记录当前是不是展开的状态
     */
    private boolean isExpanded = true;
    /**
     * 阻尼
     */
    private static final float TOUCH_SCALE = 0.4f;
    public XiamiContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /**
         * 这里也是一样,我们在高度上都是要把父控件给的参考高度全部用完的
         */
        int width_size = MeasureSpec.getSize(widthMeasureSpec);
        int height_size = MeasureSpec.getSize(heightMeasureSpec);
        int width_mode = MeasureSpec.getMode(widthMeasureSpec);
        int new_width = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            measureChild(view, widthMeasureSpec, heightMeasureSpec);
            MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
            if (i == 0) {
                firstViewMarginTop = params.topMargin;
                firstViewMarginBottom = params.bottomMargin;
                firstViewRange = firstViewMarginTop + firstViewMarginBottom + view.getMeasuredHeight();
            }
            if (new_width < params.leftMargin + view.getMeasuredWidth() + params.rightMargin) {
                new_width = params.leftMargin + view.getMeasuredWidth() + params.rightMargin;
            }
        }
        new_width = getPaddingLeft() + getPaddingRight() + new_width;
        if (width_mode == MeasureSpec.EXACTLY) {
            new_width = width_size;
        }
        setMeasuredDimension(new_width, height_size);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 2) {
            throw new RuntimeException("XiamiContainer's count can't be more than 2!!!!");
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        containerWidth = w;
        containerHeight = h;
    }

    @Override
    protected void onLayout(boolean changed, int ll, int tt, int rr, int bb) {
        if (getChildCount() == 0) {
            return;
        }
        int l = 0 + getPaddingLeft();
        int t = 0 + getPaddingTop();
        int r = containerWidth - getPaddingRight();
        int b = containerHeight - getPaddingBottom();
        /**
         * 布局只有一个子View 的情况
         */
        if (getChildCount() == 1) {
            View view1 = getChildAt(0);
            MarginLayoutParams view1Params = (MarginLayoutParams) view1.getLayoutParams();
            int view1Top = t + view1Params.topMargin;
            int view1Bttom = view1Top + view1.getMeasuredHeight();
            int view1Left = l + view1Params.leftMargin;
            int view1Right = r - view1Params.rightMargin;
            view1.layout(view1Left, view1Top, view1Right, view1Bttom);
        }

        /**
         * 布局有两个子View 的情况,通过展开的标记位来决定,如果是展开的,那么就放在第一个View 的下面,如果是折叠的就重叠在第一个View 的上面
         */
        if (getChildCount() == 2) {
            View view1 = getChildAt(0);
            View view2 = getChildAt(1);
            MarginLayoutParams view1Params = (MarginLayoutParams) view1.getLayoutParams();
            MarginLayoutParams view2Params = (MarginLayoutParams) view2.getLayoutParams();
            int view1Top = t + view1Params.topMargin;
            int view1Bottom = view1Top + view1.getMeasuredHeight();
            int view1Left = l + view1Params.leftMargin;
            int view1Right = r - view1Params.rightMargin;
            view1.layout(view1Left, view1Top, view1Right, view1Bottom);
            int view2Top;
            if (isExpanded) {
                view2Top = view1Bottom + view1Params.bottomMargin + view2Params.topMargin;
            } else {
                view2Top = view2Params.topMargin;
            }
            int view2Bottom = view2Top + view2.getMeasuredHeight();
            int view2Left = l + view2Params.leftMargin;
            int view2Right = r - view2Params.rightMargin;
            view2.layout(view2Left, view2Top, view2Right, view2Bottom);
        }
    }


    /**
     * 是不是只有一个子view,也就是看看是不是可折叠
     *
     * @return
     */
    public boolean isSingleView() {
        return getChildCount() == 1;
    }


    /**
     * 获取是不是展开的状态
     *
     * @return
     */
    public boolean isExpanded() {
        return isExpanded;
    }

    /**
     * 设置是否展开
     *
     * @param expanded
     */
    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
        /**
         * 如果要展开那么就要滚动到顶部
         */
        if (expanded) {
            if (isSingleView()) {
                getChildAt(0).scrollTo(0, 0);
            } else {
                getChildAt(1).scrollTo(0, 0);
            }
        }
        /**
         * 请求重新布局
         */
        if (!isSingleView()) {
            requestLayout();
        }
    }


    /**
     * 如果是两个子View 的情况,判断第二个View的顶部,是不是在第一个View 的高度范围之内,
     * 这个是看了虾米音乐的效果做的方法
     *
     * @return
     */
    public boolean isInFirstViewRange() {
        if (getChildCount() == 1) {
            return true;
        } else {
            View secondView = getChildAt(1);
            MarginLayoutParams secondParams = (MarginLayoutParams) secondView.getLayoutParams();
            return secondView.getTop() - secondParams.topMargin < firstViewRange;
        }
    }


    /**
     * 被XiamiLayout调用,用来进行折叠的联动,该方法被move事件调用
     *
     * @param layoutMaxOffset 是XiamiLayout搜索框的高度,
     * @param offsetY         一次MOVE事件的偏移值
     */
    public void offsetScaleByMove(int layoutMaxOffset, int offsetY) {
        /**
         * 折叠效果一定是在两个子View 的情况下进行的
         */
        if (getChildCount() == 2) {
            View secondView = getChildAt(1);
            MarginLayoutParams secondParams = (MarginLayoutParams) secondView.getLayoutParams();
            /**
             * 利用传过来的搜索框的高度,计算一个比值,因为搜索框的高度和第一个子view 的高度是不一样的,要做到同步折叠,需要处理一下
             */
            float scale = firstViewRange * 1.f / layoutMaxOffset;
            /**
             * 利用比值我们计算一下需要偏移的数值
             */
            int secondOffsetY = (int) (offsetY * scale);
            /**
             * 偏移后第二个view可能到达的位置,为什么说可能因为,滑动是有范围的,如果超出了范围,我们需要矫正回来的
             */
            int secondToTop = secondView.getTop() + secondOffsetY - secondParams.topMargin;
            int realOffset;
            if (secondToTop < 0) {
                /**
                 * 对于超出顶部的矫正
                 */
                realOffset = secondOffsetY - secondToTop;
            } else {
                if (secondToTop > firstViewRange) {
                    /**
                     *偏移到了第一个view 的下面
                     */
                    if (offsetY < 0) {
                        /**
                         * 往上滑动,尽可能快的回到第一个View 的范围去,不要使用阻尼系数
                         */
                        realOffset = offsetY;
                    } else {
                        /**
                         * 继续往下滑动,设置一个阻尼,不要滑动的太过分
                         */
                        realOffset = (int) (offsetY * TOUCH_SCALE);
                    }
                } else {
                    /**
                     * 在第一个view的范围之内
                     */
                    realOffset = secondOffsetY;
                }
            }
            secondView.offsetTopAndBottom(realOffset);
        }
    }


    /**
     * 被XiamiLayout调用,用来进行折叠的联动,该方法被up事件动画调用
     *
     * @param layoutMaxOffset 是XiamiLayout搜索框的高度,
     * @param offsetY         一次MOVE事件的偏移值
     */
    public void offsetScaleByUp(int layoutMaxOffset, int offsetY) {
        /**
         * 折叠效果一定是在两个子View 的情况下进行的
         */
        if (getChildCount() == 2) {
            View secondView = getChildAt(1);
            MarginLayoutParams secondParams = (MarginLayoutParams) secondView.getLayoutParams();
            /**
             * 利用传过来的搜索框的高度,计算一个比值,因为搜索框的高度和第一个子view 的高度是不一样的,要做到同步折叠,需要处理一下
             */
            float scale = firstViewRange * 1.f / layoutMaxOffset;
            /**
             * 利用比值我们计算一下需要偏移的数值
             */
            int secondOffsetY = (int) (offsetY * scale);
            /**
             * 偏移后第二个view可能到达的位置,为什么说可能因为,滑动是有范围的,如果超出了范围,我们需要矫正回来的
             */
            int secondToTop = secondView.getTop() + secondOffsetY - secondParams.topMargin;
            int realOffset;
            if (secondToTop < 0) {
                /**
                 * 对于超出顶部的矫正
                 */
                realOffset = secondOffsetY - secondToTop;
            } else if (secondToTop > firstViewRange) {
                realOffset = secondOffsetY - (secondToTop - firstViewRange);
            } else {
                realOffset = secondOffsetY;
            }
            secondView.offsetTopAndBottom(realOffset);
        }
    }


    /**
     * 获取第二个view的区域顶部
     *
     * @return
     */
    public int getSecondViewRealTop() {
        if (getChildCount() == 1) {
            return 0;
        }
        View view = getChildAt(1);
        MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
        return view.getTop() - params.topMargin;
    }


    /**
     * 获取第一个子view 的高度
     *
     * @return
     */
    public int getFirstViewRange() {
        return firstViewRange;
    }


    /**
     * 是否顶部可见,顶部可见的时候,如果XiamiLayout是展开的状态,那么XiamiLayout可以往上折叠,
     * 也就是说,只有XiamiLayout是折叠的状态,ViewPager子View才能进行滚动操作
     * 主要是判断里面的可以滚动的View是不是在顶部
     *
     * @return
     */
    public boolean isTop() {
        if (getChildCount() == 1) {
            View view = getChildAt(0);
            if (view instanceof ScrollView) {
                return view.getScrollY() == 0;
            } else if (view instanceof NestedScrollView) {
                return view.getScrollY() == 0;
            } else {
                return true;
            }
        }
        if (getChildCount() == 2) {
            View view = getChildAt(1);
            if (view instanceof ScrollView) {
                return view.getScrollY() == 0;
            } else if (view instanceof NestedScrollView) {
                return view.getScrollY() == 0;
            } else {
                return true;
            }
        }
        return false;
    }


    /* 生成默认的LayoutParams */

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }


    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }


    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }


}
