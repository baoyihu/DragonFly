package com.baoyihu.dragonfly.streamer;

import java.util.ArrayList;
import java.util.List;

import com.baoyihu.dragonfly.constant.ErrorCode;
import com.baoyihu.dragonfly.controller.DownloadTask;
import com.baoyihu.dragonfly.streamer.StreamInterface.StreamCallBack;

public class VideoBuffer
{
    public static final String TAG = "VideoBuffer";
    
    private long rawBufferFrom = 0;
    
    public long getRawBufferFrom()
    {
        return rawBufferFrom;
    }
    
    public void setRawBufferFrom(long rawBufferFrom)
    {
        this.rawBufferFrom = rawBufferFrom;
    }
    
    public byte[] raw;
    
    public byte[] getSequenceByteArray()
    {
        if (stocks.isEmpty())
        {
            return raw;
        }
        else
        {
            ByteStock firstStock = stocks.get(0);
            int indexEnd = firstStock.getFilled() + firstStock.getFrom();
            byte[] ret = new byte[indexEnd];
            System.arraycopy(raw, 0, ret, 0, indexEnd);
            return ret;
        }
    }
    
    private final List<ByteStock> stocks;
    
    private boolean finished = false;
    
    private StreamCallBack callback;
    
    private DownloadTask task;
    
    private StreamResult result;
    
    public void setCallBack(DownloadTask task, StreamResult result, StreamCallBack callback)
    {
        this.task = task;
        this.result = result;
        this.callback = callback;
    }
    
    public boolean isFinished()
    {
        return finished;
    }
    
    public void onBufferUpdate(ByteStock stock)
    {
        //     DebugLog.info(TAG, "onBufferUpdate " + stock.getName() + " :" + getSequenceByteArray().length);
        //FIXME we need to fix it latter          callback.onUpdateTask(task, result, ErrorCode.MEDIA_ERROR_OK);
        //   callback.onFinishTask(task, result, ErrorCode.MEDIA_ERROR_OK);
        
    }
    
    public void onBufferFull(ByteStock stock)
    {
        //      DebugLog.info(TAG, "onBufferFull " + stock.getName() + " :" + raw.length);
        stocks.remove(stock);
        if (stocks.isEmpty())
        {
            //          DebugLog.info(TAG, "VideoBuffer isFinished ");
            finished = true;
            callback.onFinishTask(task, result, ErrorCode.MEDIA_ERROR_OK);
        }
        else
        {
            //        DebugLog.info(TAG, "VideoBuffer update ");
            callback.onFinishTask(task, result, ErrorCode.MEDIA_ERROR_OK);
        }
    }
    
    public void addStock(ByteStock stock)
    {
        stocks.add(stock);
    }
    
    public int getCapcity()
    {
        return raw.length;
    }
    
    public VideoBuffer(int count)
    {
        raw = new byte[count];
        stocks = new ArrayList<ByteStock>();
    }
    
    //    public static void main(String[] args)
    //    {
    //        VideoBuffer videoBuffer = new VideoBuffer(62);
    //        int readerCount = 3;
    //        int allcocated = 0;
    //        int rent = videoBuffer.getCapcity() / readerCount;
    //        for (int index = 0; index < readerCount; index++)
    //        {
    //            if (index == readerCount - 1)
    //            {
    //                rent = videoBuffer.getCapcity() - allcocated;
    //            }
    //            ByteStock stock = new ByteStock(videoBuffer, allcocated, rent);
    //            stock.setName("Stock" + index);
    //            Testor testor = new Testor(stock);
    //            videoBuffer.addStock(stock);
    //            testor.start();
    //            allcocated += rent;
    //        }
    //    }
}
