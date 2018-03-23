package com.tomgs.spring.data.coherence.core.objects;


import com.tomgs.spring.data.coherence.core.Entity;


/**
 * @author Aleksandar Seovic  2013.10.09
 */
public abstract class LiveObject<T> implements Entity<T>
    {
    public abstract void start();
    public abstract void stop();

    public void restart()
        {
        stop();
        start();
        }
    }
