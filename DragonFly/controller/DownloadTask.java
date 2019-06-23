package com.baoyihu.dragonfly.controller;

import java.util.Arrays;
import java.util.List;

import com.baoyihu.common.util.Pair;
import com.baoyihu.dragonfly.node.dash.DashRepresentation;
import com.baoyihu.dragonfly.streamer.StreamType;

public class DownloadTask
{
    public StreamType streamType;
    
    public String url;
    
    public int indexOfSegment = -1;
    
    public boolean isAudio;
    
    public DashRepresentation representation;
    
    public List<Pair<String, String>> properties;
    
    public long startTimeMilli = 0;//for VOD this is 
    
    public int getRangeSize()
    {
        for (Pair<String, String> pair : properties)
        {
            if (pair.first.equals("Range"))
            {
                String temp = pair.second;
                temp = temp.substring(temp.indexOf("=") + 1);
                String[] array = temp.split("-");
                int pre = Integer.valueOf(array[0]);
                int next = Integer.valueOf(array[1]);
                return next - pre + 1;
            }
        }
        return 0;
    }
    
    public int getRangeFrom()
    {
        for (Pair<String, String> pair : properties)
        {
            if (pair.first.equals("Range"))
            {
                String temp = pair.second;
                temp = temp.substring(temp.indexOf("=") + 1);
                String[] array = temp.split("-");
                int pre = Integer.valueOf(array[0]);
                return pre;
            }
        }
        return 0;
    }
    
    @Override
    public String toString()
    {
        return "DownloadTask type:" + streamType + " url:" + url + " indexOfSegment:" + indexOfSegment + " properties"
            + Arrays.toString(properties.toArray()) + " representation:" + representation;
    }
}
