package com.baoyihu.dragonfly.node;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "QualityLevel", strict = false)
public class QualityLevel
{
    @Attribute(required = false, name = "Index")
    String index;
    
    @Attribute(required = false, name = "Bitrate")
    String bitrate;
    
    @Attribute(required = false, name = "FourCC")
    String cc;
    
    @Attribute(required = false, name = "MaxWidth")
    String width;
    
    @Attribute(required = false, name = "MaxHeight")
    String height;
    
    public String getIndex()
    {
        return index;
    }
    
    public void setIndex(String index)
    {
        this.index = index;
    }
    
    public String getBitrate()
    {
        return bitrate;
    }
    
    public void setBitrate(String bitrate)
    {
        this.bitrate = bitrate;
    }
    
    public String getCc()
    {
        return cc;
    }
    
    public void setCc(String cc)
    {
        this.cc = cc;
    }
    
    public String getWidth()
    {
        return width;
    }
    
    public void setWidth(String width)
    {
        this.width = width;
    }
    
    public String getHeight()
    {
        return height;
    }
    
    public void setHeight(String height)
    {
        this.height = height;
    }
    
}
