/**
 * 
 */
package com.novartis.opensource.yada.plugin;

import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;

/**
 * @author David Varon
 * @since 0.4.2
 */
public abstract class AbstractBypass implements Bypass
{

	/**
	 * Null implementation
	 * @see com.novartis.opensource.yada.plugin.Bypass#engage(com.novartis.opensource.yada.YADARequest)
	 */
	@Override
	public String engage(YADARequest yadaReq) throws YADAPluginException
	{
		return null;
	}

	/**
	 * Null implementation
	 * @see com.novartis.opensource.yada.plugin.Bypass#engage(com.novartis.opensource.yada.YADARequest, com.novartis.opensource.yada.YADAQuery)
	 */
	@Override
	public YADAQueryResult engage(YADARequest yadaReq, YADAQuery yq) throws YADAPluginException
	{
		return null;
	}

}
