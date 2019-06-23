package com.baoyihu.dragonfly.node;

public class Sample
{
    //   private static final String TAG = "Sample";
    
    private int startFromByte;
    
    private int size;
    
    private long timeOffset;
    
    private byte[] buffer;
    
    public Sample(byte[] buffer, long timeOffset)
    {
        this.timeOffset = timeOffset;
        this.buffer = buffer;
        this.size = buffer.length;
    }
    
    public Sample(int from, long timeOffset, int size)
    {
        this.timeOffset = timeOffset;
        this.startFromByte = from;
        this.size = size;
    }
    
    public byte[] getBuffer()
    {
        return buffer;
    }
    
    public void setBuffer(byte[] buffer)
    {
        this.buffer = buffer;
    }
    
    public long getTimeOffset()
    {
        return timeOffset;
    }
    
    public int getStartByte()
    {
        return startFromByte;
    }
    
    public int getSize()
    {
        return size;
    }
}
