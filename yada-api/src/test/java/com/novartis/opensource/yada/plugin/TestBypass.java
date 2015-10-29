/**
 * 
 */
package com.novartis.opensource.yada.plugin;

import com.novartis.opensource.yada.YADARequest;

/**
 * @author David Varon
 *
 */
public class TestBypass extends AbstractBypass
{

	/**
	 * Simply returns the string {@code Bypass worked."}
	 * @see com.novartis.opensource.yada.plugin.Bypass#engage(com.novartis.opensource.yada.YADARequest)
	 */
	@Override
	public String engage(YADARequest yadaReq) throws YADAPluginException
	{
		return "Bypass worked.";
	}

}
