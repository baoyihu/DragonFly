package com.baoyihu.dragonfly.render;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.baoyihu.common.util.DebugLog;
import com.baoyihu.dragonfly.controller.MediaStatus;
import com.baoyihu.dragonfly.controller.PlayerController;
import com.baoyihu.dragonfly.controller.SegmentInterface;
import com.baoyihu.dragonfly.streamer.StreamControllerInterface;
import com.baoyihu.dragonfly.util.AbsoluteClock;
import com.baoyihu.dragonfly.util.C;
import com.baoyihu.dragonfly.util.ClockInterface;
import com.baoyihu.dragonfly.util.Util;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

public class AudioRender implements RenderInterface, ClockInterface
{
    private static final String TAG = "AudioRender";
    
    private ConcurrentLinkedQueue<SegmentInterface> audioSegmentList = new ConcurrentLinkedQueue<SegmentInterface>();
    
    private AudioTrack bufferedAudioTrack;
    
    private MediaCodec decoder;
    
    private AbsoluteClock audioClock = null;
    
    private PlayerController controller;
    
    private boolean needRun = false;
    
    private RenderHandler handler = null;
    
    private RenderInterface videoRender = null;
    
    private StreamControllerInterface streamer = null;
    
    private int mediaState = MediaStatus.UNINIT;
    
    public int getMediaState()
    {
        return mediaState;
    }
    
    public void setMediaState(int mediaState)
    {
        this.mediaState = mediaState;
    }
    
    public AudioRender(PlayerController controller, StreamControllerInterface streamController)
    {
        this.controller = controller;
        this.streamer = streamController;
        videoRender = new VideoRender(controller, streamController, this);
        audioClock = new AbsoluteClock();
        needRun = true;
        new Thread(this).start();
    }
    
    boolean isInBuffering = false;
    
    @Override
    public void start()
    {
        DebugLog.info(TAG, "_start_");
        videoRender.start();
        if (mediaState == MediaStatus.UNINIT)
        {
            mediaState = MediaStatus.INITED;
        }
        else if (mediaState == MediaStatus.PAUSE)
        {
            mediaState = MediaStatus.PLAYING;
        }
        else if (mediaState == MediaStatus.INITED)
        {
            mediaState = MediaStatus.PLAYING;
        }
        else if (mediaState == MediaStatus.PLAYING)
        {
            if (isInBuffering)
            {
                isInBuffering = false;
                mediaState = MediaStatus.PLAYING;
                synchronized (AudioRender.this)
                {
                    AudioRender.this.notifyAll();
                    audioClock.start();
                }
            }
        }
    }
    
    @Override
    public void pause()
    {
        DebugLog.info(TAG, "pause");
        mediaState = MediaStatus.PAUSE;
        if (bufferedAudioTrack != null)
        {
            bufferedAudioTrack.pause();
        }
        
        if (videoRender != null)
        {
            videoRender.pause();
        }
        if (audioClock != null)
        {
            audioClock.hold();
        }
    }
    
    @Override
    public void resume()
    {
        DebugLog.info(TAG, "resume");
        if (mediaState == MediaStatus.PAUSE)
        {
            mediaState = MediaStatus.PLAYING;
            if (bufferedAudioTrack != null)
            {
                bufferedAudioTrack.play();
            }
            if (videoRender != null)
            {
                videoRender.resume();
            }
            synchronized (AudioRender.this)
            {
                AudioRender.this.notifyAll();
                if (audioClock != null)
                {
                    audioClock.go();
                }
            }
        }
    }
    
    @Override
    public void stop()
    {
        DebugLog.info(TAG, "stop");
        needRun = false;
        if (videoRender != null)
        {
            videoRender.stop();
        }
        synchronized (AudioRender.this)
        {
            AudioRender.this.notifyAll();
        }
        if (audioClock != null)
        {
            audioClock.stop();
        }
    }
    
