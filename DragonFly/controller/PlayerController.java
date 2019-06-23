package com.baoyihu.dragonfly.controller;

import java.util.List;

import com.baoyihu.common.util.DebugLog;
import com.baoyihu.dragonfly.constant.ErrorCode;
import com.baoyihu.dragonfly.render.AudioRender;
import com.baoyihu.dragonfly.render.RenderHandler;
import com.baoyihu.dragonfly.render.RenderInterface;
import com.baoyihu.dragonfly.streamer.StreamControllerCallback;
import com.baoyihu.dragonfly.streamer.StreamControllerInterface;
import com.baoyihu.dragonfly.streamer.dash.DashStreamController;
import com.baoyihu.dragonfly.streamer.hls.HlsStreamController;
import com.baoyihu.dragonfly.url.Media;

import android.graphics.Rect;
import android.os.Handler;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PlayerController
    implements SurfaceHolder.Callback, DragonPlayerInterface, RenderHandler, StreamControllerCallback
{
    
    public static final int MSG_MONITOR_FRAME_SIZE_CHANGE = 9987;
    
    public static final int MSG_MONITOR_POSITION_CHANGE = 9988;
    
    public static final int MSG_MONITOR_ON_ERROR = 9989;
    
    public static final int MSG_MONITOR_ON_INDEX_OK = 9990;
    
    private final String TAG = "PlayerController";
    
    public static final boolean DEBUG = false;
    
    public static boolean testAudio = true;
    
    private Media media = null;
    
    private RenderInterface audioRender = null;
    
    private SurfaceView view = null;
    
    private StreamControllerInterface streamController = null;
    
    private PlayerMonitor monitor = null;
    
    private Rect rect = null;
    
    private long bufferBeginTime = 2000;
    
    private int userStatus = PlayerStatus.STATUS_UNINIT;
    
    private long positionMilli = 0;
    
    @Override
    public void setMonitor(PlayerMonitor monitor)
    {
        this.monitor = monitor;
    }
    
    public PlayerController(Media media)
    {
        DebugLog.info(TAG, "startToPlay");
        this.media = media;
        String url = media.getUrl();
        if (url.contains(".mpd"))
        {
            streamController = new DashStreamController();
        }
        else if (url.contains(".m3u8"))
        {
            streamController = new HlsStreamController();
        }
        else
        {
            throw new RuntimeException("not supported type，URL:" + url);
        }
        streamController.setCallBack(this);
        
        audioRender = new AudioRender(this, streamController);
        audioRender.setRenderHandler(this);
        
        streamController.setSegmentReceiver(audioRender);
        
        userStatus = PlayerStatus.STATUS_INITED;
    }
    
    @Override
    public void pause()
    {
        DebugLog.info(TAG, "pause");
        setStatus(PlayerStatus.STATUS_PAUSE);
        if (audioRender != null)
        {
            audioRender.pause();
        }
    }
    
    @Override
    public void resume()
    {
        DebugLog.info(TAG, "resume");
        setStatus(PlayerStatus.STATUS_PLAYING);
        if (audioRender != null)
        {
            audioRender.resume();
        }
    }
    
    @Override
    public void release()
    {
        DebugLog.info(TAG, "release");
        if (audioRender != null)
        {
            audioRender.stop();
            audioRender = null;
        }
        if (streamController != null)
        {
            streamController.release();
            streamController = null;
        }
        setStatus(PlayerStatus.STATUS_RELEASED);
        if (monitorHandler != null)
        {
            monitorHandler.removeMessages(MSG_MONITOR_POSITION_CHANGE);
        }
    }
    
    public Surface getSurface()
    {
        if (view != null)
        {
            return view.getHolder().getSurface();
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public boolean start()
    {
        DebugLog.info(TAG, "start");
        setStatus(PlayerStatus.STATUS_PLAYING);
        monitorHandler.removeMessages(MSG_MONITOR_POSITION_CHANGE);
        monitorHandler.sendEmptyMessageDelayed(MSG_MONITOR_POSITION_CHANGE, 1000);
        return true;
    }
    
    @Override
    public void setSurfaceView(SurfaceView view)
    {
        DebugLog.info(TAG, "setSurfaceView");
        this.view = view;
        view.getHolder().addCallback(this);
    }
    
    @Override
    public void prepare()
    {
        DebugLog.info(TAG, "prepare");
        streamController.prepare(media.getUrl());
    }
    
    public void setVideoRecetangle(Rect rect)
    {
        DebugLog.info(TAG, "setVideoRecetangle:" + rect);
        this.rect = rect;
        monitorHandler.sendMessage(monitorHandler.obtainMessage(MSG_MONITOR_FRAME_SIZE_CHANGE, rect));
    }
    
    @Override
    public Rect getVideoRecetangle()
    {
        DebugLog.info(TAG, "getVideoRecetangle:" + rect);
        return rect;
    }
    
    Handler monitorHandler = new Handler()
    {
        @Override
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case MSG_MONITOR_FRAME_SIZE_CHANGE:
                    if (monitor != null)
                    {
                        monitor.onFrameSizeChange((Rect)msg.obj);
                    }
                    break;
                
                case MSG_MONITOR_POSITION_CHANGE:
                    if (monitor != null)
                    {
                        if (userStatus < PlayerStatus.STATUS_RELEASED)
                        {
                            DebugLog.info(TAG, "MSG_MONITOR_POSITION_CHANGE:");
                            monitorHandler.sendEmptyMessageDelayed(MSG_MONITOR_POSITION_CHANGE, 1000);
                            monitor.onPositionChange(positionMilli);
                        }
                    }
                    break;
                
                case MSG_MONITOR_ON_ERROR:
                    if (errorListener != null)
                    {
                        errorListener.onError((ErrorCode)msg.obj, null);
                    }
                    break;
                
                case MSG_MONITOR_ON_INDEX_OK:
                    if (monitor != null)
                    {
                        monitor.onPrepare();
                    }
                    break;
            }
        }
    };
    
    @Override
    public void surfaceCreated(SurfaceHolder arg0)
    {
        DebugLog.info(TAG, "surfaceCreated");
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder arg0)
    {
        DebugLog.info(TAG, "surfaceDestroyed");
        audioRender.pause();
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
    {
        DebugLog.info(TAG, "surfaceChanged");
    }
    
    @Override
    public void notifiTime(long time, boolean absolute)
    {
        DebugLog.debug(TAG, "currentPosition:" + time);
        positionMilli = time;
    }
    
    @Override
    public int getStatus()
    {
        return userStatus;
    }
    
    public void setStatus(int value)
    {
        userStatus = value;
    }
    
    @Override
    public int seekTo(long timeMilliSecond)
    {
        DebugLog.info(TAG, "seekTo:" + timeMilliSecond);
        audioRender.restart(timeMilliSecond, true, true);
        streamController.seekTo(timeMilliSecond);
        return 0;
    }
    
    @Override
    public long getDuration()
    {
        long ret = streamController.getDuration();
        DebugLog.info(TAG, "getDuration:" + ret);
        return ret;
    }
    
    @Override
    public void onBuffering(long time)
    {
        int percent = (int)(time * 100 / bufferBeginTime);
        percent = percent > 100 ? 100 : percent;
        DebugLog.info(TAG, "onBuffering:" + time + " userStatus:" + userStatus + " bufferBeginTime:" + bufferBeginTime);
        if (time > bufferBeginTime)
        {
            if (userStatus == PlayerStatus.STATUS_PLAYING && audioRender.isInBuffering())
            {
                audioRender.start();
                monitor.onPlaying();
                monitor.onBuffering(percent);
            }
        }
        else
        {
            if (userStatus == PlayerStatus.STATUS_PLAYING && !audioRender.isInBuffering())
            {
                audioRender.buffer();
                monitor.onBuffering(percent);
            }
        }
    }
    
    @Override
    public void setStartBufferTimeMill(long bufferLenth)
    {
        bufferBeginTime = bufferLenth;
    }
    
    @Override
    public void onStreamError(ErrorCode code)
    {
        DebugLog.info(TAG, "onStreamError:" + code);
        monitorHandler.sendMessage(monitorHandler.obtainMessage(MSG_MONITOR_ON_ERROR, code));
    }
    
    OnErrorListener errorListener;
    
    @Override
    public void setOnErrorListener(OnErrorListener listener)
    {
        this.errorListener = listener;
    }
    
    @Override
    public void setOnInfoListener(OnInfoListener listener)
    {
        DebugLog.info(TAG, "setOnInfoListener:" + listener);
        
    }
    
    @Override
    public void onIndexOk()
    {
        DebugLog.info(TAG, "onIndexOk:");
        monitorHandler.sendEmptyMessage(MSG_MONITOR_ON_INDEX_OK);
    }
    
    @Override
    public List<Integer> getBitrateList()
    {
        return streamController.getBitrateList();
    }
    
    @Override
    public int getCurrentBitrate()
    {
        return streamController.getCurrentBitrate();
    }
    
    @Override
    public void switchBitrate(int bitrate)
    {
        DebugLog.info(TAG, "switchBitrate:" + bitrate + " time:" + positionMilli);
        audioRender.restart(positionMilli, false, true);
        streamController.switchBitrate(bitrate, positionMilli);
        return;
    }
    
    @Override
    public long getCurrentPosition()
    {
        DebugLog.info(TAG, "getCurrentPosition:" + positionMilli);
        return positionMilli;
    }
    
    @Override
    public long getBufferedSize(int type)
    {
        long ret = audioRender.getBufferedSize(type);
        return ret;
    }
    
    @Override
    public void setMaxBufferTimeMill(long millSecond)
    {
        streamController.setMaxBufferTimeMill(millSecond);
    }
}
