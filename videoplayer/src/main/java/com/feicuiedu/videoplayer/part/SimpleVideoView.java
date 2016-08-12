package com.feicuiedu.videoplayer.part;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.feicuiedu.videoplayer.R;
import com.feicuiedu.videoplayer.full.VideoViewActivity;

import java.io.IOException;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;

/**
 * 一个自定义的VideoView,使用MediaPlayer+SurfaceView来实现视频的播放
 * <p/>
 * MediaPlayer来做视频播放的控制，SurfaceView来显示视频
 * <p/>
 * 视图方面将简单实现:放一个播放/暂停按钮，一个进度条,一个全屏按钮,和一个SurfaceView
 * <p/>
 * 本API实现结构：
 * <ul>
 * <li/>提供setVideoPath方法(一定要在onResume方法调用前来调用): 设置播放谁
 * <li/>提供onResume方法(在activity的onResume来调用): 初始化MediaPlayer,准备MediaPlayer
 * <li/>提供onPause方法 (在activity的onPause来调用): 释放MediaPlayer,暂停mediaPlayer
 * </ul>
 * <p/>
 * 作者：yuanchao on 2016/8/10 0010 16:52
 * 邮箱：yuanchao@feicuiedu.com
 */
public class SimpleVideoView extends FrameLayout {

    private String videoPath; //播放资源路径
    //---------------------------相关控件---------------------------//
    private ImageView ivPreview;
    private ImageButton btnToggle;
    private ProgressBar progressBar;
    private ImageButton btnFullScreen;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mediaPlayer;
    private boolean isPrepared; // 当前视频是否已准备好
    private boolean isPlaying; // 当前视频是否正在播放
    private static final int PROGRESSBAR_MAX=1000;

    public SimpleVideoView(Context context) {
        this(context, null);
    }

    public SimpleVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(isPlaying){
                //如果是播放则获取当前进度和总进度
                long newProgress = mediaPlayer.getCurrentPosition();
                long mduration =mediaPlayer.getDuration();
                long progress = newProgress * PROGRESSBAR_MAX / mduration;
                //更新当前进度条
                progressBar.setProgress((int) progress);
                //每200ms更新一次
                handler.sendEmptyMessageDelayed(0,200);
            }
        }
    };

    public SimpleVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    private void init() {
        Vitamio.isInitialized(getContext());
        LayoutInflater.from(getContext()).inflate(R.layout.view_simple_video_player, this, true);
        //初始化surfaceView
        initSurface();
        //初始化控制视图以里面的控件
        initcontrols();
    }

    private void initSurface() {
            surfaceView= (SurfaceView) findViewById(R.id.surfaceView);
            surfaceHolder=surfaceView.getHolder();
            surfaceHolder.setFormat(PixelFormat.RGBA_8888);
    }

    private void initcontrols() {
            //预览图片的
            ivPreview= (ImageView) findViewById(R.id.ivPreview);
            //播放和暂停按钮
           btnToggle = (ImageButton) findViewById(R.id.btnToggle);
        //在开始播放后更新进度
        //以后在每200ms进行一次更新，mediaplayer.getcurrentposiont()当前进度
        //mediaplayer.getduration（）总进度
        //根据结果更新进度条UI
        //用线程
        //思路在开始播放后，handle发一次消息，handle收到处理消息后再发送消息形成一个循环，进度条不能自动更新只能手动更新。
        //进度条相关操作
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //设置progressBar的最大值
        progressBar.setMax(PROGRESSBAR_MAX);
            //全屏按钮
              btnFullScreen = (ImageButton) findViewById(R.id.btnFullScreen);
            //播放和暂停按钮的监听
            btnToggle.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mediaPlayer.isPlaying()){
                        pausMedia();
                        //如果已近准备好了视频播放
                    }else if(isPrepared){
                        startMedia();
                    }
                }
            });
            //全屏按钮的监听
            btnFullScreen.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //走VideoViewActivity类中去调用open方法来进入全屏
                    VideoViewActivity.open(getContext(),videoPath);
                }
            });
    }
    //开始播放Mediaplayer
    private void startMedia() {
        //准备好了才进行播放
        if(isPrepared) {
            mediaPlayer.start();
        }
        isPlaying=true;
        //更新进度条
        handler.sendEmptyMessage(0);
        btnToggle.setImageResource(R.drawable.ic_pause);
    }

    //暂停mediaPlayer更新暂停按钮的图片
    private void pausMedia() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
        //为没有播放了状态
        isPlaying=false;
        //停止handler
        handler.removeMessages(0);
        //变为播放图片
        btnToggle.setImageResource(R.drawable.ic_play_arrow);
    }
    //释放mediaplayer
    public void releaseMedia(){
        mediaPlayer.release();
        mediaPlayer=null;
        isPlaying=false;
        isPrepared=false;
    }
    //初始化MediaPlayer,准备MediaPlayer(和activity的onResume同步执行):
    public void onResume() {
        //初始化mediaplayer
        initMedia();
        //设置资源准备
        prepareMedia();


    }
    //准备Mediaplayer 同时更新
    private void prepareMedia() {
        try {
            //重置mediaplayer
            mediaPlayer.reset();
            //s设置资源
            mediaPlayer.setDataSource(videoPath);
            //循环
            mediaPlayer.isLooping();
            //异步准备
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //初始化mediaplayer
    private void initMedia() {
        mediaPlayer=new MediaPlayer(getContext());
        mediaPlayer.setDisplay(surfaceHolder);
        //设置监听
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //准备好了就开始播放
                isPrepared=true;
                mediaPlayer.start();
            }
        });
        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_INFO_FILE_OPEN_OK) {
                    // 注意：Vitamio5.0 要对音频进行设置才能播放
                    // 否则，不能播放在线视频
                    long bufferSize = mediaPlayer.audioTrackInit();
                    mediaPlayer.audioInitedOk(bufferSize);
                    return true;
                }
                return false;
            }
        });
        //因为视频宽高不一定是匹配的所以调用setOnVideoSizeChangedListener来适配宽和高
        mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                //首先拿到宽和高并且宽可以不用改变只根据宽来改变高就可以了
                int sWidth =surfaceView.getWidth();
                int sHeight =sWidth  * height/width;
                //重置surfaceView的宽和高
                ViewGroup.LayoutParams layoutParams =surfaceView.getLayoutParams();
                layoutParams.width=sWidth;
                layoutParams.height=sHeight;
                surfaceView.setLayoutParams(layoutParams);
            }
        });
    }
    public void onPasuse() {
            //暂停播放
        pausMedia();
        //释放mediaplayer
        releaseMedia();
    }


}