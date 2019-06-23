package com.baoyihu.dragonfly.render;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.baoyihu.common.util.DebugLog;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class BufferedAudioTrack
{
    private static final String TAG = "BufferedAudioTrack";
    
    private AudioTrack mAudioTrack;
    
    private boolean needWork = true;
    
    private long currentAudioTime = 0;
    
    public BufferedAudioTrack(int rate, int pcmBufferSize)
    {
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, rate, // 设置音频数据的采样率
            AudioFormat.CHANNEL_OUT_STEREO, // 设置输出声道为双声道立体声
            AudioFormat.ENCODING_PCM_16BIT, // 设置音频数据块是8位还是16位
            pcmBufferSize, AudioTrack.MODE_STREAM);// 设置模式类型，在这里设置为流类型    
        
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (needWork)
                {
                    if (!bufferQueue.isEmpty())
                    {
                        try
                        {
                            BufferedBuffer buffer = bufferQueue.take();
                            byte[] byteBuffer = buffer.getByteBuffer();
                            mAudioTrack.write(byteBuffer, 0, byteBuffer.length);
                            mAudioTrack.flush();
                            currentAudioTime = buffer.getAudioTime();
                        }
                        catch (InterruptedException e)
                        {
                            DebugLog.trace(TAG, e);
                        }
                    }
                }
            }
        }).start();
        
    }
    
    public long getCurrentAudioTime()
    {
        return currentAudioTime;
    }
    
    public void pause()
    {
        if (mAudioTrack != null)
        {
            mAudioTrack.pause();
        }
    }
    
    public void play()
    {
        if (mAudioTrack != null)
        {
            mAudioTrack.play();
        }
    }
    
    public void stop()
    {
        if (mAudioTrack != null)
        {
            mAudioTrack.stop();
        }
    }
    
    public void setPlaybackRate(int rate)
    {
        if (mAudioTrack != null)
        {
            mAudioTrack.setPlaybackRate(rate);
        }
    }
    
    static class BufferedBuffer
    {
        byte[] buffer = null;
        
        long audioTime = 0;
        
        private BufferedBuffer(byte[] bufffer, int start, int size, long audioTime)
        {
            this.audioTime = audioTime;
            buffer = new byte[size];
            System.arraycopy(bufffer, start, buffer, 0, size);
        }
        
        public byte[] getByteBuffer()
        {
            return buffer;
        }
        
        public long getAudioTime()
        {
            return audioTime;
        }
    }
    
    private BlockingQueue<BufferedBuffer> bufferQueue = new ArrayBlockingQueue<BufferedBuffer>(2);
    
    public void write(byte[] bufffer, int start, int size, long audioTime)
    {
        if (mAudioTrack != null)
        {
            long time = System.currentTimeMillis();
            int alreadyCount = bufferQueue.size();
            try
            {
                bufferQueue.put(new BufferedBuffer(bufffer, start, size, audioTime));
            }
            catch (InterruptedException e)
            {
                DebugLog.trace(TAG, e);
            }
            DebugLog.debug(TAG, "writeCost:" + (System.currentTimeMillis() - time) + " alreadyCount:" + alreadyCount);
        }
    }
    
    public void flush()
    {
        if (mAudioTrack != null)
        {
            mAudioTrack.flush();
        }
    }
    
    public void release()
    {
        needWork = false;
        if (mAudioTrack != null)
        {
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }
}
