package com.baoyihu.dragonfly.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.baoyihu.common.util.Bytes;
import com.baoyihu.common.util.DebugLog;
import com.baoyihu.common.util.Pair;
import com.baoyihu.dragonfly.streamer.ByteStock;
import com.baoyihu.dragonfly.streamer.StreamResult;
import com.baoyihu.dragonfly.streamer.VideoBuffer;

public class NetWork
{
    private static final String TAG = "NetWork";
    
    public static String downloadToString(String urlString, String stringCode)
        throws IOException
    {
        HttpURLConnection connection = null;
        InputStreamReader reader = null;
        StringBuilder builder = new StringBuilder();
        try
        {
            URL url = new URL(urlString);
            connection = (HttpURLConnection)url.openConnection();
            int retCode = connection.getResponseCode();
            DebugLog.debug(TAG, "retCode:" + retCode);
            
            reader = new InputStreamReader(connection.getInputStream(), stringCode);
            int lenth = 0;
            int bufferSize = 1024 * 1024;
            char[] buffer = new char[bufferSize];
            do
            {
                lenth = reader.read(buffer, 0, bufferSize);
                if (lenth > -1)
                {
                    builder.append(buffer, 0, lenth);
                }
            } while (lenth > -1);
            
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
            
            if (reader != null)
            {
                reader.close();
            }
        }
        return builder.toString();
    }
    
    public static byte[] downloadToByteArray(String urlString)
        throws IOException
    {
        HttpURLConnection connection = null;
        InputStream reader = null;
        byte[] byteArray = new byte[0];
        try
        {
            URL url = new URL(urlString);
            connection = (HttpURLConnection)url.openConnection();
            int retCode = connection.getResponseCode();
            DebugLog.debug(TAG, "retCode:" + retCode);
            
            reader = connection.getInputStream();
            int lenth = 0;
            int bufferSize = 1024 * 1024;
            byte[] buffer = new byte[bufferSize];
            do
            {
                lenth = reader.read(buffer, 0, bufferSize);
                if (lenth > -1)
                {
                    byteArray = Bytes.connect(byteArray, byteArray.length, buffer, lenth);
                }
            } while (lenth > -1);
            
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
            
            if (reader != null)
            {
                reader.close();
            }
        }
        return byteArray;
    }
    
    public static byte[] downloadMp4(String urlString, List<Pair<String, String>> properties)
        throws IOException
    {
        HttpURLConnection connection = null;
        InputStream reader = null;
        long time = System.currentTimeMillis();
        byte[] ret = new byte[0];
        try
        {
            URL url = new URL(urlString);
            connection = (HttpURLConnection)url.openConnection();
            if (properties != null)
            {
                for (Pair<String, String> property : properties)
                {
                    connection.setRequestProperty(property.first, property.second);
                }
            }
            connection.setAllowUserInteraction(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "NetFox");
            int retCode = connection.getResponseCode();
            byte[] buffer = new byte[1024 * 1024];
            if (retCode == 200 || retCode == 206)
            {
                reader = connection.getInputStream();
                int size = 0;
                while ((size = reader.read(buffer)) > 0)
                {
                    ret = Bytes.connect(ret, ret.length, buffer, size);
                }
            }
            else
            {
                throw new IOException("the urlString:" + urlString + " ret:" + retCode);
            }
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
            
            if (reader != null)
            {
                reader.close();
            }
        }
        time = (System.currentTimeMillis() - time);
        long rate = ret.length * 8 / time;
        DebugLog.debug(TAG, " dowanload succeed size:" + ret.length + "  time:" + time + " Kpbs:" + rate);
        return ret;
    }
    
    public static void downloadMp4MultiThread(final String urlString, final StreamResult result,
        final List<Pair<String, String>> properties)
        throws IOException
    {
        VideoBuffer videoBuffer = result.getVideoBuffer();
        int readerCount = 1;
        int allcocated = 0;
        int rent = videoBuffer.getCapcity() / readerCount;
        long rangeFrom = videoBuffer.getRawBufferFrom();
        
        Iterator<Pair<String, String>> iterator = properties.iterator();
        while (iterator.hasNext())
        {
            Pair<String, String> temp = iterator.next();
            if (temp.first.equals("Range"))
            {
                iterator.remove();
                break;
            }
        }
        
        List<Pair<String, String>> pureProperties = new ArrayList<Pair<String, String>>(properties);
        for (int index = 0; index < readerCount; index++)
        {
            if (index == readerCount - 1)
            {
                rent = videoBuffer.getCapcity() - allcocated;
            }
            final ByteStock stock = new ByteStock(videoBuffer, allcocated, rent);
            stock.setName("Stock" + index);
            
            final List<Pair<String, String>> tempProperties = new ArrayList<Pair<String, String>>(pureProperties);
            String range = "bytes=" + (allcocated + rangeFrom) + "-" + (rangeFrom + allcocated + rent - 1);
            tempProperties.add(new Pair<String, String>("Range", range));
            DebugLog.debug(TAG, " downloadMp4MultiThread start range:" + range);
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        downloadMp4Stock(urlString, stock, tempProperties);
                    }
                    catch (IOException e)
                    {
                        DebugLog.trace(TAG, e);
                    }
                }
                
            }).start();
            
            videoBuffer.addStock(stock);
            allcocated += rent;
        }
    }
    
    public static void downloadMp4Stock(String urlString, ByteStock stock, List<Pair<String, String>> properties)
        throws IOException
    {
        HttpURLConnection connection = null;
        InputStream reader = null;
        try
        {
            URL url = new URL(urlString);
            connection = (HttpURLConnection)url.openConnection();
            if (properties != null)
            {
                for (Pair<String, String> property : properties)
                {
                    connection.setRequestProperty(property.first, property.second);
                }
            }
            connection.setAllowUserInteraction(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "NetFox");
            int retCode = connection.getResponseCode();
            byte[] buffer = new byte[1024 * 1024];
            if (retCode == 200 || retCode == 206)
            {
                reader = connection.getInputStream();
                int size = 0;
                while ((size = reader.read(buffer)) > 0)
                {
                    stock.addBuffer(buffer, size);
                }
            }
            else
            {
                throw new IOException("the urlString:" + urlString + " ret:" + retCode);
            }
            DebugLog.info(TAG, "downloadMp4Stock end");
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
            
            if (reader != null)
            {
                reader.close();
            }
        }
        //  Logger.debug(TAG, " dowanload succeed size:" + ret.length);
        
    }
    
    public interface BufferCallback
    {
        public void onBuffer(byte[] buffer);
    }
}
