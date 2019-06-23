package com.baoyihu.dragonfly.url;

import java.util.HashMap;
import java.util.Map;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

@Root(name = "Media", strict = false)
public class Media implements Parcelable
{
    @Element(required = false, name = "Name")
    private String name;
    
    @Element(required = false, name = "Type")
    private String type;//HAPlayerConstant.VideoType
    
    @Element(required = false, name = "Url")
    private String url;
    
    @Element(required = false, name = "PureAudio")
    private boolean pureAudio;
    
    public boolean isPureAudio()
    {
        return pureAudio;
    }
    
    public void setPureAudio(boolean pureAudio)
    {
        this.pureAudio = pureAudio;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getType()
    {
        return type;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }
    
    public String getUrl()
    {
        return url;
    }
    
    public void setUrl(String url)
    {
        this.url = url;
    }
    
    public String getUrlDirectory()
    {
        String ret = null;
        if (!TextUtils.isEmpty(url) && url.contains("/"))
        {
            ret = url.substring(0, url.lastIndexOf("/") + 1);
        }
        return ret;
    }
    
    public Map<String, String> toMap()
    {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("name", name);
        ret.put("type", type);
        ret.put("url", url);
        return ret;
    }
    
    public Media()
    {
        //Do not remove default constructor, necessary for some case.
    }
    
    public Media(String name, String type, String url)
    {
        this.name = name;
        this.type = type;
        this.url = url;
    }
    
    @SuppressWarnings("unchecked")
    public Media(Parcel source)
    {
        name = source.readString();
        type = source.readString();
        url = source.readString();
        pureAudio = (Boolean)source.readValue(Boolean.class.getClassLoader());
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(url);
        dest.writeValue(pureAudio);
    }
    
    public static final Parcelable.Creator<Media> CREATOR = new Creator<Media>()
    {
        @Override
        public Media[] newArray(int size)
        {
            return new Media[size];
        }
        
        @Override
        public Media createFromParcel(Parcel source)
        {
            return new Media(source);
        }
    };
    
    @Override
    public int describeContents()
    {
        return 0;
    }
    
}