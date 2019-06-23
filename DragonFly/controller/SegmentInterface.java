package com.baoyihu.dragonfly.controller;

import java.nio.ByteBuffer;

import com.baoyihu.dragonfly.constant.ErrorCode;

public interface SegmentInterface
{
    public boolean execceed();
    
    public long getBufferSize();
    
    public boolean hasNext();
    
    public int getFrame(ByteBuffer buffer);
    
    public long getCurrentFrameTime();
    
    public int getBitrate();
    
    public int getSegmentIndex();
    
    public long getSegmentStartTime();
    
    public void setSegmentStartTime(long time);
    
    public void mergeData(SegmentInterface other);
    
    public void parseDetail(byte[] buffer, double segmentDuration);
    
    public boolean isAudio();
    
    public void setAudio(boolean isAudio);
    
    public long getDurationTime();
    
    public interface SegmentControllerCallback
    {
        void onSegmentError(ErrorCode code);
    }
}
