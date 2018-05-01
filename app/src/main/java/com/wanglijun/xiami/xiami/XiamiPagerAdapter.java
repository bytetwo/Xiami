package com.wanglijun.xiami.xiami;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by wanglijun on 2018/4/28.
 */

public class XiamiPagerAdapter extends PagerAdapter{

    private List<XiamiContainer> views;
    private List<String> titls;

    public XiamiPagerAdapter(List<XiamiContainer> views, List<String> titls) {
        this.views = views;
        this.titls = titls;
    }

    @Override
    public int getCount() {
        return views == null ? 0 : views.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titls.get(position);
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // 将当前视图添加到container中
        container.addView(views.get(position));
        // 设置当前视图的唯一标示Key
        return views.get(position);
    }


    public List<XiamiContainer> getViews() {
        return views;
    }

    public List<String> getTitls() {
        return titls;
    }


}
