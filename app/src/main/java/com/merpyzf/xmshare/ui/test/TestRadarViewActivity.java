package com.merpyzf.xmshare.ui.test;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.merpyzf.radarview.RadarLayout;
import com.merpyzf.xmshare.R;

public class TestRadarViewActivity extends AppCompatActivity {

    private RadarLayout radar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_radar_view);

        radar = findViewById(R.id.radar);
        radar.setDuration(2000);
        radar.setStyleIsFILL(false);
        radar.setRadarColor(Color.GRAY);
        radar.start();



    }
}
