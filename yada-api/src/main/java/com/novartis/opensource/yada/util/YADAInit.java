package com.novartis.opensource.yada.util;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import net.sf.ehcache.CacheManager;

/**
 * A stubbed servlet for setting up the YADA environment. It instantiates the ehcache.
 * @author David Varon
 * @since 0.4.1.0
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
	 * Initialization of ehcache for YADA Index.  See {@link "http://ehcache.org"}
	 * @since 0.4.1.0
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
 		
 		
 		//TODO implement URL string mapping 
	}
 	
 	

}
