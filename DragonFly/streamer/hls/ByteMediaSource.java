package com.baoyihu.dragonfly.streamer.hls;

import java.io.IOException;

import android.media.MediaDataSource;

public class ByteMediaSource extends MediaDataSource
{
    private static final String TAG = "ByteMediaSource";
    
    private byte[] byteBuffer = null;
    
    private int bufferSize = 0;
    
    public ByteMediaSource(byte[] byteArray)
    {
        //  DebugLog.info(TAG, "ByteMediaSource:" + byteArray.length);
        this.byteBuffer = byteArray.clone();
    }
    
    @Override
    public void close()
        throws IOException
    {
        // do nothing
        
    }
    
    @Override
    public int readAt(long position, byte[] buffer, int offset, int size)
        throws IOException
    {
        //DebugLog.info(TAG, "readAt position:" + position + " from:" + offset + " size:" + size);
        if (byteBuffer != null && position + size <= byteBuffer.length && offset + size <= buffer.length)
        {
            System.arraycopy(byteBuffer, (int)position, buffer, offset, size);
            return size;
        }
        else
        {
            return -1;
        }
    }
    
    @Override
    public long getSize()
        throws IOException
    {
        //   DebugLog.info(TAG, "getSize");
        return bufferSize;
    }
    
    public void updateBuffer(byte[] buffer)
    {
        byteBuffer = buffer.clone();
        bufferSize = byteBuffer.length;
    }
    
}
