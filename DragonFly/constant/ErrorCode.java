package com.baoyihu.dragonfly.constant;

public enum ErrorCode
{
    MEDIA_ERROR_OK("everything is fine "),
    
    /**
     * */
    MEDIA_ERROR_UNKNOWN("we don't know what happend"),
    
    /**
     * */
    MEDIA_URL_ERROR("this url is not well formed"),
    
    /**
     * */
    MEDIA_ERROR_SERVER_DIED("can't connect to servic"),
    
    /**
     * */
    MEDIA_ERROR_UNSUPPORTED_FORMAT("index or mpd is not correct"),
    
    /**
     * */
    MEDIA_ERROR_NET_CONNREFUSED("connect is refused"),
    
    /**
     * */
    MEDIA_ERROR_NET_UNREACHABLE("there is no network"),
    
    /**
     * */
    MEDIA_ERROR_NET_TIMEOUT("network time out"),
    
    /**
     * */
    MEDIA_ERROR_NET_PROTOCOL_SPEC("network error ,such as 404,505"), //404,505
    
    /**
     * */
    MEDIA_ERROR_PARSE_FAILED("media parse error"),
    
    /**
     * */
    MEDIA_ERROR_DRM_FAILED("Drm protected"),
    
    /**
     * */
    MEDIA_ERROR_SEGMENT_NO_SAMPLE("there is no samble in segment");
    
    private String means;
    
    ErrorCode(String value)
    {
        this.means = value;
    }
    
    public String getMeans()
    {
        return means;
    }
}
