package com.baoyihu.dragonfly.render;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.baoyihu.common.util.DebugLog;
import com.baoyihu.common.util.Files;
import com.baoyihu.dragonfly.controller.MediaStatus;
import com.baoyihu.dragonfly.controller.PlayerController;
import com.baoyihu.dragonfly.controller.SegmentInterface;
import com.baoyihu.dragonfly.streamer.StreamControllerInterface;
import com.baoyihu.dragonfly.util.ClockInterface;

import android.graphics.Rect;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.view.Surface;

public class VideoRender implements RenderInterface
{
    private static final String TAG = "VideoRender";
    
    private MediaCodec decoder;
    
    private PlayerController controller;
    
    private boolean needRun = false;
    
    private ConcurrentLinkedQueue<SegmentInterface> videoSegmentList = new ConcurrentLinkedQueue<SegmentInterface>();
    
    private StreamControllerInterface streamer = null;
    
    private int mediaState = MediaStatus.UNINIT;
    
    public VideoRender(PlayerController controller, StreamControllerInterface streamController, ClockInterface clock)
    {
        this.streamer = streamController;
        this.controller = controller;
        needRun = true;
        new Thread(this).start();
    }
    
    @Override
    public void start()
    {
        DebugLog.debug(TAG, "start");
        if (mediaState == MediaStatus.UNINIT)
        {
            mediaState = MediaStatus.INITED;
        }
        else if (mediaState == MediaStatus.INITED)
        {
            mediaState = MediaStatus.PLAYING;
        }
        else if (mediaState == MediaStatus.PAUSE)
        {
            mediaState = MediaStatus.PLAYING;
        }
        else if (mediaState == MediaStatus.PLAYING)
        {
            if (isInBuffering)
            {
                isInBuffering = false;
                mediaState = MediaStatus.PLAYING;
                synchronized (VideoRender.this)
                {
                    VideoRender.this.notifyAll();
                }
            }
        }
    }
    
    long audioTime = 0L;
    
    @Override
    public void onAdjustTime(long time, boolean clear)
    {
        audioTime = time;
        DebugLog.info(TAG, "onAdjustTime:" + time + " clear:" + clear);
    }
    
    @Override
    public void pause()
    {
        mediaState = MediaStatus.PAUSE;
        DebugLog.debug(TAG, "pause");
    }
    
    @Override
    public void resume()
    {
        DebugLog.debug(TAG, "resume");
        mediaState = MediaStatus.PLAYING;
        synchronized (VideoRender.this)
        {
            VideoRender.this.notifyAll();
            DebugLog.debug(TAG, "enter playing");
        }
    }
    
    @Override
    public void stop()
    {
        DebugLog.debug(TAG, "stop");
        needRun = false;
        mediaState = MediaStatus.STOP;
        synchronized (VideoRender.this)
        {
            VideoRender.this.notifyAll();
        }
        DebugLog.debug(TAG, "stop end");
    }
    
    int frameIndex = 0;
    
    ByteBuffer[] inputBuffers = null;
    
    ByteBuffer[] outputBuffers = null;
    
    @Override
    public boolean isinited()
    {
        Surface surface = null;
        SegmentInterface videoController = null;
        
        boolean isOk = false;
        while (!isOk)
        {
            surface = controller.getSurface();
            videoController = videoSegmentList.peek();
            MediaFormat format = streamer.getVideoMediaFormat();
            DebugLog.info(TAG, " videoController " + videoController + " surface:" + surface + " format:" + format);
            if (videoController != null && surface != null && format != null)
            {
                break;
            }
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                DebugLog.trace(TAG, e);
            }
        }
        
