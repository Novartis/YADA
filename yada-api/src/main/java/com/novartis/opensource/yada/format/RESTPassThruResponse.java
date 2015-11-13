/**
 * Copyright 2015 Novartis Institutes for BioMedical Research Inc.
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
package com.novartis.opensource.yada.format;

import com.novartis.opensource.yada.YADAQueryResult;

/**
 * As the name suggests, this {@link Response} implementation should deliver the result
 * of the REST request unaffected in it's raw form. 
 * @author David Varon
 *
 */
public class RESTPassThruResponse extends AbstractResponse
{

	/**
	 * Ivar containing the result to be returned by this class's {@link #toString()} method
	 */
	private String response;
	/**
	 * 
	 */
	//TODO enable this as the default for REST requests by enabling configurable properties
	public RESTPassThruResponse()
	{
		this.response = "";
	}
	
	/**
	 * Sets the internal {@link #response} string to the REST result.
	 * @see com.novartis.opensource.yada.format.AbstractResponse#compose(com.novartis.opensource.yada.YADAQueryResult[])
	 */
	@Override
	public Response compose(YADAQueryResult[] yqrs) 
	{
		setYADAQueryResults(yqrs);
		for(YADAQueryResult lYqr : yqrs)
		{
			setYADAQueryResult(lYqr);
			// iterate over results
			// at this point 
			if(lYqr != null)
			{
				if(lYqr.getResults() != null && lYqr.getResults().size() > 0)
				{
					for(Object result : lYqr.getResults())
					{
						this.response += (String) result; 
					}
				}
			}
		}
		return this;
	}
	
	/**
	 * Returns the internal {@link #response}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() 
	{
		return this.response;
	}
}
