package com.feicuiedu.videoplayer.full;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import com.feicuiedu.videoplayer.R;

import io.vov.vitamio.widget.MediaController;

/**
 * 继承{@link MediaController}，实现自定义的视频播放控制器。
 * <p/>
 * 重写{@link #makeControllerView()}方法，提供自定义的视图，视图规则如下：
 * <ul>
 * <li/>SeekBar的id必须是mediacontroller_seekbar
 * <li/>播放/暂停按钮的id必须是mediacontroller_play_pause
 * <li/>当前时间的id必须是mediacontroller_time_current
 * <li/>总时间的id必须是mediacontroller_time_total
 * <li/>视频名称的id必须是mediacontroller_file_name
 * <li/>drawable资源中必须有pause_button和play_button
 * </ul>
 * <p/>
 * 作者：yuanchao on 2016/8/10 0010 09:53
 * 邮箱：yuanchao@feicuiedu.com
 */
public class CustomMediaController extends MediaController{

    private MediaPlayerControl mediaPlayerControl;

    public CustomMediaController(Context context) {
        super(context);
    }

    // 重写这个方法(vitamio MediaController的)，来自定义MediaController的视图
    @Override protected View makeControllerView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_custom_video_controller, this);
        initView(view);
        return view;
    }

    // 父类的MediaPlayerControl是私有的,重写这个方法，就是为了将player保存一份，方便我们使用
    @Override public void setMediaPlayer(MediaPlayerControl player) {
        super.setMediaPlayer(player);
        this.mediaPlayerControl = player;
    }

    private void initView(View view) {
        // 设置forward快进
        ImageButton btnFastForward = (ImageButton)view.findViewById(R.id.btnFastForward);
        btnFastForward.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                // 获取当前进度
                long position = mediaPlayerControl.getCurrentPosition();
                position += 10000;
                mediaPlayerControl.seekTo(position);
            }
        });
        // 设置rewind快退
        ImageButton btnFastRewind = (ImageButton)view.findViewById(R.id.btnFastRewind);
        btnFastRewind.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                long position = mediaPlayerControl.getCurrentPosition();
                position -= 10000;
                mediaPlayerControl.seekTo(position);
            }
        });
        // 调整屏幕亮度(左边)和音量(右边)
    }
}