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
 * This is a stand-in class to comply with the dynamic instantiation process of the YADA format API, however it 
 * does not override any methods. 
 * @author David Varon
 *
 */
public class ResultSetResultHTMLConverter extends ResultSetResultDelimitedConverter {

	/**
	 * Default constructor
	 * @throws YADAConverterException when instantiation fails
	 */
	public ResultSetResultHTMLConverter() throws YADAConverterException
	{
		super();
	}
  
  /**
   * Constructor with {@link YADAQueryResult}
   * @param yqr the container for result processing artifacts
   * @throws YADAConverterException 
   */
  public ResultSetResultHTMLConverter(YADAQueryResult yqr) throws YADAConverterException {
    super(yqr);
  }
}
