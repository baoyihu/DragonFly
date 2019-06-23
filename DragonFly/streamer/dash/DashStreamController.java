package com.baoyihu.dragonfly.streamer.dash;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mp4parser.boxes.iso14496.part12.SegmentIndexBox;
import org.simpleframework.xml.core.PersistenceException;

import com.baoyihu.common.util.DebugLog;
import com.baoyihu.common.util.Pair;
import com.baoyihu.dragonfly.constant.ErrorCode;
import com.baoyihu.dragonfly.controller.DashSegment;
import com.baoyihu.dragonfly.controller.DownloadTask;
import com.baoyihu.dragonfly.controller.SegmentInterface;
import com.baoyihu.dragonfly.node.MediaDescription;
import com.baoyihu.dragonfly.node.dash.DashInitialization;
import com.baoyihu.dragonfly.node.dash.DashNode;
import com.baoyihu.dragonfly.node.dash.DashRepresentation;
import com.baoyihu.dragonfly.node.dash.DashSegmentBase;
import com.baoyihu.dragonfly.render.RenderInterface;
import com.baoyihu.dragonfly.streamer.StreamControllerCallback;
import com.baoyihu.dragonfly.streamer.StreamControllerInterface;
import com.baoyihu.dragonfly.streamer.StreamInterface;
import com.baoyihu.dragonfly.streamer.StreamResult;
import com.baoyihu.dragonfly.streamer.StreamType;
import com.baoyihu.dragonfly.streamer.VideoBuffer;
import com.baoyihu.dragonfly.xml.SerializerService;

import android.media.MediaFormat;
import android.text.TextUtils;

