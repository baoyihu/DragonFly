package com.baoyihu.dragonfly.streamer;

import com.baoyihu.dragonfly.constant.ErrorCode;

public interface StreamControllerCallback
{
    void onBuffering(long time);
    
    void onIndexOk();
    
    void onStreamError(ErrorCode code);
}
