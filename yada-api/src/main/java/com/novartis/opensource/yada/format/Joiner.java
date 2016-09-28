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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;

/**
 * Executes on-the-fly joins of result sets from multiple queries.  For the moment, only supports delimited and shallow JSON results. (Isn't that enough?!)
 * @author Dave Varon
 * @since  6.2.0
 *
 */
public class Joiner {
  
  /**
   * Local logger handle
   */
  private static Logger     l     = Logger.getLogger(Joiner.class);
  /**
   * Flag indicating whether to perform an outer join.
   */
  private boolean           outer = false;
  /**
   * The array of {@link YADAQueryResult} objects currently processed by the {@link Response}
   */
  private YADAQueryResult[] yqrs;
  
  /**
   * Required constructor, called in the {@link Response#compose(YADAQueryResult[])} method.
   * @param yqrs the array of {@link YADAQueryResult} objects currently processed by the {@link Response}
   */
  public Joiner(YADAQueryResult[] yqrs) {
    this.setYadaQueryResults(yqrs); 
  }
  
  /**
   * The meaty bit. Uses hsqld to create in memory db tables for the combined rows of converted results in each yqr. 
   * Then uses the join spec to build data structures, mapping columns to tables, tables to columns, and table pairs to columns.
   * Then builds a select join query from the structures, executes it, wraps and returns the results.
   * @return a {@link JSONArray} containing structured results, or a {@link StringBuffer} containing delimited results
   * @throws YADAResponseException if there is a problem with the in-memory database
   */
  public Object join() throws YADAResponseException
  {
    Object result = null;
    try 
    {
      Class.forName("org.hsqldb.jdbcDriver");
    } 
    catch (ClassNotFoundException e1) 
    {
      //TODO exception handling
    }
    try(Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "");)
    {
      StringBuffer sql    = null;
      StringBuffer buffer = new StringBuffer();
      JSONArray    rows   = new JSONArray();
      boolean      isFormatStructured = isFormatStructured();
      // create tables and insert data
      for(YADAQueryResult yqr : getYadaQueryResults())
      {
         // create tables
         sql = new StringBuffer();
         sql.append("CREATE TABLE");
         sql.append(" T"+yqr.hashCode());
         sql.append(" (");
         for(int col=0;col<yqr.getConvertedHeader().size();col++)
         {
           sql.append(yqr.getConvertedHeader().get(col).replaceAll("\"", ""));
           sql.append(" VARCHAR(4000)");
           if(col<yqr.getConvertedHeader().size()-1)
             sql.append(",");
         }
         sql.append(")");
         l.debug(sql.toString());
         try(PreparedStatement create = c.prepareStatement(sql.toString());)
         {
           create.executeUpdate();
         }
         catch(SQLException e)
         {
           String msg = "Unable to create in-memory tables";
           throw new YADAResponseException(msg, e);
         }
         
         StringBuilder header = new StringBuilder();
         StringBuilder params = new StringBuilder();
         String delim = "";
         
         //TODO build these in first iteration of converted header during CREATE construction
         for (String hdr : yqr.getConvertedHeader()) 
         {
             header.append(delim).append(hdr);
             params.append(delim).append("?");
             delim = ",";
         }
         
         // inserts
         
         sql = new StringBuffer();
         sql.append("INSERT INTO T"+yqr.hashCode());
         sql.append(" (");
         sql.append(header.toString().replaceAll("\"",""));
         sql.append(") VALUES (");
         sql.append(params);
         sql.append(")");
         l.debug(sql.toString());
         try(PreparedStatement insert = c.prepareStatement(sql.toString());)
         {
           
           for(int i=0;i<yqr.getConvertedResults().size();i++)
           {
             //TODO xml
             if(isFormatStructured) // json (and someday xml)
             {
               @SuppressWarnings("unchecked")
              List<String> results = (List<String>)yqr.getConvertedResult(i);
               for(String res : results)
               {
                 JSONObject row = new JSONObject(res);
                 for(int k=1;k<=yqr.getConvertedHeader().size();k++)
                 {
                   String key = yqr.getConvertedHeader().get(k-1);
                   insert.setString(k, row.getString(key));
                 }
                 insert.addBatch();
               }
             }
             else // delimited
             {
               @SuppressWarnings("unchecked")
              List<List<String>> results = (List<List<String>>) yqr.getConvertedResult(i);
               for(int j=0;j<results.size();j++)
               {
                 for(int k=1;k<=yqr.getConvertedHeader().size();k++)
                 {
                   insert.setString(k, results.get(j).get(k-1));
                 }
                 insert.addBatch();
               }
             }
           }
           insert.executeBatch();
         }
         catch(SQLException e)
         {
           String msg = "Unable to populate in-memory tables";
           throw new YADAResponseException(msg, e);
         }
      }
      
      // derive/apply the join spec
      // get columns from converted headers
      // TODO create this list in previous YQR iteration
      List<List<String>> localHeaders = new ArrayList<>();
      for(int i=0;i<getYadaQueryResults().length;i++)
      {
        localHeaders.add(getYadaQueryResults()[i].getConvertedHeader());
      }
      
      String        specStr = "";
      if(isOuter())
        specStr = getYADAQueryParamValue(YADARequest.PS_LEFTJOIN);
      else
        specStr = getYADAQueryParamValue(YADARequest.PS_JOIN);
      
      HashSet<String> specSet = null;
      
      if(!specStr.equals("")) 
      {
        if(specStr.equals("true"))
        {
          specSet = new HashSet<>();
          for(int i=0;i<localHeaders.size()-1;i++)
          {
            for(int j=0;j<localHeaders.get(i).size();j++)
            {
              String hdr = localHeaders.get(i).get(j);
              for(int k=i+1;k<localHeaders.size();k++)
              {
                if(localHeaders.get(k).contains(hdr))
                  specSet.add(hdr.replaceAll("\"", ""));
              }
            }
          }
        }
        else
        {
          specSet = new HashSet<>(Arrays.asList(specStr.split(",")));
        }
        l.debug("specStr = "+specStr);
        l.debug("specSet = "+specSet.toString());
      }
      
      
      
      // hash the column indexes by request
      Map<String,Set<String>> S_t2c  = new LinkedHashMap<>(); // the cols mapped to tables
      Map<String,Set<String>> S_c2t  = new HashMap<>(); // the tables mapped to the columns
      for(int i=0;i<localHeaders.size()-1;i++)
      {
        String table  = "T"+getYadaQueryResults()[i].hashCode();
        String nextTable = "T"+getYadaQueryResults()[i+1].hashCode();
        HashSet<String> dupeCheck = new HashSet<>();
        List<String> iHdr = localHeaders.get(i);
        List<String> jHdr = localHeaders.get(i+1);
        for(String hdr : iHdr)
        {
          String _hdr = hdr.replaceAll("\"","");
          dupeCheck.add(_hdr);
        }
        for(String hdr : jHdr)
        {
          String _hdr = hdr.replaceAll("\"","");
          if(dupeCheck.contains(_hdr) 
              && (specSet == null 
                  || (specSet.contains(_hdr) || specSet.contains(_hdr.toLowerCase()) || specSet.contains(_hdr.toUpperCase()))))
          {
            // table to columns
            if(!S_t2c.containsKey(table))
            {
              S_t2c.put(table, new HashSet<String>());
            }
            S_t2c.get(table).add(_hdr);
            
            // column to tables
            if(!S_c2t.containsKey(_hdr))
            {
              S_c2t.put(_hdr, new HashSet<String>());
            }
            S_c2t.get(_hdr).add(table);
            
            // nextTable to columns
            if(!S_t2c.containsKey(nextTable))
            {
              S_t2c.put(nextTable, new HashSet<String>());
            }
            S_t2c.get(nextTable).add(_hdr);
            // column to tables
            S_c2t.get(_hdr).add(nextTable);
          }
        }
      }
      
      // hash the table combo to the col
      HashMap<List<String>,List<String>> S_tt2c = new HashMap<>();
      for(String col : S_c2t.keySet())
      {
        List<String> tables = new ArrayList<>(S_c2t.get(col));
        if(tables.size() == 2)
        {
          if(S_tt2c.get(tables) == null)
            S_tt2c.put(tables, new ArrayList<>(Arrays.asList(col)));
          else
            S_tt2c.get(tables).add(col);
        }
        else
        {
          for(int i=0;i<tables.size()-1;i++)
          {
            List<String> biTabs = new ArrayList<>();
            biTabs.add(tables.get(i));
            biTabs.add(tables.get(++i));
            if(S_tt2c.get(biTabs) == null)
              S_tt2c.put(biTabs, new ArrayList<>(Arrays.asList(col)));
            else
              S_tt2c.get(biTabs).add(col);
          }
        }
      }
      
      /*
       *   i=0, table = t1,
       *   i=1, table = t2,
       *   joinTable = (
       */
      
      // build join
      sql = new StringBuffer();
      sql.append("SELECT");
      String delim=" ";
      StringBuilder gh = new StringBuilder();
      Set<String> globalHeader = getGlobalHeader();
      for(String hdr : globalHeader)
      {
        //TODO consider using COALESCE in here to return empty strings instead of 'null' on LEFT JOINs, maybe make that a parameter as well
        gh.append(delim+hdr.replaceAll("\"",""));
        delim=", ";
      }
      sql.append(gh);
      sql.append(" FROM");
      String[] tables = S_t2c.keySet().toArray(new String[S_t2c.size()]);
//      Arrays.sort(tables);
      for(int i=0;i<tables.length;i++)
      {
        String table = tables[i];
        if(i==0)
        {
          sql.append(" "+table);
        }
        else
        {
          List<String> joinTables = Arrays.asList(tables[i-1],tables[i]);
          List<String> columns    = S_tt2c.get(joinTables);
          if(columns == null)
          {
            joinTables = Arrays.asList(tables[i],tables[i-1]);
            columns    = S_tt2c.get(joinTables);
          }
          if(isOuter())
            sql.append(" LEFT");
          sql.append(" JOIN "+table+" ON");
          for(int j=0;j<columns.size();j++)
          {
            String col = columns.get(j);
            if(j>0)
              sql.append(" AND");
            sql.append(" "+joinTables.get(0)+"."+col+" = "+joinTables.get(1)+"."+col);
          }
        }
      }
      sql.append(" GROUP BY");
      sql.append(gh);
      l.debug(sql.toString());
      String colsep  = getYADAQueryParamValue(YADARequest.PS_DELIMITER);
      String recsep  = getYADAQueryParamValue(YADARequest.PS_ROW_DELIMITER);
      try(PreparedStatement select = c.prepareStatement(sql.toString());ResultSet rs = select.executeQuery();)
      {
        if(isFormatStructured)
        {
          while(rs.next())
          {
            JSONObject j = new JSONObject();
            for(String key : globalHeader)
            {
              j.put(key, rs.getString(key));
            }
            rows.put(j);
          }
          result = rows;
        }
        else
        {
          while(rs.next())
          {
            delim="";
            for(int j=0;j<rs.getMetaData().getColumnCount();j++)
            {
              buffer.append(delim+rs.getString(j+1));
              delim=colsep;
            }
            buffer.append(recsep);
          }
          result = buffer;
        }
      }
      catch(SQLException e)
      {
        String msg = "Unable to format result sets.";
        throw new YADAResponseException(msg, e);
      }
    } 
    catch (SQLException e) 
    {
      String msg = "Unable to connect to in-memory database.";
      throw new YADAResponseException(msg, e);
    }
    return result;
  }

  /**
   * Standard accessor
   * @return the array of {@link YADAQueryResult}s to process
   */
  public YADAQueryResult[] getYadaQueryResults() {
    return this.yqrs;
  }

  /**
   * Standard mutator.
   * @param yqrs the yqrs to set
   */
  public void setYadaQueryResults(YADAQueryResult[] yqrs) {
    this.yqrs = yqrs;
  }

  /**
   * Standard accessor.
   * @return the outer
   */
  private boolean isOuter() 
  {
    return getYadaQueryResults()[0].hasOuterJoin();
  }
  
  /**
   * Extracts the {@link YADARequest} parameter corresponding to {@code key} that was passed to the 0-index {@link YADAQueryResult} stored in {@link #getYadaQueryResults()}
   * @param key the name of the parameter
   * @return the value of the parameter
   */
  private String getYADAQueryParamValue(String key)
  {
    return getYadaQueryResults()[0].getYADAQueryParamValue(key);
  }
  
  /**
   * Returns the set of unique headers across all requests
   * @return the {@link LinkedHashSet} containing all the requested columns across all headers
   */
  private LinkedHashSet<String> getGlobalHeader() 
  {
    LinkedHashSet<String> globalHeader = new LinkedHashSet<>();
    for(YADAQueryResult yqr : getYadaQueryResults())
    {
      // iterate over results and stitch together in StringBuffer
      for(String hdr : yqr.getConvertedHeader())
      {
        globalHeader.add(hdr);
      }
    }
    return globalHeader;
  }

  /**
   * Interrogates the 0-index {@link YADAQueryResult} stored in {@link #getYadaQueryResults()}
   * @return {@code true} for JSON and XML, otherwise {@code false}
   */
  private boolean isFormatStructured() {
    return getYadaQueryResults()[0].isFormatStructured();
  }

  
}
