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
    private RangeSeekBarView mRangeSeekBar2;
    private RangeSeekBarView mRangeSeekBar3;
    private RangeSeekBarView mRangeSeekBar4;
    private TextView mRangeSeekBarTv;
    private TextView mRangeSeekBarTv2;
    private TextView mRangeSeekBarTv3;
    private TextView mRangeSeekBarTv4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRangeSeekBar = findViewById(R.id.range_seek_bar);
        mRangeSeekBarTv = findViewById(R.id.range_seek_bar_tv);
        mRangeSeekBar2 = findViewById(R.id.range_seek_bar2);
        mRangeSeekBarTv2 = findViewById(R.id.range_seek_bar_tv2);
        mRangeSeekBar3 = findViewById(R.id.range_seek_bar3);
        mRangeSeekBarTv3 = findViewById(R.id.range_seek_bar_tv3);
        mRangeSeekBar4 = findViewById(R.id.range_seek_bar4);
        mRangeSeekBarTv4 = findViewById(R.id.range_seek_bar_tv4);
        mRangeSeekBar.setRangeData(getSeekBarData())
                .setOnDragFinishedListener(new RangeSeekBarView.OnDragFinishedListener() {
                    @Override
                    public void dragFinished(int leftPostion, int rightPostion, float leftPosRatio, float rightPosRatio) {
//                        mRangeSeekBarTv.setText(getSeekBarData().get(leftPostion) + "~" + getSeekBarData().get(rightPostion));
                        mRangeSeekBarTv.setText((int) Math.round(leftPosRatio * 140) + "~" + (int) Math.round(rightPosRatio * 140));
                    }

                }).setOnLayoutLoadCompleteListener(new RangeSeekBarView.OnLayoutLoadCompleteListener() {
            @Override
            public void loadComplete() {
                mRangeSeekBar.setLeftSeekBallPos(1);
                mRangeSeekBar.setRightSeekBallPos(4);
            }
        });
        mRangeSeekBar2.setRangeData(getSeekBarData())
                .setOnDragFinishedListener(new RangeSeekBarView.OnDragFinishedListener() {
                    @Override
                    public void dragFinished(int leftPostion, int rightPostion, float leftPosRatio, float rightPosRatio) {
//                        mRangeSeekBarTv.setText(getSeekBarData().get(leftPostion) + "~" + getSeekBarData().get(rightPostion));
                        mRangeSeekBarTv2.setText((int) Math.round(leftPosRatio * 140) + "~" + (int) Math.round(rightPosRatio * 140));
                    }

                }).setOnLayoutLoadCompleteListener(new RangeSeekBarView.OnLayoutLoadCompleteListener() {
            @Override
            public void loadComplete() {
                mRangeSeekBar2.setLeftSeekBallPos(1);
                mRangeSeekBar2.setRightSeekBallPos(4);
            }
        });
        mRangeSeekBar3.setRangeData(getSeekBarData())
                .setOnDragFinishedListener(new RangeSeekBarView.OnDragFinishedListener() {
                    @Override
                    public void dragFinished(int leftPostion, int rightPostion, float leftPosRatio, float rightPosRatio) {
//                        mRangeSeekBarTv.setText(getSeekBarData().get(leftPostion) + "~" + getSeekBarData().get(rightPostion));
                        mRangeSeekBarTv3.setText((int) Math.round(leftPosRatio * 140) + "~" + (int) Math.round(rightPosRatio * 140));
                    }

                }).setOnLayoutLoadCompleteListener(new RangeSeekBarView.OnLayoutLoadCompleteListener() {
            @Override
            public void loadComplete() {
                mRangeSeekBar3.setLeftSeekBallPos(1);
                mRangeSeekBar3.setRightSeekBallPos(4);
            }
        });
        mRangeSeekBar4.setRangeData(getSeekBarData())
                .setOnDragFinishedListener(new RangeSeekBarView.OnDragFinishedListener() {
                    @Override
                    public void dragFinished(int leftPostion, int rightPostion, float leftPosRatio, float rightPosRatio) {
//                        mRangeSeekBarTv.setText(getSeekBarData().get(leftPostion) + "~" + getSeekBarData().get(rightPostion));
                        mRangeSeekBarTv4.setText((int) Math.round(leftPosRatio * 140) + "~" + (int) Math.round(rightPosRatio * 140));
                    }

                }).setOnLayoutLoadCompleteListener(new RangeSeekBarView.OnLayoutLoadCompleteListener() {
            @Override
            public void loadComplete() {
                mRangeSeekBar4.setLeftSeekBallPos(1);
                mRangeSeekBar4.setRightSeekBallPos(4);
            }
        });


    }


    public List<String> getSeekBarData() {
        List<String> data = new ArrayList<>();
        data.add("0");
        data.add("20");
        data.add("40");
        data.add("60");
        data.add("80");
        data.add("100");
        data.add("120");
        data.add("不限");
        return data;
    }
}
