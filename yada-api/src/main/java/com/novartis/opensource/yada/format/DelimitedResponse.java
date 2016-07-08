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
package com.novartis.opensource.yada.format;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.novartis.opensource.yada.YADAQueryConfigurationException;
import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADARequestException;

/**
 * An implementation of {@link Response} for returning query results as delimited files.
 * 
 * @author David Varon
 * @since 4.0.0
 */
public class DelimitedResponse extends AbstractResponse {

	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(DelimitedResponse.class);
	/**
	 * Instance variable containing the result to be returned by this class's {@link #toString()} method
	 */
	private StringBuffer buffer = new StringBuffer();
	
	
	/**
	 * Delimited results always require the global harmony map 
	 * because columns must be aligned.  If no harmony maps
	 * exist in the request, then results won't be merged anyway.
	 * If even one harmony map is included, results have to be
	 * merged and aligned.
	 */
	@Override
	protected JSONObject getHarmonyMap() 
	{
    return getYADAQueryResult().getGlobalHarmonyMap();
	}
	
	/**
	 * Skeletal override of method, calls {@link #append(Object)}
	 * @throws YADAQueryConfigurationException 
	 * @see com.novartis.opensource.yada.format.AbstractResponse#compose(com.novartis.opensource.yada.YADAQueryResult[])
	 */
	@SuppressWarnings("unchecked")
  @Override
	public Response compose(YADAQueryResult[] yqrs)	throws YADAResponseException, YADAConverterException, YADAQueryConfigurationException
	{
		setYADAQueryResults(yqrs);
		String colsep = YADARequest.DEFAULT_DELIMITER; 
		String recsep = YADARequest.DEFAULT_ROW_DELIMITER;
		for(YADAQueryResult yqr : yqrs)
		{
			setYADAQueryResult(yqr);
	    colsep  = this.yqr.getYADAQueryParamValue(YADARequest.PS_DELIMITER);
	    recsep  = this.yqr.getYADAQueryParamValue(YADARequest.PS_ROW_DELIMITER);
			for(Object result : yqr.getResults())
			{
			  // stores all results in yqr.convertedResults List
				this.append(result); 
			}
		}
		
		
		// process converted headers into unique ordered Set
    Set<String> globalHeader = new LinkedHashSet<>(); 
    for(YADAQueryResult yqr : getYadaQueryResults())
    {
      // iterate over results and stitch together in StringBuffer
      for(String hdr : yqr.getConvertedHeader())
      {
        globalHeader.add(hdr);
      }
    }
    
    // put header line into buffer
    int colCount = globalHeader.size(), g=0;
    for(String hdr : globalHeader)
    {
      this.buffer.append(hdr);
      if(++g < colCount)
        this.buffer.append(colsep);
    }
    this.buffer.append(recsep);
    
    // keys:
    // if 'join' (ij) spec = true, then inner, using matching keys from local converted headers
    // if 'join' (ij) spec is list, then inner, using designated keys from local converted headers
    // if 'leftjoin' (lj) spec = true, then outer, using matching keys
    // if 'leftjoin' (lj) spec is list, then outer, using designated keys
        
    // each yqr is a list of results from one or more executions of a single query
    // each result is a list of indexed values
    
    boolean join  = this.yqr.hasJoin();  
//    boolean outer = this.yqr.hasOuterJoin(); 
    
    if(join)
    {
      Joiner joiner = new Joiner(getYadaQueryResults());
      this.buffer.append((StringBuffer)joiner.join()); 
    } // END JOIN
    else
    { 
      // process converted data and add to buffer
      for(YADAQueryResult yqr : getYadaQueryResults())
      {
        List<String> localHeader      = yqr.getConvertedHeader();
        List<Object> convertedResults = yqr.getConvertedResults();
        for(int i=0;i<convertedResults.size();i++)
        {
          List<List<String>> convertedResult = (List<List<String>>)convertedResults.get(i);
          for(List<String> row : convertedResult)
          {
            int j=0;
            for(String globalHdr : globalHeader)
            {
              String val = "";
              int localHdrIdx = localHeader.indexOf(globalHdr);
              if(localHdrIdx > -1)
              {
                val = row.get(localHdrIdx);
              }
              this.buffer.append(val);
              if(++j<colCount)
              {
                this.buffer.append(colsep);
              }
            }
            this.buffer.append(recsep);
          }
        }
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
	 * Appends a converted string containing the contents of {@code o} to the {@link YADAQueryResult#getConvertedResults()} {@link List}
	 * @throws YADAQueryConfigurationException 
	 * @see com.novartis.opensource.yada.format.AbstractResponse#append(java.lang.Object)
	 */
	@Override
	public Response append(Object o) throws YADAResponseException, YADAConverterException, YADAQueryConfigurationException {
		try
		{
			String colsep  = this.yqr.getYADAQueryParamValue(YADARequest.PS_DELIMITER);
			String recsep  = this.yqr.getYADAQueryParamValue(YADARequest.PS_ROW_DELIMITER);
			Converter converter = getConverter(this.yqr);
			if(getHarmonyMap() != null)
				converter.setHarmonyMap(getHarmonyMap());
			converter.convert(o,colsep,recsep);
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
