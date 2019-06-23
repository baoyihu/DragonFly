package com.baoyihu.dragonfly.node;

/**
 * 设置播放器参�?定义设置播放器参数的类型:视频类型、最大时移时长�?解密、受限最大码率�?受限�?��码率、全屏非全屏
 * 
 * @author w00188401
 * @version [版本�? 2012-12-18]
 * @see [相关�?方法]
 * @since [产品/模块版本]
 */
public enum HASetParam
{
    /**Integer
     * 视频类型
     */
    VIDEO_TYPE(Integer.class),
    
    /**Integer
     * �?��时移时长
     */
    TSTV_LENGTH(Integer.class),
    
    /**Integer
     * 过滤�?��码率 player会把播放的码率限制在MAX_BITRATE和MIN_BITRATE之间�?
     * 此时UI来获取码率列表，在该二�?码率之外的码率将不会包含在码率列表里面�? 比如：片源码率：500K�?000K�?500K。设�?
     * MAX_BITRATE=1200K�?MIN_BITRATE=800K�?
     * 则播放器只播�?000K码率，同时UI来获取码率列表，将也只有1000K这个码率值�?
     */
    MAX_BITRATE(Integer.class),
    
    /**Integer
     * 过滤�?��码率 具体描述参见：MAX_BITRATE
     */
    MIN_BITRATE(Integer.class),
    
    /**Integer
     * 指定固定码率播放
     */
    DESIGNATED_BITRATE(Integer.class),
    
    /**Integer
     * 全屏、非全屏
     */
    SCALE_MODE(Integer.class),
    
    /**Integer
     * 设置播放的最大码�?�?MAX_BITRATE 相比，不同的是：该设置不会影响UI获取码率列表值�?
     */
    MAX_PLAYER_BITRATE(Integer.class),
    
    /**String
     * 选择某音轨播�?
     */
    SWITCH_AUDIO_TRACK(String.class),
    
    /**String
     * 选择字幕播放
     */
    SWITCH_SUBTITLES_TRACK(String.class),
    
    /**Integer
     * �?��者关CC字幕
     */
    SET_CC_ONOFF(Integer.class),
    
    /**Long
     * 传入UTC时间（NTP时间）与本地时间的差值diff（单位：second）：比如北京时间�?2:00 ，UTC时间此时�? �?diff =
     * T(UTC) - T（local�?= �? - 12�?* 3600L = -8 * 3600L
     * （s）对应终端，如果终端时间被修改，那应该用修改好的本地之间和ntp做同步比较如上式
     * ：比如，实际本地时间�?2:00，由于本机时间被修改�?3:00，那应该用本地被修改后的时间去计算，即：diff = T(UTC) -
     * T(local被修�? = �? - 13�?3600L = -9 *
     * 3600L（s）一句话：调用�?确保输入的diff参数是UTC时间和本地时间（可能是正确也可能是被修改不正确的时间�?的差值�?
     */
    TIME_DIFF_UTC(Long.class),
    
    /**Integer
     * 不使用proxy，用于内部调�?
     */
    PROXY_ON(Integer.class),
    
    /**Integer
     * 历史书签播放
     */
    HISTORY_PLAY_POINT(Integer.class),
    
    /**String
     * 设置偏好音轨
     */
    AUDIO_PREFER_LANG(String.class),
    
    /**String
     * 设置偏好字幕
     */
    TEXT_PREFER_LANG(String.class),
    
    /**Integer
     * 设置FingerPrint�?��
     */
    FINGER_PRINT(Integer.class),
    
    /**Integer
     * Finger Print 显示间隔 单位：秒
     */
    FINGER_PRINT_INTERVAL(Integer.class),
    
    /**Integer
     * Finger Print 显示时长 单位：秒
     */
    FINGER_PRINT_DURATION(Integer.class),
    
    /**Integer
     * Finger Print 透明�?
     */
    FINGER_PRINT_OPACITY(Integer.class),
    
    /**Integer
     * Finger Print 字体大小
     */
    FINGER_PRINT_FONTSIZE(Integer.class),
    
    /**String
     * Finger Print 背景颜色
     */
    FINGER_PRINT_BKCOLOR(String.class),
    
    /**String
     * Finger Print 字体颜色
     */
    FINGER_PRINT_FONTCOLOR(String.class),
    
    /**String
     * Finger Print 内容
     */
    FINGER_PRINT_CONTENT(String.class),
    
