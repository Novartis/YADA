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
	private Integer result = 0;
	
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
	//					result += Integer.valueOf((Integer)yqr.getCountResult(0));
					for(int i=0;i<lYqr.getCountResults().size();i++)
					{
						this.result += Integer.valueOf((Integer)lYqr.getCountResult(i));
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
