/**
 * 
 */
package com.novartis.opensource.yada.format;

import org.json.JSONObject;

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
	 * Sets the {@code harmonyMap} instance variable as a new {@link JSONObject}.
	 * @see com.novartis.opensource.yada.format.Converter#setHarmonyMap(java.lang.Object)
	 */
	@Override
	public void setHarmonyMap(Object harmonyMap)
	{
		this.harmonyMap = harmonyMap;
	}
	
	/**
	 * Base implementation returns {@code false}.
	 * @return {@code true} if this object contains a popluated harmony map
	 */
	protected boolean isHarmonized() 
	{
		if(this.harmonyMap != null && JSONObject.getNames(this.harmonyMap).length > 0)
			return true;
		return false;
	}
}
