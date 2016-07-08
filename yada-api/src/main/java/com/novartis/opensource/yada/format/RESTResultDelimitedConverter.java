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
/**
 * 
 */
package com.novartis.opensource.yada.format;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADAResourceException;
import com.novartis.opensource.yada.io.YADAIOException;

/**
 * @author David Varon
 * @since 4.0.0
 */
public class RESTResultDelimitedConverter extends AbstractConverter {

  /**
   * Default constructor
   * @throws YADAResourceException when the path to {@code r.js} or {@code harmony.js} is corrupt
   * @throws YADAIOException when the path to {@code r.js} or {@code harmony.js} can't be read
   * @throws YADAConverterException when the Rhino javascript engine throws an error
   */
  public RESTResultDelimitedConverter() throws YADAResourceException, YADAIOException, YADAConverterException {
    // default constructor
    setHarmonizer(new Harmonizer());
  }
  
  /**
   * Constructor with {@link YADAQueryResult}
   * @param yqr the container for result processing artifacts
   * @throws YADAResourceException when the path to {@code r.js} or {@code harmony.js} is corrupt
   * @throws YADAIOException when the path to {@code r.js} or {@code harmony.js} can't be read
   * @throws YADAConverterException when the Rhino javascript engine throws an error
   */
  public RESTResultDelimitedConverter(YADAQueryResult yqr) throws YADAResourceException, YADAIOException, YADAConverterException {
    this.setYADAQueryResult(yqr);
    setHarmonizer(new Harmonizer());
  }
  
  /**
   * Creates a delimited result object using {@code newColSep} as a column
   * separator and {@code newRecSep} as a row separator.
   * 
   * @see com.novartis.opensource.yada.format.AbstractConverter#convert(java.lang.Object,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public Object convert(Object o, String newColSep, String newRecSep)
      throws YADAConverterException {

    StringBuffer result = new StringBuffer();
    this.colsep = newColSep;
    this.recsep = newRecSep;

    try 
    {
      getDelimitedRows(o);
    } 
    catch (JSONException e) 
    {
      String msg = "Unable to read Harmony Map";
      throw new YADAConverterException(msg, e);
    }
    return result;
  }
  
  /**
   * Parses global harmony map for duplicate values, and removes key/value pairs that
   * are not present in local harmony map.
   * @return the parsed global harmony map
   * @since 6.1.0
   */
  @Override
  public Object getHarmonyMap()
  {
    JSONObject globMap = (JSONObject)this.harmonyMap;
    JSONObject local   = new JSONObject(getYADAQueryResult().getYADAQueryParamValue(YADARequest.PS_HARMONYMAP));
    ArrayList<Object> locVals = new ArrayList<>(); 
    ArrayList<String> omit    = new ArrayList<>();
    for(Object k : local.keySet() )
    {
      locVals.add(local.get((String)k));
    }
    for(Object globalKey : globMap.keySet())
    {
      String gk = (String)globalKey;
      Object gv = globMap.get(gk);
      if(locVals.contains(gv) && !local.has(gk)) // the value is associated to a different key in the local map
      {
        omit.add(gk);
      }
    }
    for(String k : omit)
    {
      globMap.remove(k);
    }
    return globMap;
  }

  /**
   * Converts hierarchical data in a JSON result to a
   * {@link java.lang.StringBuffer} containing tabular data delimited
   * accordingly per request parameters
   * 
   * @param rs
   *          the result set to convert
   * @throws YADAConverterException 
   * @throws SQLException
   *           when {@link ResultSet} or {@link ResultSetMetaData} iteration
   *           fails
   */
  protected void getDelimitedRows(Object result) throws YADAConverterException  
  {
    String     harm       = (String)result;
    List<List<String>> convertedResults = new ArrayList<>();
    Harmonizer harmonizer = getHarmonizer();
    Object     map        = getHarmonyMap().toString();
    Object[]   o          = new Object[] { result, map };
    if (map != null)
    {
      harm = harmonizer.call(HARMONIZE, o);
    }
    o = new Object[] { harm };
    String delimited = harmonizer.call(FLATTEN, o), s = "";
    try(LineNumberReader lnr = new LineNumberReader(new StringReader(delimited)))
    {
      while((s = lnr.readLine()) != null)
      {
        if(lnr.getLineNumber() == 1)
          this.yqr.setConvertedHeader(Arrays.asList(s.split(",")));
        else
          convertedResults.add(Arrays.asList(s.split(",")));
      }
    }
    catch(IOException e)
    {
      String msg = "Rhino javascript result could not be parsed.";
      throw new YADAConverterException(msg, e);
    }
    this.yqr.getConvertedResults().add(convertedResults);
  }
}
