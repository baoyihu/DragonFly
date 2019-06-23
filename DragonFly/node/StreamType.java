package com.baoyihu.dragonfly.node;

public enum StreamType
{
    HLS, HSS, DASH;
    
    public static StreamType getTypeByUrl(String url)
    {
        String trimString = url.substring(0, url.indexOf("?"));
        if (trimString.endsWith("m3u8"))
        {
            return HLS;
        }
        else if (trimString.endsWith("manifest"))
        {
            return HSS;
        }
        else if (trimString.endsWith("xml"))
        {
            return DASH;
        }
        return DASH;
    }
}
