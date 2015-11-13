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