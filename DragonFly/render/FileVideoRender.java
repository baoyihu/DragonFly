package com.baoyihu.dragonfly.render;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.baoyihu.common.util.DebugLog;
import com.baoyihu.common.util.Files;
import com.baoyihu.dragonfly.controller.PlayerController;
import com.baoyihu.dragonfly.controller.SegmentInterface;
import com.baoyihu.dragonfly.streamer.StreamControllerInterface;
import com.baoyihu.dragonfly.util.AbsoluteClock;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;

public class FileVideoRender implements RenderInterface
{
    
    private MediaExtractor extractor;
    
    private MediaCodec decoder;
    
    private String filePath = "/sdcard/mission5.mp4";
    
    private static final String TAG = "FilePlayerRender";
    
    private PlayerController controller;
    
    private StreamControllerInterface streamer = null;
    
    private AbsoluteClock clock = null;
    
    public FileVideoRender(PlayerController controller, StreamControllerInterface streamController, AbsoluteClock clock)
    {
        DebugLog.info(TAG, "onCreate:");
        this.streamer = streamController;
        this.controller = controller;
        this.clock = clock;
        new Thread(this).start();
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
        
        for (int i = 0; i < extractor.getTrackCount(); i++)
        {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/"))
            {
                DebugLog.info(TAG, "video_mime:" + mime);
                extractor.selectTrack(i);
                try
                {
                    decoder = MediaCodec.createDecoderByType(mime);
                }
                catch (IOException e)
                {
                    DebugLog.trace(TAG, e);
                }
                decoder.configure(format, controller.getSurface(), null, 0);
                DebugLog.info(TAG, "info:" + format.toString());
                break;
            }
        }
        
        // MediaFormat format = new MediaFormat();
        // format.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
        // format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        // format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
        // MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
        // decoder = MediaCodec.createDecoderByType("vedio/avc");
        // decoder.configure(format, surface, null, 0);
        
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
        
        while (!Thread.interrupted())
        {
            if (!isEOS)
            {
                int inIndex = decoder.dequeueInputBuffer(10000);
                if (inIndex >= 0)
                {
                    ByteBuffer buffer = inputBuffers[inIndex];
                    int sampleSize = extractor.readSampleData(buffer, 0);
                    if (sampleSize < 0)
                    {
                        // We shouldn't stop the playback at this point,
                        // just pass the EOS
                        // flag to decoder, we will get it again from the
                        // dequeueOutputBuffer
                        DebugLog.debug(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                        decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isEOS = true;
                    }
                    else
                    {
                        if (PlayerController.DEBUG)
                        {
                            byte[] writeBuffer = new byte[sampleSize];
                            buffer.get(writeBuffer);
                            Files.writeFiles("/sdcard/File/" + frameIndex++ + ".txt", writeBuffer, true);
                        }
                        long time = extractor.getSampleTime();
                        DebugLog.debug(TAG,
                            "queueInputBuffer :" + sampleSize + "  Index:" + inIndex + "  time:" + time);
                        decoder.queueInputBuffer(inIndex, 0, sampleSize, time, 0);
                        extractor.advance();
                    }
                }
            }
            
            int outIndex = decoder.dequeueOutputBuffer(info, 10000);
            
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
                    // Logger.error(TAG,
                    // "We can't use this buffer but render it due to the API limit, "
                    // + buffer);
                    
                    // We use a very simple clock to keep the video FPS, or the
                    // video
                    // playback will be too fast
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
                    DebugLog.debug(TAG, "dequeueOutputBuffer time:" + info.presentationTimeUs);
                    decoder.releaseOutputBuffer(outIndex, true);
                    break;
            }
            
            // All decoded frames have been rendered, we can stop playing
            // now
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
