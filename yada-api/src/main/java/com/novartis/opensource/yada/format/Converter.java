/**
 * 
 */
package com.novartis.opensource.yada.format;

import org.json.JSONObject;

/**
 * The YADA API for formatting query results for insertion into {@link Response} objects. 
 * @since 0.4.0.0
 * @author David Varon
 *
 */
public interface Converter {
	/**
	 * A (usually) {@link JSONObject} implementation of the Harmony Map Spec.  As there are numerous JSON java implemtations
	 * this object is stored as a generic {@link Object}
	 */
	Object harmonyMap = null;
	
	/**
	 * Intended to take raw results from a query and wrap or transform them into the proper format, including 
	 * the mapping of fields based on provided {@code harmonyMap} specification
	 * @param result the raw query results
	 * @return the converted result
	 * @throws YADAConverterException when result conversion fails
	 */
	public Object convert(Object result) throws YADAConverterException;
	
	/**
	 * Intended to take raw results from a query and wrap or transform them into the proper delimited format, including 
	 * the mapping of fields based on provided {@code harmonyMap} specification
	 * @param result the raw query result
	 * @param colsep the column delimiter
	 * @param recsep the line delimiter
	 * @return the converted, delimited result
	 * @throws YADAConverterException when result conversion fails
	 */
	public Object convert(Object result, String colsep, String recsep) throws YADAConverterException;
	
	/**
	 * Standard mutator for variable
	 * @param harmonyMap json object describing column header translation
	 */
	public void setHarmonyMap(Object harmonyMap);
}