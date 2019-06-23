package com.baoyihu.dragonfly.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.mp4parser.Box;
import org.mp4parser.IsoFile;

import com.baoyihu.common.util.DebugLog;

import android.text.TextUtils;

public class IsoPrinter
{
    private static final String TAG = "IsoPrinter";
    
    private static final String NEW_LINE = System.getProperty("line.separator");
    
    public static void print(String filePath)
    {
        IsoFile isoFile = null;
        FileInputStream stream = null;
        StringBuilder builder = new StringBuilder();
        DebugLog.info(TAG, "ISO begin:");
        try
        {
            stream = new FileInputStream(filePath);
            isoFile = new IsoFile(stream.getChannel());
            if (isoFile != null)
            {
                for (Box b : isoFile.getBoxes())
                {
                    builder.append(getXMLOfBox(b, b.getType()) + NEW_LINE);
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
        DebugLog.info(TAG, "ISO is:" + builder.toString());
    }
    
    public static String getXMLOfBox(Box rootBox, String fieldName)
    {
        Class<?> rootClass = rootBox.getClass();
        
        Field[] fields = rootClass.getDeclaredFields();
        StringBuilder builder = new StringBuilder();
        if (TextUtils.isEmpty(fieldName))
        {
            fieldName = rootClass.getSimpleName();
        }
        
        builder.append("<" + fieldName + ">" + NEW_LINE);
        for (Field field : fields)
        {
            field.setAccessible(true);
            String fieldStr = fieldToXMLString(rootBox, field);
            if (!TextUtils.isEmpty(fieldStr))
            {
                builder.append(fieldStr);
            }
        }
        builder.append("</" + fieldName + ">" + NEW_LINE);
        return builder.toString();
    }
    
    private static String fieldToXMLString(Box rootBox, Field field)
    {
        StringBuilder builder = new StringBuilder();
        try
        {
            Object fieldValue = field.get(rootBox);
            if (fieldValue == null)
            {
                return "";
            }
            Class<?> fieldClass = fieldValue.getClass();
            if (fieldClass == Integer.class)
            {
                
            }
            else if (fieldClass == Byte.class)
            {
                
            }
            else if (fieldClass == Character.class)
            {
                
            }
            else if (fieldClass == Short.class)
            {
                
            }
            else if (fieldClass == Long.class)
            {
                
            }
            else if (fieldClass == Float.class)
            {
                
            }
            else if (fieldClass == Double.class)
            {
                
            }
            else if (fieldClass == String.class)
            {
                
            }
            else if (fieldClass.isArray())
            {
                Class<?> innerType = fieldClass.getComponentType();
                String innerStr = getArrayString(rootBox, fieldValue, field.getName(), innerType);
                builder.append(innerStr);
                
            }
            else if (isList(fieldClass))
            {
                //   Class<?> innerType = getListInnerType(fieldValue, fieldClass);
                List<Object> objectList = (List<Object>)fieldValue;
                builder.append("<" + field.getName() + ">" + NEW_LINE);
                for (Object temp : objectList)
                {
                    if (temp instanceof Box)
                    {
                        builder.append(getXMLOfBox((Box)temp, ""));
                    }
                }
                builder.append("</" + field.getName() + ">" + NEW_LINE);
            }
            else
            {
                if (fieldValue instanceof Box)
                {
                    String boxString = getXMLOfBox((Box)fieldValue, "");
                    builder.append(boxString);
                }
                else
                {
                    DebugLog.error(TAG, "There is some  Object Array and not Box:" + fieldValue.getClass().getName());
                }
            }
        }
        catch (IllegalAccessException e)
        {
            DebugLog.trace(TAG, e);
        }
        catch (IllegalArgumentException e)
        {
            DebugLog.trace(TAG, e);
        }
        
        return builder.toString();
    }
    
    private static Class<?> getListInnerType(Object para, Class<?> inputType)
    {
        try
        {
            Method method = inputType.getMethod("get", int.class);
            Object innerObject = method.invoke(para, new Object[] {0});
            return innerObject.getClass();
        }
        catch (NoSuchMethodException e)
        {
            DebugLog.trace(TAG, e);
        }
        catch (IllegalAccessException e)
        {
            DebugLog.trace(TAG, e);
        }
        catch (IllegalArgumentException e)
        {
            DebugLog.trace(TAG, e);
        }
        catch (InvocationTargetException e)
        {
            DebugLog.trace(TAG, e);
        }
        return Object.class;
    }
    
    private static String getArrayString(Box rootBox, Object data, String fieldName, Class<?> innerClass)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("<" + fieldName + ">" + NEW_LINE);
        
        if (innerClass == int.class)
        {
            int[] intArray = ((int[])data).clone();
            for (int arrayValue : intArray)
            {
                builder.append("<int>" + arrayValue + "</int>" + NEW_LINE);
            }
        }
        else if (innerClass == Integer.class)
        {
            Integer[] intArray = ((Integer[])data).clone();
            for (Integer arrayValue : intArray)
            {
                builder.append("<Integer>" + arrayValue + "</Integer>" + NEW_LINE);
            }
        }
        else if (innerClass == byte.class)
        {
            byte[] intArray = ((byte[])data).clone();
            for (byte arrayValue : intArray)
            {
                builder.append("<byte>" + arrayValue + "</byte>" + NEW_LINE);
            }
        }
        else if (innerClass == Byte.class)
        {
            Byte[] intArray = ((Byte[])data).clone();
            for (Byte arrayValue : intArray)
            {
                builder.append("<Byte>" + arrayValue + "</Byte>" + NEW_LINE);
            }
        }
        else if (innerClass == char.class)
        {
            char[] intArray = ((char[])data).clone();
            for (char arrayValue : intArray)
            {
                builder.append("<char>" + arrayValue + "</char>" + NEW_LINE);
            }
        }
        else if (innerClass == Character.class)
        {
            Character[] intArray = ((Character[])data).clone();
            for (Character arrayValue : intArray)
            {
                builder.append("<Character>" + arrayValue + "</Character>" + NEW_LINE);
            }
        }
        else if (innerClass == short.class)
        {
            short[] intArray = ((short[])data).clone();
            for (short arrayValue : intArray)
            {
                builder.append("<short>" + arrayValue + "</short>" + NEW_LINE);
            }
        }
        else if (innerClass == Short.class)
        {
            Short[] intArray = ((Short[])data).clone();
            for (Short arrayValue : intArray)
            {
                builder.append("<Short>" + arrayValue + "</Short>" + NEW_LINE);
            }
        }
        else if (innerClass == long.class)
        {
            long[] intArray = ((long[])data).clone();
            for (long arrayValue : intArray)
            {
                builder.append("<long>" + arrayValue + "</long>" + NEW_LINE);
            }
        }
        else if (innerClass == Long.class)
        {
            Long[] intArray = ((Long[])data).clone();
            for (Long arrayValue : intArray)
            {
                builder.append("<Long>" + arrayValue + "</Long>" + NEW_LINE);
            }
        }
        else if (innerClass == float.class)
        {
            float[] intArray = ((float[])data).clone();
            for (float arrayValue : intArray)
            {
                builder.append("<float>" + arrayValue + "</float>" + NEW_LINE);
            }
        }
        else if (innerClass == Float.class)
        {
            Float[] intArray = ((Float[])data).clone();
            for (Float arrayValue : intArray)
            {
                builder.append("<Float>" + arrayValue + "</Float>" + NEW_LINE);
            }
        }
        else if (innerClass == double.class)
        {
            double[] intArray = ((double[])data).clone();
            for (double arrayValue : intArray)
            {
                builder.append("<double>" + arrayValue + "</double>" + NEW_LINE);
            }
        }
        else if (innerClass == Double.class)
        {
            Double[] intArray = ((Double[])data).clone();
            for (Double arrayValue : intArray)
            {
                builder.append("<Double>" + arrayValue + "</Double>" + NEW_LINE);
            }
        }
        else if (innerClass == String.class)
        {
            String[] intArray = ((String[])data).clone();
            for (String arrayValue : intArray)
            {
                builder.append("<String>" + arrayValue + "</String>" + NEW_LINE);
            }
        }
        else
        {
            for (Object arrayValue : (Object[])data)
            {
                if (arrayValue instanceof Box)
                {
                    String boxString = getXMLOfBox((Box)arrayValue, "");
                    builder.append(boxString);
                }
                else
                {
                    DebugLog.error(TAG, "There is some  Object Array and not Box");
                }
            }
        }
        builder.append("</" + fieldName + ">" + NEW_LINE);
        return builder.toString();
    }
    
    private static boolean isList(Class<?> input)
    {
        boolean ret = false;
        for (Class<?> temp : input.getInterfaces())
        {
            if (temp == List.class)
            {
                ret = true;
                break;
            }
        }
        Class<?> supperClass = input.getSuperclass();
        if (!ret && supperClass != Object.class)
        {
            ret = isList(supperClass);
        }
        return ret;
    }
}
