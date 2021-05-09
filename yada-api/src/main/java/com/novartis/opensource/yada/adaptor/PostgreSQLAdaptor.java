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
package com.novartis.opensource.yada.adaptor;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.YADARequest;

/**
 * For connecting to PostgreSQL databases via JDBC.
 * 
 * @author David Varon
 * @since 4.1.0
 */
public class PostgreSQLAdaptor extends JDBCAdaptor {
	
	/**
   * Local logger handle
   */
	private static Logger l = Logger.getLogger(PostgreSQLAdaptor.class);
	
	/**
	 * Default subclass constructor (calls {@code super()}
	 */
	public PostgreSQLAdaptor() {
		super();
		l.debug("Initializing");
	}
	
	/**
	 * Subclass constructor, calls {@code super(yadaReq)}
	 * @param yadaReq YADA request configuration
	 */
	public PostgreSQLAdaptor(YADARequest yadaReq)
	{
		super(yadaReq);
	}
	
	@Override
	protected String getQueryFilters(boolean append, StringBuffer sql, JSONObject filters) throws YADAAdaptorException
  {
	  JSONArray rules = null;
    JSONArray groups = null;
    StringBuffer lSql = sql;
    /*
     * {"groupOp":"OR",
     *  "rules":[{"field":"SAMPLE","op":"cn","data":"hly","type":"text"}],
     *  "groups":[{"groupOp":"AND",
     *               "rules":[{"field":"RPKM","op":"ge","data":"20"},
     *                        {"field":"RPKM","op":"lt","data":"30"}],
     *              "groups":[]},
     *            {"groupOp":"AND",
     *               "rules":[{"field":"RPKM","op":"ge","data":"100"},
     *                        {"field":"RPKM","op":"lt","data":"1000"}],
     *              "groups":[]}]}
     *              
     *              
     * {"groupOp":"AND",
     *    "rules":[],
     *   "groups":[{"groupOp":"AND",
     *                "rules":[{"field":"SAMPLE","op":"eq","data":""},
     *                         {"field":"SAMPLE","op":"eq","data":""}],
     *               "groups":[]},
     *             {"groupOp":"AND",
     *                "rules":[{"field":"SAMPLE","op":"eq","data":""},
     *                         {"field":"SAMPLE","op":"eq","data":""}],
     *               "groups":[]}]}
    */
    if (null == lSql)
    {
      lSql = new StringBuffer();
    }
    try
    {
      String groupOp = filters.getString(FILTERKEY_GROUPOP);
      // init groups and rules
      try 
      {
        rules = filters.has(FILTERKEY_RULES) ? filters.getJSONArray(FILTERKEY_RULES) : null;
        groups = filters.has(FILTERKEY_GROUPS) ? filters.getJSONArray(FILTERKEY_GROUPS) : null;
      }
      catch (JSONException e)
      {
        String msg = "Could not extract filter rules and groups.";
        throw new YADAAdaptorException(msg,e);
      }
      if (null != rules && rules.length() > 0)
      {
        
        for (int i=0;i<rules.length();i++)
        {
          JSONObject obj = rules.getJSONObject(i);
          String field   = obj.getString(FILTERKEY_FIELD);
          String op      = obj.getString(FILTERKEY_OP);
          
          // set type
          String type    = TYPE_TEXT;
          try
          {
            type  = obj.has(FILTERKEY_TYPE) ? obj.getString(FILTERKEY_TYPE) : type;
          }
          catch (JSONException e)
          {
            String msg = "Could not extract filter type.";
            throw new YADAAdaptorException(msg,e);
          }
          
          // set value
          String value = obj.getString(FILTERKEY_DATA);
          if (!TYPE_EXACTTEXT.equals(type))
          {
            value = value.toLowerCase();
          }
          
          // prepend group op if not 1st rule
          if (i > 0)
          {
            lSql.append(SPACE + groupOp + SPACE);
            
          }
          // nulls, not nulls
          if(op.matches(FILTER_NULL + RX_ALT + FILTER_NOTNULL))
          {
            lSql.append(SQL_CORE_ALIAS+DOT + field + SPACE);
            lSql.append(SQL_IS);
            if (FILTER_NOTNULL.equals(op))  {  lSql.append(SQL_NOT); }
            lSql.append(SQL_NULL);
          }
          // numbers
          else
          {
            // strings:  eq, ne, cn, nc, bw, bn, ew, en, in, ni
            // numbers:  eq, ne, lt, le, gt, ge, in, ni
            if (TYPE_NUMBER.equals(type))
            {
              lSql.append(SQL_CORE_ALIAS+DOT + field + SPACE);
              if(FILTER_NOTEQUAL.equals(op))  
              {  
                lSql.append(LESSTHAN+GREATERTHAN);  
              }
              else if(op.matches(FILTER_LESSTHAN + RX_ALT + FILTER_LESSEQUAL))         
              {  
                lSql.append(LESSTHAN);   
              }
              else if(op.matches(FILTER_GREATERTHAN + RX_ALT + FILTER_GREATEREQUAL))   
              {  
                lSql.append(GREATERTHAN);   
              }
              if(op.matches(FILTER_EQUAL + RX_ALT + FILTER_LESSEQUAL + RX_ALT + FILTER_GREATEREQUAL))     
              {  
                lSql.append(EQUAL);   
              }
              lSql.append(SPACE);
              if(FILTER_NOTIN.matches(op))    
              {  
                lSql.append(SQL_NOT);
              }
              if(op.matches(FILTER_IN + RX_ALT + FILTER_NOTIN))        
              {  
                lSql.append(SQL_IN + OPEN_PAREN + value + CLOSE_PAREN + SPACE); 
              }
              else 
              {  
                lSql.append(value);  
              }
            }
            // varchars
            else if (TYPE_TEXT.equals(type) || (TYPE_EXACTTEXT.equals(type)))
            {
              // case sensitive
              if (TYPE_EXACTTEXT.equals(type))
              {
                lSql.append(SQL_CORE_ALIAS+DOT + field + SPACE);
              }
              // case insensitive
              else
              {
                lSql.append("LOWER" + OPEN_PAREN + SQL_CORE_ALIAS+DOT + field + CLOSE_PAREN + SPACE);
              }
              if(FILTER_EQUAL.equals(op))          {  lSql.append(SPACE + EQUAL + SPACE); }
              else if(FILTER_NOTEQUAL.equals(op))  {  lSql.append(SPACE + LESSTHAN+GREATERTHAN + SPACE);}
              
              // not contains, not begins with, not ends with
              if(op.matches(FILTER_NOTCONTAINS + RX_ALT
                        +FILTER_NOTBEGINSWITH + RX_ALT
                        +FILTER_NOTENDSWITH + RX_ALT
                        +FILTER_NOTIN))        {  lSql.append(SQL_NOT);}
              if (op.matches(FILTER_CONTAINS + RX_ALT
                         +FILTER_BEGINSWITH + RX_ALT
                         +FILTER_ENDSWITH))    {  lSql.append(SQL_LIKE); }
              
              // equals, not equals, contains, 
              if (op.matches(FILTER_EQUAL + RX_ALT
                         +FILTER_NOTEQUAL + RX_ALT
                         +FILTER_CONTAINS + RX_ALT
                         +FILTER_NOTCONTAINS + RX_ALT
                         +FILTER_BEGINSWITH + RX_ALT
                         +FILTER_NOTBEGINSWITH + RX_ALT
                         +FILTER_ENDSWITH + RX_ALT
                         +FILTER_NOTENDSWITH)) {  lSql.append(APOS);     }
              
              // contains, ends with, not ends with
              if (op.matches(FILTER_CONTAINS + RX_ALT
                         +FILTER_ENDSWITH + RX_ALT
                         +FILTER_NOTENDSWITH)) {  lSql.append(PERCENT);   }
              if (op.matches(FILTER_EQUAL + RX_ALT
                         +FILTER_NOTEQUAL + RX_ALT
                         +FILTER_CONTAINS + RX_ALT
                         +FILTER_NOTCONTAINS + RX_ALT
                         +FILTER_BEGINSWITH + RX_ALT
                         +FILTER_NOTBEGINSWITH + RX_ALT
                         +FILTER_ENDSWITH + RX_ALT
                         +FILTER_NOTENDSWITH)) { lSql.append(value);  } 
              // in, ni
              else 
              {
                String[] split = value.split(COMMA);
                if(split.length > 1000)
                {
/*                  SELECT * FROM (
                      SELECT yadacore.*
                      FROM  (
                    select * from normal.v_phage_cluster_hit
                           ) yadacore
                    WHERE 
                        (
                    (yadacore."hit" IN (
                    
                    SELECT * FROM (
                    
                      SELECT yadacore.*
                      FROM  (
                        select * from normal.v_phage_cluster_hit
                            ) yadacore
                      JOIN (VALUES (),(),()...) vals(v) on yadacore. 
                    
                    
*/                    
                }
                else
                {
                  lSql.append(SQL_IN + OPEN_PAREN);
                  for (int j=0;j<split.length;j++)
                  {
                    lSql.append(APOS+split[j]+APOS);
                    if (j<split.length-1)
                    {
                      lSql.append(COMMA);
                    }
                  }
                  lSql.append(CLOSE_PAREN);
                }
              }
              // contains, begins with, not begins with
              if (op.matches(FILTER_CONTAINS + RX_ALT
                         +FILTER_BEGINSWITH + RX_ALT
                         +FILTER_NOTBEGINSWITH)) {  lSql.append(PERCENT); }
              if (op.matches(FILTER_EQUAL + RX_ALT
                           +FILTER_NOTEQUAL + RX_ALT
                           +FILTER_CONTAINS + RX_ALT
                           +FILTER_NOTCONTAINS + RX_ALT
                           +FILTER_BEGINSWITH + RX_ALT
                           +FILTER_NOTBEGINSWITH + RX_ALT
                           +FILTER_ENDSWITH + RX_ALT
                           +FILTER_NOTENDSWITH))   {  lSql.append(APOS); }
            } // end "text"
          } // end "non null ops"
        } // end rules iteration
      } // end is rules null
      if (null != groups && groups.length() > 0)
      {
        for(int i=0;i<groups.length();i++)
        {
          if (i > 0 || (rules != null && rules.length() > 0))
          {
            lSql.append(NEWLINE + SPACE + groupOp + NEWLINE); 
          }
          JSONObject filter = groups.getJSONObject(i);
          lSql.append(OPEN_PAREN);
          // recursive call to getQueryFilters to process nested groups
          this.getQueryFilters(false,lSql,filter);
          lSql.append(CLOSE_PAREN);
        }
      }
    } // end try
    catch(JSONException e)
    {
      String msg = "Filter to SQL translation failed";
      throw new YADAAdaptorException(msg,e);
    }
    return lSql.toString();
  }
}
