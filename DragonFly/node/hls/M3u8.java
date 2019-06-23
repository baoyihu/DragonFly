package com.baoyihu.dragonfly.node.hls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.baoyihu.common.util.Pair;

public class M3u8
{
    //#EXTM3U
    private static final String NEW_LINE = "\n";
    
    private M3u8 parent = null;
    
    private String url = null;
    
    private int bandwidth = 0;
    
    private String version;
    
    private List<StreamInfo> streamInfoList = new ArrayList<StreamInfo>();
    
    private double targetDuration;
    
    private int mediaSequence = -1;
    
    private List<Info> infoList = new ArrayList<Info>();
    
    private Map<Integer, M3u8> m3u8Map = new TreeMap<Integer, M3u8>();
    
    private List<Pair<String, String>> keyMap = new ArrayList<Pair<String, String>>();
    
    public long getBeginTime(int indexOfTrack)
    {
        double ret = 0L;
        for (int iLoop = 0; iLoop < indexOfTrack; iLoop++)
        {
            ret += infoList.get(iLoop).getDuration();
        }
        return (long)(ret * 1000);
    }
    
    public List<Pair<String, String>> getKeyMap()
    {
        return keyMap;
    }
    
    public void setKeyMap(List<Pair<String, String>> keyMap)
    {
        this.keyMap = keyMap;
    }
    
    public long getDurationMill()
    {
        long ret = 0L;
        for (Info info : infoList)
        {
            ret += info.getDuration() * 1000;
        }
        return ret;
    }
    
    public M3u8 getParent()
    {
        return parent;
    }
    
    public void setParent(M3u8 parent)
    {
        this.parent = parent;
    }
    
    public String getUrl()
    {
        return url;
    }
    
    public void setUrl(String url)
    {
        this.url = url;
    }
    
    public int getBandwidth()
    {
        return bandwidth;
    }
    
    public void setBandwidth(int bandwidth)
    {
        this.bandwidth = bandwidth;
    }
    
    public String getVersion()
    {
        return version;
    }
    
    public void setVersion(String version)
    {
        this.version = version;
    }
    
    public StreamInfo getfirstStreamInfo()
    {
        if (!streamInfoList.isEmpty())
        {
            return streamInfoList.get(0);
        }
        else
        {
            return null;
        }
    }
    
    public List<StreamInfo> getStreamInfoList()
    {
        return streamInfoList;
    }
    
    public void setStreamInfoList(List<StreamInfo> streamInfoList)
    {
        this.streamInfoList = streamInfoList;
    }
    
    public double getTargetDuration()
    {
        return targetDuration;
    }
    
    public void setTargetDuration(double targetDuration)
    {
        this.targetDuration = targetDuration;
    }
    
    public int getMediaSequence()
    {
        return mediaSequence;
    }
    
    public void setMediaSequence(int mediaSequence)
    {
        this.mediaSequence = mediaSequence;
    }
    
    public List<Info> getInfoList()
    {
        return infoList;
    }
    
    public void setInfoList(List<Info> infoList)
    {
        this.infoList = infoList;
    }
    
    public void setSubM3u8(int bandwidth, M3u8 sub)
    {
        sub.bandwidth = bandwidth;
        m3u8Map.put(bandwidth, sub);
    }
    
    public M3u8 getSubM3u8(int bitrate)
    {
        return m3u8Map.get(bitrate);
    }
    
    public M3u8(String url, String input, M3u8 parent)
    {
        this.url = url;
        this.parent = parent;
        if (input != null)
        {
            String[] array = input.split("#");
            for (String temp : array)
            {
                temp = "#" + temp;
                if (temp.startsWith("#EXT-X-VERSION"))
                {
                    version = temp.substring(temp.indexOf(":") + 1).trim();
                }
                else if (temp.startsWith("#EXT-X-STREAM-INF"))
                {
                    streamInfoList.add(new StreamInfo(temp));
                }
                else if (temp.startsWith("#EXT-X-TARGETDURATION"))
                {
                    targetDuration = Double.parseDouble(temp.substring(temp.indexOf(":") + 1).trim());
                }
                else if (temp.startsWith("#EXT-X-MEDIA-SEQUENCE"))
                {
                    mediaSequence = Integer.parseInt(temp.substring(temp.indexOf(":") + 1).trim());
                }
                else if (temp.startsWith("#EXT-X-KEY"))
                {
                    temp = temp.substring(temp.indexOf(":", 10));
                    String[] keyValue = temp.split("=");
                    if (keyValue.length >= 2)
                    {
                        keyMap.add(new Pair<String, String>(keyValue[0], keyValue[1]));
                    }
                }
                else if (temp.startsWith("#EXTINF"))
                {
                    infoList.add(new Info(temp));
                }
            }
        }
        
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("#EXTM3U");
        if (version != null)
        {
            builder.append(NEW_LINE + "#EXT-X-VERSION:" + version);
        }
        if (!streamInfoList.isEmpty())
        {
            for (StreamInfo info : streamInfoList)
            {
                builder.append(NEW_LINE + info.toString());
            }
        }
        if (targetDuration > 0.0d)
        {
            builder.append(NEW_LINE + "#EXT-X-TARGETDURATION:" + targetDuration);
        }
        if (mediaSequence > -1)
        {
            builder.append(NEW_LINE + "#EXT-X-MEDIA-SEQUENCE:" + mediaSequence);
        }
        if (!infoList.isEmpty())
        {
            for (Info info : infoList)
            {
                builder.append(NEW_LINE + info.toString());
            }
        }
        builder.append(NEW_LINE + "#EXT-X-ENDLIST");
        
        return builder.toString();
    }
}
