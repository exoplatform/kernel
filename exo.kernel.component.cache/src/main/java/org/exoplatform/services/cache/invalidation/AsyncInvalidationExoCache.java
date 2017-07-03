package org.exoplatform.services.cache.invalidation;

import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheListenerContext;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;
import org.exoplatform.services.cache.concurrent.ListenerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This implementation of {@link ExoCache} will behave exactly the same way as {@link ConcurrentFIFOExoCache}
 * except in case of a cache change, indeed the modifications will be first applied locally
 * then it will be invalidated over the cluster asynchronously to limit the performance impact
 * on the local cluster node.
 * This class can be used as a drop-in replacement for {@link ConcurrentFIFOExoCache} in a cluster environment.
 *
 */

public class AsyncInvalidationExoCache<K extends Serializable, V> extends ConcurrentFIFOExoCache<K, V>
{
    private static final Log LOG = ExoLogger.getLogger(AsyncInvalidationExoCache.class);

    /**
     * Replicated cache used to synchronise local cache
     */
    private final ExoCache<K, HashCode<V>> replicatedCache;

    /**
     * Hash Code object used to remove remote entry if not exist on replicated cache
     */
    private final HashCode<V> invalidationHashCodeObject = new HashCode<V>((V) new String("NULL_OBJECT_INVALIDATION"));

    private final int invalidationHashCode = invalidationHashCodeObject.hashCode();

    public AsyncInvalidationExoCache(ExoCache<K, HashCode<V>> replicatedCache)
    {
        this.replicatedCache = replicatedCache;

        //configure local cache
        setMaxSize(replicatedCache.getMaxSize());
        setName(replicatedCache.getName());
        setLiveTime(replicatedCache.getLiveTime());
        setLabel(replicatedCache.getLabel());
        setLogEnabled(replicatedCache.isLogEnabled());

        //add add Cache Listener
        this.replicatedCache.addCacheListener(new InvalidationListener());
    }

    @Override
    public void put(K key, V value) throws NullPointerException
    {
        HashCode<V> hashCode= new HashCode<V>(value);
        LOG.debug(getName() + "  PUT KEY " + key+ " hash "+ hashCode);
        super.put(key, value);

        //Invalidate remote key async way
        replicatedCache.put(key, hashCode);

    }

    @Override
    public void putLocal(K key, V value) throws NullPointerException
    {
        LOG.debug(getName() + " PUT Local  KEY " + key);
        super.put(key, value);
    }

    @Override
    public V remove(Serializable key) throws NullPointerException
    {
        LOG.debug(getName() + "  Remove KEY " + key);
        V value =  super.remove(key);
        HashCode<V> hashCode = replicatedCache.get(key);

        if(value != null && hashCode == null)
        {
            //entry exist only on local cache , use invalidationHashCodeObject to update remote cache
            replicatedCache.put((K) key, invalidationHashCodeObject);  ;
        }
        else
        {
            //remove entry from synchronized cache
            replicatedCache.remove(key);
        }
        return value;
    }

    @Override
    public void removeLocal(Serializable key) throws NullPointerException
    {
        LOG.debug(getName() + "  Remove Local KEY " + key);
        super.remove(key);
    }

    @Override
    public void putMap(Map<? extends K, ? extends V> objs) throws NullPointerException, IllegalArgumentException
    {
        if (objs == null)
        {
            throw new IllegalArgumentException("No null map accepted");
        }

        super.putMap(objs);

        Map<K, HashCode<V>> map = new LinkedHashMap<K, HashCode<V>>();
        for (Map.Entry<? extends K, ? extends V> entry : objs.entrySet())
        {
            if (entry.getKey() == null)
            {
                throw new IllegalArgumentException("No null cache key accepted");
            }
            else if (entry.getValue() == null)
            {
                throw new IllegalArgumentException("No null cache value accepted");
            }
            map.put(entry.getKey(), new HashCode<V>(entry.getValue()));
        }
        replicatedCache.putAsyncMap(map);
    }

    private class InvalidationListener implements CacheListener<K, HashCode<V>>
    {
        @Override
        public void onExpire(CacheListenerContext context, K key, HashCode<V> obj) throws Exception
        {
        }

        @Override
        public void onRemove(CacheListenerContext context, K key, HashCode<V> obj) throws Exception
        {
            LOG.debug(getName() + " ON onRemove KEY " + key);
            removeLocal(key);
        }

        @Override
        public void onPut(CacheListenerContext context, K key, HashCode<V> obj) throws Exception
        {
            V value = obj == null ? null : obj.getValue();
            if (value == null)
            {
                if(obj.hashCode() == invalidationHashCode)
                {
                    LOG.debug(getName() + " ON PUT invalidationHashCode KEY " + key+ " hashCode "+ obj.hashCode());
                    removeLocal(key);
                    return;
                }

                // we assume that it is a remote put since the value is not inside the HashCode object
                V currentValue = get(key);
                if (currentValue != null && obj != null && currentValue.hashCode() == obj.hashCode())
                {
                    LOG.debug(getName() + " ON PUT equal hash KEY " + key+ " hashCode "+ obj.hashCode());

                    // We assume that it is the same value so we don't change the local cache
                    value = currentValue;
                }
                else
                {
                    LOG.debug(getName() + " ON PUT not equal hash KEY " + key+ " hashCode "+ obj.hashCode());

                    value =  null;
                    // A new value has been added to the cache so we invalidate the local one
                    removeLocal(key);
                }
            }

            for (ListenerContext<? super K, ? super V> listener : getListeners() )
                try
                {
                    listener.onPut(key, value);
                }
                catch (Exception e)
                {
                    if (LOG.isWarnEnabled())
                        LOG.warn("Cannot execute the CacheListener properly", e);
                }
        }

        @Override
        public void onGet(CacheListenerContext context, K key, HashCode<V> obj) throws Exception
        {

        }

        @Override
        public void onClearCache(CacheListenerContext context) throws Exception
        {

        }
    }

    public void onExpire(K key, V obj)
    {
        replicatedCache.removeLocal(key);
        super.onExpire(key, obj);
    }

    public void onClearCache(){
        replicatedCache.clearCache();
        super.onClearCache();
    }

    public List<ListenerContext<K, V>> getListeners() {
        return super.getListeners();
    }
}
