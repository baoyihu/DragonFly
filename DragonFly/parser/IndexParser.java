package com.baoyihu.dragonfly.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.simpleframework.xml.core.PersistenceException;

import com.baoyihu.common.util.DebugLog;
import com.baoyihu.dragonfly.node.HSSNode;
import com.baoyihu.dragonfly.node.QualityLevel;
import com.baoyihu.dragonfly.node.StreamIndex;
import com.baoyihu.dragonfly.node.StreamType;
import com.baoyihu.dragonfly.util.NetWork;
import com.baoyihu.dragonfly.xml.SerializerService;

public class IndexParser
{
    public static final String TAG = "IndexParser";
    
    public static IndexInfo getIndex(String url)
    {
        IndexInfo info = null;
        
        String buffer;
        try
        {
            buffer = NetWork.downloadToString(url, "GB2312");
            switch (StreamType.getTypeByUrl(url))
            {
                case HLS:
                    info = parseHLS(buffer);
                    break;
                case HSS:
                    info = parseHSS(buffer);
                    break;
                
                default:
                    break;
            }
        }
        catch (IOException e)
        {
            DebugLog.trace(TAG, e);
        }
        
        return info;
    }
    
    private static IndexInfo parseHSS(String buffer)
    {
        IndexInfo info = new IndexInfo();
        try
        {
            HSSNode hssNode = SerializerService.fromXml(HSSNode.class, buffer);
            if (hssNode != null)
            {
                
                for (StreamIndex curentIndex : hssNode.getStreamList())
                {
                    String type = curentIndex.getType();
                    if (type.equals("video"))
                    {
                        ArrayList<QualityLevel> levelList = curentIndex.getLevelList();
                        for (QualityLevel level : levelList)
                        {
                            info.addBitrate(level.getBitrate());
                        }
                    }
                    else if (type.equals("audio"))
                    {
                        info.addAudioTrack(curentIndex.getLanguage());
                    }
                    else if (type.equals("text"))
                    {
                        info.addSubtitleTrack(curentIndex.getLanguage());
                    }
                }
            }
        }
        catch (PersistenceException e)
        {
            DebugLog.trace(TAG, e);
        }
        return info;
    }
    
    public static IndexInfo parseHLS(String buffer)
    {
        IndexInfo info = new IndexInfo();
        String[] array = buffer.split("\n");
        for (String temp : array)
        {
            if (temp.startsWith("#EXT-X-MEDIA"))
            {
                if (temp.contains("TYPE=AUDIO"))
                {
                    info.addAudioTrack(findKeyValue(temp, "LANGUAGE", ','));
                }
                else if (temp.contains("SUBTITLES"))
                {
                    info.addSubtitleTrack(findKeyValue(temp, "LANGUAGE", ','));
                }
            }
            else if (temp.startsWith("#EXT-X-STREAM-INF"))
            {
                info.addBitrate(findKeyValue(temp, "BANDWIDTH", ','));
            }
        }
        return info;
    }
    
    public static String findKeyValue(String buffer, String key, char split)
    {
        String ret = null;
        int index1 = buffer.indexOf(key);
        if (index1 >= 0)
        {
            index1 = buffer.indexOf('=', key.length() + index1) + 1;
            
            int index2 = buffer.indexOf(split, index1);
            if (index2 > index1)
            {
                ret = buffer.substring(index1, index2);
            }
        }
        return ret;
    }
    
}
