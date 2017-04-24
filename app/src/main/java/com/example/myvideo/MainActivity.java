package com.example.myvideo;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import static com.example.myvideo.R.id.time_current_tv;
import static com.example.myvideo.R.id.videoView;

public class MainActivity extends AppCompatActivity {
    private VideoView videoView;
    private LinearLayout controlerlayout;
    private ImageView play_controller_img,screen_img,volume_img;
    private TextView time_current_tv,time_total_tv;
    private SeekBar play_seek,volume_seek;
    public static final int UPDATE_UI=1;
    private int screen_width,screen_height;
    private RelativeLayout videoLayout;
    private AudioManager mAudioManager;
    private boolean isFullScreen=false;
    private boolean isAdjust=false;
    private  int threshold=54;
    private float lastx=0,lasty=0;
    private float mBrightNess;
    private ImageView operation_bg,operation_percent;
    private FrameLayout progress_layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAudioManager=(AudioManager) getSystemService(AUDIO_SERVICE);//获取音频服务
        initUI();
        setPlayerEvent();
        String path= Environment.getExternalStorageDirectory().getAbsolutePath();
        //videoView =(VideoView) findViewById(R.id.videoView);
        /**
         * 本地视频播放
         */
        videoView.setVideoPath("path");
        videoView.start();
        UIHandler.sendEmptyMessage(UPDATE_UI);


        /**
         * 网络视频播放
         */
        //videoView.setVideoURI(Uri.parse(""));
    }
    private void updateTxtViewWithTimeFormat(TextView textView,int millisecond){
        int second = millisecond/1000;
        int hh = second/3600;
        int mm = second%3600/60;
        int ss = second%60;
        String str=null;
        if(hh!=0)
        {
            str=String.format("%02d:%02d:%02d",hh,mm,ss);
        }
        else
        {
            str=String.format("%02d:%02d",mm,ss);
        }
        textView.setText(str);
    }


