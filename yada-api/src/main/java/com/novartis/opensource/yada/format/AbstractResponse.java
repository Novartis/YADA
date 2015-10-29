/**
 * 
 */
package com.novartis.opensource.yada.format;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.novartis.opensource.yada.Parser;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADARequestException;

/**
 * The default implementation of the {@link Response} interface, 
 * 
 * @author David Varon
 *
 */
public abstract class AbstractResponse implements Response
{
	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(AbstractResponse.class);
	/**
	 * Constant with value equal to: {@value}
	 */
	public final static String VERSION    = "version";
	/**
	 * Constant with value equal to: {@value}
	 */
	public final static String RESULTSETS = "RESULTSETS";
	/**
	 * Constant with value equal to: {@value}
	 */
	public final static String RESULTSET  = "RESULTSET";
	/**
	 * Constant with value equal to: {@value}
	 */
	public final static String ROWS       = "ROWS";
	/**
	 * Constant with value equal to: {@value}
	 */
	public final static String RECORDS    = "records";
	/**
	 * Constant with value equal to: {@value}
	 */
	public final static String QNAME      = "qname";
	/**
	 * Constant with value equal to: {@value}
	 */
	public final static String TOTAL      = "total";
	/**
	 * Constant with value equal to: {@value}
	 */
	public final static String PAGE       = "page";
	/**
	 * Constant with value equal to: {@value}
	 */
	public final static String FORMAT_PKG = "com.novartis.opensource.yada.format.";
	/**
	 * The array of result sets to be included in the response
	 */
	protected YADAQueryResult[] yadaQueryResults;
	/**
	 * The current result set being processed
	 */
	protected YADAQueryResult   yqr;

	/**
	 * Default constructor 
	 */
	public AbstractResponse()
	{
	}

	/**
	 * Default implementation just returns itself.
	 * @see com.novartis.opensource.yada.format.Response#compose(com.novartis.opensource.yada.YADAQueryResult[])
	 */
	@Override
	public Response compose(YADAQueryResult[] yqrs)
			throws YADAResponseException, YADAConverterException
	{
		return this;
	}

	/**
	 * Default implementation just returns itself.
	 * @see com.novartis.opensource.yada.format.Response#create()
	 */
	@Override
	public Response create() throws YADAResponseException,
			YADAConverterException
	{
		return this;
	}

	/**
	 * Default implementation just returns itself.
	 * @see com.novartis.opensource.yada.format.Response#append(java.lang.String)
	 */
	@Override
	public Response append(String s) throws YADAResponseException,
			YADAConverterException
	{
		return this;
	}
	
	
	/**
	 * Default implementation just returns itself.
	 * @see com.novartis.opensource.yada.format.Response#append(java.lang.Integer)
	 */
	@Override
	public Response append(Integer i) throws YADAResponseException,
			YADAConverterException
	{
		return this;
	}

	/**
	 * Default implementation just returns itself.
	 * @see com.novartis.opensource.yada.format.Response#append(java.lang.Object)
	 */
	@Override
	public Response append(Object o) throws YADAResponseException,
			YADAConverterException
	{
		return this;
	}

	/**
	 * Default implementation just returns itself.
	 * @see com.novartis.opensource.yada.format.Response#toString(boolean)
	 */
	@Override
	public String toString(boolean prettyPrint) throws YADAResponseException
	{
		return this.toString();
	}
	
	/**
	 * This method returns the appropriate converter {@link Converter} by looking first at the {@link YADARequest#PS_CONVERTER}
	 * parmater, if no class is set, or the class provided cannot be instantiated, the default converter is set 
	 * using {@link #getDefaultConverter(YADAQueryResult)}
	 * @param yqResult the result container
	 * @return the appropriate Converter object
	 * @throws YADARequestException when the default converter cannot be instantiated
	 * @throws YADAConverterException when result reformatting fails
	 */
	protected Converter getConverter(YADAQueryResult yqResult) throws YADARequestException, YADAConverterException
	{
		String converterClass = yqResult.getYADAQueryParamValue(YADARequest.PS_CONVERTER);
		
		Converter converter;
		if( converterClass != null && !"".equals(converterClass))
		{
			try
			{
				converter = (Converter) Class.forName(converterClass).newInstance();
			} 
			catch (Exception e)
			{
				l.warn("The specified class ["+converterClass+"] could not be instantiated.  Trying FQCN."); 
				try
				{
					converter = (Converter) Class.forName(FORMAT_PKG+converterClass).newInstance();
				}
				catch(Exception e1)
				{
					l.warn("The specified class ["+converterClass+"] could not be instantiated.  Trying default classes.",e);
					converter = getDefaultConverter(yqResult);
				}
			} 
		}
		else
		{
			converter = getDefaultConverter(yqResult);
		}
		return converter;
	}
	
