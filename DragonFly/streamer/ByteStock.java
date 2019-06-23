package com.baoyihu.dragonfly.streamer;

import com.baoyihu.common.util.DebugLog;

public class ByteStock
{
    private static final String TAG = "ByteStock";
    
    private final int from;
    
    private final int size;
    
    private int filled;
    
    public int getFilled()
    {
        return filled;
    }
    
    private final VideoBuffer lord;
    
    private String name;
    
    public String getName()
    {
        return name;
    }
    
    public ByteStock(VideoBuffer lord, int from, int size)
    {
        //     DebugLog.debug(TAG, "ByteStock onCreate, from:" + from + " size:" + size);
        this.from = from;
        this.size = size;
        this.lord = lord;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public int getFrom()
    {
        return from;
    }
    
    public int getSize()
    {
        return size;
    }
    
    public void addBuffer(byte[] buffer, int inputLenth)
    {
        //   DebugLog.debug(TAG, "addBuffer " + name + " :" + inputLenth);
        if (inputLenth <= (size - filled))
        {
            System.arraycopy(buffer, 0, lord.raw, from + filled, inputLenth);
            filled += inputLenth;
            if (filled == size)
            {
                lord.onBufferFull(this);
            }
            else
            {
                lord.onBufferUpdate(this);
            }
        }
        else
        {
            DebugLog.error(TAG, "addBuffer,error");
        }
    }
}
