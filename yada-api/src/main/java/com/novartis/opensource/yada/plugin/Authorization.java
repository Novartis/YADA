/**
 *
 */
package com.novartis.opensource.yada.plugin;

import com.novartis.opensource.yada.Finder;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADASecurityException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * @author Justin Finn
 * @since 8.7.6
 */

public interface Authorization {

	/**
	 * Constant with value: {@value}
	 *
	 * @since 2.0
	 */
	public final static String JWSKEY = "jws.key";

	/**
	 * Constant with value: {@value}
	 *
	 * @since 2.0
	 */
	public final static String JWTISS = "jwt.iss";

	// --------------------------------------------------------------------------------
	// TODO: Change these to system properties
	// --------------------------------------------------------------------------------

	/**
	 * Constant with value: {@value}
	 *
	 * @since 8.7.6
	 */
	public final static String YADA_HDR_AUTH = "Authorization";

	/**
	 * Constant with value: {@value}
	 *
	 * @since 8.7.6
	 */
	public final static String YADA_HDR_SYNC_TKN = "X-CSRF-Token";

	/**
	 * Constant with value: {@value}
	 *
	 * @since 8.7.6
	 */
	public final static String YADA_HDR_AUTH_JWT_PREFIX = "Bearer";

	/**
	 * Array of IAM headers we want to have access to
	 */
	public final static String[] YADA_HDR_AUTH_NAMES = { YADA_HDR_AUTH, YADA_HDR_SYNC_TKN };

	/**
	 * Constant with value: {@value}
	 *
	 * @since 8.7.6
	 */
	public final static String YADA_CK_TKN = "yadajwt";

	/**
	 * Constant with value: {@value}
	 *
	 * @since 8.7.6
	 */
	public final static String YADA_IDENTITY_SUB = "sub";

	/**
	 * Constant with value: {@value}
	 *
	 * @since 8.7.6
	 */
	public final static String YADA_IDENTITY_APP = "app";

	/**
	 * Constant with value: {@value}
	 *
	 * @since 8.7.6
	 */
	public final static String YADA_IDENTITY_ID = "identity";

	/**
	 * Constant with value: {@value}
	 *
	 * @since 8.7.6
	 */
	public final static String YADA_IDENTITY_KEY = "key";

	/**
	 * Constant with value: {@value}
	 *
	 * @since 8.7.6
	 */
	public final static String YADA_IDENTITY_KEYS = "keys";

	/**
	 * Constant with value: {@value}
	 *
	 * @since 8.7.6
	 */
	public final static String YADA_IDENTITY_TKN = "token";

	/**
	 * Constant with value: {@value}
	 *
	 * @since 8.7.6
	 */
	public final static String YADA_IDENTITY_GRANTS = "grants";

	/**
	 * Constant with value: {@value}
	 *
	 * @since 8.7.6
	 */
	public final static String YADA_IDENTITY_IAT = "iat";

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

	/**
	 * Constant with value: {@value}
	 *
	 * @since 3.0
	 */
	public final static String AUTH_TYPE_WHITELIST = "whitelist";

	// --------------------------------------------------------------------------------

	/**
	 * Constant equal to {@value}
	 * 
	 * @since 8.7.6
	 */
	public final static String RX_HDR_AUTH_USR_PREFIX = "(Basic)(.+?)([A-Za-z0-9\\-\\._~\\+\\/]+=*)";

	/**
	 * Constant equal to {@value}
	 * 
	 * @since 8.7.6
	 */
	public final static String RX_HDR_AUTH_USR_CREDS = "(.+)[:=](.+)";

	/**
	 * Constant equal to {@value} Formerly: (Bearer)(.+?)([a-zA-Z0-9-_.]{5,})
	 * 
	 * @since 8.7.6
	 */
	public final static String RX_HDR_AUTH_TKN_PREFIX = "(Bearer)(.+?)([A-Za-z0-9\\-\\._~\\+\\/]+=*)";

	// --------------------------------------------------------------------------------
	// TODO: Make these YADA queries?
	// --------------------------------------------------------------------------------

	/**
	 * Constant equal to {@value}. The query executed to evaluate authorization.
	 */
	public final static String YADA_LOGIN_QUERY = "SELECT a.app \"APP\", a.userid \"USERID\", a.role \"ROLE\" "
	    + "FROM yada_ug a JOIN yada_user b on a.userid = b.userid where b.userid = ? and b.pw = ? order by a.app";

	/**
	 * Constant equal to {@value}. The query executed to evaluate authorization.
	 */
	public final static String YADA_A11N_QUERY = "SELECT DISTINCT a.target, a.policy, a.type, a.qname "
	    + "FROM YADA_A11N a " // join YADA_QUERY b on (a.target = b.qname OR
	                          // a.target = b.app) "
	    + "WHERE a.target = ?";

	// --------------------------------------------------------------------------------

	/**
	 * Authorization of general use for given context
	 * 
	 * @param payload
	 * @throws YADASecurityException
	 */
	public void authorize(String payload) throws YADASecurityException;

	/**
	 * Authorization of query use for given context
	 * @throws YADASecurityException
	 */
	public void authorize() throws YADASecurityException;

	/**
	 * Confirm token is valid and user possesses necessary grants
	 * @param yadaReq 
	 * @param result 
	 * @throws YADASecurityException 
	 */
	public void authorizeYADARequest(YADARequest yadaReq, String result) throws YADASecurityException;

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
