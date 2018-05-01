package com.wanglijun.xiami;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.wanglijun.xiami.xiami.XiamiContainer;

import java.util.List;

/**
 * Created by wanglijun on 2018/4/28.
 */

public class TestPagerAdapter extends PagerAdapter{

    private List<View> views;

    public TestPagerAdapter(List<View> views) {
        this.views = views;
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





}
