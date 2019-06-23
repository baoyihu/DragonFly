package com.baoyihu.dragonfly.node.hls;

public class Info
{
    private static final String NEW_LINE = "\n";
    
    private double duration;
    
    private String url;
    
    public double getDuration()
    {
        return duration;
    }
    
    public void setDuration(double duration)
    {
        this.duration = duration;
    }
    
    public String getUrl()
    {
        return url;
    }
    
    public void setUrl(String url)
    {
        this.url = url;
    }
    
    public Info(String input)
    {
        int splitIndex = input.indexOf("\n");
        
        if (splitIndex > 0)
        {
            String pre = input.substring(0, splitIndex).trim();
            if (!pre.isEmpty() && pre.endsWith(","))
            {
                pre = pre.substring(0, pre.length() - 1);
            }
            if (!pre.isEmpty())
            {
                duration = Double.parseDouble(pre.substring(pre.indexOf(":") + 1).trim());
            }
            String next = input.substring(splitIndex + 1);
            url = next.trim();
            
        }
    }
    
    @Override
    public String toString()
    {
        //#EXTINF:5.000000,
        //  7509-1-1746.hls.ts
        StringBuilder builder = new StringBuilder("#EXTINF:" + duration);
        if (url != null)
        {
            builder.append(NEW_LINE + url);
        }
        return builder.toString();
    }
}
