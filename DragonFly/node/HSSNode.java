package com.baoyihu.dragonfly.node;

import java.util.ArrayList;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "SmoothStreamingMedia", strict = false)
public class HSSNode
{
    @ElementList(required = true, name = "StreamIndex", inline = true)
    private ArrayList<StreamIndex> streamList;
    
    public ArrayList<StreamIndex> getStreamList()
    {
        return streamList;
    }
    
    public void setStreamList(ArrayList<StreamIndex> streamList)
    {
        this.streamList = streamList;
    }
    
}
