package com.baoyihu.dragonfly.streamer;

import java.util.List;

import com.baoyihu.dragonfly.controller.SegmentInterface;
import com.baoyihu.dragonfly.render.RenderInterface;

import android.media.MediaFormat;

public interface StreamControllerInterface
{
    public List<Integer> getBitrateList();
    
    public void setCallBack(StreamControllerCallback callback);
    
    public void prepare(String url);
    
    public void seekTo(long timeMilli);
    
    public void switchBitrate(int bitrate, long timeMilli);
    
    public long getDuration();
    
    public int getCurrentBitrate();
    
    public MediaFormat getVideoMediaFormat();
    
    public String getMime(boolean isAudio);
    
    public long getVideoBeginTimeDiff();
    
    public void notifyFrameIndex(SegmentInterface segmentController, int index, boolean isAudio);
    
    public MediaFormat getAudioMediaFormat();
    
    public int getAudioSamplingRate();
    
    public long getAudioBeginTimeDiff();
    
    public void setSegmentReceiver(RenderInterface receiver);
    
    public void setMaxBufferTimeMill(long timeMill);
    
    public void release();
}
