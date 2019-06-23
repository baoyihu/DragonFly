package com.baoyihu.dragonfly.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.TreeMap;

import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.MovieFragmentBox;
import org.mp4parser.boxes.iso14496.part12.SegmentIndexBox;
import org.mp4parser.boxes.iso14496.part12.TrackFragmentBaseMediaDecodeTimeBox;
import org.mp4parser.boxes.iso14496.part12.TrackRunBox;

import com.baoyihu.common.util.DebugLog;
import com.baoyihu.dragonfly.constant.ErrorCode;
import com.baoyihu.dragonfly.node.Sample;
import com.baoyihu.dragonfly.streamer.VideoBuffer;
import com.baoyihu.dragonfly.util.Indicator;

public class DashSegment implements SegmentInterface
{
    private static final String TAG = "SegmentController";
    
    VideoBuffer videoBuffer;
    
    private boolean isAudio = false;
    
    private long segmentStartTime; //BillionSecond
    
    private byte[] rawBuffer = null;
    
    private double duration = 0;
    
    private int dataOffset;
    
    private int segmentIndex;
    
    private int bitrate;
    
    private int frameIndex = 0;
    
    private TreeMap<Integer, Sample> map;
    
    private SegmentIndexBox indexBox;
    
    public SegmentIndexBox getIndexBox()
    {
        return indexBox;
    }
    
    public void setIndexBox(SegmentIndexBox indexBox)
    {
        this.indexBox = indexBox;
    }
    
    public VideoBuffer getVideoBuffer()
    {
        return videoBuffer;
    }
    
    @Override
    public long getBufferSize()
    {
        return rawBuffer.length;
    }
    
    public void setVideoBuffer(VideoBuffer videoBuffer)
    {
        this.videoBuffer = videoBuffer;
    }
    
    @Override
    public void mergeData(SegmentInterface other)
    {
        DashSegment otherSegment = (DashSegment)other;
        this.map = otherSegment.getMap();
    }
    
    public TreeMap<Integer, Sample> getMap()
    {
        return map;
    }
    
    public void setMap(TreeMap<Integer, Sample> map)
    {
        this.map = map;
    }
    
