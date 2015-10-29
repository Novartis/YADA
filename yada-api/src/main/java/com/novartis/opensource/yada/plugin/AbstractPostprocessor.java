/**
 * 
 */
package com.novartis.opensource.yada.plugin;

import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADARequest;

/**
 * @author David Varon
 * @since 0.4.2
 */
public abstract class AbstractPostprocessor implements Postprocess
{
	/**
	 * Null implementation
	 * @see com.novartis.opensource.yada.plugin.Postprocess#engage(com.novartis.opensource.yada.YADAQuery)
	 */
	@Override
	public void engage(YADAQuery yq) throws YADAPluginException { /* nothing to do */ }
	
	/**
	 * Null implementation
	 * @throws YADAPluginException when there is a processing error
	 * @see com.novartis.opensource.yada.plugin.Postprocess#engage(com.novartis.opensource.yada.YADARequest, java.lang.String)
	 */
	@Override
	public String engage(YADARequest yadaReq, String result) throws YADAPluginException { return null; }
	
}
