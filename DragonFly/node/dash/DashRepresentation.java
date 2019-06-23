package com.baoyihu.dragonfly.node.dash;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;

import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.AudioSpecificConfig;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.DecoderConfigDescriptor;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.ESDescriptor;
import org.mp4parser.boxes.iso14496.part12.SegmentIndexBox;
import org.mp4parser.boxes.iso14496.part14.ESDescriptorBox;
import org.mp4parser.boxes.iso14496.part15.AvcConfigurationBox;
import org.mp4parser.boxes.iso14496.part15.HevcConfigurationBox;
import org.mp4parser.boxes.iso14496.part15.HevcDecoderConfigurationRecord;
import org.mp4parser.boxes.iso14496.part15.HevcDecoderConfigurationRecord.Array;
import org.mp4parser.tools.Hex;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import com.baoyihu.common.util.Bytes;
import com.baoyihu.common.util.DebugLog;

import android.media.MediaFormat;

@Root(name = "Representation", strict = false)
public class DashRepresentation
{
    public static final String TAG = "DashRepresentation";
    
    @Attribute(name = "id", required = false)
    String id;//="1"
    
    @Attribute(name = "mimeType", required = false)
    String mimeType;//"video/mp4"
    
    @Attribute(name = "codecs", required = false)
    String codecs;//="mp4a.40.5";
    
    @Attribute(name = "width", required = false)
    int width;//="1280" 
    
    @Attribute(name = "height", required = false)
    int height;//="720" 
    
    @Attribute(name = "bandwidth", required = false)
    int bandwidth;//="65528" 
    
    @Attribute(name = "audioSamplingRate", required = false)
    int audioSamplingRate;//="48000" 
    
    @Element(name = "BaseURL", required = false)
    private String baseUrl;
    
    @Element(name = "SegmentBase", required = false)
    private DashSegmentBase segment;
    
    private String rootUrl;
    
    public String getRootUrl()
    {
        return rootUrl;
    }
    
    public void setRootUrl(String rootUrl)
    {
        this.rootUrl = rootUrl;
    }
    
    private SegmentIndexBox segmentIndex;
    
    public SegmentIndexBox getSegmentIndex()
    {
        return segmentIndex;
    }
    
    public void setSegmentIndex(SegmentIndexBox segmentIndex)
    {
        this.segmentIndex = segmentIndex;
    }
    
    private byte[] initializationBuffer;
    
    public void setInitializationBuffer(byte[] input)
    {
        this.initializationBuffer = input;
    }
    
    public String getMimeType()
    {
        return mimeType;
    }
    
