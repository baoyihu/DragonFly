package com.baoyihu.dragonfly.render;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.baoyihu.common.util.DebugLog;
import com.baoyihu.common.util.Files;
import com.baoyihu.dragonfly.controller.SegmentInterface;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.view.Surface;

public class FileAudioRender implements RenderInterface
{
    private AudioTrack mAudioTrack;
    
    private MediaExtractor extractor;
    
    private MediaCodec decoder;
    
    private Surface surface;
    
    private String filePath;
    
    private static final String TAG = "FileAudioRender";
    
    public FileAudioRender(Surface surface)
    {
        this.surface = surface;
    }
    
    @Override
    public void setFilePath(String path)
    {
        this.filePath = path;
    }
    
    @Override
    public void start()
    {
        new Thread(this).start();
    }
    
    int frameIndex = 0;
    
    private int calculateAudioBufferSize(int samplerate)
    {
        int mAudioMinBufSize =
            AudioTrack.getMinBufferSize(samplerate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        DebugLog.info(TAG, "mAudioMinBufSize:" + mAudioMinBufSize);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, samplerate, // 设置音频数据的采样率
            AudioFormat.CHANNEL_OUT_STEREO, // 设置输出声道为双声道立体声
            AudioFormat.ENCODING_PCM_16BIT, // 设置音频数据块是8位还是16位
            mAudioMinBufSize * 32, AudioTrack.MODE_STREAM);// 设置模式类型，在这里设置为流类型      
        
        return mAudioMinBufSize * 10;
        
    }
    
    @Override
    public void run()
    {
        extractor = new MediaExtractor();
        try
        {
            extractor.setDataSource(filePath);
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        int trackCount = extractor.getTrackCount();
        int sampleRate = 0;
        for (int i = 0; i < trackCount; i++)
        {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            sampleRate = format.getInteger("sample-rate");
            
            if (mime.startsWith("audio/"))
            {
                extractor.selectTrack(i);
                try
                {
                    decoder = MediaCodec.createDecoderByType(mime);
                }
                catch (IOException e)
                {
                    DebugLog.trace(TAG, e);
                }
                decoder.configure(format, null, null, 0);
                DebugLog.info(TAG, "info:" + format.toString());
                break;
            }
        }
        
        if (decoder == null)
        {
            DebugLog.error(TAG, "Can't find video info!!!!!!!!!!");
            return;
        }
        
        decoder.start();
        
        ByteBuffer[] inputBuffers = decoder.getInputBuffers();
        ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
        
        BufferInfo info = new BufferInfo();
        boolean isEOS = false;
        long startMs = System.currentTimeMillis();
        
        int bufferSize = calculateAudioBufferSize(sampleRate);
        mAudioTrack.setPlaybackRate(44100);
        mAudioTrack.play();
        
        byte[] pcmBufffer = new byte[bufferSize];
        while (!Thread.interrupted())
        {
            DebugLog.debug(TAG, "one loop");
            if (!isEOS)
            {
                DebugLog.debug(TAG, "one loop2");
                int inIndex = decoder.dequeueInputBuffer(10000);
                DebugLog.debug(TAG, "one loop3");
                if (inIndex >= 0)
                {
                    ByteBuffer buffer = inputBuffers[inIndex];
                    DebugLog.debug(TAG, "one loop4");
                    int sampleSize = extractor.readSampleData(buffer, 0); //TODO in xiaomi ,this is not good performate
                    DebugLog.debug(TAG, "one loop5");
                    if (sampleSize < 0)
                    {
                        DebugLog.debug(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                        decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isEOS = true;
                    }
                    else
                    {
                        byte[] writeBuffer = new byte[sampleSize];
                        DebugLog.debug(TAG, "before get:");
                        buffer.get(writeBuffer);
                        Files.writeFiles("/sdcard/File/" + frameIndex++ + ".txt", writeBuffer, true);
                        DebugLog.debug(TAG, "rawBuffer in:" + Arrays.toString(writeBuffer));
                        long time = extractor.getSampleTime();
                        DebugLog.debug(TAG,
                            "queueInputBuffer :" + sampleSize + "  Index:" + inIndex + "  time:" + time);
                        
                        decoder.queueInputBuffer(inIndex, 0, sampleSize, time, 0);
                        extractor.advance();
                    }
                }
                DebugLog.debug(TAG, "one loop6:" + inIndex);
            }
            
            int outIndex = decoder.dequeueOutputBuffer(info, 10000);
            DebugLog.debug(TAG, "dequeueOutputBuffer outIndex:" + outIndex);
            switch (outIndex)
            {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    DebugLog.info(TAG, "----------- INFO_OUTPUT_BUFFERS_CHANGED");
                    outputBuffers = decoder.getOutputBuffers();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    DebugLog.info(TAG, "------------ New format " + decoder.getOutputFormat());
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    DebugLog.info(TAG, "----------- dequeueOutputBuffer timed outm, try again later!");
                    break;
                default:
                    ByteBuffer buffer = outputBuffers[outIndex];
                    // It is not quite need ,but keep it to match video
                    while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs)
                    {
                        try
                        {
                            Thread.sleep(10);
                        }
                        catch (InterruptedException e)
                        {
                            DebugLog.trace(TAG, e);
                            break;
                        }
                    }
                    
                    buffer.get(pcmBufffer, 0, info.size);
                    decoder.releaseOutputBuffer(outIndex, false);
                    mAudioTrack.write(pcmBufffer, 0, info.size);
                    break;
            }
            
            // All decoded frames have been rendered, we can stop playing now
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0)
            {
                DebugLog.debug(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM, decode finished!!!!!");
                break;
            }
        }
        
        decoder.stop();
        decoder.release();
        extractor.release();
    }
    
    @Override
    public void stop()
    {
        MediaPlayer plyaer = new MediaPlayer();
    }
    
    @Override
    public void pause()
    {
        
    }
    
    @Override
    public void resume()
    {
        
    }
    
    @Override
    public void onAdjustTime(long time, boolean clear)
    {
        
    }
    
    @Override
    public void setRenderHandler(RenderHandler handler)
    {
        
    }
    
    @Override
    public void onReceive(SegmentInterface segment, boolean isAudio)
    {
        
    }
    
    @Override
    public void buffer()
    {
        
    }
    
    @Override
    public boolean isinited()
    {
        return false;
    }
    
    @Override
    public void restart(long time, boolean clearAudio, boolean clearVideo)
    {
        
    }
    
    @Override
    public boolean isInBuffering()
    {
        return false;
    }
    
    @Override
    public long getBufferedSize(int type)
    {
        return 0;
    }
    
}
