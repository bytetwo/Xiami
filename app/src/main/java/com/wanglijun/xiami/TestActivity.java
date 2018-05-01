package com.wanglijun.xiami;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.wanglijun.xiami.xiami.XiamiContainer;
import com.wanglijun.xiami.xiami.XiamiLayout;
import com.wanglijun.xiami.xiami.XiamiPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        ViewPager viewPager = findViewById(R.id.viewpager);


        List<View> views = new ArrayList<>();


        View viwe1 =  new TestViewGroup(this);
        View viwe2 =   new TestViewGroup(this);
        View viwe3 =   new TestViewGroup(this);
        View viwe4 =   new TestViewGroup(this);

        views.add(viwe1);
        views.add(viwe2);
        views.add(viwe3);
        views.add(viwe4);




        TestPagerAdapter xiamiPagerAdapter = new TestPagerAdapter(views);

        viewPager.setAdapter(xiamiPagerAdapter);
    }
}
