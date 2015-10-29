/**
 * 
 */
package com.novartis.opensource.yada.plugin;

import com.novartis.opensource.yada.YADARequest;

/**
 * @author David Varon
 *
 */
public class TestPostprocessor extends AbstractPostprocessor
{

	/**
	 * Simply returns the string {@code It worked.}
	 * @see com.novartis.opensource.yada.plugin.Postprocess#engage(com.novartis.opensource.yada.YADARequest, java.lang.String)
	 */
	@Override
	public String engage(YADARequest yadaReq, String result) throws YADAPluginException
	{
	  // return an arbitrary string
		return "It worked.";
	}
}
