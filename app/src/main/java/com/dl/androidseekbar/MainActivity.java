package com.dl.androidseekbar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.dl.seekbarlib.RangeSeekBarView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RangeSeekBarView mRangeSeekBar;
    private TextView mRangeSeekBarTv;

    private float maxValue = 10;
    private float minValue = 0;

    private float stepLenght = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRangeSeekBar = findViewById(R.id.range_seek_bar);
        mRangeSeekBarTv = findViewById(R.id.range_seek_bar_tv);
        mRangeSeekBar
                .setRangeData(getSeekBarData())
                .setMinValue(minValue)
                .setMaxValue(maxValue)
                .setStepLenght(stepLenght)
                .setOnDragFinishedListener(new RangeSeekBarView.OnDragFinishedListener() {
                    @Override
                    public void dragFinished(float leftValue, float rightValue) {
                        mRangeSeekBarTv.setText(leftValue + "~" + rightValue);
//                        Log.e("123123", "leftValue=" + leftValue + "  rightValue=" + rightValue);
                    }

                }).setOnLayoutLoadCompleteListener(new RangeSeekBarView.OnLayoutLoadCompleteListener() {
            @Override
            public void loadComplete() {
                mRangeSeekBar.setLeftSeekBallValue(0);
                mRangeSeekBar.setRightSeekBallValue(3);
//                mRangeSeekBar.setSeekBarPos(maxValue);
                mRangeSeekBarTv.setText(mRangeSeekBar.getCurrentLeftValue() + "~" + mRangeSeekBar.getCurrentRightValue());
            }
        });

        mRangeSeekBar.setCanEnabled(true);
    }


    public List<String> getSeekBarData() {
        List<String> data = new ArrayList<>();
        for (float i = 0; i + minValue <= maxValue; i += stepLenght) {
            if (i == maxValue) {
                data.add("10以上");
            } else {

                data.add(String.valueOf((int) (i + minValue)));
            }
        }
        return data;
    }
}
