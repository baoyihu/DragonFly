package com.baoyihu.dragonfly.controller;

import android.graphics.Rect;

public interface PlayerMonitor
{
    void onFrameSizeChange(Rect rect);
    
    void onPositionChange(long positionMilli);
    
    void onBuffering(int percent);
    
    void onPlaying();
    
    void onPrepare();
    
    void onEnd();
    
}
