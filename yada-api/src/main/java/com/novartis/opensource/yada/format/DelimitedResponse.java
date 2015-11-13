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

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADARequestException;

/**
 * An implementation of {@link Response} for returning query results as delimited files.
 * 
 * @author David Varon
 * @since 0.4.0.0
 */
public class DelimitedResponse extends AbstractResponse {

	/**
	 * Local logger handle
	 */
	@SuppressWarnings("unused")
	private static Logger l = Logger.getLogger(DelimitedResponse.class);
	/**
	 * Ivar containing the result to be returned by this class's {@link #toString()} method
	 */
	private StringBuffer buffer = new StringBuffer();
	
	/**
	 * Skeletal override of method, calls {@link #append(Object)}
	 * @see com.novartis.opensource.yada.format.AbstractResponse#compose(com.novartis.opensource.yada.YADAQueryResult[])
	 */
	@Override
	public Response compose(YADAQueryResult[] yqrs)	throws YADAResponseException, YADAConverterException
	{
		setYADAQueryResults(yqrs);
		for(YADAQueryResult lYqr : yqrs)
		{
			setYADAQueryResult(lYqr);
			for(Object result : lYqr.getResults())
			{
				this.append(result);
			}
		}
		return this;
	}

	
	/**
	 * Appends {@code s} to the internal {@link StringBuffer}
	 * @see com.novartis.opensource.yada.format.AbstractResponse#append(java.lang.String)
	 */
	@Override
	public Response append(String s) {
		this.buffer.append(s);
		return this;
	}

	/**
	 * Appends a converted string containing the contents of {@code o} to the internal {@link StringBuffer}
	 * @see com.novartis.opensource.yada.format.AbstractResponse#append(java.lang.Object)
	 */
	@Override
	public Response append(Object o) throws YADAResponseException, YADAConverterException {
		try
		{
			String colsep  = this.yqr.getYADAQueryParamValue(YADARequest.PS_DELIMITER);
			String recsep  = this.yqr.getYADAQueryParamValue(YADARequest.PS_ROW_DELIMITER);
			Converter converter = getConverter(this.yqr);
			if(getHarmonyMap() != null)
				converter.setHarmonyMap(getHarmonyMap());
			StringBuffer rows = (StringBuffer)converter.convert(o,colsep,recsep);
			this.buffer.append(rows);
		} 
		catch (YADARequestException e)
		{
			String msg = "There was problem creating the Converter.";
			throw new YADAResponseException(msg,e);
		}
		
		return this;
	}
	
	/**
	 * Returns the contents of the internal {@link StringBuffer} as a string.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.buffer.toString();
	}

	/**
	 * Returns the contents of the internal {@link StringBuffer} as a string.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(boolean prettyPrint) throws YADAResponseException {
		return this.buffer.toString();
	}

}
