package com.baoyihu.dragonfly.node;

import java.util.ArrayList;
import java.util.List;

import com.baoyihu.dragonfly.node.dash.DashAdaptationSet;
import com.baoyihu.dragonfly.node.dash.DashNode;
import com.baoyihu.dragonfly.node.dash.DashRepresentation;
import com.baoyihu.dragonfly.node.dash.TrackType;

public class MediaDescription
{
    private List<DashRepresentation> videoList = new ArrayList<DashRepresentation>();
    
    private List<DashRepresentation> audioList = new ArrayList<DashRepresentation>();
    
    public MediaDescription(DashNode dashNode)
    {
        videoList.clear();
        audioList.clear();
        List<DashAdaptationSet> list = dashNode.getPeriod().getAdaptationSet();
        for (DashAdaptationSet set : list)
        {
            TrackType trackType = set.getTrackType();
            
            if (trackType == TrackType.Video)
            {
                if (set.getRepresentation() != null && !set.getRepresentation().isEmpty())
                {
                    for (DashRepresentation temp : set.getRepresentation())
                    {
                        temp.setRootUrl(dashNode.getUrlDirectory());
                        videoList.add(temp);
                    }
                }
            }
            else if (trackType == TrackType.Audio)
            {
                if (set.getRepresentation() != null && !set.getRepresentation().isEmpty())
                {
                    for (DashRepresentation temp : set.getRepresentation())
                    {
                        temp.setRootUrl(dashNode.getUrlDirectory());
                        audioList.add(temp);
                    }
                }
            }
        }
        
    }
    
    public List<DashRepresentation> getVideoList()
    {
        return videoList;
    }
    
    public List<DashRepresentation> getAudioList()
    {
        return audioList;
    }
    
    public DashRepresentation getFirstAudioRepresentation()
    {
        DashRepresentation ret = null;
        if (!audioList.isEmpty())
        {
            ret = audioList.get(0);
        }
        return ret;
    }
    
    public DashRepresentation getFirstVideoRepresentation()
    {
        DashRepresentation ret = null;
        if (!videoList.isEmpty())
        {
            ret = videoList.get(0);
        }
        return ret;
    }
    
    public DashRepresentation getVideoRepresentationById(String id)
    {
        DashRepresentation ret = null;
        for (DashRepresentation temp : videoList)
        {
            if (temp.getId().equals(id))
            {
                ret = temp;
                break;
            }
        }
        return ret;
    }
    
    public DashRepresentation getVideoRepresentationByBitrate(int bitrate)
    {
        DashRepresentation ret = null;
        if (bitrate == 0)
        {
            if (!videoList.isEmpty())
            {
                ret = videoList.get(0);
            }
        }
        else
        {
            for (DashRepresentation temp : videoList)
            {
                if (temp.getBandwidth() == bitrate)
                {
                    ret = temp;
                    break;
                }
            }
        }
        return ret;
    }
    
    public DashRepresentation getAudioRepresentationByBitrate(int bitrate)
    {
        DashRepresentation ret = null;
        if (bitrate == 0)
        {
            if (!audioList.isEmpty())
            {
                ret = audioList.get(0);
            }
        }
        else
        {
            for (DashRepresentation temp : audioList)
            {
                if (temp.getBandwidth() == bitrate)
                {
                    ret = temp;
                    break;
                }
            }
        }
        return ret;
    }
}
