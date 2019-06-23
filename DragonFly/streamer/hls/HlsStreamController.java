package com.baoyihu.dragonfly.streamer.hls;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.baoyihu.common.util.DebugLog;
import com.baoyihu.dragonfly.constant.ErrorCode;
import com.baoyihu.dragonfly.controller.DownloadTask;
import com.baoyihu.dragonfly.controller.HlsSegment;
import com.baoyihu.dragonfly.controller.SegmentInterface;
import com.baoyihu.dragonfly.node.hls.Info;
import com.baoyihu.dragonfly.node.hls.M3u8;
import com.baoyihu.dragonfly.node.hls.StreamInfo;
import com.baoyihu.dragonfly.render.RenderInterface;
import com.baoyihu.dragonfly.streamer.StreamControllerCallback;
import com.baoyihu.dragonfly.streamer.StreamControllerInterface;
import com.baoyihu.dragonfly.streamer.StreamInterface;
import com.baoyihu.dragonfly.streamer.StreamResult;
import com.baoyihu.dragonfly.streamer.StreamType;

import android.media.MediaFormat;

public class HlsStreamController
    implements StreamInterface.StreamCallBack, StreamControllerInterface, SegmentInterface.SegmentControllerCallback
{
    private static final String TAG = "HlsStreamController";
    
    private RenderInterface segmentReceiver = null;
    
    private String url = null;
    
    private List<DownloadTask> inDownloadingTask = new ArrayList<DownloadTask>();
    
    private StreamInterface streamer = null;
    
    private boolean needWork = true;
    
    public HlsStreamController()
    {
        streamer = new HlsStreamer(StreamType.HLS);
        streamer.setCallBack(this);
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
            
            case StreamResult.STREAM_RETURN_VIDEO_SEGMENT:
                DebugLog.info(TAG, "on video segment result:" + result);
                onStreamSegmentReturn(result);
                inDownloadingTask.remove(task);
                break;
        }
        DebugLog.error(TAG, "not implement onFinishTask");
    }
    
    private String mimeAudio = null;
    
    private String mimeVideo = null;
    
    private MediaFormat formatAudio = null;
    
    private MediaFormat formatVideo = null;
    
    private SegmentInterface onStreamSegmentReturn(StreamResult result)
    {
        DebugLog.debug(TAG, "onStreamSegmentReturn :" + result);
        HlsSegment newSegment = null;
        byte[][] arrays = result.getBytePools();
        
        if (arrays != null && arrays.length == 1)
        {
            byte[] tempSegmentBuffer = arrays[0];
            int indexOfTrack = result.getSegmentindex();
            M3u8 currentM3u8 = getCurrentM3u8();
            
            if (segmentReceiver != null)
            {
                newSegment = new HlsSegment(currentM3u8.getBandwidth(), indexOfTrack, this);
                newSegment.setAudio(true);
                newSegment.setSegmentStartTime(result.getStartTime() * 1000);
                newSegment.parseDetail(tempSegmentBuffer, currentM3u8.getTargetDuration());
                mimeAudio = newSegment.getMimeAudio();
                formatAudio = newSegment.getFormatAudio();
                segmentReceiver.onReceive(newSegment, true);
                
                audioBufferEnd = newSegment.getSegmentStartTime() + newSegment.getDurationTime();
                DebugLog.debug(TAG,
                    "onStreamSegmentReturn audioBufferEndTime:" + audioBufferEnd + "segmen return:" + indexOfTrack
                        + " segmentStart:" + newSegment.getSegmentStartTime() + " duration:"
                        + newSegment.getDurationTime());
                
                newSegment = new HlsSegment(currentM3u8.getBandwidth(), indexOfTrack, this);
                newSegment.setAudio(false);
                newSegment.setSegmentStartTime(result.getStartTime() * 1000);
                newSegment.parseDetail(tempSegmentBuffer, currentM3u8.getTargetDuration());
                mimeVideo = newSegment.getMimeVideo();
                formatVideo = newSegment.getFormatVideo();
                segmentReceiver.onReceive(newSegment, false);
                videoBufferEnd = newSegment.getSegmentStartTime() + newSegment.getDurationTime();
                DebugLog.debug(TAG,
                    "onStreamSegmentReturn videoBufferEnd:" + videoBufferEnd + "segmen return:" + indexOfTrack
                        + " segmentStart:" + newSegment.getSegmentStartTime() + " duration:"
                        + newSegment.getDurationTime());
                
                lastSegementIndex = indexOfTrack;
                currentDownloading = -1;
            }
        }
        else
        {
            throw new RuntimeException("bad in  return STREAM_RETURN_VIDEO_SEGMENT");
        }
        return newSegment;
    }
    
    private void onIndexReturn(StreamResult result)
    {
        byte[][] arrays = result.getBytePools();
        if (arrays != null && arrays.length == 1)
        {
            try
            {
                String m3u8String = new String(arrays[0], "GB2312");
                M3u8 tempM3u8 = new M3u8(result.getUrl(), m3u8String, null);
                
                StreamInfo firstInfo = tempM3u8.getfirstStreamInfo();
                if (firstInfo != null)
                {
                    rootM3u8 = tempM3u8;
                    currentVideoBitrate = firstInfo.getBandwidth();
                    DownloadTask task = new DownloadTask();
                    task.url = url.substring(0, url.lastIndexOf("/") + 1) + firstInfo.getUrl();
                    inDownloadingTask.add(task);
                    streamer.downloadIndex(task);
                }
                else
                {
                    if (rootM3u8 != null)
                    {
                        rootM3u8.setSubM3u8(currentVideoBitrate, tempM3u8);
                    }
                    controllerCallback.onIndexOk();
                }
            }
            catch (UnsupportedEncodingException e)
            {
                DebugLog.trace(TAG, e);
            }
        }
        else
        {
            throw new RuntimeException("bad in  return STREAM_RETURN_INDEX");
        }
        
    }
    
    @Override
    public void onUpdateTask(DownloadTask task, StreamResult result, ErrorCode errorCode)
    {
        // TODO Auto-generated method stub
        DebugLog.error(TAG, "not implement onUpdateTask");
    }
    
    @Override
    public void onSegmentError(ErrorCode code)
    {
        controllerCallback.onStreamError(code);
    }
    
    @Override
    public List<Integer> getBitrateList()
    {
        List<Integer> ret = new ArrayList<Integer>();
        if (rootM3u8 != null)
        {
            for (StreamInfo streamInfo : rootM3u8.getStreamInfoList())
            {
                ret.add(streamInfo.getBandwidth());
            }
        }
        return ret;
    }
    
    @Override
    public void setCallBack(StreamControllerCallback callback)
    {
        controllerCallback = callback;
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
    
    private long audioBufferEnd = 0;
    
    private long videoBufferEnd = 0;
    
    private long audioPlaying = 0;
    
    private long videoPlaying = 0;
    
    private StreamControllerCallback controllerCallback = null;
    
    private int currentDownloading = -1;
    
    private long maxBufferTimeMill = 10 * 1000;
    
    private int lastSegementIndex = -1;
    
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
        
        if (currentDownloading < 0 && audioBufferTime < maxBufferTimeMill)
        {
            DebugLog.debug(TAG,
                " calculateAudioBuffer before:" + currentDownloading + " lastSegementIndex:" + lastSegementIndex);
            currentDownloading = lastSegementIndex + 1;
            int succeed = downloadSegment(lastSegementIndex + 1);
            DebugLog.debug(TAG, " calculateAudioBuffer:" + videoBufferTime + " downlaodIndex:" + currentDownloading);
            if (succeed < 0)
            {
                currentDownloading = -1;
            }
        }
        
        return bufferTime;
    }
    
    M3u8 rootM3u8 = null;
    
    int currentVideoBitrate = 0;
    
    private M3u8 getCurrentM3u8()
    {
        if (rootM3u8 != null)
        {
            return rootM3u8.getSubM3u8(currentVideoBitrate);
        }
        else
        {
            return null;
        }
    }
    
    private synchronized int downloadSegment(final int indexOfTrack)
    {
        final M3u8 currentM3u8 = getCurrentM3u8();
        if (currentM3u8 == null)
        {
            return -1;
        }
        DebugLog.debug(TAG, "downloadSegment index:" + indexOfTrack);
        List<Info> infoList = currentM3u8.getInfoList();
        if (!infoList.isEmpty() && infoList.size() > indexOfTrack)
        {
            String baseUrl = currentM3u8.getUrl();
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf('/') + 1);
            Info info = infoList.get(indexOfTrack);
            DownloadTask task = new DownloadTask();
            task.properties = null;
            task.representation = null;
            task.indexOfSegment = indexOfTrack;
            task.isAudio = false;
            task.url = baseUrl + info.getUrl();
            task.startTimeMilli = currentM3u8.getBeginTime(indexOfTrack);
            inDownloadingTask.add(task);
            
            DebugLog.info(TAG, " downloadSegment currentDownloadingAudio:" + currentDownloading);
            streamer.downloadVideoSegment(task);
        }
        
        //        String rangeString = represent.getSegment().getIndexRange();
        //        String[] array = rangeString.split("-");
        //        int indexToVideo = Integer.parseInt(array[1].trim());
        //        
        //        long from = getSegmentStartBufferOffset(segmentIndex, indexOfTrack - 1) + indexToVideo + 1;
        //        long to = node.getReferencedSize() + from - 1;
        //        List<Pair<String, String>> properties = new ArrayList<Pair<String, String>>();
        //        properties.add(new Pair<String, String>("Range", "bytes=" + from + "-" + to));
        
        return 0;
    }
    
    @Override
    public void seekTo(long timeMilli)
    {
        // TODO Auto-generated method stub
        DebugLog.error(TAG, "not implement seekTo");
    }
    
    @Override
    public void switchBitrate(int bitrate, long timeMilli)
    {
        // TODO Auto-generated method stub
        DebugLog.error(TAG, "not implement switchBitrate");
    }
    
    @Override
    public long getDuration()
    {
        long ret = 0L;
        if (rootM3u8 != null)
        {
            StreamInfo streamInfo = rootM3u8.getfirstStreamInfo();
            if (streamInfo != null)
            {
                M3u8 subM3u8 = rootM3u8.getSubM3u8(streamInfo.getBandwidth());
                ret = subM3u8.getDurationMill();
            }
        }
        return ret;
    }
    
    @Override
    public int getCurrentBitrate()
    {
        return currentVideoBitrate;
    }
    
    @Override
    public MediaFormat getVideoMediaFormat()
    {
        return formatVideo;
    }
    
    @Override
    public String getMime(boolean isAudio)
    {
        if (isAudio)
        {
            return mimeAudio;
        }
        else
        {
            return mimeVideo;
        }
    }
    
    private long beginTimeDiff = 0;
    
    @Override
    public long getVideoBeginTimeDiff()
    {
        //FIXME need check        
        return beginTimeDiff;
    }
    
    @Override
    public long getAudioBeginTimeDiff()
    {
        //FIXME need check
        return beginTimeDiff;
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
    
    @Override
    public MediaFormat getAudioMediaFormat()
    {
        return formatAudio;
    }
    
    @Override
    public int getAudioSamplingRate()
    {
        int ret = 0;
        if (formatAudio != null)
        {
            ret = formatAudio.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        }
        return ret;
    }
    
    @Override
    public void setSegmentReceiver(RenderInterface receiver)
    {
        segmentReceiver = receiver;
    }
    
    @Override
    public void setMaxBufferTimeMill(long timeMill)
    {
        maxBufferTimeMill = timeMill;
    }
    
    @Override
    public void release()
    {
        needWork = false;
    }
    
}
