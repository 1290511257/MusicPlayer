package com.mbwr.xx.littlerubbishmusicplayer.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mbwr.xx.littlerubbishmusicplayer.R;


public class seekbar extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "=================";

    // 与“系统默认SeekBar”对应的TextView
    private TextView mTvDef;
    // 与“自定义SeekBar”对应的TextView
    private TextView mTvSelf;
    // “系统默认SeekBar”
    private SeekBar mSeekBarDef;
    // “自定义SeekBar”
    private SeekBar mSeekBarSelf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seekbar);

        // 与“系统默认SeekBar”对应的TextView
        mTvDef = findViewById(R.id.tv_def);
        // “系统默认SeekBar”
        mSeekBarDef = findViewById(R.id.seekbar_def);
        mSeekBarDef.setOnSeekBarChangeListener(this);

        // 与“自定义SeekBar”对应的TextView
        mTvSelf = findViewById(R.id.tv_self);
        // “自定义SeekBar”
        mSeekBarSelf = findViewById(R.id.seekbar_self);
        mSeekBarSelf.setOnSeekBarChangeListener(this);
    }

    /*
     * SeekBar停止滚动的回调函数
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(TAG,"onStopTrackingTouch");
    }

    /*
     * SeekBar开始滚动的回调函数
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d(TAG,"onStartTrackingTouch");
    }

    /*
     * SeekBar滚动时的回调函数
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        Log.d(TAG, "seekid:"+seekBar.getId()+", progess = "+progress);

        switch(seekBar.getId()) {
            case R.id.seekbar_def:{
                // 设置“与系统默认SeekBar对应的TextView”的值
                mTvDef.setText(getResources().getString(R.string.text_def)+" : "+String.valueOf(seekBar.getProgress()));
                break;
            }
            case R.id.seekbar_self: {
                // 设置“与自定义SeekBar对应的TextView”的值
                mTvSelf.setText(getResources().getString(R.string.text_self)+" : "+String.valueOf(seekBar.getProgress()));
                break;
            }
            default:
                break;
        }
    }
}
