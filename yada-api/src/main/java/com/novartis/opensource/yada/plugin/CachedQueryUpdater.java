/**
 * Copyright 2016 Novartis Institutes for BioMedical Research Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADAQueryConfigurationException;

/**
 * A post-processer to update the cache with a new version of the query just
 * updated in the admin tool. This plugin could also be executed on-demand with
 * standard parameters, for instance if a singl query was updated directly in
 * the index. To update all queries in the cache use the {@link CacheUpdater}
 * {@link Bypass} plugin.
 * 
 * @author David Varon
 * @since 4.1.0
 */
public class CachedQueryUpdater extends AbstractPostprocessor
{

	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(CachedQueryUpdater.class);
	/**
	 * Constant with value equal to: {@value}
	 */
	private final static String QNAME = "QNAME";

	/**
	 * Removes the {@link YADAQuery} from the cache, and re-requests it from
	 * {@link Finder}, which will re-add it.
	 * 
	 * @see com.novartis.opensource.yada.plugin.Postprocess#engage(YADAQuery)
	 */
	@Override
	public void engage(YADAQuery yq) throws YADAPluginException
	{
		String q = yq.getData().get(0).get(QNAME)[0];
		Cache yadaIndex = ConnectionFactory.getCacheConnection(	Finder.YADA_CACHE_MGR,
																														Finder.YADA_CACHE);
		try
		{
			l.debug("Refreshing verson of [" + q + "] in cache.");
			Element element = new Element(q, new Finder().getQueryFromIndex(q));
			yadaIndex.put(element); // automatically overwrites, or writes anew
			yq.getResult().getResults().add(0, "{\"cache_status\":\"UPDATED\",\"timestamp\":\""+new java.util.Date().toString()+"\"}");
		} 
		catch (YADAConnectionException e)
		{
			throw new YADAPluginException(e.getMessage(), e);
		} 
		catch (YADAFinderException e)
		{
			throw new YADAPluginException(e.getMessage(), e);
		} 
		catch (YADAQueryConfigurationException e) 
		{
		  throw new YADAPluginException(e.getMessage(), e);
    }
	}
}
