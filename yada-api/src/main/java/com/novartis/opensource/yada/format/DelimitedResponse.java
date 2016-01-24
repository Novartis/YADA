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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADARequestException;
import com.novartis.opensource.yada.adaptor.JDBCAdaptor;

/**
 * An implementation of {@link Response} for returning query results as delimited files.
 * 
 * @author David Varon
 * @since 0.4.0.0
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
	 * @see com.novartis.opensource.yada.format.AbstractResponse#compose(com.novartis.opensource.yada.YADAQueryResult[])
	 */
	@SuppressWarnings("unchecked")
  @Override
	public Response compose(YADAQueryResult[] yqrs)	throws YADAResponseException, YADAConverterException
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
//      try(Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "");)
//      {
//        StringBuffer sql = null;
//        
//        // create tables and insert data
//        for(YADAQueryResult yqr : getYadaQueryResults())
//        {
//           // create tables
//           sql = new StringBuffer();
//           sql.append("CREATE TABLE");
//           sql.append(" T"+yqr.hashCode());
//           sql.append(" (");
//           for(int col=0;col<yqr.getConvertedHeader().size();col++)
//           {
//             sql.append(yqr.getConvertedHeader().get(col).replaceAll("\"", ""));
//             sql.append(" VARCHAR(4000)");
//             if(col<yqr.getConvertedHeader().size()-1)
//               sql.append(",");
//           }
//           sql.append(")");
//           l.debug(sql.toString());
//           try(PreparedStatement create = c.prepareStatement(sql.toString());)
//           {
//             create.executeUpdate();
//           }
//           catch(SQLException e)
//           {
//             //TODO exception handling
//           }
//           
//           StringBuilder header = new StringBuilder();
//           StringBuilder params = new StringBuilder();
//           String delim = "";
//           for (String hdr : yqr.getConvertedHeader()) 
//           {
//               header.append(delim).append(hdr);
//               params.append(delim).append("?");
//               delim = ",";
//           }
//           
//           // inserts
//           
//           sql = new StringBuffer();
//           sql.append("INSERT INTO T"+yqr.hashCode());
//           sql.append(" (");
//           sql.append(header.toString().replaceAll("\"",""));
//           sql.append(") VALUES (");
//           sql.append(params);
//           sql.append(")");
//           l.debug(sql.toString());
//           try(PreparedStatement insert = c.prepareStatement(sql.toString());)
//           {
//             for(int i=0;i<yqr.getConvertedResults().size();i++)
//             {
//               List<List<String>> results = (List<List<String>>) yqr.getConvertedResult(i);
//               for(int j=0;j<results.size();j++)
//               {
//                 for(int k=1;k<=yqr.getConvertedHeader().size();k++)
//                 {
//                   insert.setString(k, results.get(j).get(k-1));
//                 }
//                 insert.addBatch();
//               }
//             }
//             insert.executeBatch();
//           }
//           catch(SQLException e)
//           {
//             //TODO exception handling
//           }
//        }
//        
//        // derive/apply the join spec
//        List<Integer> spec    = new ArrayList<>();
//        String        specStr = "";
//        if(outer)
//          specStr = this.yqr.getYADAQueryParamValue(YADARequest.PS_LEFTJOIN);
//        else
//          specStr = this.yqr.getYADAQueryParamValue(YADARequest.PS_JOIN);
//        HashSet<String> specSet = !specStr.equals("") ? new HashSet<>(Arrays.asList(specStr.split(","))) : null; 
//        
//        // get columns from converted headers
//        List<List<String>> localHeaders = new ArrayList<>();
//        for(int i=0;i<getYadaQueryResults().length;i++)
//        {
//          localHeaders.add(getYadaQueryResults()[i].getConvertedHeader());
//        }
//        
//        // hash the column indexes by request
//        LinkedHashMap<String,List<String>> S_t2c  = new LinkedHashMap<>(); // the cols mapped to tables
//        HashMap<String,List<String>>       S_c2t  = new HashMap<>(); // the tables mapped to the columns
//        for(int i=0;i<localHeaders.size()-1;i++)
//        {
//          String table  = "T"+getYadaQueryResults()[i].hashCode();
//          String nextTable = "T"+getYadaQueryResults()[i+1].hashCode();
//          HashSet<String> dupeCheck = new HashSet<>();
//          List<String> iHdr = localHeaders.get(i);
//          List<String> jHdr = localHeaders.get(i+1);
//          for(String hdr : iHdr)
//          {
//            String _hdr = hdr.replaceAll("\"","");
//            dupeCheck.add(_hdr);
//          }
//          for(String hdr : jHdr)
//          {
//            String _hdr = hdr.replaceAll("\"","");
//            if(dupeCheck.contains(_hdr) && (specSet == null || specSet.contains(_hdr)))
//            {
//              // table to columns
//              if(!S_t2c.containsKey(table))
//              {
//                S_t2c.put(table, new ArrayList<String>());
//              }
//              S_t2c.get(table).add(_hdr);
//              
//              // column to tables
//              if(!S_c2t.containsKey(_hdr))
//              {
//                S_c2t.put(_hdr, new ArrayList<String>());
//              }
//              S_c2t.get(_hdr).add(table);
//              
//              // is this the last iteration, if so, add the nextTable vals to the hashes
//              if(i+1 == localHeaders.size()-1)
//              {
//                // table to columns
//                if(!S_t2c.containsKey(nextTable))
//                {
//                  S_t2c.put(nextTable, new ArrayList<String>());
//                }
//                S_t2c.get(nextTable).add(_hdr);
//                // column to tables
//                S_c2t.get(_hdr).add(nextTable);
//              }
//            }
//          }
//        }
//        
//        // hash the table combo to the col
//        HashMap<List,List> S_tt2c = new HashMap<>();
//        for(String col : S_c2t.keySet())
//        {
//          List<String> tables = S_c2t.get(col);
//          if(tables.size() == 2)
//          {
//            if(S_tt2c.get(tables) == null)
//              S_tt2c.put(tables, Arrays.asList(col));
//            else
//              S_tt2c.get(tables).add(col);
//          }
//          else
//          {
//            for(int i=0;i<tables.size()-1;i++)
//            {
//              List<String> biTabs = new ArrayList<>();
//              biTabs.add(tables.get(i));
//              biTabs.add(tables.get(++i));
//              if(S_tt2c.get(biTabs) == null)
//                S_tt2c.put(biTabs, Arrays.asList(col));
//              else
//                S_tt2c.get(biTabs).add(col);
//            }
//          }
//        }
//        
//        /*
//         *   i=0, table = t1,
//         *   i=1, table = t2,
//         *   joinTable = (
//         */
//        
//        // build join
//        sql = new StringBuffer();
//        sql.append("SELECT");
//        String delim=" ";
//        StringBuilder gh = new StringBuilder();
//        for(String hdr : globalHeader)
//        {
//          gh.append(delim+hdr.replaceAll("\"",""));
//          delim=", ";
//        }
//        sql.append(gh);
//        sql.append(" FROM");
//        String[] tables = S_t2c.keySet().toArray(new String[S_t2c.size()]);
//        for(int i=0;i<tables.length;i++)
//        {
//          String table = tables[i];
//          if(i==0)
//          {
//            sql.append(" "+table);
//          }
//          else
//          {
//            
//            List<String> joinTables = Arrays.asList(tables[i-1],tables[i]);
//            List<String> columns    = S_tt2c.get(joinTables);
//            if(outer)
//              sql.append(" LEFT");
//            sql.append(" JOIN "+table+" ON");
//            for(int j=0;j<columns.size();j++)
//            {
//              String col = columns.get(j);
//              if(j>0)
//                sql.append(" AND");
//              sql.append(" "+joinTables.get(0)+"."+col+" = "+joinTables.get(1)+"."+col);
//            }
//          }
//        }
//        sql.append(" GROUP BY");
//        sql.append(gh);
//        l.debug(sql.toString());
//        ResultSet rs = null;
//        try(PreparedStatement select = c.prepareStatement(sql.toString());)
//        {
//          rs = select.executeQuery();
//          while(rs.next())
//          {
//            delim="";
//            for(int j=0;j<rs.getMetaData().getColumnCount();j++)
//            {
//              this.buffer.append(delim+rs.getString(j+1));
//              delim=colsep;
//            }
//            this.buffer.append(recsep);
//          }
//          System.out.println(this.buffer);
//        }
//        catch(SQLException e)
//        {
//          //TODO exception handling
//        }
//        finally
//        {
//          try
//          {
//            if(rs != null)
//              rs.close();
//          }
//          catch(SQLException e)
//          {
//            //TODO exception handling
//          }
//        }
//      } 
//      catch (SQLException e) 
//      {
//        //TODO driver/connection exception handling
//      } 
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
	 * @see com.novartis.opensource.yada.format.AbstractResponse#append(java.lang.Object)
	 */
	@Override
	public Response append(Object o) throws YADAResponseException, YADAConverterException {
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
