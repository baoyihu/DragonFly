package com.baoyihu.dragonfly.parser;

import java.util.ArrayList;
import java.util.List;

public class IndexInfo
{
    List<Integer> bitrates = new ArrayList<Integer>();
    
    List<String> subtitles = new ArrayList<String>();
    
    List<String> audios = new ArrayList<String>();
    
    public void addBitrate(String bitrate)
    {
        if (bitrate != null)
        {
            bitrate = trim(bitrate);
            int value = Integer.valueOf(bitrate);
            if (!bitrates.contains(value))
            {
                bitrates.add(value);
            }
        }
    }
    
    public void addAudioTrack(String audio)
    {
        if (audio != null)
        {
            audio = trim(audio);
            if (!audios.contains(audio))
            {
                audios.add(audio);
            }
        }
    }
    
    public void addSubtitleTrack(String subtitle)
    {
        if (subtitle != null)
        {
            subtitle = trim(subtitle);
            if (!subtitles.contains(subtitle))
            {
                subtitles.add(subtitle);
            }
        }
    }
    
    public static String trim(String input)
    {
        input = input.trim();
        if (input.startsWith("\""))
        {
            input = input.substring(1);
        }
        if (input.endsWith("\""))
        {
            input = input.substring(0, input.length() - 1);
        }
        return input;
    }
    
}
