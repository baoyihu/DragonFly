package com.baoyihu.dragonfly.controller;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.baoyihu.common.util.DebugLog;
import com.baoyihu.dragonfly.constant.ErrorCode;
import com.baoyihu.dragonfly.extractor.MyMediaExtractor;
import com.baoyihu.dragonfly.node.Sample;
import com.baoyihu.dragonfly.streamer.hls.ByteMediaSource;
import com.baoyihu.dragonfly.util.C;

import android.media.MediaExtractor;
import android.media.MediaFormat;

public class HlsSegment implements SegmentInterface
{
    private static final String TAG = "HlsSegment";
    
    private long segmentStartTime; //BillionSecond
    
    private List<Sample> sampleList = new ArrayList<Sample>();
    
    private int segmentIndex = 0;
    
    private int bitrate = 0;
    
    private SegmentControllerCallback segmentCallback;
    
    private int frameIndex = 0;
    
    private long bufferedSize = 0L;
    
    private boolean isAudio = false;
    
    private long durationTime;
    
    public String getMimeAudio()
    {
        return mimeAudio;
    }
    
    public String getMimeVideo()
    {
        return mimeVideo;
    }
    
    private String mimeAudio = null;
    
    private String mimeVideo = null;
    
    private MediaFormat formatAudio = null;
    
    private MediaFormat formatVideo = null;
    
    public MediaFormat getFormatAudio()
    {
        return formatAudio;
    }
    
    public MediaFormat getFormatVideo()
    {
        return formatVideo;
    }
    
    ///time in billiont second
    @Override
    public long getDurationTime()
    {
        return durationTime;
    }
    
    public void setDurationTime(long durationTime)
    {
        this.durationTime = durationTime;
    }
    
    public HlsSegment(int bitrate, int indexOfSegment, SegmentControllerCallback callback)
    {
        this.segmentIndex = indexOfSegment;
        this.bitrate = bitrate;
        this.segmentCallback = callback;
    }
    
