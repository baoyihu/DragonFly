package com.baoyihu.dragonfly.streamer;

public class StreamResult
{
    
    public static final int STREAM_RETURN_INDEX = 1100;
    
    public static final int STREAM_RETURN_AUDIO_HEAD = 1101;
    
    public static final int STREAM_RETURN_VIDEO_HEAD = 1102;
    
    public static final int STREAM_RETURN_AUDIO_SEGMENT = 1103;
    
    public static final int STREAM_RETURN_VIDEO_SEGMENT = 1104;
    
    private String url;
    
    public String getUrl()
    {
        return url;
    }
    
    public void setUrl(String url)
    {
        this.url = url;
    }
    
    private StreamType type;
    
    private int returnType;
    
    private int segmentIndex;
    
    private VideoBuffer videoBuffer;
    
    private long startTime;
    
    public long getStartTime()
    {
        return startTime;
    }
    
    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }
    
    public VideoBuffer getVideoBuffer()
    {
        return videoBuffer;
    }
    
    public void setVideoBuffer(VideoBuffer videoBuffer)
    {
        this.videoBuffer = videoBuffer;
    }
    
    private byte[][] bytePools = null;
    
    public int getReturnType()
    {
        return returnType;
    }
    
    public byte[][] getBytePools()
    {
        return bytePools;
    }
    
    public void setBytePools(byte[][] bytePools)
    {
        this.bytePools = bytePools;
    }
    
    public StreamResult(StreamType type, int returnType)
    {
        this.type = type;
        this.returnType = returnType;
    }
    
    public int getSegmentindex()
    {
        return segmentIndex;
    }
    
    public void setSegmentindex(int segmentindex)
    {
        this.segmentIndex = segmentindex;
    }
    
    @Override
    public String toString()
    {
        return "StreamResult Type:" + type + " returnType:" + returnType + " segmentIndex:" + segmentIndex;
    }
}