private Handler UIHandler = new Handler(){
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if(msg.what==UPDATE_UI) {
            //获取视频当前的播放时间
            int currentPosition = videoView.getCurrentPosition();
            //获取视频播放的总时间
            int totalduration = videoView.getDuration();
            //格式化视频播放时间
            updateTxtViewWithTimeFormat(time_current_tv, currentPosition);
            updateTxtViewWithTimeFormat(time_total_tv, totalduration);

            play_seek.setMax(totalduration);
            play_seek.setProgress(currentPosition);
            UIHandler.sendEmptyMessageDelayed(UPDATE_UI, 500);//500毫秒
        }

    }
};

    @Override
    protected void onPause() {
        super.onPause();
        UIHandler.removeMessages(UPDATE_UI);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setPlayerEvent() {
    /**
     * 控制视频播放与暂停
     */
    play_controller_img.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (videoView.isPlaying()) {
                play_controller_img.setImageResource(R.drawable.video_stop_style);
                //暂停播放
                videoView.pause();
                UIHandler.removeMessages(UPDATE_UI);
            } else {
                play_controller_img.setImageResource(R.drawable.video_start_style);
                //继续播放
                videoView.start();
                UIHandler.sendEmptyMessage(UPDATE_UI);
            }

        }
    });
        play_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTxtViewWithTimeFormat(time_current_tv,progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                UIHandler.removeMessages(UPDATE_UI);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress=seekBar.getProgress();
                //另视频播放进度遵循seekbar停止拖动的这一刻的进度
                videoView.seekTo(progress);
                UIHandler.sendEmptyMessage(UPDATE_UI);
            }
        });

        volume_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                /**
                 * 设置当前设备的音量
                 */
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);//（类型，音量大小，标记）
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        /**
         * 横竖屏切换
         */
        screen_img.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
              if(isFullScreen)
              {
                  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

              }
                else
              {
                  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
              }
            }
        });
        /**
         * 控制VideoView的手势事件
         */
        videoView.setOnTouchListener(new  View.OnTouchListener() {
        @Override
            public boolean onTouch(View v, MotionEvent event){
            float x=event.getX();
            float y=event.getY();
            switch (event.getAction())
            {
                /**
                 * 手指落下屏幕的那一刻（只会调用一次）
                 */
                case MotionEvent.ACTION_DOWN:
                {
                    lastx = x;
                    lasty = y;
                    break;
                }
                /**
                 * 手指在屏幕上移动（调用多次）
                 */
                case MotionEvent.ACTION_MOVE:
                {
                    float detlaX=x-lastx;
                    float detlaY=y-lasty;
                    float absdetlaX=Math.abs(detlaX);
                    float absdetlaY=Math.abs(detlaY);
                    /**
                     * 手势合法性的验证
                     */
                    if(absdetlaX>threshold&&absdetlaY>threshold)
                    {
                        if(absdetlaX<absdetlaY) {
                            isAdjust=true;
                        }
                        else {
                            isAdjust=false;
                        }

                    } else if(absdetlaX<threshold && absdetlaY>threshold){
                        isAdjust=true;

                    } else if(absdetlaX>threshold&&absdetlaY<threshold){
                        isAdjust=false;
                    }
                    Log.e("Main","手势是否合法"+isAdjust);
                    if (isAdjust)
                    {
                        if (x < screen_width / 2)
                        {
                            //调节亮度
                            if (detlaY > 0)
                            {
                                //降低亮度
                                Log.d("MainActivity" , detlaY+ "");
                            }
                            else
                            {
                                //调高亮度
                                Log.d("MainActivity" , detlaY + "");
                            }
                            changeBrightNess(-detlaY);
                        }
                        else
                        {
                            /**
                             * 调节音量
                             */
                            if (detlaY > 0)
                            {
                                //减小音量
                                Log.d("MainActivity" , detlaY + "");
                            }
                            else
                            {
                                //增大音量
                                Log.d("MainActivity" , detlaY + "");
                            }
                            changeVolume(-detlaY);
                        }
                    }
                    lastx=x;
                    lasty=y;

                    break;
                }
                /**
                 * 手指离开屏幕的那一刻（只调用一次）
                 */
                case MotionEvent.ACTION_UP:
                {
                    progress_layout.setVisibility(videoView.GONE);
                    break;
                }
            }
            return true;
        }
        });
    }
    private void changeVolume(float detlaY)
    {
        int max=mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current=mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int index=(int)(detlaY/screen_height*max*3);
        int volume=Math.max(current+index,0);
        volume_seek.setProgress(volume);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC , volume , 0);

        if (progress_layout.getVisibility() == View.GONE)
        {
            progress_layout.setVisibility(View.VISIBLE);
        }
        operation_bg.setImageResource(R.mipmap.video_voice_bg);
        ViewGroup.LayoutParams layoutParams = operation_percent.getLayoutParams();
        layoutParams.width = (int) (PixeUtil.dp2px(this , 94f) * (float) volume / max);
        operation_percent.setLayoutParams(layoutParams);


    }

    private void changeBrightNess(float detlaY)
    {
        WindowManager.LayoutParams attribute = getWindow().getAttributes();
        mBrightNess = attribute.screenBrightness;
        Log.d("mBrightness" , mBrightNess + "");
        float index = detlaY / screen_height/3;
        mBrightNess += index;

        if (mBrightNess > 1.0f)
        {
            mBrightNess = 1.0f;
        }

        if (mBrightNess < 0.01f)
        {
            mBrightNess = 0.01f;
        }

        attribute.screenBrightness = mBrightNess;
        if (progress_layout.getVisibility() == View.GONE)
        {
            progress_layout.setVisibility(View.VISIBLE);
        }
        operation_bg.setImageResource(R.mipmap.video_brightness_bg);
        ViewGroup.LayoutParams layoutParams = operation_percent.getLayoutParams();
        layoutParams.width = (int) (PixeUtil.dp2px(this , 94f) * mBrightNess);
        operation_percent.setLayoutParams(layoutParams);
        getWindow().setAttributes(attribute);


    }
    /**
     * 初始化UI布局
     */
    private void initUI(){
       // PixeUtil.initContext(this);
        videoView =(VideoView) findViewById(R.id.videoView);
        controlerlayout = (LinearLayout) findViewById(R.id.controlerbar_layout);
        play_controller_img = (ImageView) findViewById(R.id.pause_img);
        screen_img = (ImageView) findViewById(R.id.screen_img);
        time_current_tv = (TextView) findViewById(R.id.time_current_tv);
        time_total_tv = (TextView) findViewById(R.id.time_total_tv);
        play_seek = (SeekBar) findViewById(R.id.play_seek);
        volume_seek =(SeekBar) findViewById(R.id.volume_seek);
        volume_img=(ImageView) findViewById(R.id.volume_img);
        videoLayout=(RelativeLayout) findViewById(R.id.videoLayout);
        screen_width=getResources().getDisplayMetrics().widthPixels;
        screen_height=getResources().getDisplayMetrics().heightPixels;
       operation_bg=(ImageView)findViewById(R.id.operation_bg);
        operation_percent=(ImageView)findViewById(R.id.operation_percent);
        progress_layout=(FrameLayout)findViewById(R.id.progress_layout);
        /**
         * 当前设备的最大音量
         */
        int streamMaxVolume=mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        /**
         * 获取设备当前音量
         */
        int streamVolume=mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume_seek.setMax(streamMaxVolume);
        volume_seek.setProgress(streamVolume);

    }
    private void setVideoViewScale(int width,int height){
        ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
        layoutParams.width=width;
        layoutParams.height=height;
        videoView.setLayoutParams(layoutParams);

        ViewGroup.LayoutParams layoutParams1 = videoLayout.getLayoutParams();
        layoutParams1.width=width;
        layoutParams1.height=height;
        videoLayout.setLayoutParams(layoutParams1);
    }

    /**
     * 监听到屏幕方向的改变
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /**
         * 当屏幕方向为横屏的时候
         */
        if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE)
        {
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            volume_img.setVisibility(View.VISIBLE);//可见
            volume_seek.setVisibility(View.VISIBLE);
            isFullScreen=true;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);//全屏黑边问题
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        /**
         * 当屏幕方向为竖屏的时候
         */
        else
        {
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT,PixeUtil.dp2px(this,240));
            volume_img.setVisibility(View.GONE);
            volume_seek.setVisibility(View.GONE);
            isFullScreen=false;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }
}
