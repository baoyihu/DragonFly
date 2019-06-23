package com.baoyihu.dragonfly.streamer.hls;

import java.io.IOException;
import java.net.ConnectException;
import java.util.LinkedList;

import com.baoyihu.common.util.DebugLog;
import com.baoyihu.dragonfly.constant.ErrorCode;
import com.baoyihu.dragonfly.controller.DownloadTask;
import com.baoyihu.dragonfly.streamer.StreamInterface;
import com.baoyihu.dragonfly.streamer.StreamResult;
import com.baoyihu.dragonfly.streamer.StreamType;
import com.baoyihu.dragonfly.util.NetWork;

public class HlsStreamer implements StreamInterface
{
    public static final String TAG = "HlsStreamer";
    
    private LinkedList<Runnable> taskList = new LinkedList<Runnable>();
    
    private StreamCallBack callback = null;
    
    private StreamType type = null;
    
    private boolean needWork = false;
    
    public HlsStreamer(StreamType streamType)
    {
        this.type = streamType;
        needWork = true;
        DebugLog.info(TAG, "HlsStreamer onCreate:");
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
                        result.setUrl(task.url);
                        DebugLog.debug(TAG, "downloadIndex finish");
                        callback.onFinishTask(task, result, ErrorCode.MEDIA_ERROR_OK);
                    }
                }
                catch (IOException e)
                {
                    callback.onFinishTask(task, null, getNetError(e));
                }
            }
        });
        
        DebugLog.error(TAG, "not implements downloadIndex ");
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
    public void downloadVideoHead(DownloadTask task)
    {
        
        //   DebugLog.error(TAG, "not implements downloadVideoHead ");
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
                
                {
                    String dest = task.url;
                    try
                    {
                        byte[] videoBuffer = NetWork.downloadMp4(dest, task.properties);
                        
                        if (callback != null)
                        {
                            StreamResult result = new StreamResult(type, StreamResult.STREAM_RETURN_VIDEO_SEGMENT);
                            result.setBytePools(new byte[][] {videoBuffer});
                            result.setSegmentindex(task.indexOfSegment);
                            result.setStartTime(task.startTimeMilli);
                            DebugLog.debug(TAG, "downloadVideoSegment finish");
                            callback.onFinishTask(task, result, ErrorCode.MEDIA_ERROR_OK);
                        }
                    }
                    catch (IOException e)
                    {
                        callback.onFinishTask(task, null, getNetError(e));
                    }
                    //                    String dest = directory + represent.getBaseUrl();
                    //                    final StreamResult result = new StreamResult(type, StreamResult.STREAM_RETURN_VIDEO_SEGMENT);
                    //                    int byteCount = task.getRangeSize();
                    //                    VideoBuffer videoBuffer = new VideoBuffer(byteCount);
                    //                    videoBuffer.setRawBufferFrom(task.getRangeFrom());
                    //                    videoBuffer.setCallBack(task, result, callback);
                    //                    result.setVideoBuffer(videoBuffer);
                    //                    result.setSegmentindex(task.indexOfSegment);
                    //                    try
                    //                    {
                    //                        NetWork.downloadMp4MultiThread(dest, result, task.properties);
                    //                        
                    //                    }
                    //                    catch (IOException e)
                    //                    {
                    //                        callback.onFinishTask(task, null, getNetError(e));
                    //                    }
                }
                
            }
        });
        
        //   DebugLog.error(TAG, "not implements downloadVideoSegment ");
        
    }
    
    @Override
    public void downloadAudioHead(DownloadTask task)
    {
        
        //   DebugLog.error(TAG, "not implements downloadAudioHead ");
    }
    
    @Override
    public void downloadAudioSegment(DownloadTask task)
    {
        
        //   DebugLog.error(TAG, "not implements downloadAudioSegment ");
    }
    
    @Override
    public void setCallBack(StreamCallBack callback)
    {
        this.callback = callback;
        DebugLog.info(TAG, "setCallBack :" + callback);
    }
    
}
