package com.baoyihu.dragonfly.node.dash;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import com.baoyihu.dragonfly.node.dash.DashInitialization;

@Root(name = "SegmentBase", strict = false)
public class DashSegmentBase
{
    @Attribute(name = "timescale", required = false)
    private int timescale;
    
    @Attribute(name = "indexRangeExact", required = false)
    private boolean indexRangeExact;
    
    @Attribute(name = "indexRange", required = false)
    private String indexRange;// = "756-17847";
    
    @Element(name = "Initialization", required = false)
    private DashInitialization initialization;
    
    public int getTimescale()
    {
        return timescale;
    }
    
    public void setTimescale(int timescale)
    {
        this.timescale = timescale;
    }
    
    public boolean isIndexRangeExact()
    {
        return indexRangeExact;
    }
    
    public void setIndexRangeExact(boolean indexRangeExact)
    {
        this.indexRangeExact = indexRangeExact;
    }
    
    public String getIndexRange()
    {
        return indexRange;
    }
    
    public void setIndexRange(String indexRange)
    {
        this.indexRange = indexRange;
    }
    
    public DashInitialization getInitialization()
    {
        return initialization;
    }
    
    public void setInitialization(DashInitialization initialization)
    {
        this.initialization = initialization;
    }
    
}
