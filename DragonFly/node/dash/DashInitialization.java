package com.baoyihu.dragonfly.node.dash;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "Initialization", strict = false)
public class DashInitialization
{
    @Attribute(name = "range", required = false)
    String range;
    
    public String getRange()
    {
        return range;
    }
}
