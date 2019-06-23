package com.baoyihu.dragonfly.xml;

import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

public class APIBooleanMatcher implements Matcher
{
    
    @SuppressWarnings("rawtypes")
    @Override
    public Transform match(Class type)
    {
        if (type.equals(boolean.class) || type.equals(Boolean.class))
        {
            return new APIBooleanTransform();
        }
        return null;
    }
}