    int frameIndex = 0;
    
    public void setSurface(Surface surface)
    {
    }
    
    private int calculateAudioBufferSize1(int samplerate)
    {
        int ret = 0;
        int mAudioMinBufSize =
            AudioTrack.getMinBufferSize(samplerate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        bufferedAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, samplerate, // 设置音频数据的采样率
            AudioFormat.CHANNEL_OUT_STEREO, // 设置输出声道为双声道立体声
            AudioFormat.ENCODING_PCM_16BIT, // 设置音频数据块是8位还是16位
            mAudioMinBufSize * 4, AudioTrack.MODE_STREAM);// 设置模式类型，在这里设置为流类型      
        bufferedAudioTrack.setPlaybackRate(samplerate);
        
        ret = mAudioMinBufSize * 4;
        DebugLog.info(TAG,
            " calculateAudioBufferSize:" + ret + " sampleRate:" + samplerate + " mAudioMinBufSize:" + mAudioMinBufSize);
        return ret;
    }
    
    private boolean checkAudioTrack()
    {
        boolean ret = true;
        if (bufferedAudioTrack == null)
        {
            initialAudioTrack(samplingRate);
        }
        return ret;
    }
    
    private static final long MIN_BUFFER_DURATION_US = 250000;
    
    private static final long MAX_BUFFER_DURATION_US = 750000;
    
    private static final int BUFFER_MULTIPLICATION_FACTOR = 4;
    
    private long durationUsToFrames(long durationUs, int sampleRate)
    {
        return (durationUs * sampleRate) / C.MICROS_PER_SECOND;
    }
    
    //createAudioTrackV21
    private boolean initialAudioTrack(int samplerate)
    {
        boolean ret = false;
        
        int minBufferSize =
            AudioTrack.getMinBufferSize(samplerate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        
        int multipliedBufferSize = minBufferSize * BUFFER_MULTIPLICATION_FACTOR;
        
        int outputPcmFrameSize = Util.getPcmFrameSize(C.ENCODING_PCM_16BIT, 2);
        
        int minAppBufferSize = (int)durationUsToFrames(MIN_BUFFER_DURATION_US, samplerate) * outputPcmFrameSize;
        int maxAppBufferSize =
            (int)Math.max(minBufferSize, durationUsToFrames(MAX_BUFFER_DURATION_US, samplerate) * outputPcmFrameSize);
        int bufferSize = multipliedBufferSize < minAppBufferSize ? minAppBufferSize
            : multipliedBufferSize > maxAppBufferSize ? maxAppBufferSize : multipliedBufferSize;
        
        AudioAttributes attributes = new android.media.AudioAttributes.Builder()
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_UNKNOWN)//CONTENT_TYPE_MOVIE)
            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
            .setFlags(0)//android.media.AudioAttributes.FLAG_HW_AV_SYNC
            .build();
        
        AudioFormat format = new AudioFormat.Builder().setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(samplerate)
            .build();
        int audioSessionId = 162;
        DebugLog.info("AudioTrack",
            "init21 attributes:" + attributes + " format:" + format + " bufferSize:" + bufferSize + " audioSessionId:"
                + audioSessionId);
        bufferedAudioTrack = new AudioTrack(attributes, format, bufferSize, AudioTrack.MODE_STREAM, audioSessionId);
        DebugLog.info(TAG,
            " calculateAudioBufferSize:" + ret + " sampleRate:" + samplerate + "bufferSize:" + bufferSize);
        
        bufferedAudioTrack.play();
        return ret;
    }
    
    ByteBuffer[] inputBuffers = null;
    
    ByteBuffer[] outputBuffers = null;
    