    @Override
    public boolean execceed()
    {
        //       DebugLog.info(TAG, "execceed  frame:" + frameIndex + " size:" + sampleList.size());
        frameIndex++;
        if (frameIndex < sampleList.size())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public List<Sample> getSampleList()
    {
        return sampleList;
    }
    
    @Override
    public long getBufferSize()
    {
        return bufferedSize;
    }
    
    @Override
    public boolean hasNext()
    {
        return frameIndex < sampleList.size();
    }
    
    @Override
    public boolean isAudio()
    {
        return isAudio;
    }
    
    @Override
    public void setAudio(boolean isAudio)
    {
        this.isAudio = isAudio;
    }
    
    @Override
    public int getFrame(ByteBuffer byteBuffer)
    {
        int size = -1;
        
        if (frameIndex < sampleList.size())
        {
            Sample sample = sampleList.get(frameIndex);
            byteBuffer.clear();
            byte[] rawBuffer = sample.getBuffer();
            byteBuffer.put(rawBuffer, 0, rawBuffer.length);
            byteBuffer.rewind();
            size = rawBuffer.length;
            //            DebugLog.debug(TAG,
            //                "getFrame:" + isAudio + " " + segmentIndex + "." + frameIndex + " size:" + size + " buffer:"
            //                    + Arrays.toString(rawBuffer));
        }
        
        return size;
    }
    
    @Override
    public long getCurrentFrameTime()
    {
        long time = 0;
        if (frameIndex < sampleList.size())
        {
            Sample sample = sampleList.get(frameIndex);
            time = sample.getTimeOffset();
        }
        //        DebugLog.info(TAG,
        //            "getCurrentFrameTime segmentIndex:" + segmentIndex + " isAudio:" + isAudio + " frameIndex:" + frameIndex
        //                + " time:" + time);
        return time;
    }
    
    @Override
    public int getBitrate()
    {
        return bitrate;
    }
    
    @Override
    public int getSegmentIndex()
    {
        return segmentIndex;
    }
    
    @Override
    public long getSegmentStartTime()
    {
        return segmentStartTime;
    }
    
    //billionSecond
    @Override
    public void setSegmentStartTime(long time)
    {
        segmentStartTime = time;
    }
    
    @Override
    public void mergeData(SegmentInterface other)
    {
        HlsSegment otherSegment = (HlsSegment)other;
        this.sampleList = otherSegment.getSampleList();
    }
    
    @Override
    public void parseDetail(byte[] buffer, double segmentDuration)
    {
        //FIXME cost too many time ,it should be in 40ms ;now is 500-700ms
        boolean findTrack = false;
        MyMediaExtractor extractor = new MyMediaExtractor();
        int type = isAudio ? C.TRACK_TYPE_AUDIO : C.TRACK_TYPE_VIDEO;
        DebugLog.info(TAG, "parseDetail begin");
        extractor.setDataBuffer(buffer);
        DebugLog.info(TAG, "parseDetail begin 1");
        try
        {
            extractor.parse();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        DebugLog.info(TAG, "parseDetail begin 2");
        if (isAudio)
        {
            mimeAudio = extractor.getMime(type);
            formatAudio = extractor.getMediaFormat(type);
            findTrack = true;
        }
        else
        {
            mimeVideo = extractor.getMime(type);
            formatVideo = extractor.getMediaFormat(type);
            findTrack = true;
        }
        
        if (findTrack)
        {
            sampleList = new ArrayList<Sample>();
            long lastFrameTime = segmentStartTime;
            List<byte[]> sampleArray = extractor.getSampleBuffer(type);
            List<Long> sampleTimeList = extractor.getSampleTime(type);
            for (int iLoop = 0; iLoop < sampleArray.size(); iLoop++)
            {
                //      DebugLog.info(TAG, "addSample isAudio:" + isAudio + " time:" + lastFrameTime);
                sampleList.add(new Sample(sampleArray.get(iLoop), lastFrameTime));
                lastFrameTime = sampleTimeList.get(iLoop) + segmentStartTime;
            }
            bufferedSize = extractor.getTotalSampleSize(type);
            durationTime = (lastFrameTime - segmentStartTime);
        }
        else
        {
            DebugLog.error(TAG, "Can not find track of:" + (isAudio ? "Audio" : "Video"));
        }
        if (sampleList.isEmpty())
        {
            segmentCallback.onSegmentError(ErrorCode.MEDIA_ERROR_SEGMENT_NO_SAMPLE);
            DebugLog.error(TAG, "sampleList.size:" + sampleList.size());
        }
        else
        {
            DebugLog.info(TAG, "parseDetail end");
        }
    }
    
    public void parseDetailOld(byte[] buffer, double segmentDuration)
    {
        DebugLog.info(TAG, "parseDetail begin");
        MediaExtractor extractor = new MediaExtractor();
        boolean findTrack = false;
        try
        {
            extractor.setDataSource(new ByteMediaSource(buffer));
            int count = extractor.getTrackCount();
            for (int i = 0; i < count; i++)
            {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                DebugLog.info(TAG, "track mime:" + mime);
                if (isAudio)
                {
                    if (mime.startsWith("audio/"))
                    {
                        findTrack = true;
                        mimeAudio = mime;
                        formatAudio = format;
                        extractor.selectTrack(i);
                        DebugLog.info(TAG, "parseDetail find audio track:");
                        break;
                    }
                }
                else
                {
                    if (mime.startsWith("video/"))
                    {
                        findTrack = true;
                        mimeVideo = mime;
                        formatVideo = format;
                        extractor.selectTrack(i);
                        DebugLog.info(TAG, "parseDetail find video track:");
                        break;
                    }
                }
            }
            if (findTrack)
            {
                List<byte[]> sampleArray = new ArrayList<byte[]>();
                sampleList = new ArrayList<Sample>();
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
                int sampleSize = 0;
                long lastFrameTime = segmentStartTime;
                do
                {
                    sampleSize = extractor.readSampleData(byteBuffer, 0);
                    if (sampleSize > 0)
                    {
                        if (isAudio)
                        {
                            lastFrameTime = extractor.getSampleTime() + segmentStartTime;
                        }
                        else
                        {
                            lastFrameTime += 40000;//FIXME this should not hard code 40000; lastFrameTime from Extractor is not stable
                        }
                        extractor.advance();
                        byte[] tempBytes = new byte[byteBuffer.limit()];
                        byteBuffer.get(tempBytes, 0, byteBuffer.limit());
                        sampleArray.add(tempBytes);
                        byteBuffer.rewind();
                        sampleList.add(new Sample(tempBytes, lastFrameTime));
                        DebugLog.info(TAG, "addSample isAudio:" + isAudio + " time:" + lastFrameTime);
                        bufferedSize += tempBytes.length;
                        
                    }
                } while (sampleSize > 0);
                durationTime = (lastFrameTime - segmentStartTime);
            }
            else
            {
                DebugLog.error(TAG, "Can not find track of:" + (isAudio ? "Audio" : "Video"));
            }
        }
        catch (IOException e)
        {
            DebugLog.trace(TAG, e);
        }
        
        if (sampleList.isEmpty())
        {
            segmentCallback.onSegmentError(ErrorCode.MEDIA_ERROR_SEGMENT_NO_SAMPLE);
            DebugLog.error(TAG, "sampleList.size:" + sampleList.size());
        }
        else
        {
            DebugLog.info(TAG, "parseDetail end");
        }
    }
    
}
