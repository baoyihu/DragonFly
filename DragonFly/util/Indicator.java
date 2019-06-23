package com.baoyihu.dragonfly.util;

public class Indicator
{
    
    public static int getInt8(byte[] buffer, int index)
    {
        int ret = buffer[index];
        return ret;
    }
    
    public static int getInt16(byte[] buffer, int index)
    {
        int temp1 = buffer[index] < 0 ? (buffer[index] + 256) : buffer[index];
        int temp2 = buffer[index + 1] < 0 ? (buffer[index + 1] + 256) : buffer[index + 1];
        int result = (temp1 << 8) + temp2;
        index += 2;
        return result;
    }
    
    public static long getLong32(byte[] buffer, int index)
    {
        
        long temp1 = buffer[index] < 0 ? (buffer[index] + 256) : buffer[index];
        long temp2 = buffer[index + 1] < 0 ? (buffer[index + 1] + 256) : buffer[index + 1];
        long temp3 = buffer[index + 2] < 0 ? (buffer[index + 2] + 256) : buffer[index + 2];
        long temp4 = buffer[index + 3] < 0 ? (buffer[index + 3] + 256) : buffer[index + 3];
        
        long result = (temp1 << 24) + (temp2 << 16) + (temp3 << 8) + temp4;
        index += 4;
        return result;
    }
    
    public static long getLong64(byte[] buffer, int index)
    {
        
        long temp1 = buffer[index] < 0 ? (buffer[index] + 256) : buffer[index];
        long temp2 = buffer[index + 1] < 0 ? (buffer[index + 1] + 256) : buffer[index + 1];
        long temp3 = buffer[index + 2] < 0 ? (buffer[index + 2] + 256) : buffer[index + 2];
        long temp4 = buffer[index + 3] < 0 ? (buffer[index + 3] + 256) : buffer[index + 3];
        long temp5 = buffer[index + 4] < 0 ? (buffer[index + 4] + 256) : buffer[index + 4];
        long temp6 = buffer[index + 5] < 0 ? (buffer[index + 5] + 256) : buffer[index + 5];
        long temp7 = buffer[index + 6] < 0 ? (buffer[index + 6] + 256) : buffer[index + 6];
        long temp8 = buffer[index + 7] < 0 ? (buffer[index + 7] + 256) : buffer[index + 7];
        
        long result = (temp1 << 56) + (temp2 << 48) + (temp3 << 40) + (temp4 << 32) + (temp5 << 24) + (temp6 << 16)
            + (temp7 << 8) + temp8;
        index += 8;
        return result;
    }
    
    public static long[] getLong32Arrray(byte[] buffer, int index, int lenth)
    {
        int count = lenth / 4;
        long[] array = new long[count];
        for (int iLoop = 0; iLoop < count; iLoop++)
        {
            array[iLoop] = getLong32(buffer, index);
            index += 4;
        }
        return array;
    }
    
    public static byte[] getByteArray(byte[] buffer, int index, int lenth)
    {
        byte[] ret = new byte[lenth];
        System.arraycopy(buffer, index, ret, 0, lenth);
        index += lenth;
        return ret;
    }
    
    public static String getStrArray(byte[] buffer, int index, int size)
    {
        String result = new String(buffer, index, size);
        index += size;
        return result;
    }
    
    public static String getStr32(byte[] buffer, int index)
    {
        String result = new String(buffer, index, 4);
        index += 4;
        return result;
    }
    
    private static final byte[] fixedHead = new byte[] {0, 0, 0, 1};
    
    public static final void setFixedHead(byte[] target, int from)
    {
        System.arraycopy(fixedHead, 0, target, from, 4);
    }
}