    /**Integer
     * 设置OutPutBlocking�?��
     */
    OUTPUT_BLOCKING(Integer.class),
    
    /**Integer
     * 设置是否支持性能自�?�?
     */
    PERFORMANCE_ADAPTIVE(Integer.class),
    
    /**Float
     * 快进快�?倍�?
     */
    PLAY_RATE(Float.class),
    
    /**Integer
     * 默认缓冲大小
     */
    DEFAULT_BUFFER_SIZE(Integer.class),
    
    /**Integer
     * HTTP下载的信�?
     */
    HTTP_MONITOR(Integer.class),
    
    /**Integer
     * 分片下载的信�?
     */
    SEGMENT_MONITOR(Integer.class),
    
    /**Integer
     * 自�?应码率切换的前后码率
     */
    BWSWITCH_MONITOR(Integer.class),
    
    /**String
     * 设置字幕字体文件路径
     */
    SUBTITLE_FONT_FILE_PATH(String.class),
    
    /**String
     * 设置cc字幕
     */
    SET_CC_SUBITITLE(String.class),
    
    /**Integer
     * 设置黑频�?��或�?结束
     */
    SET_BLACK_SWITCH(Integer.class),
    
    /**Float
     * 用户自定义屏幕宽高比
     */
    ASPECT_RATIO_USER(Float.class),
    
    /**Integer
     * 网络中断，传入integer 1 or 0 whatever
     */
    NETWORK_SUSPEND(Integer.class),
    
    /**String
     * 网络恢复，传入string，表示网络恢复后新的URL地址�?"表示URL不变
     */
    NETWORK_RESUME(String.class),
    
    /**Integer
     * 设置直播列表更新个数限制,int
     */
    HLS_LIVE_PLAYLIST_SIZE_LIMIT(Integer.class),
    
    /**Integer
     * 设置平滑切换码率,int ,码率大小;
     */
    SWITCH_BANDWIDTH_SMOOTH(Integer.class),
    
    /**Integer
     * 设置是否使用本地缓存 ,int; 1:enable, 0:disable(default)
     */
    SET_LOCALCACHE(Integer.class),
    
    /**Integer
     *  设置本地缓存�?��下载线程�?,Integer
     */
    SET_LOCALCACHE_THREAD_NUM(Integer.class),
    
    /**Integer
     * 设置音频淡出效果,Integer; 1:open; 0:close(default)
     */
    SET_AUDIO_FADE_OUT(Integer.class),
    
    /**String
     * 设置日志文件存放的目�?,String; such as /sdcard/PE/
     */
    SET_LOG_OUTPUT_DIR(String.class),
    
    /**Integer
     * 设置缓冲大小 单位为时间秒 ,Integer,
     */
    SET_BUFFERING_TIME(Integer.class),
    
    /**String
     * 设置插件名称,String
     */
    SET_PE_SELECT(String.class),
    
    /**Integer
     * 设置杜比音频输出设备  int 参数类型在类 PEDolbyDaaEndp 定义 
     */
    SET_DOLBY_DAA_END_POINT(Integer.class),
    
    /**Integer
     * 设置杜比后处理开启关�? int 参数类型�?PEDolbyDaaDapOnOff 定义
     */
    SET_DOLBY_DAA_DAP_ONOFF(Integer.class),
    
    /**Integer
     * 设置杜比对话增强功能 int 参数类型�?PEDolbyDaaDialogEnhancement 定义
     */
    SET_DOLBY_DAA_DIALOG_ENHANCEMENT(Integer.class),
    
    /**Integer
     * 设置长连�?int 0 短连�?(default);1 长连�?
     */
    SET_HTTP_LONG_CONNECTION(Integer.class),
    
    /**Integer
     * 设置缓存�?��字节数限�?Integer Recommendation: 50K to 100K for audio-only stream, 1M to 2M for other
     */
    SET_BUFFERING_SIZE_LIMIT(Integer.class),
    
    /**
     * 设置三TCP的线程数 Integer
     */
    SET_THREE_TCP_THREADS(Integer.class),
    
    /**
     * 设置三TCP的分片大�?Integer
     */
    SET_THREE_TCP_SLICE_SIZE(Integer.class);
    // /**
    // * 设置是否打开直播时延 默认打开 1：打�??0：关�?
    // */
    // LIVE_DELAY
    
    private Class<?> valueType;
    
    HASetParam(Class<?> paramClass)
    {
        this.valueType = paramClass;
    }
    
    public Class<?> getValueType()
    {
        return valueType;
    }
}
