package com.baoyihu.dragonfly.streamer.dash;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.baoyihu.common.util.DebugLog;
import com.baoyihu.common.util.Pair;
import com.baoyihu.dragonfly.constant.ErrorCode;
import com.baoyihu.dragonfly.controller.DownloadTask;
import com.baoyihu.dragonfly.node.dash.DashRepresentation;
import com.baoyihu.dragonfly.streamer.StreamInterface;
import com.baoyihu.dragonfly.streamer.StreamResult;
import com.baoyihu.dragonfly.streamer.StreamType;
import com.baoyihu.dragonfly.streamer.VideoBuffer;
import com.baoyihu.dragonfly.util.NetWork;

public class DashStreamer implements StreamInterface
{
    private static final String TAG = "DashStreamer";
    
    private LinkedList<Runnable> taskList = new LinkedList<Runnable>();
    
    private boolean needWork = false;
    
    private StreamType type = null;
    
    public DashStreamer(StreamType type)
    {
        this.type = type;
        needWork = true;
        //     System.setProperty("java.net.preferIPv4Stack", "true");
        //    System.setProperty("java.net.preferIPv6Addresses", "false");
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                runLoop();
            }
        }).start();
    }
    
    public void runLoop()
    {
        while (needWork)
        {
            Runnable task = taskList.poll();
            if (task != null)
            {
                task.run();
            }
            else
            {
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
    }
    
    public void release()
    {
        needWork = false;
    }
    
    @Override
    public void downloadIndex(final DownloadTask task)
    {
        taskList.add(new Runnable()
        {
            @Override
            public void run()
            {
                DebugLog.info(TAG, "downloadIndex");
                byte[] temp;
                try
                {
                    temp = NetWork.downloadToByteArray(task.url);
                    if (callback != null)
                    {
                        StreamResult result = new StreamResult(type, StreamResult.STREAM_RETURN_INDEX);
                        result.setBytePools(new byte[][] {temp});
                        DebugLog.debug(TAG, "downloadIndex finish");
                        callback.onFinishTask(task, result, ErrorCode.MEDIA_ERROR_OK);
                    }
                }
                catch (IOException e)
                {
                    callback.onFinishTask(task, null, getNetError(e));
                }
                
                //Logger.info(TAG, "dashNode:" + dashNode);
                
            }
        });
        
    }
    
    private ErrorCode getNetError(IOException exectption)
    {
        if (exectption instanceof ConnectException)
        {
            String detail = exectption.getMessage();
            if (detail.contains("ENETUNREACH"))
            {
                return ErrorCode.MEDIA_ERROR_NET_UNREACHABLE;
            }
            else if (detail.contains("ECONNREFUSED"))
            {
                return ErrorCode.MEDIA_ERROR_NET_CONNREFUSED;
            }
            else
            {
                return ErrorCode.MEDIA_ERROR_UNKNOWN;
            }
        }
        else
        {
            DebugLog.error(TAG, "getNetError not Defined:" + exectption.toString());
            return ErrorCode.MEDIA_ERROR_NET_UNREACHABLE;
        }
    }
    
    @Override
    public void downloadVideoHead(final DownloadTask task)
    {
        taskList.add(new Runnable()
        {
            @Override
            public void run()
            {
                DebugLog.info(TAG, "downloadVideoHead");
                byte[] ddd = null;
                String directory = task.representation.getRootUrl();
                if (directory != null)
                {
                    String dest = directory + task.representation.getBaseUrl();
                    try
                    {
                        ddd = NetWork.downloadMp4(dest, task.properties);
                        List<Pair<String, String>> properties = new ArrayList<Pair<String, String>>();
                        String rangeString = task.representation.getSegment().getIndexRange();
                        
                        properties.add(new Pair<String, String>("Range", "bytes=" + rangeString));
                        byte[] segmentIndexBuffer = NetWork.downloadMp4(dest, properties);
                        
                        if (callback != null)
                        {
                            StreamResult result = new StreamResult(type, StreamResult.STREAM_RETURN_VIDEO_HEAD);
                            result.setBytePools(new byte[][] {ddd, segmentIndexBuffer});
                            DebugLog.debug(TAG, "downloadVideoHead finish");
                            callback.onFinishTask(task, result, ErrorCode.MEDIA_ERROR_OK);
                        }
                    }
                    catch (IOException e)
                    {
                        callback.onFinishTask(task, null, getNetError(e));
                    }
                    
                }
                else
                {
                    DebugLog.error(TAG, "url is wrong:" + directory);
                }
            }
        });
    }
    
    @Override
    public void downloadVideoSegment(final DownloadTask task)
    {
        taskList.add(new Runnable()
        {
            @Override
            public void run()
            {
                DebugLog.info(TAG, "downloadVideoSegment");
                DashRepresentation represent = task.representation;
                
                String directory = represent.getRootUrl();
                if (directory != null)
                {
                    String dest = directory + represent.getBaseUrl();
                    final StreamResult result = new StreamResult(type, StreamResult.STREAM_RETURN_VIDEO_SEGMENT);
                    int byteCount = task.getRangeSize();
                    VideoBuffer videoBuffer = new VideoBuffer(byteCount);
                    videoBuffer.setRawBufferFrom(task.getRangeFrom());
                    videoBuffer.setCallBack(task, result, callback);
                    result.setVideoBuffer(videoBuffer);
                    result.setSegmentindex(task.indexOfSegment);
                    try
                    {
                        NetWork.downloadMp4MultiThread(dest, result, task.properties);
                        
                    }
                    catch (IOException e)
                    {
                        callback.onFinishTask(task, null, getNetError(e));
                    }
                }
                else
                {
                    DebugLog.error(TAG, "url is wrong:" + directory);
                }
            }
        });
        
    }
    
    @Override
    public void downloadAudioHead(final DownloadTask task)
    {
        taskList.add(new Runnable()
        {
            @Override
            public void run()
            {
                DebugLog.debug(TAG, "downloadAudioHead ");
                byte[] ddd = null;
                String directory = task.representation.getRootUrl();
                if (directory != null)
                {
                    String dest = directory + task.representation.getBaseUrl();
                    try
                    {
                        ddd = NetWork.downloadMp4(dest, task.properties);
                        
                        List<Pair<String, String>> properties = new ArrayList<Pair<String, String>>();
                        String rangeString = task.representation.getSegment().getIndexRange();
                        
                        properties.add(new Pair<String, String>("Range", "bytes=" + rangeString));
                        byte[] segmentIndexBuffer = NetWork.downloadMp4(dest, properties);
                        
                        if (callback != null)
                        {
                            StreamResult result = new StreamResult(type, StreamResult.STREAM_RETURN_AUDIO_HEAD);
                            result.setBytePools(new byte[][] {ddd, segmentIndexBuffer});
                            DebugLog.debug(TAG, "downloadAudioHead finish");
                            callback.onFinishTask(task, result, ErrorCode.MEDIA_ERROR_OK);
                        }
                    }
                    catch (IOException e)
                    {
                        callback.onFinishTask(task, null, getNetError(e));
                    }
                }
                else
                {
                    DebugLog.error(TAG, "url is wrong:" + directory);
                }
            }
        });
    }
    
    @Override
    public void downloadAudioSegment(final DownloadTask task)
    {
        taskList.add(new Runnable()
        {
            @Override
            public void run()
            {
                DebugLog.debug(TAG, "downloadAudioSegment");
                DashRepresentation represent = task.representation;
                
                String directory = represent.getRootUrl();
                if (directory != null)
                {
                    String dest = directory + represent.getBaseUrl();
                    byte[] tempSegmentBuffer;
                    try
                    {
                        tempSegmentBuffer = NetWork.downloadMp4(dest, task.properties);
                        if (callback != null)
                        {
                            StreamResult result = new StreamResult(type, StreamResult.STREAM_RETURN_AUDIO_SEGMENT);
                            result.setBytePools(new byte[][] {tempSegmentBuffer});
                            result.setSegmentindex(task.indexOfSegment);
                            DebugLog.debug(TAG, "downloadAudioSegment finish");
                            callback.onFinishTask(task, result, ErrorCode.MEDIA_ERROR_OK);
                        }
                    }
                    catch (IOException e)
                    {
                        callback.onFinishTask(task, null, ErrorCode.MEDIA_ERROR_NET_UNREACHABLE);
                        DebugLog.trace(TAG, e);
                    }
                }
                else
                {
                    DebugLog.error(TAG, "url is wrong:" + directory);
                }
                
            }
        });
        
    }
    
    StreamCallBack callback = null;
    
    @Override
    public void setCallBack(StreamCallBack callback)
    {
        this.callback = callback;
    }
    
}
