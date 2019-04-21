package com.dl.androidseekbar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.dl.seekbarlib.RangeSeekBarView;
import com.dl.seekbarlib.SingleSeekBarView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RangeSeekBarView mRangeSeekBar;
    private SingleSeekBarView mSingleSeekBar;
    private TextView mRangeSeekBarTv;
    private TextView mSingleSeekBarTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRangeSeekBar = findViewById(R.id.range_seek_bar);
        mRangeSeekBarTv = findViewById(R.id.range_seek_bar_tv);
        mSingleSeekBar = findViewById(R.id.single_seek_bar);
        mSingleSeekBarTv = findViewById(R.id.single_seek_bar_tv);
        mRangeSeekBar.setRangeData(getSeekBarData())
                .setOnDragFinishedListener(new RangeSeekBarView.OnDragFinishedListener() {
                    @Override
                    public void dragFinished(int leftPostion, int rightPostion) {
                        Log.e(TAG, "leftPostion=" + leftPostion + "   rightPostion=" + rightPostion);
                        mRangeSeekBarTv.setText(getSeekBarData().get(leftPostion) + "~" + getSeekBarData().get(rightPostion));
                    }
                }).setOnLayoutLoadCompleteListener(new RangeSeekBarView.OnLayoutLoadCompleteListener() {
            @Override
            public void loadComplete() {
                mRangeSeekBar.setLeftSeekBallPos(1);
                mRangeSeekBar.setRightSeekBallPos(4);
            }
        });
        mSingleSeekBar.setRangeData(getSeekBarData())
                .setOnDragFinishedListener(new SingleSeekBarView.OnDragFinishedListener() {
                    @Override
                    public void dragFinished(int position) {
                        Log.e(TAG, "position=" + position);
                        mSingleSeekBarTv.setText(getSeekBarData().get(position));
                    }

                }).setOnLayoutLoadCompleteListener(new SingleSeekBarView.OnLayoutLoadCompleteListener() {
            @Override
            public void loadComplete() {
                mSingleSeekBar.setSeekBallPos(3);
            }
        })
        ;
    }


    public List<String> getSeekBarData() {
        List<String> data = new ArrayList<>();
        data.add("0");
        data.add("20");
        data.add("40");
        data.add("60");
        data.add("80");
        data.add("100");
        data.add("不限");
        return data;
    }
}
