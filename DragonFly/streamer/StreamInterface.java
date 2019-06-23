package com.baoyihu.dragonfly.streamer;

import com.baoyihu.dragonfly.constant.ErrorCode;
import com.baoyihu.dragonfly.controller.DownloadTask;

public interface StreamInterface
{
    public void downloadIndex(DownloadTask task);
    
    public void downloadVideoHead(DownloadTask task);
    
    public void downloadVideoSegment(DownloadTask task);
    
    public void downloadAudioHead(DownloadTask task);
    
    public void downloadAudioSegment(DownloadTask task);
    
    public void setCallBack(StreamCallBack callback);
    
    public interface StreamCallBack
    {
        public void onUpdateTask(DownloadTask task, StreamResult result, ErrorCode errorCode);
        
        public void onFinishTask(DownloadTask task, StreamResult result, ErrorCode errorCode);
    }
}
