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

import com.novartis.opensource.yada.YADAQueryResult;

/**
 * Base implemetation of {@link Converter} interface.
 * @author David Varon
 *
 */
public abstract class AbstractConverter implements Converter {
	
	/**
	 * Constant equal to: {@value}
	 */
	public static final String NULL = "null";
	/**
	 * Constant equal to: {@value} (empty string)
	 */
	public static final String NULL_REPLACEMENT = "";
	/**
	 * A json object compliant with the Harmany Map specifcation
	 */
	protected Object harmonyMap; 
	/**
	 * The container for result management
	 */
	protected YADAQueryResult yqr;
	/**
	 * Base implemetation returns {@code null}.
	 * @see com.novartis.opensource.yada.format.Converter#convert(java.lang.Object)
	 */
	@Override
	public Object convert(Object result) throws YADAConverterException {
		return null;
	}
	
	/**
	 * Base implemetation returns {@code null}.
	 * @see com.novartis.opensource.yada.format.Converter#convert(java.lang.Object, java.lang.String, java.lang.String)
	 */
	@Override
	public Object convert(Object o, String colsep, String recsep) throws YADAConverterException {
		return null;
	}
	
	/**
	 * Sets the {@code harmonyMap} instance variable as a new {@link JSONArray}.
	 * @see com.novartis.opensource.yada.format.Converter#setHarmonyMap(java.lang.Object)
	 */
	@Override
	public void setHarmonyMap(Object harmonyMap)
	{
		this.harmonyMap = harmonyMap;
	}
	
	@Override
	public Object getHarmonyMap()
	{
	  return this.harmonyMap;
	}
	

	@Override
  public void setYADAQueryResult(YADAQueryResult yqr)
	{
	  this.yqr = yqr;
	}
	
	@Override
	public YADAQueryResult getYADAQueryResult()
	{
	  return this.yqr;
	}
	/**
	 * Base implementation returns {@code false}.
	 * @return {@code true} if this object contains a popluated harmony map
	 */
	protected boolean isHarmonized() 
	{
		if(null != this.harmonyMap 
		    && null != JSONObject.getNames(this.harmonyMap)
		    && JSONObject.getNames(this.harmonyMap).length > 0)
			return true;
		return false;
	}
}
