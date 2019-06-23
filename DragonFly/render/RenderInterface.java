package com.baoyihu.dragonfly.render;

import com.baoyihu.dragonfly.controller.SegmentInterface;

public interface RenderInterface extends Runnable
{
    public void start();
    
    public void pause();
    
    public void buffer();
    
    public void resume();
    
    public void stop();
    
    public void setFilePath(String path);
    
    public void onAdjustTime(long time, boolean clear);
    
    public void setRenderHandler(RenderHandler handler);
    
    public void restart(long time, boolean clearAudio, boolean clearVideo);
    
    public void onReceive(SegmentInterface segment, boolean isAudio);
    
    public boolean isinited();
    
    public boolean isInBuffering();
    
    public long getBufferedSize(int type);
}
