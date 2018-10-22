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
package com.novartis.opensource.yada.format;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;

/**
 * Returns the number of rows processed as an {@code Integer}.  This class is useful for accounting for 
 * updates, inserts, and deletes from data sources. It is also used automatically, for backward 
 * compatibility when the deprecated {@link YADARequest#METHOD_UPDATE} parameter is included in the request
 * @author David Varon
 */
@SuppressWarnings("javadoc")
public class CountResponse extends AbstractResponse
{

	/**
	 * Local logger handle
	 */
	@SuppressWarnings("unused")
	private static Logger l = Logger.getLogger(CountResponse.class);
	/**
	 * Ivar containing the result to be returned by this class's {@link #toString()} method
	 */
	private Integer result = new Integer(0);
	
	/**
	 * Null constructor. 
	 */
	public CountResponse()
	{
	}
	
	/**
	 * Works with only the {@link YADAQueryResult#getCountResults()} list to return the result count 
	 * simply as a number, rather than as a json string.
	 */
	@Override
	public Response compose(YADAQueryResult[] yqrs) throws YADAResponseException, YADAConverterException
	{
		setYADAQueryResults(yqrs);
		create();
		for(YADAQueryResult lYqr : yqrs)
		{
			if(lYqr != null)
			{
				setYADAQueryResult(lYqr);
				if(lYqr.getCountResults() != null && lYqr.getCountResults().size() > 0)
				{
					for(int i=0;i<lYqr.getCountResults().size();i++)
					{ 
					  int res = this.result.intValue();
					  res += ((Integer)lYqr.getCountResult(i)).intValue();
						this.result = Integer.valueOf(res);
					}
				}
			}
		}
		return this;
	}
	
	/**
	 * Returns the value of {@link #result} as a {@link String}
	 */
	@Override
	public String toString() {
		return String.valueOf(this.result);
	}

}
