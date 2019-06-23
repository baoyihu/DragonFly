package com.baoyihu.dragonfly.url;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.PersistenceException;

import com.baoyihu.common.util.DebugLog;
import com.baoyihu.common.util.Files;
import com.baoyihu.dragonfly.xml.SerializerService;

@Root(name = "MediaDefinition", strict = false)
public class MediaList
{
    public static final String TAG = "MediaList";
    
    public static final String FILEPATH = "/sdcard/url.xml";
    
    @ElementList(required = true, name = "MediaList")
    List<Media> mediaList;
    
    public MediaList()
    {
        
    }
    
    public MediaList(List<Media> mediaList)
    {
        this.mediaList = mediaList;
    }
    
    public List<Media> getList()
    {
        return mediaList;
    }
    
    public static List<Media> parseFile(String path)
    {
        List<Media> mediaList = new ArrayList<Media>();
        try
        {
            String bufferString = Files.readFile(path, "UTF-8");
            MediaList list = SerializerService.fromXml(MediaList.class, bufferString);
            mediaList = list.mediaList;
        }
        catch (PersistenceException e)
        {
            DebugLog.trace(TAG, e);
        }
        return mediaList;
    }
    
    public static List<Map<String, String>> toMapList(List<Media> list)
    {
        List<Map<String, String>> tempList = new ArrayList<Map<String, String>>();
        for (Media media : list)
        {
            tempList.add(media.toMap());
        }
        return tempList;
    }
}
