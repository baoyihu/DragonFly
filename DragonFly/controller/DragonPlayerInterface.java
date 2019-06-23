package com.baoyihu.dragonfly.controller;

import java.util.List;

import com.baoyihu.dragonfly.constant.ErrorCode;
import com.baoyihu.dragonfly.constant.InfoCode;

import android.graphics.Rect;
import android.view.SurfaceView;

public interface DragonPlayerInterface
{
    public void setSurfaceView(SurfaceView view);
    
    public void prepare();
    
    public boolean start();
    
    public void pause();
    
    public void resume();
    
    public int seekTo(long positionMilli);
    
    public void release();
    
    public Rect getVideoRecetangle();
    
    public void setMonitor(PlayerMonitor monitor);
    
    public int getStatus();
    
    public long getDuration();
    
    public void setStartBufferTimeMill(long millSecond);
    
    public void setMaxBufferTimeMill(long millSecond);
    
    public void setOnErrorListener(OnErrorListener listener);
    
    public void setOnInfoListener(OnInfoListener listener);
    
    public List<Integer> getBitrateList();
    
    public int getCurrentBitrate();
    
    public void switchBitrate(int bitrate);
    
    public long getCurrentPosition();
    
    public long getBufferedSize(int type);
    
    public interface OnErrorListener
    {
        boolean onError(ErrorCode error, Object obj);
    }
    
    public interface OnInfoListener
    {
        boolean onInfo(InfoCode info, int what, int extra, Object obj);
    }
    
}
