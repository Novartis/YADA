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
package com.novartis.opensource.yada.util;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import com.novartis.opensource.yada.ConnectionFactory;

import net.sf.ehcache.CacheManager;

/**
 * A stubbed servlet for setting up the YADA environment. It instantiates the ehcache.
 * @author David Varon
 * @since 4.1.0
 */
public class YADAInit extends HttpServlet {

	/**
	 * Support for serialization
	 */
	private static final long serialVersionUID = 8596892218815479807L;
	/**
	 * A map of urls to aliases (intended)
	 * @since PROVISIONAL
	 */
	@SuppressWarnings("unused")
	private Map<String,String> urls = new HashMap<String,String>();
 	
	
	/**
	 * Initialization of ehcache for YADA Index.  See <a href="http://ehcache.org">http://ehcache.org</a>
	 * @since 4.1.0
	 */
	@Override
	public void init() 
	{
 		String file = getInitParameter("ehcache-config");
 		if(file != null) 
 		{
 			System.out.println("ehcache config is at ["+file+"]");
 			URL url = getClass().getResource(file);
 	 		CacheManager.create(url);
 		}
// 		ConnectionFactory.getConnectionFactory();
 		//TODO implement URL string mapping 
	}
 	
 	

}
