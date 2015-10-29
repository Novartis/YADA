/**
 * 
 */
package com.novartis.opensource.yada.plugin;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.ConnectionFactory;
import com.novartis.opensource.yada.Finder;
import com.novartis.opensource.yada.YADAConnectionException;
import com.novartis.opensource.yada.YADAFinderException;
import com.novartis.opensource.yada.YADARequest;

/**
 * Updates all queries in the cache.  Useful when updating the YADA Index from the command line.
 * @author David Varon
 * @since 0.4.1.0
 */
public class CacheUpdater extends AbstractBypass
{

	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(CacheUpdater.class);

	/** 
	 * @see com.novartis.opensource.yada.plugin.Bypass#engage(com.novartis.opensource.yada.YADARequest)
	 */
	@SuppressWarnings("unchecked")
  @Override
	public String engage(YADARequest yadaReq) throws YADAPluginException
	{
		Cache yadaIndex = ConnectionFactory.getCacheConnection(Finder.YADA_CACHE_MGR,Finder.YADA_CACHE);
		for(Object q : yadaIndex.getKeys().toArray(new Object[yadaIndex.getKeys().size()]))
		{
			try
			{
				l.debug("Refreshing verson of [" + q + "] in cache.");
				Element element = new Element(q, new Finder().getQueryFromIndex((String)q));
				yadaIndex.put(element); // automatically overwrites, or writes anew
			} 
			catch (YADAConnectionException e)
			{
				throw new YADAPluginException(e.getMessage(), e);
			} 
			catch (YADAFinderException e)
			{
				throw new YADAPluginException(e.getMessage(), e);
			}
		}
		return "Cache successfully updated on " + new java.util.Date().toString();
	}

}
