package com.baoyihu.dragonfly.node.dash;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import com.baoyihu.dragonfly.node.dash.DashAdaptationSet;

@Root(name = "Period", strict = false)
public class DashPeriod
{
    @ElementList(required = true, name = "AdaptationSet", inline = true)
    private List<DashAdaptationSet> adaptationSet;
    
    public List<DashAdaptationSet> getAdaptationSet()
    {
        return adaptationSet;
    }
    
    public void setAdaptationSet(List<DashAdaptationSet> adaptationSet)
    {
        this.adaptationSet = adaptationSet;
    }
}
