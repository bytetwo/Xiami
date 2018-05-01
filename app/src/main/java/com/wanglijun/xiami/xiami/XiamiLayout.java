package com.wanglijun.xiami.xiami;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import com.wanglijun.xiami.R;


/**
 * 外层的布局
 * Created by wanglijun on 2018/4/28.
 */

public class XiamiLayout extends ViewGroup implements ViewPager.OnPageChangeListener, XiamiTitleIndicator.OnTitleSelectListener {
    private static final String TAG = "XiamiLayout";
    /**
     * 背景图-小姐姐
     */
    private ImageView bg;
    /**
     * 上面的四个标题标签
     */
    private XiamiTitleIndicator titleIndicator;
    /**
     * 这个应该都知道吧
     */
    private ViewPager viewPager;
    /**
     * 整个界面能滑动的最大范围,这里默认就是上面的搜索框的高度
     */
    private int maxOffset;
    /**
     * 搜索框
     */
    private View searchView;
    private int layoutWidth, layoutHeight;
    /**
     * 标签控件的margin 的一般,用来实现viewpager和标签控件的重叠效果
     */
    private int titleMarginHalf;
    /**
     * 当前是否是展开状态
     */
    private boolean isExpanded = true;
    /**
     * 全局的一个阻尼效果,暂且设置1
     */
    private static final float XIAMI_LAYOUT_TOUCH_SCALE = 1.f;
    private XiamiPagerAdapter adapter;
    public XiamiLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        titleMarginHalf = ViewUtil.dp2px(context, XiamiTitleMargin.XIAMI_TITLE_MARGIN / 2);
    }
    /**
     * 获取当前是否是展开状态
     *
     * @return
     */
    public boolean isExpanded() {
        return isExpanded;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() < 4) {
            throw new RuntimeException("child view in XiamiLayout must be more than 4!!!");
        }
        /**
         * 为了有些人可能顺序写反了,我们采用findViewById来绑定控件
         */
        bg = findViewById(R.id.iv_xiami_bg);
        searchView = findViewById(R.id.cv_xiami_search);
        titleIndicator = findViewById(R.id.xti_xiami_title);
        viewPager = findViewById(R.id.vp_xiami_content);
        titleIndicator.setOnTitleSelectListener(this);
        viewPager.addOnPageChangeListener(this);
        /**
         * 为了保证效果,这里如果少一个控件我们就炸裂
         */
        if (bg == null || !(bg instanceof ImageView)) {
            throw new RuntimeException("XiamiLayout keep no bg(ImageView)!!!");
        }
        if (searchView == null) {
            throw new RuntimeException("XiamiLayout keep no searchView!!!");
        }
        if (titleIndicator == null || !(titleIndicator instanceof XiamiTitleIndicator)) {
            throw new RuntimeException("XiamiLayout keep no xiamiTitleIndicator(XiamiTitleIndicator)!!!");
        }
        if (viewPager == null || !(viewPager instanceof ViewPager)) {
            throw new RuntimeException("XiamiLayout keep no viewPager(android.support.v4.view.ViewPager)!!!");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /**
         * 虽然自定义控件的时候一定要考虑AT_MOST和EXACTLY两种情况,但是这里我们在高度上都是要把父控件给的参考高度全部用完的,就是这么霸气,所以我们只需要考虑宽度的情况
         */
        int width_size = MeasureSpec.getSize(widthMeasureSpec);
        int height_size = MeasureSpec.getSize(heightMeasureSpec);
        int width_mode = MeasureSpec.getMode(widthMeasureSpec);
        int new_width = 0;
        /**
         * 不管怎样我们只需要这4个控件,如果xml里面多写了几个我们不考虑,所以这里就不要一把measureChildren了
         * 这里主要考虑的是下面这个ViewPager 的高度,因为他可以向上滑动一个searchView的高度所以要考虑进去,所以我们要在下面预留一点高度,那么预留多少呢?
         * 肯定是大于等于searchView 的高度,最好是刚好searchView 的高度,虽然再大一点也可以,但是ViewPager里面的内容最底部就要有空白来填充,所以最好还是searchView 的高度,
         * 当然还要考虑标签控件的半个margin问题,因为要做到重叠
         */

        /**
         * 1.测量bg
         */
        measureChild(bg, widthMeasureSpec, heightMeasureSpec);
        MarginLayoutParams bgParams = (MarginLayoutParams) bg.getLayoutParams();
        if (new_width < bg.getMeasuredWidth() + bgParams.leftMargin + bgParams.rightMargin) {
            new_width = bg.getMeasuredWidth() + bgParams.leftMargin + bgParams.rightMargin;
        }
        /**
         * 2.测量searchView
         */
        measureChild(searchView, widthMeasureSpec, heightMeasureSpec);
        MarginLayoutParams searchParams = (MarginLayoutParams) searchView.getLayoutParams();
        if (new_width < searchView.getMeasuredWidth() + searchParams.leftMargin + searchParams.rightMargin) {
            new_width = searchView.getMeasuredWidth() + searchParams.leftMargin + searchParams.rightMargin;
        }
        /**
         * 3.测量标签
         */
        measureChild(titleIndicator, widthMeasureSpec, heightMeasureSpec);
        MarginLayoutParams titleParams = (MarginLayoutParams) titleIndicator.getLayoutParams();
        if (new_width < titleIndicator.getMeasuredWidth() + titleParams.leftMargin + titleParams.rightMargin) {
            new_width = titleIndicator.getMeasuredWidth() + titleParams.leftMargin + titleParams.rightMargin;
        }
        /**
         * 4.最后处理ViewPager,先确定一下,整个控件能够滑动的范围,我们默认是searchView的区域
         */
        maxOffset = searchView.getMeasuredHeight() + searchParams.topMargin + searchParams.bottomMargin;
        /**
         * ViewPager 的高度就是总的高度减去搜索框的区域高度,减去标签控件的区域高度,加上标签控件的一半margin
         */
        int viewPagerHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height_size - maxOffset + titleMarginHalf, MeasureSpec.EXACTLY);
        measureChild(viewPager, widthMeasureSpec, viewPagerHeightMeasureSpec);
        MarginLayoutParams viewPagerParams = (MarginLayoutParams) viewPager.getLayoutParams();
        if (new_width < viewPager.getMeasuredWidth() + viewPagerParams.leftMargin + viewPagerParams.rightMargin) {
            new_width = viewPager.getMeasuredWidth() + viewPagerParams.leftMargin + viewPagerParams.rightMargin;
        }
        new_width = getPaddingLeft() + getPaddingRight() + new_width;
        if (width_mode == MeasureSpec.EXACTLY) {
            new_width = width_size;
        }
        setMeasuredDimension(new_width, height_size);
    }


    /**
     * 拿到控件的宽度和高度
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layoutWidth = w;
        layoutHeight = h;
    }

    @Override
    protected void onLayout(boolean changed, int ll, int tt, int rr, int bb) {
        int l = 0 + getPaddingLeft();
        int t = 0 + getPaddingTop();
        int r = layoutWidth - getPaddingRight();
        int b = layoutHeight - getPaddingBottom();
        /**
         * 1.布局背景图
         */
        MarginLayoutParams bgParams = (MarginLayoutParams) bg.getLayoutParams();
        int bgLeft = l + bgParams.leftMargin;
        int bgRight = r - bgParams.rightMargin;
        int bgTop = t - bgParams.topMargin;
        int bgBottom = b - bgParams.bottomMargin;
        bg.layout(bgLeft, bgTop, bgRight, bgBottom);
        /**
         * 布局搜索框
         */
        MarginLayoutParams searchParams = (MarginLayoutParams) searchView.getLayoutParams();
        int searchTop = t + searchParams.topMargin;
        int searchBottom = searchTop + searchView.getMeasuredHeight();
        int searchLeft = l + searchParams.leftMargin;
        int searchRight = r - searchParams.rightMargin;
        searchView.layout(searchLeft, searchTop, searchRight, searchBottom);
        /**
         * 布局标签
         */
        MarginLayoutParams titleParams = (MarginLayoutParams) titleIndicator.getLayoutParams();
        int titleTop;
        int titleBottom;
        int titleLeft;
        int titleRight;
        /**
         * 区分展开和折叠时候布局的不同
         */
        if (isExpanded) {
            titleTop = searchBottom + searchParams.bottomMargin + titleParams.topMargin;
        } else {
            titleTop = titleParams.topMargin;
        }
        titleBottom = titleTop + titleIndicator.getMeasuredHeight();
        titleLeft = l + titleParams.leftMargin;
        titleRight = r - titleParams.rightMargin;
        titleIndicator.layout(titleLeft, titleTop, titleRight, titleBottom);
        /**
         * 布局ViewPager
         */
        MarginLayoutParams viewPagerParams = (MarginLayoutParams) viewPager.getLayoutParams();
        int viewPagerTop = titleBottom + titleParams.bottomMargin + viewPagerParams.topMargin - titleMarginHalf;
        int viewPagerBottom = viewPagerTop + viewPager.getMeasuredHeight();
        int viewPagerLeft = l - viewPagerParams.leftMargin;
        int viewPagerRight = r - viewPagerParams.rightMargin;
        viewPager.layout(viewPagerLeft, viewPagerTop, viewPagerRight, viewPagerBottom);
    }


    /**
     * 外部调用的设置适配器的方法,适配器封装过,一把返回标签和视图
     *
     * @param adapter
     */
    public void setAdapter(XiamiPagerAdapter adapter, int position) {
        if (adapter == null) {
            throw new RuntimeException("adapter is null!!");
        }
        this.adapter = adapter;
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
        titleIndicator.setTitles(adapter.getTitls(), position);
        doSelect(position);
    }


    /**
     * 向左滑动的时候positionOffset增大positionOffsetPixels增大
     * 向右滑动positionOffset减小positionOffsetPixels减小
     * 先向左再向右, positionOffset先增大后减小,positionOffsetPixels先增大后减小
     * 先向右再向左, positionOffset先减小后增大, positionOffsetPixels先减小后增大
     * <p>
     * SCROLL_STATE_DRAGGING 1
     * SCROLL_STATE_IDLE = 0;
     * SCROLL_STATE_SETTLING = 2;
     *
     * @param position
     * @param positionOffset
     * @param positionOffsetPixels
     */

    private int state;

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//        Log.i("onPageScrolled", "position:" + position + "=positionOffset:" + positionOffset + "=positionOffsetPixels:" + positionOffsetPixels);
//        Log.i("onPageScrolled", "state:" + state + "=======" + positionOffsetPixels);
//        XiamiContainer curContainer = adapter.getViews().get(viewPager.getCurrentItem());
    }

    @Override
    public void onPageSelected(int p) {
        titleIndicator.setCurrentItem(p);
        doSelect(p);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        this.state = state;
    }


    @Override
    public void onTitleSelect(int position) {
        viewPager.setCurrentItem(position);
        doSelect(position);
    }


    /**
     * 当表情或者ViewPager选中一页的时候调用的方法,
     * 主要设置一些不同页面的差异性,比如是否显示背景图,设置标签颜色什么的
     */
    private void doSelect(int position) {
        if (adapter.getCount() == 0) {
            return;
        }
        if (isExpanded) {
            XiamiContainer container = adapter.getViews().get(position);
            if (container.isSingleView()) {
                bg.setAlpha(0.f);
                titleIndicator.setTextColorMode(XiamiTitleIndicator.TEXT_COLOR_MODE_DARK);
            } else {
                titleIndicator.setTextColorMode(XiamiTitleIndicator.TEXT_COLOR_MODE_LIGHT);
                bg.setAlpha(1.f);
            }
        } else {
            bg.setAlpha(0.f);
            titleIndicator.setTextColorMode(XiamiTitleIndicator.TEXT_COLOR_MODE_DARK);
        }
    }


    private int lastY, lastX;


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (adapter == null) {
            return true;
        }
        if (upValueAnimator != null && upValueAnimator.isRunning()) {
            return true;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = (int) ev.getY();
                lastX = (int) ev.getX();
            case MotionEvent.ACTION_MOVE:
                int offsetX = (int) (ev.getX() - lastX);
                int offsetY = (int) (ev.getY() - lastY);
                XiamiContainer curContainer = adapter.getViews().get(viewPager.getCurrentItem());
                /**
                 * 只对纵向的滑动进行事件的判断
                 */
                if (Math.abs(offsetX) < Math.abs(offsetY)) {
                    /**
                     * 当前XiamiLayout如果是展开状态,必然当前的子view是在top位置的
                     *
                     * 这个时候是可以往上滑也可以往下滑
                     */
                    if (isExpanded) {
                        return true;
                    }
                    /**
                     * 当前XiamiLayout如果是折叠的状态并且子view顶部可见且是向下移动的话,拦截事件
                     *
                     * 这个时候只能往下滑动
                     */
                    if (!isExpanded && curContainer.isTop() && offsetY > 0) {
                        return true;
                    }
                }
                break;
        }
        return false;
    }


    /**
     * 松手时执行的动画,目的是为了松开手时平滑的过度到某个状态
     */
    private ValueAnimator upValueAnimator;
    /**
     * 松手时,背景图当前的alpha
     */
    private float upBgAlpha;
    /**
     * 松手时当前搜索框的alpha
     */
    private float upSearchAlpha;
    /**
     * 上次动画的值
     */
    private float lastAnimationFraction = 0;

    /**
     * 松手的时候,第二个子view 的位置
     */
    private int upChildSecondViewOffset;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (adapter == null) {
            return false;
        }
        if (upValueAnimator != null && upValueAnimator.isRunning()) {
            upValueAnimator.end();
            return true;
        }
        final MarginLayoutParams titleParams = (MarginLayoutParams) titleIndicator.getLayoutParams();
        final MarginLayoutParams viewPagerParams = (MarginLayoutParams) viewPager.getLayoutParams();
        final XiamiContainer curContainer = adapter.getViews().get(viewPager.getCurrentItem());
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int offsetY = (int) (event.getY() - lastY);
                offsetY = (int) (offsetY * XIAMI_LAYOUT_TOUCH_SCALE);
                /**
                 * 标签控件和下面的ViewPager的滑动范围0-searchView.getMeasuredHeight()
                 */
                if (curContainer.isInFirstViewRange()) {
                    /**
                     * 对于背景图的一个矫正,就是如果回到第一个view的范围之内的时候要保证背景图是正常比例的
                     */
                    if (bg.getScaleX() != 1 || bg.getScaleY() != 1) {
                        bg.setScaleX(1);
                        bg.setScaleY(1);
                    }
                    /**
                     * 标签即将到达的位置,为了做到同步效果,只有在子view的第二个view活动在第一个view高度里面才进行标签和viewpager的移动
                     */
                    int titleToTop = titleIndicator.getTop() + offsetY - titleParams.topMargin;
                    if (titleToTop < 0) {
                        Log.i("滑动矫正", "titleToTop < 0");
                        /**
                         * 对于超出顶部的矫正
                         */
                        titleIndicator.layout(titleIndicator.getLeft(), titleParams.topMargin, titleIndicator.getRight(), titleIndicator.getMeasuredHeight());
                        viewPager.layout(viewPager.getLeft(), titleParams.topMargin + titleIndicator.getMeasuredHeight() + titleParams.bottomMargin + viewPagerParams.topMargin - titleMarginHalf
                                , viewPager.getRight(), titleParams.topMargin + titleIndicator.getMeasuredHeight() + titleParams.bottomMargin - titleMarginHalf + viewPagerParams.topMargin + viewPager.getMeasuredHeight());
                    } else if (titleToTop >= 0 && titleToTop <= maxOffset) {
                        Log.i("滑动矫正", "titleToTop >= 0 && titleToTop <= maxOffset");
                        /**
                         * 正常范围之内
                         */
                        titleIndicator.offsetTopAndBottom(offsetY);
                        viewPager.offsetTopAndBottom(offsetY);
                    } else {
                        Log.i("滑动矫正", "超出搜索框的区域范围,进行矫正");
                        /**
                         * 超出搜索框的区域范围,进行矫正
                         */
                        titleIndicator.offsetTopAndBottom(offsetY - titleToTop + maxOffset);
                        viewPager.offsetTopAndBottom(offsetY - titleToTop + maxOffset);
                    }
                } else {
                    /**
                     * 子view对于顶部做了矫正,所以这里只能是在第一个view 的下面,只有子view包含两个视图的时候才能走到这里
                     *
                     * 往下拉的时候进行一个背景图的放大效果,scale这个比值肯定是大于等于1的
                     */
                    float scale = curContainer.getSecondViewRealTop() * 1.f / curContainer.getFirstViewRange();
                    bg.setScaleX(scale);
                    bg.setScaleY(scale);
                }
                curContainer.offsetScaleByMove(maxOffset, offsetY);
                /**
                 * 透明搜索框
                 */
                float scale = (titleIndicator.getTop() - titleParams.topMargin) * 1.f / maxOffset;
                searchView.setAlpha(scale);
                if (!curContainer.isSingleView()) {
                    /**
                     * 如果当前是个可折叠的View,那么需要对字体和背景图进行设置
                     */
                    bg.setAlpha(scale);
                    if (scale > 0.5f) {
                        titleIndicator.setTextColorMode(XiamiTitleIndicator.TEXT_COLOR_MODE_LIGHT);
                    } else {
                        titleIndicator.setTextColorMode(XiamiTitleIndicator.TEXT_COLOR_MODE_DARK);
                    }
                }
                lastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_UP:
                /**
                 * 利用标签的位置来判断松开手的时候是什么状态,这边主要是在松手的时候,处理掉展开还是折叠的问题
                 */
                final int titleRealTop = titleIndicator.getTop() - titleParams.topMargin;
                upBgAlpha = bg.getAlpha();
                upSearchAlpha = searchView.getAlpha();
                if (titleRealTop == 0) {
                    setExpanded(false);
                    Log.i("最后松开的位置", "刚好在顶部");
                } else if (titleRealTop > 0 && titleRealTop < maxOffset / 2) {
                    Log.i("最后松开的位置", "上半部分");
                    /**
                     * 松开的时候,当这个标签滚动到searchView上半部分,那么即将到达的状态是折叠
                     *
                     * 动画的范围    titleRealTop - 0
                     */
                    /**
                     * 就是从现在的位置滑动到顶部
                     */
                    upChildSecondViewOffset = curContainer.getSecondViewRealTop();
                    upValueAnimator = ValueAnimator.ofInt(titleRealTop, 0);
                    upValueAnimator.setDuration(400);
                    upValueAnimator.setInterpolator(new AccelerateInterpolator());
                    upValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int value = (int) animation.getAnimatedValue();
                            int titleTop = value + titleParams.topMargin;
                            int titleBottom = titleTop + titleIndicator.getMeasuredHeight();
                            int viewPagerTop = titleBottom + titleParams.bottomMargin + viewPagerParams.topMargin - titleMarginHalf;
                            int viewPagerBottom = viewPagerTop + viewPager.getMeasuredHeight();
                            titleIndicator.layout(titleIndicator.getLeft(), titleTop, titleIndicator.getRight(), titleBottom);
                            viewPager.layout(titleIndicator.getLeft(), viewPagerTop, titleIndicator.getRight(), viewPagerBottom);
                            searchView.setAlpha(upSearchAlpha * (1 - animation.getAnimatedFraction()));
                            if (!curContainer.isSingleView()) {

                                bg.setAlpha(upBgAlpha * (1 - animation.getAnimatedFraction()));
                            }
                            int childSecondViewOffset = (int) (upChildSecondViewOffset * (animation.getAnimatedFraction() - lastAnimationFraction));
                            curContainer.offsetScaleByUp(maxOffset, -childSecondViewOffset);
                            lastAnimationFraction = animation.getAnimatedFraction();
                        }
                    });
                    upValueAnimator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            lastAnimationFraction = 0;
                            setExpanded(false);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    upValueAnimator.start();


                } else if (titleRealTop >= maxOffset / 2 && titleRealTop < maxOffset) {
                    Log.i("最后松开的位置", "下半部分");
                    /**
                     * 松开的时候,标签滚动在searchView下半部分那么,即将到达的状态是展开
                     */
                    upChildSecondViewOffset = curContainer.getFirstViewRange() - curContainer.getSecondViewRealTop();
                    upValueAnimator = ValueAnimator.ofInt(titleRealTop, maxOffset);
                    upValueAnimator.setDuration(400);
                    upValueAnimator.setInterpolator(new AccelerateInterpolator());
                    upValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int value = (int) animation.getAnimatedValue();
                            int titleTop = value + titleParams.topMargin;
                            int titleBottom = titleTop + titleIndicator.getMeasuredHeight();
                            int viewPagerTop = titleBottom + titleParams.bottomMargin + viewPagerParams.topMargin - titleMarginHalf;
                            int viewPagerBottom = viewPagerTop + XiamiLayout.this.viewPager.getMeasuredHeight();
                            titleIndicator.layout(titleIndicator.getLeft(), titleTop, titleIndicator.getRight(), titleBottom);
                            XiamiLayout.this.viewPager.layout(titleIndicator.getLeft(), viewPagerTop, titleIndicator.getRight(), viewPagerBottom);
                            searchView.setAlpha(upSearchAlpha + (1 - upSearchAlpha) * animation.getAnimatedFraction());
                            if (!curContainer.isSingleView()) {
                                bg.setAlpha(upBgAlpha + (1 - upBgAlpha) * animation.getAnimatedFraction());
                            }
                            int childSecondViewOffset = (int) (upChildSecondViewOffset * (animation.getAnimatedFraction() - lastAnimationFraction));
                            curContainer.offsetScaleByUp(maxOffset, childSecondViewOffset);
                            lastAnimationFraction = animation.getAnimatedFraction();
                        }
                    });
                    upValueAnimator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            lastAnimationFraction = 0;
                            setExpanded(true);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    upValueAnimator.start();
                } else {
                    Log.i("最后松开的位置", "第一个子view 的下面部分");
                    /**
                     * 松开的时候,标签刚好是在展开状态的位置,那么这个时候有可能是往下滑动的
                     */
                    if (titleRealTop == maxOffset) {
                        setExpanded(true);
                    } else {

                    }
                }
                break;
        }
        return true;
    }

    /**
     * 设置展开或者折叠的状态
     *
     * @param expanded
     */
    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
        /**
         * ViewPager所有的子view的折叠状态都要统一
         */
        XiamiPagerAdapter adapter = (XiamiPagerAdapter) viewPager.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            XiamiContainer container = adapter.getViews().get(i);
            container.setExpanded(expanded);
        }

        /**
         * 主要设置一下背景图是否要隐藏,搜索框是否要隐藏,标签空的文字是否需要变色
         */
        XiamiContainer container = adapter.getViews().get(viewPager.getCurrentItem());
        if (expanded && container.isSingleView()) {
            /**
             * 展开状态,子view不能折叠
             */
            searchView.setAlpha(1);
            bg.setAlpha(0.f);
            titleIndicator.setTextColorMode(XiamiTitleIndicator.TEXT_COLOR_MODE_DARK);
        } else if (expanded && !container.isSingleView()) {
            /**
             * 展开状态,子view能折叠
             */
            searchView.setAlpha(1);
            bg.setAlpha(1.f);
            titleIndicator.setTextColorMode(XiamiTitleIndicator.TEXT_COLOR_MODE_LIGHT);
        } else {
            /**
             * 折叠状态,不管子view是怎样的折叠状态都是一样的
             */
            searchView.setAlpha(0);
            bg.setAlpha(0.f);
            titleIndicator.setTextColorMode(XiamiTitleIndicator.TEXT_COLOR_MODE_DARK);
        }

        /**
         * 对于背景图的一个矫正
         */
        if (bg.getScaleX() != 1 || bg.getScaleY() != 1) {
            bg.setScaleX(1);
            bg.setScaleY(1);
        }
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
