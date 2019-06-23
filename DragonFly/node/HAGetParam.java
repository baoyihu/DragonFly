package com.baoyihu.dragonfly.node;

/**
 * @author lWX230753
 *
 */
/**
 * @author lWX230753
 * 
 */
public enum HAGetParam
{
    /**
     * 获取视频码率
     */
    MEDIA_BITRATES,
    
    /**
     * 获取实时下载速率 单位：bit/s
     */
    DOWNLOAD_SPEED,
    
    /**
     * 获取播放器缓存大�?单位：毫�?
     */
    BUFFER_LENTH,
    
    /**
     * 获取当前播放的码�?单位：bps
     */
    PLAY_BITRATE,
    
    /**
     * 获取当前片源的音轨信�?
     */
    AUDIO_TRACK_INFO,
    
    /**
     * 获取当前片源的字幕信�?
     */
    SUBTITLES_TRACK_INFO,
    
    /**
     * 获取当前播放帧率
     */
    VIDOE_FPS,
    
    /**
     * 获取播放器版本号
     */
    PLAYER_VERSION,
    
    /**
     * 获取I帧数�?
     */
    I_FRAME_NUM,
    
    /**
     * 已下载的字节�?
     */
    DOWNLOADED_SIZE,
    
    /**
     * 视频当前丢帧�?
     */
    VIDEO_DROPPED_FRAMES,
    
    /**
     * 获取当前正在播放的视频码流的帧率
     */
    VIDEO_INFO_FPS,
    
    /**
     * 获取cc字幕相关的数�?
     */
    CC_SUBTITLE_DATA,
    /**
     * 获取当前的cc字幕的编�?
     */
    CC_PRESENT_CCID,
    /**
     * 获取当前正在播放的字�?
     */
    PRESENT_SUBTITLE,
    /**
     * 获取当前正在播放的音�?
     */
    PRESENT_AUDIO,
    /**
     * 获取当前播放的绝对时�?
     */
    PLAYING_ABSOLUTE_TIME,
    // �?��的几个参数手机agm中需要用到的
    /**
     * 获取播放器当前的状�?
     */
    PRESENT_STATE,
    /**
     * 获取码率的个�?
     */
    BITRATE_NUMBER,
    /**
     * 原始的url
     */
    ORIGINAL_URL,
    /**
     * �?��播放的url
     */
    FINAL_URL,
    /**
     * 播放的片源的类型
     */
    PLAY_TYPE,
    /**
     * 视频码率的编�?
     */
    BITRATE_IDENTIFIER,
    /**
     * 获取当前播放的最后一个分片的HMS地址
     */
    PLAYER_CHUNCK_SOURCE_IP,
    /**
     * 获取当前CC显示�?��是否打开
     */
    CC_SWITCH,
    
    /**
     * 获取当前播放的视频的解码方式
     */
    MEDIA_CODEC_TYPE,
    
    /**
     * 获取杜比音频输出设备 
     */
    DOLBY_DAA_END_POINT,
    
    /**
     * 获取杜比后处理开启关�?
     */
    DOLBY_DAA_DAP_ONOFF,
    
    /**
     * 获取杜比对白增强功能
     */
    DOLBY_DAA_DIALOG_ENHANCEMENT,
    /**
     * 已经收到的字节数 ：Byte
     */
    PLAYER_RECEIVED_BYTE_NUMBER
}