    @Override
    public long getSegmentStartTime()
    {
        return segmentStartTime;
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
    public int getBitrate()
    {
        return bitrate;
    }
    
    @Override
    public int getSegmentIndex()
    {
        return segmentIndex;
    }
    
    private SegmentControllerCallback segmentCallback = null;
    
    public DashSegment(int bitrate, int index, SegmentControllerCallback callback)
    {
        this.bitrate = bitrate;
        this.segmentIndex = index;
        this.segmentCallback = callback;
    }
    
    public int getFrameCount()
    {
        return map.size();
    }
    
    public long getDurationTime()
    {
        return (long)duration;
    }
    
    @Override
    public void parseDetail(byte[] buffer, double segmentDuration)
    {
        this.rawBuffer = buffer;
        this.duration = segmentDuration;
        
        map = getSmapleList(buffer, segmentDuration, 0);
        if (map.isEmpty())
        {
            segmentCallback.onSegmentError(ErrorCode.MEDIA_ERROR_SEGMENT_NO_SAMPLE);
            DebugLog.error(TAG, "map.size:" + map.size());
        }
    }
    
    @Override
    public long getCurrentFrameTime()
    {
        long time = 0;
        if (frameIndex < map.size())
        {
            Sample sample = map.get(frameIndex);
            time = sample.getTimeOffset();
        }
        DebugLog.info(TAG, "getCurrentFrameTime():" + time);
        return time;
    }
    
    @Override
    public boolean hasNext()
    {
        return frameIndex < map.size();
    }
    
    @Override
    public int getFrame(ByteBuffer buffer)
    {
        int size = -1;
        if (isAudio)
        {
            if (frameIndex < map.size())
            {
                Sample sample = map.get(frameIndex);
                int start = sample.getStartByte();
                size = sample.getSize();
                int copyStart = start + dataOffset;
                
                buffer.clear();
                buffer.put(rawBuffer, copyStart, size);
                buffer.rewind();
                DebugLog.debug(TAG,
                    "getFrame_isAudio:" + isAudio + " segment:" + segmentIndex + " frame:" + frameIndex);
            }
        }
        else
        {
            if (frameIndex < map.size())
            {
                Sample sample = map.get(frameIndex);
                int start = sample.getStartByte();
                size = sample.getSize();
                int copyStart = start + dataOffset;
                
                int from = copyStart;
                int end = copyStart + size;
                
                byte[] sequenceBuffer = videoBuffer.getSequenceByteArray();
                while (from > 0 && from < end)
                {
                    from = dealHead(sequenceBuffer, from);
                }
                
                buffer.clear();
                buffer.put(sequenceBuffer, copyStart, size);
                buffer.rewind();
                //Logger.debug(TAG, "getFrame() isAudio:" + isAudio + " segment:" + segmentIndex + " frame:" + frameIndex);
                
            }
        }
        return size;
    }
    
    private int dealHead(byte[] buffer, int index)
    {
        long headSize = Indicator.getLong32(buffer, index);
        int nualType = buffer[index + 4] & 0x1F;
        int ret = -1;
        //      if (nualType == 7 || nualType == 8 || nualType == 5 || nualType == 2)
        {//I Frame: sps 7; pps 8 ; IDR2,5
            ret = (int)(headSize + index + 4);
            //Logger.info(TAG, "current I Frame head SPS :" + ret);
        }
        // B Frame;
        Indicator.setFixedHead(buffer, index);
        return ret;
    }
    
    @Override
    public boolean execceed()
    {
        DebugLog.info(TAG, "execceed audio:" + isAudio + " frame:" + frameIndex + " size:" + map.size());
        frameIndex++;
        if (frameIndex < map.size())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private TreeMap<Integer, Sample> getSmapleList(byte[] buffer, double totalTime, double oneFrameTime)
    {
        DebugLog.debug(TAG, " getSmapleList isAudio:" + isAudio + " bufferSize:" + buffer.length);
        ReadableByteChannel channel = null;
        channel = Channels.newChannel(new ByteArrayInputStream(buffer));
        IsoFile isoFile = null;
        TreeMap<Integer, Sample> sampleMap = new TreeMap<Integer, Sample>();
        
        try
        {
            try
            {
                isoFile = new IsoFile(channel);
                List<MovieFragmentBox> fragmentList = isoFile.getBoxes(MovieFragmentBox.class);
                if (!fragmentList.isEmpty())
                {
                    MovieFragmentBox fragmentBox = fragmentList.get(0);
                    segmentStartTime = paraseSegmentStartIme(indexBox, fragmentBox);
                    List<TrackRunBox> boxList = fragmentBox.getTrackRunBoxes();
                    TrackRunBox box = boxList.get(0);
                    box.parseDetails();
                    dataOffset = box.getDataOffset();
                    int from = 0;
                    long timeOffset = segmentStartTime;
                    
                    List<TrackRunBox.Entry> list = box.getEntries();
                    int count = list.size();
                    if (totalTime > 0d)
                    {
                        duration = totalTime;
                        oneFrameTime = totalTime / count;
                    }
                    else
                    {
                        duration = oneFrameTime * count;
                    }
                    for (int jLoop = 0; jLoop < count; jLoop++)
                    {
                        TrackRunBox.Entry temp = list.get(jLoop);
                        int sampleSize = (int)temp.getSampleSize();
                        sampleMap.put(jLoop, new Sample(from, timeOffset, sampleSize));
                        from += sampleSize;
                        timeOffset += oneFrameTime;
                    }
                }
                else
                {
                    
                }
            }
            finally
            {
                if (isoFile != null)
                {
                    isoFile.close();
                }
            }
        }
        catch (IOException e)
        {
            DebugLog.trace(TAG, e);
        }
        
        return sampleMap;
    }
    
    private long paraseSegmentStartIme(SegmentIndexBox segmentIndex, MovieFragmentBox fragmentBox)
    {
        TrackFragmentBaseMediaDecodeTimeBox boxList = fragmentBox.getTrackFragmentBaseMediaDecodeTimeBox();
        long ret = 0L;
        if (boxList == null)
        {
            DebugLog.error(TAG, " paraseSegmentStartIme empty TimeBox!!!");
        }
        else
        {
            TrackFragmentBaseMediaDecodeTimeBox box = boxList;
            box.parseDetails();
            long baseTimeBillion = box.getBaseMediaDecodeTime() * 1000000 / segmentIndex.getTimeScale();
            ret = baseTimeBillion + segmentIndex.getEarliestPresentationTime() * 1000000 / segmentIndex.getTimeScale();
        }
        return ret;
    }
    
    @Override
    public int hashCode()
    {
        return (bitrate * 3 + segmentIndex * 17);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof DashSegment))
        {
            return false;
        }
        else
        {
            DashSegment other = (DashSegment)o;
            return (bitrate == other.bitrate && segmentIndex == other.segmentIndex);
        }
    }
    
    @Override
    public void setSegmentStartTime(long time)
    {
        segmentStartTime = time;
    }
}
