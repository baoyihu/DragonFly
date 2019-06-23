package com.baoyihu.dragonfly.node.hls;

public class StreamInfo
{
    private static final String NEW_LINE = "\n";
    
    private int prgramId;
    
    private int bandwidth;
    
    private int averageBandwidth;
    
    private String codecs;
    
    private int width;
    
    private int height;
    
    private String url;
    
    public int getPrgramId()
    {
        return prgramId;
    }
    
    public void setPrgramId(int prgramId)
    {
        this.prgramId = prgramId;
    }
    
    public int getBandwidth()
    {
        return bandwidth;
    }
    
    public void setBandwidth(int bandwidth)
    {
        this.bandwidth = bandwidth;
    }
    
    public int getAverageBandwidth()
    {
        return averageBandwidth;
    }
    
    public void setAverageBandwidth(int averageBandwidth)
    {
        this.averageBandwidth = averageBandwidth;
    }
    
    public String getCodecs()
    {
        return codecs;
    }
    
    public void setCodecs(String codecs)
    {
        this.codecs = codecs;
    }
    
    public int getWidth()
    {
        return width;
    }
    
    public void setWidth(int width)
    {
        this.width = width;
    }
    
    public int getHeight()
    {
        return height;
    }
    
    public void setHeight(int height)
    {
        this.height = height;
    }
    
    public String getUrl()
    {
        return url;
    }
    
    public void setUrl(String url)
    {
        this.url = url;
    }
    
    public static String getNewLine()
    {
        return NEW_LINE;
    }
    
    public StreamInfo(String input)
    {
        String[] arrays = input.split(NEW_LINE);
        for (String temp : arrays)
        {
            if (temp.startsWith("#EXT-X-STREAM-INF"))
            {
                String config = temp.substring(temp.indexOf(":") + 1);
                String[] pairs = config.split(",");
                for (String pair : pairs)
                {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2)
                    {
                        if (keyValue[0].equals("PROGRAM-ID"))
                        {
                            prgramId = Integer.parseInt(keyValue[1].trim());
                        }
                        else if (keyValue[0].equals("BANDWIDTH"))
                        {
                            bandwidth = Integer.parseInt(keyValue[1].trim());
                        }
                        else if (keyValue[0].equals("AVERAGEBANDWIDTH"))
                        {
                            averageBandwidth = Integer.parseInt(keyValue[1].trim());
                        }
                        else if (keyValue[0].equals("CODECS"))
                        {
                            codecs = keyValue[1].trim();
                        }
                        else if (keyValue[0].equals("RESOLUTION"))
                        {
                            String resolution = keyValue[1].trim();
                            String[] widHeight = resolution.split("x");
                            if (widHeight.length == 2)
                            {
                                width = Integer.parseInt(widHeight[0].trim());
                                height = Integer.parseInt(widHeight[1].trim());
                            }
                        }
                    }
                }
            }
            else if (!temp.startsWith("#"))
            {
                url = temp.trim();
            }
        }
    }
    
    @Override
    public String toString()
    {
        //#EXT-X-STREAM-INF:PROGRAM-ID=5,BANDWIDTH=3362231,AVERAGEBANDWIDTH=783482,CODECS=mp4a.40.2,hev1,RESOLUTION=1280x720
        StringBuilder builder = new StringBuilder("#EXT-X-STREAM-INF:");
        
        if (prgramId >= 0)
        {
            builder.append("PROGRAM-ID=" + prgramId + ",");
        }
        if (bandwidth > 0)
        {
            builder.append("BANDWIDTH=" + bandwidth + ",");
        }
        if (averageBandwidth > 0)
        {
            builder.append("AVERAGEBANDWIDTH=" + averageBandwidth + ",");
        }
        
        if (codecs != null)
        {
            builder.append("CODECS=" + codecs + ",");
        }
        
        if (width > 0 && height > 0)
        {
            builder.append("RESOLUTION=" + width + "x" + height + ",");
        }
        
        if (url != null)
        {
            builder.append(NEW_LINE + url);
        }
        
        return builder.toString();
    }
}
