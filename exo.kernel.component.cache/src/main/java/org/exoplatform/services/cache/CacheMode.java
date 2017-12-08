package org.exoplatform.services.cache;

/**
 * Cache topology mode.
 */
public enum CacheMode
{
    /**
     * Data is not replicated.
     */
    NONE("", true, CacheMode.CACHE_MODE_LOCAL),

    /**
     * Data is not replicated.
     */
    LOCAL("local", true, CacheMode.CACHE_MODE_LOCAL),

    /**
     * Data replicated synchronously.
     */
    REPLICATION("replication", true, CacheMode.CACHE_MODE_REPLICATED),

    /**
     * Data replicated asynchronously.
     */
    ASYNCREPLICATION("asyncReplication", false, CacheMode.CACHE_MODE_REPLICATED),

    /**
     * Data invalidated asynchronously.
     */
    ASYNCINVALIDATION("asyncInvalidation", false, CacheMode.CACHE_MODE_INVALIDATED),

    /**
     * Data invalidated synchronously.
     */
    SYNCINVALIDATION("syncInvalidation", true, CacheMode.CACHE_MODE_INVALIDATED),

    /**
     * Synchronous DIST
     */
    DISTRIBUTED("distributed", true, CacheMode.CACHE_MODE_DISTRIBUTED);

    /**
     * replicated mode const
     */
    private final static int CACHE_MODE_REPLICATED = 0;

    /**
     * invalidated mode const
     */
    private final static int CACHE_MODE_INVALIDATED = 1;

    /**
     * distributed mode const
     */
    private final static int CACHE_MODE_DISTRIBUTED = 2;

    /**
     * local mode const
     */
    private final static int CACHE_MODE_LOCAL = 3;

    private final String name;

    private final boolean sync;

    private final int topology;

    CacheMode(String name, boolean sync, int topology)
    {
        this.name = name;
        this.sync = sync;
        this.topology = topology;
    }

    public boolean isReplicated()
    {
        return topology == CACHE_MODE_REPLICATED;
    }

    public boolean isInvalidated()
    {
        return topology == CACHE_MODE_INVALIDATED;
    }

    public boolean isDistributed()
    {
        return topology == CACHE_MODE_DISTRIBUTED;
    }

    public boolean isLocal()
    {
        return topology == CACHE_MODE_LOCAL;
    }

    public String getName()
    {
        return name;
    }

    public boolean isSync()
    {
        return sync;
    }

}
