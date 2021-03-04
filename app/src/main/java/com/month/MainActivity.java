package com.month;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.month.view.ProgressView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ProgressView mProgressView;
    private TextView mTitle1, mTitle2, mTitle3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化组件
        mProgressView = findViewById(R.id.progress_view);
        mTitle1 = findViewById(R.id.title1);
        mTitle2 = findViewById(R.id.title2);
        mTitle3 = findViewById(R.id.title3);

        //设置进度条
        mProgressView.attachViews(mTitle1, mTitle2, mTitle3);
        mProgressView.setProgress(2);
    }

}