/**
 *
 */
package com.novartis.opensource.yada.plugin;

import com.novartis.opensource.yada.Finder;
import com.novartis.opensource.yada.YADASecurityException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * @author Justin Finn
 * @since 8.7.6
 */

public interface Authorization {

	// --------------------------------------------------------------------------------
	// TODO: Change these to system properties
	// --------------------------------------------------------------------------------
	/**
	 * Constant equal to: {@value}
	 */
	public final static String YADA_IDENTITY_CACHE = "identity";

	/**
	 * Constant equal to: {@value}
	 */
	public final static Integer YADA_IDENTITY_TTL = 14399;

	/**
	 * Constant equal to: {@value}
	 */
	public final static String YADA_GRANT_CACHE = "grant";

	/**
	 * Constant equal to: {@value}
	 */
	public final static Integer YADA_GRANT_TTL = 1799;

	/**
	 * Constant equal to: {@value}
	 */
	public final static String YADA_GROUP_CACHE = "groupList";

	/**
	 * Constant equal to: {@value}
	 */
	public final static Integer YADA_GROUP_TTL = 119;

	// --------------------------------------------------------------------------------

	/**
	 * Authorization of general use for given context
	 * 
	 * @param payload
	 * @throws YADAPluginException
	 */
	public void authorize(String payload) throws YADASecurityException;

	/**
	 * Authorization of query use for given context
	 */
	public void authorize() throws YADASecurityException;

	/**
	 * Write to the IAM cache
	 * 
	 * @param cache
	 * @param key
	 * @param cacheValue
	 * @param ttl
	 * @since 8.7.6
	 */
	public default void setCacheEntry(String cache, String key, Object cacheValue, Integer ttl) {
		CacheManager cacheManager = CacheManager.getCacheManager(Finder.YADA_CACHE_MGR);
		if (cacheManager.getCache(cache) == null && cacheManager != null) { // .name(cache)
			// .maxElementsInMemory(1000) ... 1000
			// elements
			// .overflowToDisk(false)
			// .eternal(false)
			// .timeToLiveSeconds(ttl) ... ttl minute
			// token total lifetime
			// .timeToIdleSeconds(ttl) ... ttl minute
			// token idle lifetime
			Cache cachez = new Cache(cache, 1000, false, false, ttl, ttl);
			cacheManager.addCache(cachez);
		}
		Cache cacheIndex = cacheManager.getCache(cache);
		Element cacheElement = new Element(key, cacheValue);
		cacheIndex.put(cacheElement);
	}

	/**
	 * Read the IAM cache
	 * 
	 * @param cache
	 * @param key
	 * @return the stored string
	 * @since 8.7.6
	 */
	public default Object getCacheEntry(String cache, String key) {
		Object cacheValue = null;
		Element cacheDict = null;
		CacheManager cacheManager = CacheManager.getCacheManager(Finder.YADA_CACHE_MGR);
		if (cacheManager != null) {
			Cache cacheIndex = cacheManager.getCache(cache);
			if (cacheIndex != null) {
				cacheDict = cacheIndex.get(key);
				if (cacheDict != null) {
					cacheValue = cacheDict.getObjectValue();
				}
			}
		}
		return cacheValue;
	}

}