    public boolean isAudio()
    {
        if (audioSamplingRate > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public MediaFormat getVideoMediaFormat()
    {
        DebugLog.info(TAG, "getMediaFormat,isAudio:" + isAudio());
        byte[] fixedHead = new byte[] {0, 0, 0, 1};
        byte[] pictureParm = null;
        byte[] sequenceParm = null;
        if (initializationBuffer == null)
        {
            return null;
        }
        ReadableByteChannel channel = Channels.newChannel(new ByteArrayInputStream(initializationBuffer));
        IsoFile isoFile;
        try
        {
            isoFile = new IsoFile(channel);
            List<AvcConfigurationBox> boxList = isoFile.getBoxes(AvcConfigurationBox.class, true);
            if (!boxList.isEmpty())
            {
                //   if (mimeType == null)
                {
                    mimeType = "video/avc";
                    //mimeType = "video/" + codecs.substring(0, 4);
                }
                AvcConfigurationBox configuration = boxList.get(0);
                configuration.parseDetails();
                
                List<String> temp = configuration.getavcDecoderConfigurationRecord().getPictureParameterSetsAsStrings();
                if (!temp.isEmpty())
                {
                    pictureParm = Hex.decodeHex(temp.get(0));
                    pictureParm = Bytes.connect(fixedHead, fixedHead.length, pictureParm, pictureParm.length);
                }
                temp = configuration.getavcDecoderConfigurationRecord().getSequenceParameterSetsAsStrings();
                if (!temp.isEmpty())
                {
                    sequenceParm = Hex.decodeHex(temp.get(0));
                    sequenceParm = Bytes.connect(fixedHead, fixedHead.length, sequenceParm, sequenceParm.length);
                }
            }
            else
            {
                List<HevcConfigurationBox> hevConfigList = isoFile.getBoxes(HevcConfigurationBox.class, true);
                if (!hevConfigList.isEmpty())
                {
                    // if (mimeType == null)
                    {
                        mimeType = "video/hevc";
                        //                        mimeType = "video/" + codecs.substring(0, 4);
                        //                        mimeType = "video/hvcC";
                    }
                    sequenceParm = null;
                    HevcConfigurationBox configuration = hevConfigList.get(0);
                    configuration.parseDetails();
                    
                    HevcDecoderConfigurationRecord recorder = configuration.getHevcDecoderConfigurationRecord();
                    for (Array ardd : recorder.getArrays())
                    {
                        for (byte[] t1 : ardd.nalUnits)
                        {
                            byte[] temp1 = Bytes.connect(fixedHead, fixedHead.length, t1, t1.length);
                            if (sequenceParm == null)
                            {
                                sequenceParm = temp1;
                            }
                            else
                            {
                                sequenceParm = Bytes.connect(sequenceParm, sequenceParm.length, temp1, temp1.length);
                            }
                            //  Logger.info("array:", "fefe:" + Arrays.toString(t1));
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            DebugLog.trace(TAG, e);
        }
        
        MediaFormat format = new MediaFormat();
        format.setInteger("height", height);
        format.setInteger("width", width);
        format.setString("mime", mimeType);
        
        if (pictureParm != null)
        {
            format.setByteBuffer("csd-1", ByteBuffer.wrap(pictureParm));
        }
        if (sequenceParm != null)
        {
            format.setByteBuffer("csd-0", ByteBuffer.wrap(sequenceParm));
        }
        return format;
    }
    
    public MediaFormat getAudioMediaFormat()
    {
        DebugLog.info(TAG, "getAudioMediaFormat,isAudio:" + isAudio());
        if (initializationBuffer == null)
        {
            return null;
        }
        ReadableByteChannel channel = Channels.newChannel(new ByteArrayInputStream(initializationBuffer));
        IsoFile isoFile;
        
        mimeType = "audio/mp4a-latm";
        
        MediaFormat format = new MediaFormat();
        format.setString("mime", mimeType);
        try
        {
            isoFile = new IsoFile(channel);
            List<ESDescriptorBox> compositionList = isoFile.getBoxes(ESDescriptorBox.class, true);
            if (!compositionList.isEmpty())
            {
                ESDescriptorBox composition = compositionList.get(0);
                composition.parseDetails();
                ESDescriptor esDescriptoer = (ESDescriptor)composition.getDescriptor();
                if (esDescriptoer != null)
                {
                    DecoderConfigDescriptor decodeConfig = esDescriptoer.getDecoderConfigDescriptor();
                    if (decodeConfig != null && decodeConfig.getAudioSpecificInfo() != null)
                    {
                        AudioSpecificConfig audioConfig = decodeConfig.getAudioSpecificInfo();
                        
                        int audioObjectType = audioConfig.getAudioObjectType();
                        int extensionAudioObjectType = audioConfig.getExtensionAudioObjectType();
                        
                        DebugLog.info("DashRepresentation", "csd-0:" + Arrays.toString(audioConfig.getConfigBytes()));
                        format.setByteBuffer("csd-0", ByteBuffer.wrap(audioConfig.getConfigBytes()));
                        
                        if (audioObjectType == 2 || audioObjectType == 5 || audioObjectType == 39)
                        {
                            //AAC-LC     //SBR      //"ER AAC ELD"
                            format.setInteger("aac-profile", extensionAudioObjectType);
                        }
                        
                        format.setInteger("channel-count", audioConfig.getChannelConfiguration());
                        format.setInteger("sample-rate", audioConfig.getSamplingFrequency());
                        //              format.setLong("durationUs", 2847616000L);
                    }
                }
            }
            
        }
        catch (IOException e)
        {
            DebugLog.trace(TAG, e);
        }
        return format;
    }
    
    public int getIndexOfSegmentByTime(long time)
    {
        final SegmentIndexBox segmentIndex = getSegmentIndex();
        SegmentIndexBox.Entry find = null;
        int index = 0;
        if (segmentIndex != null)
        {
            for (SegmentIndexBox.Entry node : segmentIndex.getEntries())
            {
                long startTime = node.getStartFrom() * 1000 / segmentIndex.getTimeScale();
                long endTime =
                    (node.getStartFrom() + node.getSubsegmentDuration()) * 1000 / segmentIndex.getTimeScale();
                if (startTime <= time && endTime >= time)
                {
                    find = node;
                    break;
                }
                index++;
            }
        }
        if (find != null)
        {
            return index;
        }
        else
        {
            return -1;
        }
    }
    
    public byte[] getInitializationBuffer()
    {
        return initializationBuffer;
    }
    
    public String getId()
    {
        return id;
    }
    
    public void setId(String id)
    {
        this.id = id;
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
    
    public int getBandwidth()
    {
        return bandwidth;
    }
    
    public void setBandwidth(int bandwidth)
    {
        this.bandwidth = bandwidth;
    }
    
    public int getAudioSamplingRate()
    {
        return audioSamplingRate;
    }
    
    public void setAudioSamplingRate(int audioSamplingRate)
    {
        this.audioSamplingRate = audioSamplingRate;
    }
    
    public String getCodecs()
    {
        return codecs;
    }
    
    public void setCodecs(String codecs)
    {
        this.codecs = codecs;
    }
    
    public String getBaseUrl()
    {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }
    
    public DashSegmentBase getSegment()
    {
        return segment;
    }
    
    public void setSegment(DashSegmentBase segment)
    {
        this.segment = segment;
    }
    
}
