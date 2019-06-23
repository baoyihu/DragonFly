package com.baoyihu.dragonfly.util;

public interface ClockInterface
{
    long getTime();
    
    void adjustTime(long input);
    
    void start();
    
    void stop();
    
    void hold();
    
    void go();
    
}
