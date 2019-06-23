package com.baoyihu.dragonfly.test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;

import org.mp4parser.Box;
import org.mp4parser.Container;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.DecoderConfigDescriptor;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.ESDescriptor;
import org.mp4parser.boxes.iso14496.part12.MediaDataBox;
import org.mp4parser.boxes.iso14496.part12.MediaHeaderBox;
import org.mp4parser.boxes.iso14496.part12.MovieExtendsHeaderBox;
import org.mp4parser.boxes.iso14496.part12.MovieHeaderBox;
import org.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import org.mp4parser.boxes.iso14496.part12.SegmentIndexBox;
import org.mp4parser.boxes.iso14496.part12.SegmentIndexBox.Entry;
import org.mp4parser.boxes.iso14496.part12.TrackHeaderBox;
import org.mp4parser.boxes.iso14496.part14.ESDescriptorBox;
import org.mp4parser.boxes.sampleentry.AudioSampleEntry;

import com.baoyihu.common.util.DebugLog;
import com.baoyihu.common.util.Files;

import android.media.MediaExtractor;
import android.media.MediaFormat;

public class TestIso
{
    private static final String TAG = "TestIso";
    
    public static void parseMp4Config(String url)
    {
        MediaExtractor extractor = new MediaExtractor();
        try
        {
            extractor.setDataSource(url);
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        for (int i = 0; i < extractor.getTrackCount(); i++)
        {
            MediaFormat format = extractor.getTrackFormat(i);
            format.toString();
            ByteBuffer byteBuffer = format.getByteBuffer("csd-0");
            byte[] dd = byteBuffer.array();
            dd.toString();
        }
    }
    
    public static void testParser(byte[] buffer)
    {
        ReadableByteChannel channel = null;
        IsoFile isoFile = null;
        
        try
        {
            try
            {
                channel = Channels.newChannel(new ByteArrayInputStream(buffer));
                isoFile = new IsoFile(channel);
                if (isoFile != null)
                {
                    List<SampleDescriptionBox> boxList = isoFile.getBoxes(SampleDescriptionBox.class, true);
                    if (!boxList.isEmpty())
                    {
                        
                        for (SampleDescriptionBox configuration : boxList)
                        {
                            List<Box> subBoxs = configuration.getBoxes();
                            for (Box box : subBoxs)
                            {
                                DebugLog.info("accConfig", "subBox:" + box.toString());
                            }
                        }
                    }
                    
                    List<AudioSampleEntry> sampleList = isoFile.getBoxes(AudioSampleEntry.class, true);
                    if (!sampleList.isEmpty())
                    {
                        
                        for (AudioSampleEntry configuration : sampleList)
                        {
                            List<Box> subBoxs = configuration.getBoxes();
                            for (Box box : subBoxs)
                            {
                                DebugLog.info("accConfig", "sample:" + box.toString());
                            }
                        }
                    }
                    
                    List<ESDescriptorBox> esList = isoFile.getBoxes(ESDescriptorBox.class, true);
                    if (!esList.isEmpty())
                    {
                        for (ESDescriptorBox configuration : esList)
                        {
                            configuration.parseDetails();
                            String boxString = configuration.getDescriptorAsString();
                            while (boxString.length() > 800)
                            {
                                DebugLog.info(TAG, "esDescription:" + boxString.substring(0, 800));
                                boxString = boxString.substring(800);
                            }
                            DebugLog.info(TAG, "esDescription:" + boxString);
                        }
                    }
                    
                    List<TrackHeaderBox> list1 = isoFile.getBoxes(TrackHeaderBox.class, true);
                    TrackHeaderBox box1 = list1.get(0);
                    box1.parseDetails();
                    long duration = box1.getDuration();
                    
                    //    List<SegmentIndexBox> list2 = isoFile.getBoxes(SegmentIndexBox.class, true);
                    
                    List<MovieHeaderBox> list3 = isoFile.getBoxes(MovieHeaderBox.class, true);
                    MovieHeaderBox box3 = list3.get(0);
                    box3.parseDetails();
                    duration = box3.getDuration();
                    
                    List<MovieExtendsHeaderBox> list4 = isoFile.getBoxes(MovieExtendsHeaderBox.class, true);
                    if (!list4.isEmpty())
                    {
                        MovieExtendsHeaderBox box4 = list4.get(0);
                        box4.parseDetails();
                        duration = box4.getFragmentDuration();
                    }
                    
                    List<MediaHeaderBox> list5 = isoFile.getBoxes(MediaHeaderBox.class, true);
                    if (!list5.isEmpty())
                    {
                        MediaHeaderBox box5 = list5.get(0);
                        box5.parseDetails();
                        duration = box5.getDuration();
                    }
                    
                    DebugLog.info(TAG, "esDescription:" + list1 + "" + "" + list3 + "" + list4 + "" + list5 + duration);
                    
                }
            }
            finally
            {
                if (isoFile != null)
                {
                    isoFile.close();
                }
            }
        }
        catch (FileNotFoundException e)
        {
            DebugLog.trace(TAG, e);
        }
        catch (IOException e)
        {
            DebugLog.trace(TAG, e);
        }
    }
    
    public static void testParser(String filePath)
    {
        byte[] buffer = Files.readFile(filePath);
        if (buffer == null)
        {
            DebugLog.error(TAG, "testParser filePath may be wrong!");
            return;
        }
        checkBuffer(buffer, new byte[] {47, 118, 0});
        if (2 > 0)
        {
            return;
        }
        testParser(buffer);
        IsoFile isoFile = null;
        FileInputStream stream = null;
        try
        {
            try
            {
                stream = new FileInputStream(filePath);
                isoFile = new IsoFile(stream.getChannel());
                if (isoFile != null)
                {
                    for (Box b : isoFile.getBoxes())
                    {
                        DebugLog.info(TAG, "Box:" + b.getType());
                        //   if (b.getType().equals("ftyp") || b.getType().equals("moov"))
                        {
                            searchChild(b);
                        }
                        
                        if (b.getType().equals("mdat"))
                        {
                            MediaDataBox mediaDataBox = (MediaDataBox)b;
                        }
                    }
                    List<SegmentIndexBox> segmentIndexList = isoFile.getBoxes(SegmentIndexBox.class, true);
                    SegmentIndexBox segmentIndex = segmentIndexList.get(0);
                    segmentIndex.parseDetails();
                    List<Entry> list = segmentIndex.getEntries();
                    long alltime = 0L;
                    for (Entry temp : list)
                    {
                        alltime += temp.getSubsegmentDuration();
                    }
                    
                    DebugLog.info(TAG, "list.allTime:" + alltime);
                    
                    List<ESDescriptorBox> compositionList = isoFile.getBoxes(ESDescriptorBox.class, true);
                    if (!compositionList.isEmpty())
                    {
                        ESDescriptorBox composition = compositionList.get(0);
                        composition.parseDetails();
                        String temp = composition.getDescriptorAsString();
                        ESDescriptor esDescriptoer = (ESDescriptor)composition.getDescriptor();
                        
                        DecoderConfigDescriptor decodeConfig = esDescriptoer.getDecoderConfigDescriptor();
                        byte[] dd = decodeConfig.getAudioSpecificInfo().getConfigBytes();
                        int audioType = decodeConfig.getAudioSpecificInfo().getAudioObjectType();
                        DebugLog.info(TAG, "audioType:" + audioType);
                        DebugLog.info(TAG, "decodeConfig dd:" + Arrays.toString(dd));
                        
                        DebugLog.info(TAG, "DescriptorAsString:" + temp);
                        alltime = 0;
                        //                        for (CompositionTimeToSample.Entry temp : composition.getEntries())
                        //                        {
                        //                            alltime += temp.getOffset();
                        //                        }
                    }
                    DebugLog.info(TAG, "list.allTime:" + alltime);
                    
                }
                DebugLog.info(TAG, "endTestISO");
            }
            finally
            {
                if (isoFile != null)
                {
                    isoFile.close();
                }
                if (stream != null)
                {
                    stream.close();
                }
            }
            
        }
        catch (FileNotFoundException e)
        {
            DebugLog.trace(TAG, e);
        }
        catch (IOException e)
        {
            DebugLog.trace(TAG, e);
        }
    }
    
    public static void checkBuffer(byte[] buffer, byte[] tag)
    {
        int iLoop = 0;
        byte[] sample = new byte[] {11, 31};
        if (tag != null)
        {
            sample = tag;
        }
        boolean find = false;
        for (iLoop = 0; iLoop < buffer.length - sample.length; iLoop++)
        {
            find = true;
            for (int jLoop = 0; jLoop < sample.length; jLoop++)
            {
                if (buffer[iLoop + jLoop] != sample[jLoop])
                {
                    find = false;
                    break;
                }
            }
            if (find == false)
            {
                continue;
            }
            break;
        }
        if (find)
        {
            DebugLog.info(TAG, "find sampel at :" + iLoop);
        }
        else
        {
            DebugLog.info(TAG, "not find sampel at :");
        }
    }
    
    public static void searchChild(Box b)
    {
        
        String boxString = b.toString();
        while (boxString.length() > 800)
        {
            DebugLog.info(TAG, "Box:" + boxString.substring(0, 800));
            boxString = boxString.substring(800);
        }
        DebugLog.info(TAG, "Box:" + boxString);
        
        if ("stbl".equals(b.getType()))
        {
            //SampleTableBox
            DebugLog.info(TAG, "find SampleTableBox:");
        }
        if (b instanceof Container)
        {
            Container container = (Container)b;
            
            for (Box temp : container.getBoxes())
            {
                searchChild(temp);
            }
        }
        
    }
}
