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
public abstract class AbstractPreprocessor implements Preprocess
{

	/**
	 * Null implementation
	 * @see com.novartis.opensource.yada.plugin.Preprocess#engage(com.novartis.opensource.yada.YADARequest)
	 */
	@Override
	public YADARequest engage(YADARequest yadaReq) throws YADAPluginException
	{
		return null;
	}

	/**
	 * Null implementation
	 * @see com.novartis.opensource.yada.plugin.Preprocess#engage(com.novartis.opensource.yada.YADARequest, com.novartis.opensource.yada.YADAQuery)
	 */
	@Override
	public void engage(YADARequest yadaReq, YADAQuery yq) throws YADAPluginException {/* Nothing to do */}

}
