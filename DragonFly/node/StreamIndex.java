package com.baoyihu.dragonfly.node;

import java.util.ArrayList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "StreamIndex", strict = false)
public class StreamIndex
{
    
    @Attribute(required = false, name = "Type")
    private String type;
    
    @Attribute(required = false, name = "Name")
    private String name;
    
    @Attribute(required = false, name = "MaxWidth")
    private String width;
    
    @Attribute(required = false, name = "MaxHeight")
    private String height;
    
    @Attribute(required = false, name = "Language")
    private String language;
    
    public String getLanguage()
    {
        return language;
    }
    
    public void setLanguage(String language)
    {
        this.language = language;
    }
    
    @ElementList(required = false, name = "QualityLevel", inline = true)
    private ArrayList<QualityLevel> levelList;
    
    public String getType()
    {
        return type;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getWidth()
    {
        return width;
    }
    
    public void setWidth(String width)
    {
        this.width = width;
    }
    
    public String getHeight()
    {
        return height;
    }
    
    public void setHeight(String height)
    {
        this.height = height;
    }
    
    public ArrayList<QualityLevel> getLevelList()
    {
        return levelList;
    }
    
    public void setLevelList(ArrayList<QualityLevel> levelList)
    {
        this.levelList = levelList;
    }
    
}