	/**
	 * Derives the default converter using the format and protocol values.
	 * @param yqResult the result container
	 * @return the default Converter
	 * @throws YADARequestException when the default converter cannot be instantiated
	 * @throws YADAConverterException when result reformatting fails
	 */
	protected Converter getDefaultConverter(YADAQueryResult yqResult) throws YADARequestException, YADAConverterException
	{
		Converter converter = null;
		String format = yqResult.getYADAQueryParamValue(YADARequest.PS_FORMAT);
		if(yqResult.isFormatStructured())
		{
			try
			{
				if(getProtocol().equals(Parser.JDBC))
				{
					converter = (AbstractConverter) Class.forName(FORMAT_PKG+"ResultSetResult"+format.toUpperCase()+"Converter").newInstance();
				}
				else if(getProtocol().equals(Parser.SOAP))
				{
					converter = (AbstractConverter) Class.forName(FORMAT_PKG+"SOAPResult"+format.toUpperCase()+"Converter").newInstance();
				}
				else if(getProtocol().equals(Parser.REST))
				{
					converter = (AbstractConverter) Class.forName(FORMAT_PKG+"RESTResult"+format.toUpperCase()+"Converter").newInstance();
				}
				else if(getProtocol().equals(Parser.FILE))
				{
					converter = (AbstractConverter) Class.forName(FORMAT_PKG+"FileSystemResult"+format.toUpperCase()+"Converter").newInstance();
				}
				else
				{
					String msg = "The converter you are attempting to instantiate requires a protocol or class that is not supported.  This could be a configuration issue.";
					throw new YADAConverterException(msg);
				}
			} 
			catch (InstantiationException e)
			{
				String msg = "Could not instantiate Converter.";
				throw new YADARequestException(msg,e);
			} 
			catch (IllegalAccessException e)
			{
				String msg = "Could not access Converter class.";
				throw new YADARequestException(msg,e);

			} 
			catch (ClassNotFoundException e)
			{
				String msg = "Could not find Converter class";
				throw new YADARequestException(msg,e);
			}
		}
		else // delimited
		{
			converter = new ResultSetResultDelimitedConverter();
		}
		return converter;
	}
	
	/**
	 * The {@link #yadaQueryResults} array serves as a proxy for the queries being handled in the response.
	 * Since each {@link YADAQuery} object maps to a single {@link YADAQueryResult} this works out fine.
	 * Thus the method returns {@code true} if the {@link #yadaQueryResults} array has a {@code length} = 1.
	 * @return {@code true} if the {@link #yadaQueryResults} array has a {@code length} = 1
	 */
	protected boolean hasSingularQuery() {
		if(getYadaQueryResults().length == 1)
			return true;
		return false;
	}
		
	/**
	 * This method interrogates the {@link #yadaQueryResults} array. If {@link #hasSingularQuery()} returns {@code true} 
	 * then the value at index 1 is checked.  If this value is {@code null} or {@link YADAQueryResult#hasSingularResult()}
	 * returns {@code true} then this method returns {@code true} as well.
	 *  
	 * @return {@code true} if the {@link #yadaQueryResults} array contains a null value at it's first index or the result 
	 * object contains only a single result set 
	 */
	protected boolean hasSingularResult() {
		YADAQueryResult[] yqrs = getYadaQueryResults();
		if(hasSingularQuery())
		{  
			if(yqrs[0] == null
		   	||(yqrs[0] != null && yqrs[0].hasSingularResult()))
			{		
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns {@code true} if {@link #hasSingularResult()} returns {@code false} and {@link #getHarmonyMap()} returns {@code null}
	 * @return {@code true} if {@link #hasSingularResult()} returns {@code false} and {@link #getHarmonyMap()} returns {@code null}
	 */
	protected boolean hasMultipleResults() {
		return !hasSingularResult() && getHarmonyMap() == null;
	}
	
//	void setHarmonyMap(JSONObject harmonyMap)
//	{
//		this.harmonyMap = (JSONObject)harmonyMap;
//	}
	
	/**
	 * Extracts a {@link JSONObject} built from the value of {@link YADARequest#PS_HARMONYMAP} in the {@link #yqr}
	 * @return a {@link JSONObject} built from the value of {@link YADARequest#PS_HARMONYMAP} in the {@link #yqr}
	 */
	protected JSONObject getHarmonyMap()
	{
		JSONObject h = null;
		try 
		{ 
			h = new JSONObject(getYADAQueryResult().getYADAQueryParamValue(YADARequest.PS_HARMONYMAP));
		}
		catch(Exception e)
		{
			return null;
		}
		return h;
	}
	
	/**
	 * Standard mutator for variable
	 * @param yqr the result container to process
	 */
	protected void setYADAQueryResult(YADAQueryResult yqr)
	{
		this.yqr = yqr;
	}
	
	/**
	 * Accessor for object, returning the {@link YADAQueryResult} referenced by {@link #yqr} or first index of {@link #yadaQueryResults} if {@link #yqr} is null.
	 * @return the {@link YADAQueryResult} referenced by {@link #yqr} or first index of {@link #yadaQueryResults} if {@link #yqr} is null.
	 */
	protected YADAQueryResult getYADAQueryResult() 
	{
		return this.yqr == null ? getYadaQueryResults()[0] : this.yqr;
	}
	
	/**
	 * Standard mutator for variable
	 * @param yadaQueryResults array of result containers (from the request)
	 */
	protected void setYADAQueryResults(YADAQueryResult[] yadaQueryResults)
	{
		this.yadaQueryResults = yadaQueryResults;
	}
	
	/**
	 * Standard accessor for variable
	 * @return the array of {@link YADAQueryResult}s
	 */
	protected YADAQueryResult[] getYadaQueryResults()
	{
		return this.yadaQueryResults;
	}

	/**
	 * Returns the current protocol value set in {@link #yqr}
	 * @return protocol value
	 * @see YADARequest#getProtocol()
	 * @see YADAQueryResult#getYADAQueryParamValue(String)
	 */
	protected String getProtocol()
	{
		return this.yqr.getYADAQueryParamValue(YADARequest.PS_PROTOCOL);
	}

}
