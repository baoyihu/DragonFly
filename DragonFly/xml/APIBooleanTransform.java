package com.baoyihu.dragonfly.xml;

import org.simpleframework.xml.transform.Transform;

public class APIBooleanTransform implements Transform<Boolean>
{
    
    @Override
    public Boolean read(String value)
    {
        if (value == null)
        {
            return Boolean.FALSE;
        }
        
        if ("1".equals(value))
        {
            return Boolean.TRUE;
        }
        
        return Boolean.valueOf(value);
    }
    
    @Override
    public String write(Boolean value)
    {
        return value ? "1" : "0";
    }
}
