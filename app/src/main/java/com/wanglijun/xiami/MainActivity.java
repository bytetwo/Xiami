package com.wanglijun.xiami;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;


import com.wanglijun.xiami.xiami.XiamiContainer;
import com.wanglijun.xiami.xiami.XiamiLayout;
import com.wanglijun.xiami.xiami.XiamiPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private XiamiLayout xiamiLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        xiamiLayout = findViewById(R.id.xiamilayout);



        List<XiamiContainer> views = new ArrayList<>();


        XiamiContainer viwe1 = (XiamiContainer) LayoutInflater.from(this).inflate(R.layout.view1, null);
        XiamiContainer viwe2 = (XiamiContainer) LayoutInflater.from(this).inflate(R.layout.view2, null);
        XiamiContainer viwe3 = (XiamiContainer) LayoutInflater.from(this).inflate(R.layout.view3, null);
        XiamiContainer viwe4 = (XiamiContainer) LayoutInflater.from(this).inflate(R.layout.view4, null);

        views.add(viwe1);
        views.add(viwe2);
        views.add(viwe3);
        views.add(viwe4);

        List<String> titles = new ArrayList<>();

        titles.add("乐库");
        titles.add("推荐");
        titles.add("趴间");
        titles.add("看点");


        XiamiPagerAdapter xiamiPagerAdapter = new XiamiPagerAdapter(views, titles);

        xiamiLayout.setAdapter(xiamiPagerAdapter,0);

        xiamiLayout.setExpanded(true);
    }
}
