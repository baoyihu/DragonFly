package com.baoyihu.dragonfly.streamer;

import java.util.Random;

public class Testor
{
    private final ByteStock stock;
    
    private final int from;
    
    private final int count;
    
    private int filled;
    
    private Random random = null;
    
    public Testor(ByteStock stock)
    {
        this.stock = stock;
        this.from = stock.getFrom();
        this.count = stock.getSize();
        random = new Random();
    }
    
}
