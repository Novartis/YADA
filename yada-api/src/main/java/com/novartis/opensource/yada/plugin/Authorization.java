/**
 *
 */
package com.novartis.opensource.yada.plugin;

import com.novartis.opensource.yada.YADASecurityException;

/**
 * @author Justin Finn
 * @since 8.7.7
 */

public interface Authorization {
	 
	  /**
	   * Authorization of general use for given context
	   * @param string payload
	   */
	  public void authorize(String payload) throws YADASecurityException;
	  
	  /**
	   * Authorization of query use for given context
	   */
	  public void authorize() throws YADASecurityException;

}