    @Override
    public boolean isinited()
    {
        
        Surface surface = null;
        SegmentInterface audioController = null;
        
        boolean isOk = false;
        while (!isOk)
        {
            surface = controller.getSurface();
            audioController = audioSegmentList.peek();
            MediaFormat format = streamer.getAudioMediaFormat();
            DebugLog.info(TAG, " audioController " + audioController + " surface:" + surface + " format:" + format);
            if (audioController != null && surface != null && format != null && videoRender.isinited())
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
    
    long currentFrameTime = 0;
    
    int samplingRate = 0;
    
    long timeStampOfInfo = -10000000;
    
    int count = 0;
    
    @Override
    public void run()
    {
        isinited();
        
        try
        {
            String mime = streamer.getMime(true);
            DebugLog.info(TAG, "mime:" + mime);
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
        
        MediaFormat audioFormate = streamer.getAudioMediaFormat();
        DebugLog.info(TAG, "audioFormate of Audio:" + audioFormate.toString());
        decoder.configure(audioFormate, null, null, 0);
        decoder.start();
        
        inputBuffers = decoder.getInputBuffers();
        outputBuffers = decoder.getOutputBuffers();
        BufferInfo info = new BufferInfo();
        
        samplingRate = streamer.getAudioSamplingRate();
        
        long timeDiff = 4 * 42;//声音落后，就调大此值
        DebugLog.info(TAG, " audioTimeDiff: " + timeDiff);
        while (needRun)
        {
            while (needRun && (isInBuffering || mediaState == MediaStatus.PAUSE || mediaState == MediaStatus.UNINIT))
            {
                try
                {
                    DebugLog.debug(TAG,
                        "enter waiting !!! neeedRun:" + needRun + " isInBuffering:" + isInBuffering + " mediaState:"
                            + mediaState);
                    synchronized (AudioRender.this)
                    {
                        AudioRender.this.wait();
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
            while (needRun && audioSegmentList.isEmpty())
            {
                DebugLog.debug(TAG, "no Frame sleep:");
                sleep(10);
                continue;
            }
            DebugLog.info(TAG, "dequeueInputBuffer:");
            inIndex = decoder.dequeueInputBuffer(10000);
            long lastFrameTime = 0;
            if (inIndex > -1)
            {
                ByteBuffer buffer = inputBuffers[inIndex];
                
                int sampleSize = nextFrame(buffer, true);
                if (sampleSize < 0)
                {
                    sleep(10);
                    continue;
                }
                else
                {
                    lastFrameTime = getCurrentFrameTime(true);
                    DebugLog.info(TAG, "getCurrentFrameTime:" + lastFrameTime + " audioFrameTime:" + lastFrameTime);
                    decoder.queueInputBuffer(inIndex, 0, sampleSize, lastFrameTime, 0);
                    execceedAudio();
                }
            }
            
            DebugLog.info(TAG, "----------- dequeueOutputBuffer");
            int outIndex = decoder.dequeueOutputBuffer(info, 10000);
            
            switch (outIndex)
            {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    DebugLog.info(TAG, "----------- INFO_OUTPUT_BUFFERS_CHANGED");
                    outputBuffers = decoder.getOutputBuffers();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    MediaFormat newFormate = decoder.getOutputFormat();
                    samplingRate = newFormate.getInteger("sample-rate");
                    if (bufferedAudioTrack != null)
                    {
                        releaseAudioTrack(bufferedAudioTrack);
                        bufferedAudioTrack = null;
                    }
                    DebugLog.info(TAG, "------------ New format " + decoder.getOutputFormat());
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    DebugLog.info(TAG, "----------- dequeueOutputBuffer timed outm, try again later!");
                    break;
                default:
                    if (timeStampOfInfo == info.presentationTimeUs
                        || (info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0)
                    {
                        DebugLog.info(TAG,
                            "we drop one sample of Aduio:" + info.presentationTimeUs + " flag:" + info.flags);
                        decoder.releaseOutputBuffer(outIndex, false);
                        break;
                    }
                    timeStampOfInfo = info.presentationTimeUs;
                    currentFrameTime = info.presentationTimeUs / 1000 + timeDiff;
                    long clockTime = audioClock.getTime();
                    DebugLog.debug(TAG, "currentClockTime:" + currentFrameTime + " clockTime:" + clockTime);
                    while (currentFrameTime > audioClock.getTime() + 80)
                    {
                        DebugLog.debug(TAG, "sleep 10 info:");
                        sleep(10);
                    }
                    if (videoRender != null)
                    {
                        videoRender.onAdjustTime(currentFrameTime, false);
                    }
                    if (handler != null)
                    {
                        handler.notifiTime(currentFrameTime, false);
                    }
                    
                    ByteBuffer buffer = outputBuffers[outIndex];
                    DebugLog.debug(TAG,
                        " info.offset:" + info.offset + " info.size:" + info.size + " buffer.size:"
                            + buffer.capacity());
                    
                    if (buffer != null)
                    {
                        buffer.position(info.offset);
                        buffer.limit(info.offset + info.size);
                    }
                    
                    long timeBeforeWrite = System.currentTimeMillis();
                    if (decoder != null && checkAudioTrack())
                    {
                        //                        //FIXME Debug
                        //                        byte[] pcmBufffer = new byte[info.size];
                        //                        buffer.get(pcmBufffer, 0, info.size);
                        //                        buffer.clear();
                        
                        //                        //FIXME
                        //                        if (isDeadBuffer(pcmBufffer))
                        //                        {
                        //                            DebugLog.info(TAG, "we drop one sample of Aduio:" + Arrays.toString(pcmBufffer));
                        //                            decoder.releaseOutputBuffer(outIndex, false);
                        //                            break;
                        //                        }
                        //  pcmBufffer = Files.readFile("/sdcard/dragon/audio_" + count++ + ".txt");
                        //Files.writeFiles("/sdcard/dragon/audio_" + count++ + ".txt", pcmBufffer, false);
                        //                        DebugLog.debug(TAG,
                        //                            "pcmBuffer size:" + info.size + " flags:" + info.flags + " buffer:"
                        //                                + Arrays.toString(pcmBufffer));
                        Log.i(TAG, "writeBuffer size " + info.size);
                        //bufferedAudioTrack.write(pcmBufffer, 0, info.size);// it may stack for seconds  
                        bufferedAudioTrack.write(buffer, info.size, AudioTrack.WRITE_NON_BLOCKING);
                        //                        bufferedAudioTrack.flush();not need
                        decoder.releaseOutputBuffer(outIndex, false);
                    }
                    DebugLog.debug(TAG,
                        "compareTime info:" + currentFrameTime + " timeDiff:" + timeDiff + " costTime:"
                            + (System.currentTimeMillis() - timeBeforeWrite));
                    break;
            }
            
            // All decoded frames have been rendered, we can stop playing now
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0)
            {
                DebugLog.debug(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM, decode finished!!!!!");
                break;
            }
        }
        
        if (bufferedAudioTrack != null)
        {
            DebugLog.debug(TAG, "release AudioTrack");
            //     bufferedAudioTrack.flush();
            bufferedAudioTrack.release();
            bufferedAudioTrack = null;
        }
        if (decoder != null)
        {
            DebugLog.debug(TAG, "release Audio Decoder");
            decoder.release();
            decoder = null;
        }
        if (audioClock != null)
        {
            audioClock = null;
        }
    }
    
    private void releaseAudioTrack(final AudioTrack inputTrack)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    inputTrack.flush();
                    inputTrack.release();
                }
                finally
                {
                    //  releasingConditionVariable.open();
                }
            }
        }.start();
        
    }
    
    public boolean isDeadBuffer(byte[] input)
    {
        int count = input.length > 5 ? 5 : input.length;
        for (int iLoop = 0; iLoop < count; iLoop++)
        {
            if (input[iLoop] > 0 || input[iLoop] < -3)
            {
                return false;
            }
        }
        return true;
    }
    
    public boolean execceedAudio()
    {
        boolean ret = false;
        SegmentInterface audioController = audioSegmentList.peek();
        if (audioController != null)
        {
            if (!audioController.execceed())
            {
                ret = switchToNextSegment(audioController, audioController.getSegmentIndex());
            }
        }
        return ret;
    }
    
    public boolean switchToNextSegment(SegmentInterface segmentController, int segmentIndex)
    {
        DebugLog.info(TAG, "switchToNextSegment from:" + segmentIndex);
        Iterator<SegmentInterface> iterator = null;
        
        iterator = audioSegmentList.iterator();
        
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
        SegmentInterface tempController = audioSegmentList.peek();
        
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
        SegmentInterface tempController = audioSegmentList.peek();
        if (tempController != null)
        {
            return tempController.getFrame(buffer);
        }
        return 0;
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
    public void onAdjustTime(long time, boolean clear)
    {
        DebugLog.info(TAG, "onAdjustTime:" + time + " clear:" + clear);
    }
    
    @Override
    public void setRenderHandler(RenderHandler handler)
    {
        this.handler = handler;
    }
    
    long newPosition = -1;
    
    boolean inRestart = false;
    
    @Override
    public void restart(long time, boolean clearAudio, boolean clearVideo)
    {
        DebugLog.info(TAG, "restart at:" + time);
        inRestart = true;
        if (clearAudio)
        {
            audioSegmentList.clear();
        }
        if (videoRender != null)
        {
            videoRender.restart(time, clearAudio, clearVideo);
        }
    }
    
    private void restartCodec()
    {
        inRestart = false;
        decoder.release();
        if (isinited())
        {
            try
            {
                decoder = MediaCodec.createDecoderByType(streamer.getMime(true));
            }
            catch (IOException e)
            {
                DebugLog.trace(TAG, e);
            }
            
            MediaFormat audioFormate = streamer.getAudioMediaFormat();
            decoder.configure(audioFormate, null, null, 0);
            decoder.start();
            
            inputBuffers = decoder.getInputBuffers();
            outputBuffers = decoder.getOutputBuffers();
            
            bufferedAudioTrack.stop();
            bufferedAudioTrack.play();
        }
    }
    
    @Override
    public void onReceive(SegmentInterface segment, boolean isAudio)
    {
        if (isAudio)
        {
            DebugLog.info(TAG,
                "add audio segment :" + segment.getSegmentIndex() + " startTim:" + segment.getSegmentStartTime());
            audioSegmentList.add(segment);
        }
        else
        {
            DebugLog.info(TAG,
                "add video segment :" + segment.getSegmentIndex() + " startTim:" + segment.getSegmentStartTime());
            videoRender.onReceive(segment, isAudio);
        }
        
    }
    
    @Override
    public void buffer()
    {
        DebugLog.info(TAG, "enter in  buffering !");
        isInBuffering = true;
        if (videoRender != null)
        {
            videoRender.buffer();
        }
    }
    
    @Override
    public boolean isInBuffering()
    {
        return isInBuffering;
    }
    
    @Override
    public long getTime()
    {
        return currentFrameTime;
    }
    
    @Override
    public void adjustTime(long input)
    {
        // no need to adjust
        return;
    }
    
    @Override
    public void hold()
    {
        return;
    }
    
    @Override
    public void go()
    {
        return;
    }
    
    // 0: all ,1: audio ,2:video
    @Override
    public long getBufferedSize(int type)
    {
        long ret = 0L;
        if (type < 2)
        {
            for (SegmentInterface controller : audioSegmentList)
            {
                ret += controller.getBufferSize();
            }
            if (type == 0)
            {
                ret = videoRender.getBufferedSize(2);
            }
        }
        else
        {
            ret = videoRender.getBufferedSize(2);
        }
        
        return ret;
    }
}
