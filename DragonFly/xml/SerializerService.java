package com.baoyihu.dragonfly.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import org.mp4parser.Box;
import org.mp4parser.IsoFile;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.PersistenceException;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;

import com.baoyihu.common.util.DebugLog;

public final class SerializerService
{
    public static final String TAG = "SerializerService";
    
    private static Serializer xmlSerializer;
    
    static
    {
        Strategy strategy = new AnnotationStrategy();
        xmlSerializer = new Persister(strategy, new APIBooleanMatcher());
    }
    
    private SerializerService()
    {
        
    }
    
    private static final String NEW_LINE = System.getProperty("line.separator");
    
    public static <T> String toXml(final T object, String codeType)
        throws Exception
    {
        String pre = "<?xml version=\"1.0\" encoding=\"" + codeType + "\"?>" + NEW_LINE;
        byte[] buffer = SerializerService.toXml(object);
        String info = new String(buffer, codeType);
        return pre + info;
    }
    
    public static <T> byte[] toXml(final T object)
        throws Exception
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] retBytes = null;
        if (outputStream != null)
        {
            
            try
            {
                xmlSerializer.write(object, outputStream);
                retBytes = outputStream.toByteArray();
            }
            finally
            {
                outputStream.close();
            }
            
        }
        return retBytes;
        
    }
    
    public static <T> T fromStream(final Class<T> klass, InputStream stream)
        throws PersistenceException
    {
        try
        {
            return xmlSerializer.read(klass, stream, false);
        }
        catch (Exception e)
        {
            throw new PersistenceException(e, "all parseException to psersistence");
        }
        
    }
    
    public static <T> T fromXml(final Class<T> klass, final String xml)
        throws PersistenceException
    {
        try
        {
            return xmlSerializer.read(klass, xml, false);
        }
        catch (Exception e)
        {
            throw new PersistenceException(e, "all parseException to psersistence");
        }
        
    }
    
    public static <T extends Box> T parseDashNode(byte[] buffer, Class<T> clazz)
    {
        ReadableByteChannel channel = null;
        channel = Channels.newChannel(new ByteArrayInputStream(buffer));
        IsoFile isoFile = null;
        try
        {
            try
            {
                isoFile = new IsoFile(channel);
                List<T> objectList = isoFile.getBoxes(clazz);
                if (!objectList.isEmpty())
                {
                    return objectList.get(0);
                }
                else
                {
                    return null;
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
        catch (IOException e)
        {
            DebugLog.trace(TAG, e);
        }
        
        return null;
        
    }
}