        return true;
    }
    
    int videoWidth = 0;
    
    int videoHeight = 0;
    
    @Override
    public void run()
    {
        isinited();
        try
        {
            String mime = streamer.getMime(false);
            DebugLog.debug(TAG, "mime:" + mime);
            decoder = MediaCodec.createDecoderByType(mime);
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        
        if (decoder == null)
        {
            DebugLog.error(TAG, "Can't find video info!!!!!!!!!!");
            return;
        }
        MediaFormat mime = streamer.getVideoMediaFormat();
        DebugLog.debug(TAG, "mime:" + mime + " surface:" + controller.getSurface());
        decoder.configure(mime, controller.getSurface(), null, 0);
        decoder.start();
        
        inputBuffers = decoder.getInputBuffers();
        outputBuffers = decoder.getOutputBuffers();
        BufferInfo info = new BufferInfo();
        long timeDiff = 0;
        DebugLog.info(TAG, " videoTimeDiff: " + timeDiff);
        while (needRun)
        {
            while (needRun && (isInBuffering || mediaState == MediaStatus.PAUSE || mediaState == MediaStatus.UNINIT))
            {
                try
                {
                    DebugLog.debug(TAG, "enter waiting !!!");
                    synchronized (VideoRender.this)
                    {
                        VideoRender.this.wait();
                    }
                }
                catch (InterruptedException e)
                {
                    DebugLog.trace(TAG, e);
                }
            }
            
            if (inRestart)
            {
                restartCodec();
            }
            
            int inIndex = 0;
            while (needRun && !hasFrame(false))
            {
                DebugLog.debug(TAG, "no Frame sleep:");
                sleep(10);
                continue;
            }
            if (decoder != null)
            {
                inIndex = decoder.dequeueInputBuffer(10000);
                if (inIndex > -1)
                {
                    ByteBuffer buffer = inputBuffers[inIndex];
                    int sampleSize = nextFrame(buffer, false);
                    if (sampleSize < 0)
                    {
                        DebugLog.debug(TAG, "There is no frame ,try 10ms later");
                        sleep(10);
                        continue;
                    }
                    else
                    {
                        if (PlayerController.DEBUG)
                        {
                            byte[] writeBuffer = new byte[sampleSize];
                            buffer.get(writeBuffer);
                            Files.writeFiles("/sdcard/File1/" + frameIndex++ + ".txt", writeBuffer, true);
                        }
                        
                        long lastFrameTime = getCurrentFrameTime(false);
                        DebugLog.debug(TAG, "queueInputBuffer:" + inIndex + " videoFrameTime:" + lastFrameTime);
                        if (decoder != null)
                        {
                            decoder.queueInputBuffer(inIndex, 0, sampleSize, lastFrameTime, 0);
                        }
                        
                        boolean execceed = execceedVideo();
                        if (!execceed)
                        {
                            DebugLog.debug(TAG, "execceedVideo failed ,try 10ms later");
                            sleep(10);
                            continue;
                        }
                    }
                }
            }
            if (decoder != null)
            {
                int outIndex = decoder.dequeueOutputBuffer(info, 10000);
                
                switch (outIndex)
                {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        DebugLog.info(TAG, "----------- INFO_OUTPUT_BUFFERS_CHANGED");
                        outputBuffers = decoder.getOutputBuffers();
                        break;
                    
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        MediaFormat newFormate = decoder.getOutputFormat();
                        DebugLog.info(TAG, "------------ New format " + decoder.getOutputFormat());
                        int width = newFormate.getInteger("width");
                        int height = newFormate.getInteger("height");
                        if (width > 0 && height > 0)
                        {
                            videoWidth = width;
                            videoHeight = height;
                            controller.setVideoRecetangle(new Rect(0, 0, width, height));
                        }
                        break;
                    
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        DebugLog.info(TAG, "----------- dequeueOutputBuffer timed outm, try again later!");
                        break;
                    
                    default:
                        while (needRun && info.presentationTimeUs / 1000 > audioTime + timeDiff)
                        {
                            DebugLog.info(TAG,
                                " sleep to time: " + info.presentationTimeUs / 1000 + " clockTime:" + audioTime
                                    + " timeDiff:" + timeDiff);
                            sleep(10);
                        }
                        DebugLog.debug(TAG, "dequeueOutputBuffer time:" + info.presentationTimeUs);
                        if (decoder != null)
                        {
                            decoder.releaseOutputBuffer(outIndex, true);
                        }
                        break;
                }
            }
            // All decoded frames have been rendered, we can stop playing now
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0)
            {
                DebugLog.debug(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM, decode finished!!!!!");
                break;
            }
        }
        DebugLog.debug(TAG, "exit from play,needRun" + needRun);
        if (decoder != null)
        {
            //    decoder.stop();
            decoder.release();
            decoder = null;
        }
    }
    
    public boolean execceedVideo()
    {
        boolean ret = false;
        SegmentInterface videoController = videoSegmentList.peek();
        if (videoController != null)
        {
            if (!videoController.execceed())
            {
                ret = switchToNextSegment(videoController, videoController.getSegmentIndex());
            }
            else
            {
                ret = true;
            }
        }
        return ret;
    }
    
    public boolean switchToNextSegment(SegmentInterface segmentController, int segmentIndex)
    {
        DebugLog.info(TAG, "switchToNextSegment from:" + segmentIndex);
        Iterator<SegmentInterface> iterator = null;
        
        iterator = videoSegmentList.iterator();
        
        SegmentInterface find = null;
        
        while (iterator.hasNext())
        {
            SegmentInterface temp = iterator.next();
            if (temp == segmentController)
            {
                iterator.remove();
            }
            else
            {
                if (temp.getBitrate() == segmentController.getBitrate())
                {
                    if (temp.getSegmentIndex() == segmentIndex + 1)
                    {
                        find = temp;
                        break;
                    }
                    else if (temp.getSegmentIndex() > segmentIndex + 1)
                    {
                        find = temp;
                        break;
                    }
                    else
                    {
                        DebugLog.error(TAG, "switchToNextSegment fail to:" + temp.getSegmentIndex() + "!!");
                    }
                }
            }
        }
        
        return find != null;
    }
    
    public long getCurrentFrameTime(boolean isAudio)
    {
        SegmentInterface tempController = null;
        
        tempController = videoSegmentList.peek();
        if (tempController != null)
        {
            long ret = tempController.getCurrentFrameTime();
            streamer.notifyFrameIndex(tempController, frameIndex, isAudio);
            return ret;
        }
        else
        {
            return 0;
        }
    }
    
    public int nextFrame(ByteBuffer buffer, boolean isAudio)
    {
        SegmentInterface tempController = null;
        
        tempController = videoSegmentList.peek();
        if (tempController != null)
        {
            return tempController.getFrame(buffer);
        }
        return 0;
    }
    
    public boolean hasFrame(boolean isAudio)
    {
        SegmentInterface tempController = videoSegmentList.peek();
        
        if (tempController != null)
        {
            return tempController.hasNext();
        }
        return false;
    }
    
    public void sleep(int time)
    {
        try
        {
            Thread.sleep(time);
        }
        catch (InterruptedException e)
        {
            DebugLog.trace(TAG, e);
        }
    }
    
    @Override
    public void setFilePath(String path)
    {
        
    }
    
    @Override
    public void setRenderHandler(RenderHandler handler)
    {
        DebugLog.info(TAG, "setRenderHandler:");
        
    }
    
    long newPosition = -1;
    
    boolean inRestart = false;
    
    long restartedTime = -1L;
    
    @Override
    public void restart(long time, boolean clearAudio, boolean clearVideo)
    {
        DebugLog.info(TAG, "restart:");
        restartedTime = time;
        inRestart = true;
        if (clearVideo)
        {
            videoSegmentList.clear();
        }
    }
    
    private void restartCodec()
    {
        inRestart = false;
        decoder.release();
        decoder = null;
        if (isinited())
        {
            try
            {
                decoder = MediaCodec.createDecoderByType(streamer.getMime(false));
            }
            catch (IOException e)
            {
                DebugLog.trace(TAG, e);
            }
            
            MediaFormat videoFormate = streamer.getVideoMediaFormat();
            decoder.configure(videoFormate, controller.getSurface(), null, 0);
            decoder.start();
            
            inputBuffers = decoder.getInputBuffers();
            outputBuffers = decoder.getOutputBuffers();
            inRestart = false;
            restartedTime = -1L;
        }
    }
    
    @Override
    public void onReceive(SegmentInterface segment, boolean isAudio)
    {
        SegmentInterface last = videoSegmentList.peek();
        if (last != null && last.equals(segment))
        {
            DebugLog.info(TAG, "merge one segment:");
            last.mergeData(segment);
        }
        else
        {
            DebugLog.info(TAG, "add one segment:");
            videoSegmentList.add(segment);
        }
    }
    
    boolean isInBuffering = false;
    
    @Override
    public void buffer()
    {
        isInBuffering = true;
    }
    
    @Override
    public boolean isInBuffering()
    {
        return isInBuffering;
    }
    
    @Override
    public long getBufferedSize(int type)
    {
        long ret = 0L;
        for (SegmentInterface controller : videoSegmentList)
        {
            ret += controller.getBufferSize();
        }
        return ret;
    }
    
}
