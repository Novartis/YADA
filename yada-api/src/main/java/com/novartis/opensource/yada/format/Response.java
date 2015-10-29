package com.novartis.opensource.yada.format;

import com.novartis.opensource.yada.YADAQueryResult;

/**
 * The YADA API for creating formatted results.
 * 
 * @author David Varon
 *
 */
public interface Response {
	
	/**
	 * Use to access the raw results stored in {@code yadaQueryResults}, establish the Response structure
	 * and initiate population of the object via {@link #create()}, and fill the object via one of the {@code append} methods
	 * <p>
	 * The difference between {@link #compose(YADAQueryResult[])} and {@link #create()} is somewhat academic. {@code compose}
	 * enables the caller, typically {@link com.novartis.opensource.yada.Service} to pass the {@link YADAQueryResult}s associated to
	 * a {@link com.novartis.opensource.yada.YADAQuery} to the Response, whereas {@link #create()} does not.  Theoretically, {@link #compose(YADAQueryResult[])}
	 * could do everything {@link #create()} does, but the added granularity makes it a bit easier to code.
	 * </p>
	 * @param yadaQueryResults array containing result objects
	 * @return this Response object
	 * @throws YADAResponseException when response composition fails
	 * @throws YADAConverterException when result reformatting fails
	 * @see com.novartis.opensource.yada.format.JSONResponse
	 */
	public Response compose(YADAQueryResult[] yadaQueryResults) throws YADAResponseException, YADAConverterException;
	/**
	 * A method meant to be called by {@link #compose(YADAQueryResult[])} for filling the new Response object with 
	 * it's foundational attributes, i.e., root nodes, root keys, headers, etc.
	 * @return the Response object
	 * @throws YADAResponseException when response composition fails
	 * @throws YADAConverterException when result reformatting fails
	 */
	public Response create() throws YADAResponseException, YADAConverterException;
	/**
	 * Append the {@link String} {@code s} to the Response 
	 * @param s a {@link String} of content to append to the response
	 * @return the Response object
	 * @throws YADAResponseException when response composition fails
	 * @throws YADAConverterException when result reformatting fails
	 */
	public Response append(String s) throws YADAResponseException, YADAConverterException;
	/**
	 * Append the {@link java.lang.Integer} {@code i} to the Response 
	 * @param i a {@link java.lang.Integer} to append to the response
	 * @return the Response object
	 * @throws YADAResponseException when response composition fails
	 * @throws YADAConverterException when result reformatting fails
	 */
	public Response append(Integer i) throws YADAResponseException, YADAConverterException;
	/**
	 * Append the {@link java.lang.Object} {@code i} to the Response 
	 * @param o an {@link java.lang.Object} to append to the response
	 * @return the Response object
	 * @throws YADAResponseException when response composition fails
	 * @throws YADAConverterException when result reformatting fails
	 */
	public Response append(Object o) throws YADAResponseException, YADAConverterException;
	/**
	 * Get a string version of the Response object.  This should be callable by {@link com.novartis.opensource.yada.Service} to return
	 * the Response content to the client
	 * @param prettyPrint flag indicating whether or not to format a response string, for example, a json string, with indenting
	 * @return the Response as a {@link String}
	 * @throws YADAResponseException when response composition fails
	 */
	public String   toString(boolean prettyPrint) throws YADAResponseException;
	
}
