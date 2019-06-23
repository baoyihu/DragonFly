package com.baoyihu.dragonfly.util;

import com.baoyihu.common.util.DebugLog;

public class AbsoluteClock implements ClockInterface
{
    public static final String TAG = "AbsoluteClock";
    
    long zeroTime;
    
    long currentTime;
    
    boolean needWork = false;
    
    boolean status = true;
    
    public AbsoluteClock()
    {
        
    }
    
    @Override
    public void start()
    {
        if (!needWork)
        {
            needWork = true;
            zeroTime = System.currentTimeMillis();
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    while (needWork)
                    {
                        try
                        {
                            Thread.sleep(1);
                        }
                        catch (InterruptedException e)
                        {
                            DebugLog.trace(TAG, e);
                        }
                        if (status)
                        {
                            currentTime = System.currentTimeMillis() - zeroTime;
                        }
                    }
                    
                }
            }).start();
        }
    }
    
    @Override
    public void stop()
    {
        needWork = false;
    }
    
    /**millSecond
     * */
    @Override
    public long getTime()
    {
        return currentTime;
    }
    
    @Override
    public void adjustTime(long input)
    {
        zeroTime = System.currentTimeMillis() - input;
    }
    
    @Override
    public void hold()
    {
        status = false;
    }
    
    @Override
    public void go()
    {
        status = true;
        zeroTime = System.currentTimeMillis() - currentTime;
    }
}
