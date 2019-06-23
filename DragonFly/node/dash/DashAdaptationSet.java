package com.baoyihu.dragonfly.node.dash;

import java.util.ArrayList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import android.text.TextUtils;

@Root(name = "AdaptationSet", strict = false)
public class DashAdaptationSet
{
    
    @Attribute(name = "id", required = false)
    private int id;//="1";
    
    @Attribute(name = "group", required = false)
    private int group;//="1"
    
    @Attribute(name = "startWithSAP", required = false)
    private int startWithSAP;
    
    @Attribute(name = "par", required = false)
    private String par = "4:3";
    
    @Attribute(name = "subsegmentStartsWithSAP", required = false)
    private int subsegmentStartsWithSAP;//="1"
    
    @Attribute(name = "segmentAlignment", required = false)
    private boolean segmentAlignment;//="true"
    
    @Attribute(name = "subsegmentAlignment", required = false)
    private boolean subsegmentAlignment;//="true"
    
    @Attribute(name = "contentType", required = false)
    private String contentType;//="audio"
    
    @Attribute(name = "lang", required = false)
    private String lang;//="und"
    
    @Attribute(name = "mimeType", required = false)
    private String mimeType;//= "audio/mp4";
    
    @ElementList(name = "Representation", required = false, inline = true)
    private ArrayList<DashRepresentation> representation;
    
    public int getId()
    {
        
        return id;
    }
    
    public void setId(int id)
    {
        this.id = id;
    }
    
    public int getGroup()
    {
        return group;
    }
    
    public void setGroup(int group)
    {
        this.group = group;
    }
    
    public int getSubsegmentStartsWithSAP()
    {
        return subsegmentStartsWithSAP;
    }
    
    public void setSubsegmentStartsWithSAP(int subsegmentStartsWithSAP)
    {
        this.subsegmentStartsWithSAP = subsegmentStartsWithSAP;
    }
    
    public boolean isSegmentAlignment()
    {
        return segmentAlignment;
    }
    
    public void setSegmentAlignment(boolean segmentAlignment)
    {
        this.segmentAlignment = segmentAlignment;
    }
    
    public boolean isSubsegmentAlignment()
    {
        return subsegmentAlignment;
    }
    
    public void setSubsegmentAlignment(boolean subsegmentAlignment)
    {
        this.subsegmentAlignment = subsegmentAlignment;
    }
    
    public String getContentType()
    {
        return contentType;
    }
    
    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }
    
    public String getLang()
    {
        return lang;
    }
    
    public void setLang(String lang)
    {
        this.lang = lang;
    }
    
    public String getMimeType()
    {
        return mimeType;
    }
    
    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }
    
    public ArrayList<DashRepresentation> getRepresentation()
    {
        return representation;
    }
    
    public void setRepresentation(ArrayList<DashRepresentation> representation)
    {
        this.representation = representation;
    }
    
    public TrackType getTrackType()
    {
        String tag = null;
        if (!TextUtils.isEmpty(mimeType))
        {
            tag = mimeType;
        }
        else if (!TextUtils.isEmpty(contentType))
        {
            tag = contentType;
        }
        if (!TextUtils.isEmpty(tag))
        {
            if (tag.startsWith("video"))
            {
                return TrackType.Video;
            }
            else if (tag.startsWith("audio"))
            {
                return TrackType.Audio;
            }
            
        }
        return TrackType.UnSupport;
    }
}
