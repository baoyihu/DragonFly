package com.baoyihu.dragonfly.node.dash;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import com.baoyihu.dragonfly.node.MediaDescription;

@Root(name = "MPD", strict = false)
public class DashNode
{
    
    @Attribute(name = "mediaPresentationDuration", required = false)
    private String mediaPresentationDuration;//"PT0H47M27S"
    
    @Element(name = "Period", required = true)
    private DashPeriod period;
    
    private long duration = 0L;
    
    private MediaDescription description = null;
    
    public DashPeriod getPeriod()
    {
        return period;
    }
    
    public void setPeriod(DashPeriod period)
    {
        this.period = period;
    }
    
    private String urlDirectory = null;
    
    /**
     */
    public long getDurationMilli()
    {
        long time = 0;
        if (duration == 0L)
        {
            String temp = mediaPresentationDuration;
            int index0 = temp.indexOf("PT");
            if (index0 >= 0)
            {
                temp = temp.substring(index0 + 2);
            }
            
            int index1 = temp.indexOf("H");
            if (index1 >= 0)
            {
                time += Integer.parseInt(temp.substring(0, index1)) * 3600;
                temp = temp.substring(index1 + 1);
            }
            int index2 = temp.indexOf("M");
            if (index2 >= 0)
            {
                time += Integer.parseInt(temp.substring(0, index2)) * 60;
                temp = temp.substring(index2 + 1);
            }
            int index3 = temp.indexOf("S");
            if (index3 >= 0)
            {
                time += Integer.parseInt(temp.substring(0, index3));
            }
            time = time * 1000;
            duration = time;
        }
        return duration;
    }
    
    public String getUrlDirectory()
    {
        return urlDirectory;
    }
    
    public void setUrlDirectory(String dashUrl)
    {
        this.urlDirectory = dashUrl;
    }
    
    public MediaDescription getDescription()
    {
        if (description == null)
        {
            description = new MediaDescription(this);
        }
        return description;
    }
    
    public DashRepresentation getVideoRepresentationById(String id)
    {
        MediaDescription desc = getDescription();
        return desc.getVideoRepresentationById(id);
    }
    
    public DashRepresentation getRepresentationByBitrate(int bitrate, boolean isAudio)
    {
        MediaDescription desc = getDescription();
        if (isAudio)
        {
            return desc.getAudioRepresentationByBitrate(bitrate);
        }
        else
        {
            return desc.getVideoRepresentationByBitrate(bitrate);
        }
    }
    
}