public class DashStreamController
    implements StreamInterface.StreamCallBack, StreamControllerInterface, DashSegment.SegmentControllerCallback
{
    private static final String TAG = "DashStreamController";
    
    private DashNode dashNode = null;
    
    private RenderInterface segmentReceiver = null;
    
    private int currentVideoBitrate = 0;
    
    private int currentAudioBitrate = 0;
    
    private int lastAudioSegementIndex = -1;
    
    private int lastVideoSegementIndex = -1;
    
    private long audioBufferEnd = 0;
    
    private long videoBufferEnd = 0;
    
    private long audioPlaying = 0;
    
    private long videoPlaying = 0;
    
    private int currentDownloadingAudio = -1;
    
    private int currentDownloadingVideo = -1;
    
    private String url = null;
    
    private DashStreamer streamer = null;
    
    private StreamControllerCallback controllerCallback = null;
    
    private long videoBeginTimeDiff = 0;
    
    private List<DownloadTask> inDownloadingTask = new ArrayList<DownloadTask>();
    
    private boolean needWork = true;
    
    private long maxBufferTimeMill = 10 * 1000;
    
    public long getVideoBufferEndTime()
    {
        return videoBufferEnd;
    }
    
    @Override
    public long getVideoBeginTimeDiff()
    {
        return videoBeginTimeDiff;
    }
    
    private long audioBeginTimeDiff = 0;
    
    @Override
    public long getAudioBeginTimeDiff()
    {
        return audioBeginTimeDiff;
    }
    
    @Override
    public List<Integer> getBitrateList()
    {
        List<Integer> ret = new ArrayList<Integer>();
        if (dashNode != null)
        {
            MediaDescription description = dashNode.getDescription();
            for (DashRepresentation represent : description.getVideoList())
            {
                ret.add(represent.getBandwidth());
            }
        }
        
        return ret;
    }
    
    @Override
    public int getCurrentBitrate()
    {
        return currentVideoBitrate;
    }
    
    public interface SegmentReceiver
    {
        void onReceive(DashSegment segment);
    }
    
    public DashStreamController()
    {
        streamer = new DashStreamer(StreamType.DASH);
        streamer.setCallBack(this);
    }
    
    @Override
    public void setSegmentReceiver(RenderInterface receiver)
    {
        this.segmentReceiver = receiver;
    }
    
    public void startDownloadFrom(long millSecond)
    {
        
    }
    
    @Override
    public void setCallBack(StreamControllerCallback callback)
    {
        controllerCallback = callback;
    }
    
    public String getUrlDirectory()
    {
        String ret = null;
        if (!TextUtils.isEmpty(url) && url.contains("/"))
        {
            ret = url.substring(0, url.lastIndexOf("/") + 1);
        }
        return ret;
    }
    
    @Override
    public void prepare(String url)
    {
        this.url = url;
        DownloadTask task = new DownloadTask();
        task.url = url;
        inDownloadingTask.add(task);
        streamer.downloadIndex(task);
        new Thread(checkBufferRunnable).start();
    }
    
    @Override
    public void notifyFrameIndex(SegmentInterface segmentController, int index, boolean isAudio)
    {
        if (isAudio)
        {
            audioPlaying = segmentController.getCurrentFrameTime();
            DebugLog.info(TAG, "getDuration audioIndex:" + index + " audioPlaying:" + audioPlaying);
        }
        else
        {
            videoPlaying = segmentController.getCurrentFrameTime();
            DebugLog.info(TAG, "getDuration videoIndex:" + index + " videoPlaying:" + videoPlaying);
        }
    }
    
    private DashRepresentation getCurrentRepresent(boolean isAudio)
    {
        if (dashNode != null)
        {
            if (isAudio)
            {
                return dashNode.getRepresentationByBitrate(currentAudioBitrate, isAudio);
            }
            else
            {
                return dashNode.getRepresentationByBitrate(currentVideoBitrate, isAudio);
            }
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public int getAudioSamplingRate()
    {
        DashRepresentation represent = getCurrentRepresent(true);
        if (represent != null && represent.isAudio())
        {
            return represent.getAudioSamplingRate();
        }
        else
        {
            throw new RuntimeException("this is not a audio Represent");
        }
    }
    
    @Override
    public String getMime(boolean isAudio)
    {
        DashRepresentation represent = getCurrentRepresent(isAudio);
        if (represent != null)
        {
            return represent.getMimeType();
        }
        else
        {
            return null;
        }
        
    }
    
    @Override
    public long getDuration()
    {
        long duration = dashNode.getDurationMilli();
        DebugLog.info(TAG, "getDuration:" + duration);
        return duration;
    }
    
    @Override
    public MediaFormat getAudioMediaFormat()
    {
        //    Logger.debug(TAG, "getAudioMediaFormat");
        DashRepresentation represent = getCurrentRepresent(true);
        if (represent != null)
        {
            return represent.getAudioMediaFormat();
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public MediaFormat getVideoMediaFormat()
    {
        DashRepresentation represent = getCurrentRepresent(false);
        if (represent != null)
        {
            return represent.getVideoMediaFormat();
        }
        else
        {
            return null;
        }
    }
    
    private synchronized int downloadSegment(final int indexOfTrack, boolean isAudio)
    {
        final DashRepresentation represent = getCurrentRepresent(isAudio);
        if (represent == null)
        {
            return -1;
        }
        final SegmentIndexBox segmentIndex = represent.getSegmentIndex();
        if (segmentIndex == null)
        {
            return -2;
        }
        final SegmentIndexBox.Entry node = segmentIndex.getEntries().get(indexOfTrack);
        if (node == null)
        {
            return -3;
        }
        else
        {
            DebugLog.debug(TAG, "downloadSegment index:" + indexOfTrack + " isAudio" + isAudio);
            String rangeString = represent.getSegment().getIndexRange();
            String[] array = rangeString.split("-");
            int indexToVideo = Integer.parseInt(array[1].trim());
            
            long from = getSegmentStartBufferOffset(segmentIndex, indexOfTrack - 1) + indexToVideo + 1;
            long to = node.getReferencedSize() + from - 1;
            List<Pair<String, String>> properties = new ArrayList<Pair<String, String>>();
            properties.add(new Pair<String, String>("Range", "bytes=" + from + "-" + to));
            
            DownloadTask task = new DownloadTask();
            task.properties = properties;
            task.representation = represent;
            task.indexOfSegment = indexOfTrack;
            task.isAudio = isAudio;
            inDownloadingTask.add(task);
            if (isAudio)
            {
                DebugLog.info(TAG, " downloadSegment currentDownloadingAudio:" + currentDownloadingAudio);
                streamer.downloadAudioSegment(task);
            }
            else
            {
                DebugLog.info(TAG, " downloadSegment currentDownloadingVideo:" + currentDownloadingVideo);
                streamer.downloadVideoSegment(task);
            }
            return 0;
        }
        
    }
    
    @Override
    public void seekTo(long timeMilli)
    {
        int indexOfAudioSegment = getIndexOfSegmentByTime(timeMilli, true);
        int indexOfVideoSegment = getIndexOfSegmentByTime(timeMilli, false);
        DebugLog.info(TAG, "seekTo :" + timeMilli);
        if (indexOfAudioSegment >= 0 && indexOfVideoSegment >= 0)
        {
            audioPlaying = timeMilli * 1000;
            videoPlaying = timeMilli * 1000;
            DebugLog.info(TAG, "seekTo audio:" + audioPlaying + " videoPlaying:" + videoPlaying);
            inDownloadingTask.clear();
            currentDownloadingAudio = indexOfAudioSegment;
            currentDownloadingVideo = indexOfVideoSegment;
            if (downloadSegment(indexOfAudioSegment, true) < 0)
            {
                currentDownloadingAudio = -1;
            }
            if (downloadSegment(indexOfVideoSegment, false) < 0)
            {
                currentDownloadingVideo = -1;
            }
        }
    }
    
    private void removeAudioSegmentTask()
    {
        synchronized (inDownloadingTask)
        {
            Iterator<DownloadTask> iterator = inDownloadingTask.iterator();
            while (iterator.hasNext())
            {
                if (iterator.hasNext())
                {
                    DownloadTask task = iterator.next();
                    if (task.indexOfSegment >= 0 && task.isAudio)
                    {
                        iterator.remove();
                    }
                }
            }
        }
    }
    
    private void removeVideoSegmentTask()
    {
        synchronized (inDownloadingTask)
        {
            Iterator<DownloadTask> iterator = inDownloadingTask.iterator();
            while (iterator.hasNext())
            {
                if (iterator.hasNext())
                {
                    DownloadTask task = iterator.next();
                    if (task.indexOfSegment >= 0 && !task.isAudio)
                    {
                        iterator.remove();
                    }
                }
            }
        }
    }
    
    @Override
    public void switchBitrate(int bitrate, long timeMilli)
    {
        int indexOfAudioSegment = getIndexOfSegmentByTime(timeMilli, true);
        int indexOfVideoSegment = getIndexOfSegmentByTime(timeMilli, false);
        DebugLog.info(TAG, "switchBitrate :" + timeMilli);
        if (indexOfAudioSegment >= 0 && indexOfVideoSegment >= 0)
        {
            removeVideoSegmentTask();
            //    inDownloadingTask.clear();
            videoBufferEnd = 0;
            downloadRepresentInit(dashNode, bitrate, false);
            lastVideoSegementIndex = indexOfVideoSegment;
            //            lastAudioSegementIndex = indexOfAudioSegment;
        }
    }
    
    private void downloadRepresentInit(final DashNode dashNode, final int bitrate, boolean isAudio)
    {
        DashRepresentation represent = dashNode.getRepresentationByBitrate(bitrate, isAudio);
        DashSegmentBase base = represent.getSegment();
        if (base == null)
        {
            DebugLog.error(TAG, "downloadRepresentInit base null");
            return;
        }
        DashInitialization initialization = base.getInitialization();
        if (initialization == null)
        {
            DebugLog.error(TAG, "downloadRepresentInit initialization null");
            return;
        }
        
        List<Pair<String, String>> properties = new ArrayList<Pair<String, String>>();
        properties.add(new Pair<String, String>("Range", "bytes=" + initialization.getRange()));
        
        DownloadTask task = new DownloadTask();
        task.representation = represent;
        task.properties = properties;
        
        inDownloadingTask.add(task);
        if (isAudio)
        {
            currentAudioBitrate = bitrate;
            streamer.downloadAudioHead(task);
        }
        else
        {
            currentVideoBitrate = bitrate;
            streamer.downloadVideoHead(task);
        }
    }
    
    @Override
    public void onFinishTask(DownloadTask task, StreamResult result, ErrorCode errorCode)
    {
        DebugLog.info(TAG, "onFinishTask result:" + result);
        
        if (errorCode != ErrorCode.MEDIA_ERROR_OK && controllerCallback != null)
        {
            controllerCallback.onStreamError(errorCode);
            DebugLog.info(TAG, "onFinishTask errorCode:" + errorCode);
            return;
        }
        if (!inDownloadingTask.contains(task))
        {
            return;
        }
        
        switch (result.getReturnType())
        {
            case StreamResult.STREAM_RETURN_INDEX:
                inDownloadingTask.remove(task);
                onIndexReturn(result);
                break;
            
            case StreamResult.STREAM_RETURN_AUDIO_HEAD:
                inDownloadingTask.remove(task);
                onStreamHeadReturn(result, true);
                break;
            
            case StreamResult.STREAM_RETURN_VIDEO_HEAD:
                inDownloadingTask.remove(task);
                onStreamHeadReturn(result, false);
                break;
            
            case StreamResult.STREAM_RETURN_VIDEO_SEGMENT:
                if (onStreamSegmentReturn(result, false) != null)
                {
                    inDownloadingTask.remove(task);
                }
                break;
            
            case StreamResult.STREAM_RETURN_AUDIO_SEGMENT:
                if (onStreamSegmentReturn(result, true) != null)
                {
                    inDownloadingTask.remove(task);
                }
                break;
        }
        
    }
    
    private void onIndexReturn(StreamResult result)
    {
        byte[][] arrays = result.getBytePools();
        if (arrays != null && arrays.length == 1)
        {
            try
            {
                String xmlString = new String(arrays[0], "GB2312");
                dashNode = SerializerService.fromXml(DashNode.class, xmlString);
                dashNode.setUrlDirectory(getUrlDirectory());
                controllerCallback.onIndexOk();
                int videoBitrate = currentVideoBitrate;
                int audioBitrate = currentAudioBitrate;
                if (videoBitrate <= 0)
                {
                    DashRepresentation videoRepresent = dashNode.getDescription().getFirstVideoRepresentation();
                    videoBitrate = videoRepresent.getBandwidth();
                }
                if (audioBitrate <= 0)
                {
                    DashRepresentation audioRepresent = dashNode.getDescription().getFirstAudioRepresentation();
                    audioBitrate = audioRepresent.getBandwidth();
                }
                downloadRepresentInit(dashNode, videoBitrate, false);
                downloadRepresentInit(dashNode, audioBitrate, true);
            }
            catch (UnsupportedEncodingException e)
            {
                DebugLog.trace(TAG, e);
            }
            catch (PersistenceException e)
            {
                DebugLog.trace(TAG, e);
            }
        }
        else
        {
            throw new RuntimeException("bad in  return STREAM_RETURN_INDEX");
        }
        
    }
    
    private long getSegmentStartBufferOffset(SegmentIndexBox indexBox, int index)
    {
        final List<SegmentIndexBox.Entry> nodeList = indexBox.getEntries();
        int count = index < nodeList.size() ? index : nodeList.size() - 1;
        long total = 0;
        for (int iLoop = 0; iLoop <= count; iLoop++)
        {
            total += nodeList.get(iLoop).getReferencedSize();
        }
        return total;
    }
    
    private long getSegmentStartTimeOffset(SegmentIndexBox indexBox, int index)
    {
        final List<SegmentIndexBox.Entry> nodeList = indexBox.getEntries();
        int count = index < nodeList.size() ? index : nodeList.size() - 1;
        long total = 0;
        long timeScale = indexBox.getTimeScale();
        for (int iLoop = 0; iLoop <= count; iLoop++)
        {
            total += nodeList.get(iLoop).getSubsegmentDuration() * 1000000 / timeScale;
        }
        return total;
    }
    
    private DashSegment onStreamSegmentReturn(StreamResult result, boolean isAudio)
    {
        DebugLog.debug(TAG, "onStreamSegmentReturn :" + result + " isAudio:" + isAudio);
        DashSegment newSegment = null;
        byte[][] arrays = result.getBytePools();
        VideoBuffer videoBuffer = result.getVideoBuffer();
        
        if (videoBuffer != null)
        {
            int indexOfTrack = result.getSegmentindex();
            DashRepresentation represent = getCurrentRepresent(isAudio);
            final SegmentIndexBox segmentIndex = represent.getSegmentIndex();
            newSegment = new DashSegment(represent.getBandwidth(), indexOfTrack, this);
            newSegment.setIndexBox(segmentIndex);
            newSegment.setAudio(represent.isAudio());
            newSegment.setVideoBuffer(videoBuffer);
            //     long startTime = getSegmentStartTimeOffset(segmentIndex, indexOfTrack - 1);
            
            final SegmentIndexBox.Entry node = segmentIndex.getEntries().get(indexOfTrack);
            long durationTime = node.getSubsegmentDuration() * 1000000 / segmentIndex.getTimeScale();
            newSegment.parseDetail(videoBuffer.getSequenceByteArray(), durationTime);
            //   long durationTime = newSegment.getParsedDurationTime();
            if (segmentReceiver != null)
            {
                segmentReceiver.onReceive(newSegment, isAudio);
                videoBufferEnd = newSegment.getSegmentStartTime() + durationTime;
                if (videoBuffer.isFinished())
                {
                    DebugLog.debug(TAG,
                        "onStreamSegmentReturn videoBuffer  EndTime:" + videoBufferEnd + " indexOfTrack:"
                            + indexOfTrack);
                    currentDownloadingVideo = -1;
                    lastVideoSegementIndex = indexOfTrack;
                }
                else
                {
                    newSegment = null;
                    DebugLog.error(TAG, "onStreamSegmentReturn videoBuffer just update:" + videoBufferEnd);
                }
            }
        }
        else if (arrays != null && arrays.length == 1)
        {
            byte[] tempSegmentBuffer = arrays[0];
            int indexOfTrack = result.getSegmentindex();
            DashRepresentation represent = getCurrentRepresent(isAudio);
            final SegmentIndexBox segmentIndex = represent.getSegmentIndex();
            newSegment = new DashSegment(represent.getBandwidth(), indexOfTrack, this);
            newSegment.setIndexBox(segmentIndex);
            newSegment.setAudio(represent.isAudio());
            //   long startTime = getSegmentStartTimeOffset(segmentIndex, indexOfTrack - 1);
            
            final SegmentIndexBox.Entry node = segmentIndex.getEntries().get(indexOfTrack);
            
            long timeScale = segmentIndex.getTimeScale();
            long durationTime = node.getSubsegmentDuration() * 1000000 / timeScale;
            newSegment.parseDetail(tempSegmentBuffer, durationTime);
            
            if (segmentReceiver != null)
            {
                segmentReceiver.onReceive(newSegment, isAudio);
                if (isAudio)
                {
                    currentDownloadingAudio = -1;
                    lastAudioSegementIndex = indexOfTrack;
                    audioBufferEnd = newSegment.getSegmentStartTime() + newSegment.getDurationTime();
                    DebugLog.error(TAG,
                        "onStreamSegmentReturn audioBufferEndTime:" + audioBufferEnd + "segmen return:" + indexOfTrack);
                    
                }
            }
        }
        else
        {
            throw new RuntimeException("bad in  return STREAM_RETURN_VIDEO_SEGMENT");
        }
        return newSegment;
    }
    
    Runnable checkBufferRunnable = new Runnable()
    {
        
        @Override
        public void run()
        {
            while (needWork)
            {
                long bufferTime = calculateBuffer();
                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException e)
                {
                    DebugLog.trace(TAG, e);
                }
            }
        }
    };
    
    private long calculateBuffer()
    {
        DebugLog.debug(TAG,
            " buffer a:" + audioBufferEnd + "-" + audioPlaying + " v:" + videoBufferEnd + " -" + videoPlaying);
        long audioBufferTime = (audioBufferEnd - audioPlaying) / 1000;
        long videoBufferTime = (videoBufferEnd - videoPlaying) / 1000;
        long bufferTime = audioBufferTime < videoBufferTime ? audioBufferTime : videoBufferTime;
        if (controllerCallback != null)
        {
            //Logger.debug(TAG, " calculateBuffer onBuffering:" + bufferTime);
            controllerCallback.onBuffering(bufferTime);
        }
        
        if (currentDownloadingAudio < 0 && audioBufferTime < maxBufferTimeMill)
        {
            DebugLog.debug(TAG, " calculateAudioBuffer before:" + currentDownloadingAudio);
            
            currentDownloadingAudio = lastAudioSegementIndex + 1;
            int succeed = downloadSegment(lastAudioSegementIndex + 1, true);
            DebugLog.debug(TAG,
                " calculateAudioBuffer:" + videoBufferTime + " downlaodIndex:" + currentDownloadingAudio);
            if (succeed < 0)
            {
                currentDownloadingAudio = -1;
            }
        }
        
        if (currentDownloadingVideo < 0 && videoBufferTime < maxBufferTimeMill)
        {
            DebugLog.debug(TAG,
                " calculateVideoBuffer:" + videoBufferTime + " currentDownloadingVideo:" + currentDownloadingVideo
                    + "   lastVideoIndex:" + lastVideoSegementIndex);
            currentDownloadingVideo = lastVideoSegementIndex + 1;
            int succeed = downloadSegment(lastVideoSegementIndex + 1, false);
            if (succeed < 0)
            {
                currentDownloadingVideo = -1;
            }
            
        }
        return bufferTime;
    }
    
    private void onStreamHeadReturn(StreamResult result, boolean isAudio)
    {
        byte[][] arrays = result.getBytePools();
        if (arrays != null && arrays.length == 2)
        {
            DashRepresentation represent = getCurrentRepresent(isAudio);
            DebugLog.info(TAG,
                " onStreamHeadReturn isAudio:" + isAudio + " represent:" + represent + " byte3:" + arrays[0][3]);
            
            represent.setInitializationBuffer(arrays[0]);
            SegmentIndexBox segmentIndex = SerializerService.parseDashNode(arrays[1], SegmentIndexBox.class);
            segmentIndex.parseDetails();
            represent.setSegmentIndex(segmentIndex);
            if (isAudio)
            {
                audioBeginTimeDiff = segmentIndex.getEarliestPresentationTime() * 1000 / segmentIndex.getTimeScale();
                DebugLog.info(TAG, " audioBeginTimeDiff is:" + audioBeginTimeDiff);
            }
            else
            {
                videoBeginTimeDiff = segmentIndex.getEarliestPresentationTime() * 1000 / segmentIndex.getTimeScale();
                DebugLog.info(TAG, " videoBeginTimeDiff is:" + videoBeginTimeDiff);
            }
        }
        else
        {
            throw new RuntimeException("bad in  return STREAM_RETURN_VIDEO_HEAD");
        }
    }
    
    private int getIndexOfSegmentByTime(long time, boolean isAudio)
    {
        DashRepresentation represent = null;
        if (isAudio)
        {
            represent = dashNode.getRepresentationByBitrate(currentAudioBitrate, isAudio);
        }
        else
        {
            represent = dashNode.getRepresentationByBitrate(currentVideoBitrate, isAudio);
        }
        if (represent != null)
        {
            return represent.getIndexOfSegmentByTime(time);
        }
        else
        {
            return -1;
        }
    }
    
    @Override
    public void release()
    {
        needWork = false;
    }
    
    @Override
    public void onUpdateTask(DownloadTask task, StreamResult result, ErrorCode errorCode)
    {
        
    }
    
    @Override
    public void onSegmentError(ErrorCode code)
    {
        controllerCallback.onStreamError(code);
    }
    
    @Override
    public void setMaxBufferTimeMill(long timeMill)
    {
        maxBufferTimeMill = timeMill;
    }
    
}
