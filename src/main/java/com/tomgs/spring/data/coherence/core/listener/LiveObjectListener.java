package com.tomgs.spring.data.coherence.core.listener;


import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.util.MapEvent;
import com.tomgs.spring.data.coherence.core.objects.LiveObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Aleksandar Seovic  2013.10.09
 */
public class LiveObjectListener<K> extends AbstractBackingMapListener<K, LiveObject<K>>
    {
    private Map<K, LiveObject<K>> liveObjects = new ConcurrentHashMap<K, LiveObject<K>>();

    public LiveObjectListener(BackingMapManagerContext context)
        {
        super(context);
        }

    public void entryInserted(MapEvent evt)
        {
        LiveObject<K> liveObject = getNewValue(evt);
        liveObjects.put(liveObject.getId(), liveObject);
        liveObject.start();
        }

    public void entryUpdated(MapEvent evt)
        {
        LiveObject<K> liveObject = getNewValue(evt);
        LiveObject<K> old = liveObjects.put(liveObject.getId(), liveObject);
        if (old != null)
            {
            old.stop();
            }
        liveObject.start();
        }

    public void entryDeleted(MapEvent evt)
        {
        LiveObject<K> old = liveObjects.remove(getKey(evt));
        if (old != null)
            {
            old.stop();
            }
        }
    }
