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
package com.novartis.opensource.yada;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.adaptor.YADAAdaptorException;
import com.novartis.opensource.yada.adaptor.YADAAdaptorExecutionException;
import com.novartis.opensource.yada.format.Converter;
import com.novartis.opensource.yada.format.DelimitedResponse;
import com.novartis.opensource.yada.format.Response;
import com.novartis.opensource.yada.format.YADAConverterException;
import com.novartis.opensource.yada.format.YADAResponseException;
import com.novartis.opensource.yada.io.YADAIOException;
import com.novartis.opensource.yada.plugin.Bypass;
import com.novartis.opensource.yada.plugin.Postprocess;
import com.novartis.opensource.yada.plugin.Preprocess;
import com.novartis.opensource.yada.plugin.YADAPluginException;
import com.novartis.opensource.yada.util.FileUtils;
import com.novartis.opensource.yada.util.QueryUtils;
import com.novartis.opensource.yada.util.YADAUtils;

/**
 * Utility class handling process of execution of stored queries, and formatting of results via http requests.
 * 
 * @author David Varon
 *
 */
public class Service {
	
	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(Service.class);
	/**
	 * Constant equal to: {@value}
	 */
	private final static String FORMAT_PKG = "com.novartis.opensource.yada.format.";
	/**
	 * Request configuration
	 */
	private YADARequest yadaReq      = new YADARequest();
	/**
	 * Request process manager
	 */
	private QueryManager      qMgr           = null;
	/**
	 * Container for result objects
	 */
	private YADAQueryResult[] queryResults;
	/**
	 * Utility object
	 */
	private QueryUtils qutils = new QueryUtils();
	/**
	 * Ivar to store ref query currently executing query.  Used primarily to pass info to {@link #error(String, Exception)}
	 */
	private YADAQuery currentQuery = null;
	/**
	 * Default constructor.  Usually unused.
	 */
	public Service() {}
	
	/**
	 * Commonly used "service params" constructor.
	 * @param yadaReq YADA request configuration
	 */
	public Service(YADARequest yadaReq) {
		setYADARequest(yadaReq);
	}
	
	/**
	 * Called by {@code yada.jsp} when using path-style parameters, this method parses the url path into a map,
	 * stores the request, and then calls {@link #handleRequest(String, Map)}
	 * @param request the servlet request object handed off from the servlet container
	 * @param parameterString the resource path from the url
	 */
	public void handleRequest(HttpServletRequest request, String parameterString)
	{
		Map<String, String[]> map = new LinkedHashMap<>();
		String[] pathElements = parameterString.split("/");
		/*String[] split = parameterString.split("&"); 
		for (String pair : split)
		{
			String[] subsplit = pair.split("=");
			map.put(subsplit[0], new String[] {subsplit[1]});
		}
		*/
		for(int i=0;i<pathElements.length;i=i+2)
		{
			map.put(pathElements[i], new String[] {pathElements[i+1]});
		}
		getYADARequest().setRequest(request);
		handleRequest(request.getHeader("referer"), map);
	}
	
	/**
	 * Stores the {@code request} in the {@link YADARequest} object and calls {@link #handleRequest(String, Map)}.
	 * @since 4.0.0
	 * @param request the servlet request created by the servlet container 
	 */
	@SuppressWarnings("unchecked")
  public void handleRequest(HttpServletRequest request)
	{
		l.debug("Request query string is ["+request.getQueryString()+"]");
		getYADARequest().setRequest(request);
		handleRequest(request.getHeader("referer"), request.getParameterMap());
	}
	

	/**
	 * The meaty method for parsing the request parameters into {@link YADARequest}.
	 * @param referer the url of the referring page
	 * @param paraMap the parameter map provided by the servlet request
	 */
	@SuppressWarnings("deprecation")
	public void handleRequest(String referer, Map<String,String[]> paraMap) 
	{	
	  try
	  {
  		if(paraMap.get(YADARequest.PL_ARGS) != null)
  		{
  		  setDeprecatedPlugin(paraMap, YADARequest.PL_ARGS);
  		}
  		if(paraMap.get(YADARequest.PS_ARGS) != null)
  		{
  		  setDeprecatedPlugin(paraMap, YADARequest.PS_ARGS);
  		}
  		if (paraMap.get(YADARequest.PL_COLHEAD) != null)
  		{
  			getYADARequest().setColhead(paraMap.get(YADARequest.PL_COLHEAD));
  		}
  		if (paraMap.get(YADARequest.PL_COMMITQUERY) != null)
      {
        getYADARequest().setCommitQuery(paraMap.get(YADARequest.PL_COMMITQUERY));
      }
      if (paraMap.get(YADARequest.PS_COMMITQUERY) != null)
      {
        getYADARequest().setCommitQuery(paraMap.get(YADARequest.PS_COMMITQUERY));
      }
      if (paraMap.get(YADARequest.PS_COMPACT) != null)
      {
        getYADARequest().setCompact(paraMap.get(YADARequest.PS_COMPACT));
      }
  		if (paraMap.get(YADARequest.PL_COMPACT) != null)
  		{
  			getYADARequest().setCompact(paraMap.get(YADARequest.PL_COMPACT));
  		}
  		if (paraMap.get(YADARequest.PL_CONVERTER) != null)
      {
        getYADARequest().setConverter(paraMap.get(YADARequest.PL_CONVERTER));
      }
      if (paraMap.get(YADARequest.PS_CONVERTER)!= null)
      {
        getYADARequest().setConverter(paraMap.get(YADARequest.PS_CONVERTER));
      }
  		if (paraMap.get(YADARequest.PL_COUNT) != null)
  		{
  			getYADARequest().setCount(paraMap.get(YADARequest.PL_COUNT));
  		}
  		if (paraMap.get(YADARequest.PS_COUNT) != null)
  		{
  			getYADARequest().setCount(paraMap.get(YADARequest.PS_COUNT));
  		}
  		if (paraMap.get(YADARequest.PL_COOKIES) != null)
      {
        getYADARequest().setCookies(paraMap.get(YADARequest.PL_COOKIES));
      }
      if (paraMap.get(YADARequest.PS_COOKIES) != null)
      {
        getYADARequest().setCookies(paraMap.get(YADARequest.PS_COOKIES));
      }
  		if (paraMap.get(YADARequest.PL_COUNTONLY) != null)
  		{
  			getYADARequest().setCountOnly(paraMap.get(YADARequest.PL_COUNTONLY));
  		}
  		if (paraMap.get(YADARequest.PS_COUNTONLY) != null)
  		{
  			getYADARequest().setCountOnly(paraMap.get(YADARequest.PS_COUNTONLY));
  		}
  		if (paraMap.get(YADARequest.PL_DELIMITER) != null)
  		{
  			getYADARequest().setDelimiter(paraMap.get(YADARequest.PL_DELIMITER));
  		}
  		if (paraMap.get(YADARequest.PS_DELIMITER) != null)
  		{
  			getYADARequest().setDelimiter(paraMap.get(YADARequest.PS_DELIMITER));
  		}
  		if (paraMap.get(YADARequest.PL_EXPORT) != null)
  		{
  			getYADARequest().setExport(paraMap.get(YADARequest.PL_EXPORT));
  		}
  		if (paraMap.get(YADARequest.PS_EXPORT) != null)
  		{
  			getYADARequest().setExport(paraMap.get(YADARequest.PS_EXPORT));
  		}
  		if (paraMap.get(YADARequest.PL_EXPORTLIMIT) != null)
  		{
  			getYADARequest().setExportLimit(paraMap.get(YADARequest.PL_EXPORTLIMIT));
  		}
  		if (paraMap.get(YADARequest.PS_EXPORTLIMIT) != null)
  		{
  			getYADARequest().setExportLimit(paraMap.get(YADARequest.PS_EXPORTLIMIT));
  		}
  		if (paraMap.get(YADARequest.PL_FILTERS) != null)
  		{
  			getYADARequest().setFilters(paraMap.get(YADARequest.PL_FILTERS));
  		}
  		if (paraMap.get(YADARequest.PS_FILTERS) != null)
  		{
  			getYADARequest().setFilters(paraMap.get(YADARequest.PS_FILTERS));
  		}
  		if (paraMap.get(YADARequest.PL_FORMAT) != null && !paraMap.get(YADARequest.PL_FORMAT).equals(YADARequest.FORMAT_JSON))
  		{
  			getYADARequest().setFormat(paraMap.get(YADARequest.PL_FORMAT));
  		}
  		if (paraMap.get(YADARequest.PS_FORMAT) != null && !paraMap.get(YADARequest.PS_FORMAT).equals(YADARequest.FORMAT_JSON))
  		{
  			getYADARequest().setFormat(paraMap.get(YADARequest.PS_FORMAT));
  		}
  		if (paraMap.get(YADARequest.PL_HARMONYMAP) != null)
  		{
  			getYADARequest().setHarmonyMap(paraMap.get(YADARequest.PL_HARMONYMAP));
  		}
  		if (paraMap.get(YADARequest.PS_HARMONYMAP) != null)
  		{
  			getYADARequest().setHarmonyMap(paraMap.get(YADARequest.PS_HARMONYMAP));
  		}
  		if (paraMap.get(YADARequest.PL_HTTPHEADERS) != null)
  		{
  			getYADARequest().setHTTPHeaders(paraMap.get(YADARequest.PL_HTTPHEADERS));
  		}
  		if (paraMap.get(YADARequest.PS_HTTPHEADERS) != null)
  		{
  			getYADARequest().setHTTPHeaders(paraMap.get(YADARequest.PS_HTTPHEADERS));
  		}
  		if (paraMap.get(YADARequest.PL_JSONPARAMS) != null)
  		{
  			getYADARequest().setJsonParams(paraMap.get(YADARequest.PL_JSONPARAMS));
  		}
  		if (paraMap.get(YADARequest.PS_JSONPARAMS) != null)
  		{
  			getYADARequest().setJsonParams(paraMap.get(YADARequest.PS_JSONPARAMS));
  		}
  		
  		if (paraMap.get(YADARequest.PL_JOIN) != null)
      {
        getYADARequest().setJoin(paraMap.get(YADARequest.PL_JOIN));
      }
  		if (paraMap.get(YADARequest.PS_JOIN) != null)
      {
        getYADARequest().setJoin(paraMap.get(YADARequest.PS_JOIN));
      }
  		if (paraMap.get(YADARequest.PL_LEFTJOIN) != null)
      {
        getYADARequest().setLeftJoin(paraMap.get(YADARequest.PL_LEFTJOIN));
      }
      if (paraMap.get(YADARequest.PS_LEFTJOIN) != null)
      {
        getYADARequest().setLeftJoin(paraMap.get(YADARequest.PS_LEFTJOIN));
      }
      
  		if (paraMap.get(YADARequest.PL_LABELS) != null)
  		{
  			getYADARequest().setLabels(paraMap.get(YADARequest.PL_LABELS));
  		}
  		if (paraMap.get(YADARequest.PL_MAIL) != null)
  		{
  			getYADARequest().setMail(paraMap.get(YADARequest.PL_MAIL));
  		}
  		if (paraMap.get(YADARequest.PL_METHOD) != null && !paraMap.get(YADARequest.PL_METHOD).equals(YADARequest.METHOD_GET))
  		{
  			getYADARequest().setMethod(paraMap.get(YADARequest.PL_METHOD));
  		}
  		if (paraMap.get(YADARequest.PS_METHOD) != null && !paraMap.get(YADARequest.PS_METHOD).equals(YADARequest.METHOD_GET))
  		{
  			getYADARequest().setMethod(paraMap.get(YADARequest.PS_METHOD));
  		}
  		if (paraMap.get(YADARequest.PL_OAUTH) != null)
  		{
  		  getYADARequest().setOAuth(paraMap.get(YADARequest.PL_OAUTH));
  		}
  		if (paraMap.get(YADARequest.PS_OAUTH) != null)
  		{
  			getYADARequest().setOAuth(paraMap.get(YADARequest.PS_OAUTH));
  		}
  		if (paraMap.get(YADARequest.PL_BYPASSARGS) != null)
  		{
  		  setDeprecatedPlugin(paraMap, YADARequest.PL_BYPASSARGS);
  		}
  		if (paraMap.get(YADARequest.PS_BYPASSARGS) != null)
  		{
  		  setDeprecatedPlugin(paraMap, YADARequest.PS_BYPASSARGS);
  		}
  		if (paraMap.get(YADARequest.PL_PAGE) != null)
  		{
  			getYADARequest().setPage(paraMap.get(YADARequest.PL_PAGE));
  		}
  		if (paraMap.get(YADARequest.PS_PAGE) != null)
  		{
  			getYADARequest().setPage(paraMap.get(YADARequest.PS_PAGE));
  		}
  		if (paraMap.get(YADARequest.PL_PAGESIZE) != null)
  		{
  			getYADARequest().setPageSize(paraMap.get(YADARequest.PL_PAGESIZE));
  		}
  		if (paraMap.get(YADARequest.PS_PAGESIZE) != null)
  		{
  			getYADARequest().setPageSize(paraMap.get(YADARequest.PS_PAGESIZE));
  		}
  		if (paraMap.get(YADARequest.PL_PAGESTART) != null)
  		{
  			getYADARequest().setPageStart(paraMap.get(YADARequest.PL_PAGESTART));
  		}
  		if (paraMap.get(YADARequest.PS_PAGESTART) != null)
  		{
  			getYADARequest().setPageStart(paraMap.get(YADARequest.PS_PAGESTART));
  		}
  		if (paraMap.get(YADARequest.PL_PARAMS) != null)
  		{
  			getYADARequest().setParams(paraMap.get(YADARequest.PL_PARAMS));
  		}
  		if (paraMap.get(YADARequest.PS_PARAMS) != null)
  		{
  			getYADARequest().setParams(paraMap.get(YADARequest.PS_PARAMS));
  		}
  		if (paraMap.get(YADARequest.PL_PATH) != null)
  		{
  			getYADARequest().setSortKey(paraMap.get(YADARequest.PL_PATH));
  		}
  		if (paraMap.get(YADARequest.PL_PARALLEL) != null)
  		{
  			getYADARequest().setParallel(paraMap.get(YADARequest.PL_PARALLEL));
  		}
  		if (paraMap.get(YADARequest.PL_PLUGINTYPE) != null && !paraMap.get(YADARequest.PL_PLUGINTYPE).equals(YADARequest.PREPROCESS))
  		{
  			getYADARequest().setPluginType(paraMap.get(YADARequest.PL_PLUGINTYPE));
  		}
  		if (paraMap.get(YADARequest.PS_PLUGINTYPE) != null && !paraMap.get(YADARequest.PL_PLUGINTYPE).equals(YADARequest.PREPROCESS))
  		{
  			getYADARequest().setPluginType(paraMap.get(YADARequest.PS_PLUGINTYPE));
  		}
  		if (paraMap.get(YADARequest.PL_POSTARGS) != null)
  		{
  		  setDeprecatedPlugin(paraMap, YADARequest.PL_POSTARGS);
  		}
  		if (paraMap.get(YADARequest.PS_POSTARGS) != null)
  		{
  		  setDeprecatedPlugin(paraMap, YADARequest.PS_POSTARGS);
  		}
  		if (paraMap.get(YADARequest.PL_PREARGS) != null)
  		{
  		  setDeprecatedPlugin(paraMap, YADARequest.PL_PREARGS);
  		}
  		if (paraMap.get(YADARequest.PS_PREARGS) != null)
  		{
  		  setDeprecatedPlugin(paraMap, YADARequest.PS_PREARGS);
  		}
  		
  		// the next two conditionals must come after pre, post, and bypass arg handling
  		if (paraMap.get(YADARequest.PL_PLUGIN) != null)
      {
        getYADARequest().setPlugin(paraMap.get(YADARequest.PL_PLUGIN));
      }
      if (paraMap.get(YADARequest.PS_PLUGIN) != null)
      {
        getYADARequest().setPlugin(paraMap.get(YADARequest.PS_PLUGIN));
      }
      
      
  		if (paraMap.get(YADARequest.PL_PRETTY) != null)
  		{
  			getYADARequest().setPretty(paraMap.get(YADARequest.PL_PRETTY));
  		}
  		if (paraMap.get(YADARequest.PS_PRETTY) != null)
  		{
  			getYADARequest().setPretty(paraMap.get(YADARequest.PS_PRETTY));
  		}
  		if (paraMap.get(YADARequest.PL_PROXY) != null)
  		{
  			getYADARequest().setProxy(paraMap.get(YADARequest.PL_PROXY));
  		}
  		if (paraMap.get(YADARequest.PS_PROXY) != null)
  		{
  			getYADARequest().setProxy(paraMap.get(YADARequest.PS_PROXY));
  		}
  		if (paraMap.get(YADARequest.PL_QNAME) != null && !paraMap.get(YADARequest.PL_QNAME).equals(YADARequest.DEFAULT_QNAME))
  		{
  			getYADARequest().setQname(paraMap.get(YADARequest.PL_QNAME));
  		}
  		if (paraMap.get(YADARequest.PS_QNAME) != null && !paraMap.get(YADARequest.PS_QNAME).equals(YADARequest.DEFAULT_QNAME))
  		{
  			getYADARequest().setQname(paraMap.get(YADARequest.PS_QNAME));
  		}
  		if (paraMap.get(YADARequest.PL_ROW_DELIMITER) != null)
  		{
  			getYADARequest().setRowDelimiter(paraMap.get(YADARequest.PL_ROW_DELIMITER));
  		}
  		if (paraMap.get(YADARequest.PS_ROW_DELIMITER) != null)
  		{
  			getYADARequest().setRowDelimiter(paraMap.get(YADARequest.PS_ROW_DELIMITER));
  		}
  		if (paraMap.get(YADARequest.PL_RESPONSE) != null)
  		{
  			getYADARequest().setResponse(paraMap.get(YADARequest.PL_RESPONSE));
  		}
  		if (paraMap.get(YADARequest.PS_RESPONSE) != null)
  		{
  			getYADARequest().setResponse(paraMap.get(YADARequest.PS_RESPONSE));
  		}
  		if (paraMap.get(YADARequest.PL_SORTKEY) != null)
  		{
  			getYADARequest().setSortKey(paraMap.get(YADARequest.PL_SORTKEY));
  		}
  		if (paraMap.get(YADARequest.PS_SORTKEY) != null)
  		{
  			getYADARequest().setSortKey(paraMap.get(YADARequest.PS_SORTKEY));
  		}
  		if (paraMap.get(YADARequest.PL_SORTORDER) != null && !paraMap.get(YADARequest.PL_SORTORDER).equals(YADARequest.SORT_ASC))
  		{
  			getYADARequest().setSortOrder(paraMap.get(YADARequest.PL_SORTORDER));
  		}
  		if (paraMap.get(YADARequest.PS_SORTORDER) != null && !paraMap.get(YADARequest.PS_SORTORDER).equals(YADARequest.SORT_ASC))
  		{
  			getYADARequest().setSortOrder(paraMap.get(YADARequest.PS_SORTORDER));
  		}
  		if (paraMap.get(YADARequest.PL_USER) != null && !paraMap.get(YADARequest.PL_USER).equals(YADARequest.DEFAULT_USER))
  		{
  			getYADARequest().setUser(paraMap.get(YADARequest.PL_USER));
  		}
  		if (paraMap.get(YADARequest.PS_USER) != null && !paraMap.get(YADARequest.PS_USER).equals(YADARequest.DEFAULT_USER))
  		{
  			getYADARequest().setUser(paraMap.get(YADARequest.PS_USER));
  		}
  		if (paraMap.get(YADARequest.PL_VIEWLIMIT) != null)
  		{
  			getYADARequest().setViewLimit(paraMap.get(YADARequest.PL_VIEWLIMIT));
  		}
  		if (paraMap.get(YADARequest.PS_VIEWLIMIT) != null)
  		{
  			getYADARequest().setViewLimit(paraMap.get(YADARequest.PS_VIEWLIMIT));
  		}
  		if (paraMap.get(YADARequest.PL_UPDATE_STATS) != null)
      {
        getYADARequest().setUpdateStats(paraMap.get(YADARequest.PL_UPDATE_STATS));
      }
      if (paraMap.get(YADARequest.PS_UPDATE_STATS) != null)
      {
        getYADARequest().setUpdateStats(paraMap.get(YADARequest.PS_UPDATE_STATS));
      }
  		getYADARequest().setParameterMap(paraMap);
  		
  		l.debug("current settings:\n"+getYADARequest().toString());
	  }
	  catch(YADARequestException e)
	  {
	    error(e.getMessage(),e);
	  }
	}
	
	/**
	 * Wraps exceptions and request metadata in json in order to provide useful information to requesting clients in the event 
	 * and uncaught exception bubbles up to {@link #execute()} 
	 * @param msg the message to report to the client
	 * @param e the exception to report to the client
	 * @return a json string with error and request information
	 */
	private String error(String msg, Exception e)
	{
		JSONObject j = new JSONObject();
		String     result = "";
		try
		{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			YADAQuery yq = getCurrentQuery();
			j.put("Help", "https://github.com/Novartis/YADA#other");
			j.put("Source", "https://github.com/Novartis/YADA");
			j.put("Version", YADAUtils.getVersion());
			j.put("Exception", e.getClass().getName());
      j.put("Message",msg);
      j.put("Qname",yq != null ? yq.getQname() : "UNKNOWN");
      j.put("App", yq != null ? yq.getApp() : "UNKNOWN");
      j.put("Query",yq != null ? yq.getYADACode() : "");
      j.put("Params",new JSONArray());
      JSONArray ja = j.getJSONArray("Params");
      if(yq != null)
      {
  			for(int i=0;i<yq.getData().size();i++) // list of LinkedHashMaps
  			{
  			  Iterator<?> iter = yq.getDataRow(i).keySet().iterator();
  			  JSONObject jo = new JSONObject();
  			  while(iter.hasNext())
  			  {			    
  			    String key = (String) iter.next();
  			    jo.put(key, Arrays.toString(yq.getDataRow(i).get(key)));
  			  }
  			  ja.put(jo);
  			}
      }
			if(getYADARequest().getRequest() != null && getYADARequest().getRequest().getMethod() != null)
			{
				String type = getYADARequest().getRequest().getMethod(); 
				j.put("Type",type);
			}
			
			String[] strace = sw.toString().replace("\t","").split("\n");
			j.put("StackTrace",new JSONArray());
			j.put("Links", new JSONArray());
			JSONArray st = j.getJSONArray("StackTrace");
			JSONArray links = j.getJSONArray("Links");
			Pattern rx_src = Pattern.compile("at com\\.novartis\\.opensource\\.yada\\.(.+)\\..+\\(.+\\.java:(\\d+)\\)");
			for(int i=0;i<strace.length;i++)
			{
			  Matcher m = rx_src.matcher(strace[i]);
			  if(m.matches())
			  {
			    links.put(links.length(),"https://github.com/Novartis/YADA/blob/master/yada-api/src/main/java/com/novartis/opensource/yada/"+m.group(1).replace('.','/')+".java#L"+m.group(2));
			  }
		    st.put(i,strace[i]);
			}
			result = j.toString(2);
		} 
		catch (JSONException | YADAResourceException e1)
		{
			e1.printStackTrace();
		} 
		return result;
	}
	
	/** 
	 * Executes the appropriate method (select, update, or upload) per the "method" request parameter.
	 * 
	 * @return String containing the result of execution.  This could be a string of data, a record count, 
	 * a url, etc.
	 */
	@SuppressWarnings("deprecation")
	public String execute()
	{
		String result = "";
		try
		{
			/*
			 * At this point I have a collection of YADAQuery objects
			 * 
			 * If each query has it's own adaptor, even singletons, it precludes transactions (multiple queries)
			 * If each request has a single adaptor, it precludes executing multiple queries across disparate sources 
			 * 
			 */
			String method = getYADARequest().getMethod();
			if (YADARequest.METHOD_UPLOAD.equals(method))
			{
				result = executeUpload();
			}
			else
			{
				// automatically set backwards-compatibility for updates/inserts if method parameter for "update" is included
				if(YADARequest.METHOD_UPDATE.equals(method))
				{
					getYADARequest().setResponse(new String[] {"com.novartis.opensource.yada.format.CountResponse"});
				}
				this.qMgr = new QueryManager(getYADARequest());
				//TODO Sequential execution: for drivers like vertica's which won't execute a second request if the resultset of the first is still open
				result = _execute();
			}
		}
		catch (YADAExecutionException e)
		{
			l.error(e.getMessage(),e);
			result = error(e.getMessage(),e);
		}
		catch (YADAPluginException e)
		{
			l.error(e.getMessage(),e);
			result = error(e.getMessage(),e);
		}
		catch (YADAFinderException e) 
		{
			l.error(e.getMessage(),e);
			result = error(e.getMessage(),e);
		} 
		catch (YADAQueryConfigurationException e)
		{
			l.error(e.getMessage(),e);
			result = error(e.getMessage(),e);
		} 
		catch (YADAResourceException e)
		{
			l.error(e.getMessage(),e);
			result = error(e.getMessage(),e);
		} 
		catch (YADARequestException e)
		{
			l.error(e.getMessage(),e);
			result = error(e.getMessage(),e);
		} 
		catch (YADAConnectionException e)
		{
			l.error(e.getMessage(),e);
			result = error(e.getMessage(),e);
		} 
		catch (YADAUnsupportedAdaptorException e)
		{
			l.error(e.getMessage(),e);
			result = error(e.getMessage(),e);
		} 
		catch (YADAAdaptorException e)
		{
			l.error(e.getMessage(),e);
			result = error(e.getMessage(),e);
		}
		catch (YADAConverterException e)
		{
			l.error(e.getMessage(),e);
			result = error(e.getMessage(),e);
		} 
		catch (YADAResponseException e)
		{
			l.error(e.getMessage(),e);
			result = error(e.getMessage(),e);
		} 
		catch (YADAParserException e)
		{
			l.error(e.getMessage(),e);
			result = error(e.getMessage(),e);
		} 
		catch (YADAIOException e)
		{
			l.error(e.getMessage(),e);
			result = error(e.getMessage(),e);
		} 
		catch (YADAAdaptorExecutionException e)
		{
			l.error(e.getMessage(),e);
			result = error(e.getMessage(),e);
		}
		catch (Exception e)
		{
			l.error(e.getMessage(),e);
			result = error(e.getMessage(),e);
		}
		return result;
	}

	/**
	 * Internal request processor for "get" or "update" requests (i.e., non-uploads.) 
	 * @return the result to return to the requesting client
	 * @throws YADAPluginException when a plugin fails to execute successfully
	 * @throws YADAAdaptorException when adaptor instantion or query building fails
	 * @throws YADAAdaptorExecutionException when query execution fails
	 * @throws YADAConnectionException when the commit fails
	 * @throws YADAConverterException when the {@link com.novartis.opensource.yada.format.Converter} implementation encounters an error or can't be instantiated by the {@link Response}
	 * @throws YADAResponseException when the {@link Response} implementation fails to execute successfully
	 * @throws YADARequestException when the class refrenced by the {@code response} parameter can't be instantiated or found, and the default instantiation fails.
	 * @throws YADAIOException when exporting results fails
	 * @throws YADAResourceException when exported results can't be written to the file system
	 * @throws YADAQueryConfigurationException  if the {@link Converter} can't be instantiated
	 */
	private String _execute() throws YADAPluginException,  
																	YADAAdaptorException,  
																	YADAAdaptorExecutionException,
																	YADAConnectionException, 
																	YADAConverterException, 
																	YADAResponseException, 
																	YADARequestException, 
																	YADAResourceException, 
																	YADAIOException, YADAQueryConfigurationException
	{
		//TODO How can global plugins be applied to each query?  A new parameter, e.g., APPLY_TO_EACH?
		//TODO How can other global parameters be applied to each query?
		String gResult = "";
		try
		{
			// engage global bypass
			gResult = engageBypass(this.getYADARequest());
			if (gResult != null)
			{
				return gResult;
			}
			// engage global preprocessor
			engagePreprocess(getYADARequest());
			
			// iterate over the queries in the request
			for(YADAQuery yq : this.qMgr.getQueries())
			{
			  // store ref to query
			  setCurrentQuery(yq);
				// engage query bypass
				engageBypass(yq); 
				YADAQueryResult yqr = yq.getResult(); 
				if (yqr != null)
				{
					continue;
				}
				// engage query preprocessor
				engagePreprocess(yq);
				// execute query
				yq.getAdaptor().execute(yq);
				if(this.qutils.isCommitQuery(yq))
				{
					// close query transaction
					this.qMgr.commit(yq);
				}
				// engage query postprocessor
				engagePostprocess(yq);
			}
			// close all request transaction
			this.qMgr.commit();
			// get handle to results
			setYADAQueryResults(this.qMgr.getQueries().length);
			// build response
			gResult = composeResponse();
			// engage global postprocessor
			gResult = engagePostprocess(gResult);
			// process for export, if desired
			gResult = exportResult(gResult);
		}
		finally
		{
			this.qMgr.releaseResources();
		}
		
		return gResult;
	}
	
	/**
	 * @param result the composed and possibly post-processed result string
	 * @return either the original {@code result} {@link String} when {@link YADARequest#getExport()} is {@code false} or the URL of the exported file when {@code true}
	 * @throws YADAResourceException when {@link FileUtils} can't create directories for output on the file system
	 * @throws YADAIOException when the exported output can't be written to the file system or other file writing resource causes a problem
	 */
	private String exportResult(String result) throws YADAResourceException, YADAIOException
	{
		String gResult = result;
		if(getYADARequest().getExport())
		{
			String filesep  = System.getProperty("file.separator");
			String user     = getYADARequest().getUser();
			String time     = FileUtils.getTimeStamp();
			String fmt      = getYADARequest().getFormat();
			String fullPath = FileUtils.mkUserDir(user) + filesep + time + "." + fmt;
			       gResult  = FileUtils.getRelativePath(fullPath);
		  String filename = fullPath;
		  File   f        = new File(filename);
			try(FileWriter fw = new FileWriter(f))
			{
				fw.write(result);
			} 
			catch (IOException e)
			{
				String msg = "There was a problem writing the results for export.";
				throw new YADAIOException(msg,e);
			}
		}
		return gResult;
	}
	
	/**
	 * Obtains list of {@link FileItem} from {@link YADARequest#getUploadItems()}, extracts item metadata and wraps it in json
	 * @return a json string containing pertinent metadata about the upload, including where to get it from the file system
	 * @throws YADAPluginException when plugin execution fails
	 * @throws YADAExecutionException when the upload result configuration fails
	 * @see YADARequest#getUploadItems()
	 */
	private String executeUpload() throws YADAPluginException, YADAExecutionException
	{
		//TODO move upload item processing to this method from YADARequest
		String result = engageBypass(this.getYADARequest());
		l.debug("Select bypass [result] is ["+result+"]");
		if (result != null)
		{
			return result;
		}
		engagePreprocess(getYADARequest());
		try
		{
			/* must return a json object like: 
			 * {"files": [
						  {
						    "name": "picture1.jpg",
						    "size": 902604,
						    "url": "http:\/\/example.org\/files\/picture1.jpg",
						    "thumbnailUrl": "http:\/\/example.org\/files\/thumbnail\/picture1.jpg",
						    "deleteUrl": "http:\/\/example.org\/files\/picture1.jpg",
						    "deleteType": "DELETE"
						  },
						  {
						    "name": "picture2.jpg",
						    "size": 841946,
						    "url": "http:\/\/example.org\/files\/picture2.jpg",
						    "thumbnailUrl": "http:\/\/example.org\/files\/thumbnail\/picture2.jpg",
						    "deleteUrl": "http:\/\/example.org\/files\/picture2.jpg",
						    "deleteType": "DELETE"
						  }
						]}
			 */
			
			JSONArray files = new JSONArray();
			for ( FileItem fItem : getYADARequest().getUploadItems())
			{
			  JSONObject f = new JSONObject(); 
		    if(fItem.isFormField())
		    {
		      f.put("formField",true);
		      f.put("name", fItem.getFieldName());
		      f.put("value", fItem.getString());
		    }
		    else
		    {
		      DiskFileItem item = (DiskFileItem)fItem;
  				l.debug(item.toString());
  				f.put("name",item.getName());
  				f.put("size",item.getSize());
  				f.put("path",item.getStoreLocation().getAbsolutePath());
  				f.put("url","");
  				f.put("thumbnailUrl","");
  				f.put("deleteUrl","");
  				f.put("deleteType","");
		    }
  			files.put(f);
			}
			result = new JSONObject().put("files",files).toString();
		}
		catch (JSONException e)
		{
			l.error(e.getMessage());
			e.printStackTrace();
			throw new YADAExecutionException(e.getMessage());
		}

		result = engagePostprocess(result);
		l.debug("result:" + result);
		return result;
	}
	
	/**
	 * Determines the appropriate {@link Response} class from {@code format} and instantiates it
	 * @param format the value of the {@code format} or {@code f} parameter
	 * @return a {@link Response} implementation
	 * @throws YADARequestException when the default response, based on {@code format} or {@code f} parameter value cannot be instantiated, accessed, or found.
	 */
	private Response getDefaultResponse(String format) throws YADARequestException 
	{
		Response response;
		String pkg = "com.novartis.opensource.yada.format.";
		if(getYADARequest().isFormatStructured())
		{
			try
			{
				response = (Response) Class.forName(pkg+format.toUpperCase()+"Response").newInstance();
			} 
			catch (InstantiationException e)
			{
				String msg = "Could not instantiate Response.";
				throw new YADARequestException(msg,e);
			} 
			catch (IllegalAccessException e)
			{
				String msg = "Could not access Response class.";
				throw new YADARequestException(msg,e);

			} 
			catch (ClassNotFoundException e)
			{
				String msg = "Could not find Response class";
				throw new YADARequestException(msg,e);
			}
		}
		else
		{
			response = new DelimitedResponse();
		}
		return response;
	}
	
	/**
	 * Insantiates the response class and initiates result composition.
	 * @since 4.0.0
	 * @return {@link String} containing the formatted response.
	 * @throws YADAConverterException when the {@link com.novartis.opensource.yada.format.Converter} implementation encounters an error or can't be instantiated by the {@link Response}
	 * @throws YADAResponseException when the {@link Response} implementation fails to execute successfully
	 * @throws YADARequestException when the class refrenced by the {@code response} parameter can't be instantiated or found, and the default instantiation fails.
	 * @throws YADAQueryConfigurationException if the {@link Converter} can't be instantiated
	 */
	private String composeResponse() throws YADARequestException, YADAResponseException, YADAConverterException, YADAQueryConfigurationException 
	{
		String   result         = "";
		String   format         = getYADARequest().getFormat(); 
		String   responseClass  = getYADARequest().getResponse(); 
		Response response;
		
		if( responseClass != null && !"".equals(responseClass))
		{
			try
			{
				response = (Response) Class.forName(responseClass).newInstance();
			} 
			catch (Exception e)
			{
				l.warn("The specified class ["+responseClass+"], as provided, could not be instantiated.  Trying FQCN."); 
				try
				{
					response = (Response) Class.forName(FORMAT_PKG+responseClass).newInstance();
				}
				catch(Exception e1)
				{
					l.warn("The specified class ["+responseClass+"], as provided, could not be instantiated.  Trying default classes.");
					//TODO send messages like this back to the client, in the default response to indicate what happened with more specificity
					response = getDefaultResponse(format);
				}
			} 
		}
		else
		{
			response = getDefaultResponse(format);
		}
		//TODO enable verbose response options which include details about processing of query
		result = response.compose(getYADAQueryResults()).toString(getYADARequest().getPretty());
		
		return result;
	}
	
	/**
	 * Builds an array of result objects obtained from queries indexed by {@link #qMgr}
	 * @param size the length of the result array
	 */
	private void setYADAQueryResults(int size) 
	{
		this.queryResults = new YADAQueryResult[size];
		for (int i = 0; i < this.queryResults.length; i++)
		{
			this.queryResults[i] = this.qMgr.getQueries()[i].getResult();
		}
	}
	
	/**
	 * Standard accessor for variable.
	 * @return the array of query result objects
	 */
	public YADAQueryResult[] getYADAQueryResults()
	{
		return this.queryResults;
	}
	
	/**
	 * Standard accessor for variable
	 * @return the currently executing query object
	 */
	public YADAQuery getCurrentQuery()
  {
    return this.currentQuery;
  }

  /**
   * Standard mutator for variable
   * @param currentQuery the {@link YADAQuery} under execution
   */
  public void setCurrentQuery(YADAQuery currentQuery)
  {
    this.currentQuery = currentQuery;
  }

  /**
	 * Execute the bypass plugin 
	 * @since 4.0.0
	 * @param yq the yada query object
	 * @throws YADAPluginException when the plugin fails to execute successfully
	 */
	private void engageBypass(YADAQuery yq) throws YADAPluginException
	{
		YADAQueryResult yqr = null;
		if(yq.hasParam(YADARequest.PS_PLUGIN))
//				&& yq.getParam(YADARequest.PS_PLUGIN).getTarget().equals(yq.getQname()))
		{
			String[] plugins  = yq.getYADAQueryParamValuesForTarget(YADARequest.PS_PLUGIN);
			if (null != plugins && plugins.length > 0)
			{
			  for (int i=0; i < plugins.length; i++)
        {
          String plugin = plugins[i];
       // check for default parameter plugin (args) unprocessed by Service.handleRequestParameters
          int firstCommaIndex = plugin.indexOf(',');
          String    args = "";
          YADAParam yp   = null;
          if(firstCommaIndex > -1)
          {
            args   = plugin.substring(firstCommaIndex+1);
            plugin = plugin.substring(0, firstCommaIndex);
            // add a query parameter for the arg list
            yp = new YADAParam(YADARequest.PS_ARGLIST, args, plugin, YADAParam.NONOVERRIDEABLE, true); 
            yq.addParam(yp);
          }
					l.debug("possible bypass plugin is["+plugin+"]");
					if (null != plugin && !"".equals(plugin))
					{
						try
						{
							Class<?> pluginClass = plugin.indexOf(YADARequest.PLUGIN_PKG) > -1 
									? Class.forName(plugin) 
									: Class.forName(YADARequest.PLUGIN_PKG + "." + plugin);
							Class<?> bypass = Class.forName(YADARequest.PLUGIN_PKG+"."+YADARequest.BYPASS);
							if (pluginClass != null)
							{
								if(bypass.isAssignableFrom(pluginClass)) // this checks plugin type
								{
									l.info("Found a QUERY_BYPASS plugin with the classname ["+plugin+"]");
									try
									{
										Object plugObj = pluginClass.newInstance();
										if(getYADARequest().getPluginArgs().size() > 0) // api call might not set any args
										  getYADARequest().setArgs(getYADARequest().getPluginArgs().get(i));
										yqr = ((Bypass)plugObj).engage(getYADARequest(),yq);
										 // remove the param that was created earlier, to avoid potential conflicts later
                    //TODO review security and other implications of removing the arglist parameter from the query object
                    yq.getYADAQueryParams().remove(yp);
										yq.setResult(yqr);
										
									}
									catch(InstantiationException e)
									{
										String msg = "Unable to instantiate plugin for class "+pluginClass.getName();
										throw new YADAPluginException(msg,e);
									}
									catch(IllegalAccessException e)
									{
										String msg = "Unable to instantiate plugin for class "+pluginClass.getName();
										throw new YADAPluginException(msg,e);
									}
									catch(ClassCastException e)
									{
										String msg = "Unable to instantiate plugin for class "+pluginClass.getName();
										throw new YADAPluginException(msg,e);
									}
								}
								else
								{
									l.debug("Could not find an BYPASS plugin with the classname ["+plugin+"]");
								}
							}
						}
						catch(ClassNotFoundException e)
						{
							String msg = "Could not find any plugin with the classname ["+plugin+"]";
							l.error(msg);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Executes the preprocess plugin
	 * @since 4.0.0
	 * @param yq the yada query object
	 * @throws YADAPluginException when the plugin fails to execute successfully
	 */
	private void engagePreprocess(YADAQuery yq) throws YADAPluginException
	{
		if(yq.hasParam(YADARequest.PS_PLUGIN))
//				&& yq.getParam(YADARequest.PS_PLUGIN).getTarget().equals(yq.getQname()))
		{
			String[] plugins  = yq.getYADAQueryParamValuesForTarget(YADARequest.PS_PLUGIN);
			if ( null != plugins && plugins.length > 0)
			{
			  for (int pluginIndex=0; pluginIndex < plugins.length; pluginIndex++)
	      {
			    String plugin = plugins[pluginIndex];
			    // check for default parameter plugin (args) unprocessed by Service.handleRequestParameters
			    int firstCommaIndex = plugin.indexOf(',');
			    String    args = "";
			    YADAParam yp   = null;
			    if(firstCommaIndex > -1)
          {
			      args   = plugin.substring(firstCommaIndex+1);
			      plugin = plugin.substring(0, firstCommaIndex);
			      // add a query parameter for the arg list
			      //yp = new YADAParam(YADARequest.PS_ARGLIST, args, yq.getQname(), YADAParam.NONOVERRIDEABLE, true);
			      yp = new YADAParam(YADARequest.PS_ARGLIST, args, plugin, YADAParam.NONOVERRIDEABLE, true);
			      yq.addParam(yp);
          }
					l.debug("possible preprocess plugin is["+plugin+"]");
					if (null != plugin && !"".equals(plugin))
					{
						try
						{
							Class<?> pluginClass = plugin.indexOf(YADARequest.PLUGIN_PKG) > -1 
									? Class.forName(plugin) 
									: Class.forName(YADARequest.PLUGIN_PKG + "." + plugin);
							Class<?> preproc = Class.forName(YADARequest.PLUGIN_PKG+"."+YADARequest.PREPROCESS);
							
							if(preproc.isAssignableFrom(pluginClass)) 
							{
								l.info("Found a query-level PREPROCESS plugin with the classname ["+plugin+"]");
								try
								{
									Object plugObj = pluginClass.newInstance();
									((Preprocess)plugObj).engage(getYADARequest(),yq);
									// reset the query internals
									try
									{
									  // remove the param that was created earlier, to avoid potential conflicts later
									  //TODO review security and other implications of removing the arglist parameter from the query object
									  yq.getYADAQueryParams().remove(yp);
									  yq.clearResources();
										this.qMgr.prepQueryForExecution(this.qMgr.endowQuery(yq));
									} 
									catch (YADAQueryConfigurationException|YADAResourceException|YADAUnsupportedAdaptorException e)
									{
										String msg = "Unable to re-endow YADAQuery with new parameters.";
										throw new YADAPluginException(msg, e);
									} 
									catch (YADAConnectionException | YADARequestException | YADAAdaptorException | YADAParserException e) 
									{
                    String msg = "Unable to re-prep YADAQuery for execution.";
                    throw new YADAPluginException(msg, e);
                  } 
								}
								catch(InstantiationException|IllegalAccessException|ClassCastException e)
								{
									String msg = "Unable to instantiate plugin for class "+pluginClass.getName();
									throw new YADAPluginException(msg,e);
								}
							}
							else
							{
								l.debug("Could not find a PREPROCESS plugin with the classname ["+plugin+"]");
							}
						}
						catch(ClassNotFoundException e)
						{
							String msg = "Could not find any plugin with the classname ["+plugin+"]"; 
							l.error(msg,e);
						}
					}
				}
			}
			else
	    {
	      if(yq.isProtected())
	      {
	        String msg = "Unauthorized";
	        throw new YADASecurityException(msg);
	      }
	    }
		}
		else
		{
	    if(yq.isProtected())
	    {
	      String msg = "Unauthorized";
	      throw new YADASecurityException(msg);
	    }
		}
	}
	
	/**
	 * Executes a query-level post-processor plugin
	 * @param yq the yada query object
	 * @throws YADAPluginException when the plugin fails to execute successfully
	 */
	@SuppressWarnings("static-method")
	private void engagePostprocess(YADAQuery yq) throws YADAPluginException
	{
		if(yq.hasParam(YADARequest.PS_PLUGIN))
//				&& yq.getParam(YADARequest.PS_PLUGIN).getTarget().equals(yq.getQname()))
		{
			String[] plugins  = yq.getYADAQueryParamValuesForTarget(YADARequest.PS_PLUGIN);
			if ( null != plugins && plugins.length > 0 )
			{
			  for (int i=0; i < plugins.length; i++)
        {
          String plugin = plugins[i];
          // check for default parameter plugin (args) unprocessed by Service.handleRequestParameters
          int firstCommaIndex = plugin.indexOf(',');
          String    args = "";
          YADAParam yp   = null;
          if(firstCommaIndex > -1)
          {
            args   = plugin.substring(firstCommaIndex+1);
            plugin = plugin.substring(0, firstCommaIndex);
            // add a query parameter for the arg list
            yp = new YADAParam(YADARequest.PS_ARGLIST, args, plugin, YADAParam.NONOVERRIDEABLE, true); 
            yq.addParam(yp);
          }
					l.debug("possible postprocess plugin is["+plugin+"]");
					if (null != plugin && !"".equals(plugin))
					{
						try
						{
							Class<?> pluginClass = plugin.indexOf(YADARequest.PLUGIN_PKG) > -1 
									? Class.forName(plugin) 
									: Class.forName(YADARequest.PLUGIN_PKG + "." + plugin);
							Class<?> postproc = Class.forName(YADARequest.PLUGIN_PKG+"."+YADARequest.POSTPROCESS);
							if(postproc.isAssignableFrom(pluginClass)) 
							{
								l.info("Found a POSTPROCESS plugin with the classname ["+plugin+"]");
								try
								{
									Object            plugObj     = pluginClass.newInstance();
									((Postprocess)plugObj).engage(yq);
									// remove the param that was created earlier, to avoid potential conflicts later
                  //TODO review security and other implications of removing the arglist parameter from the query object
                  yq.getYADAQueryParams().remove(yp);
								}
								catch(InstantiationException e)
								{
									String msg = "Unable to instantiate plugin for class "+pluginClass.getName();
									throw new YADAPluginException(msg,e);
								}
								catch(IllegalAccessException e)
								{
									String msg = "Unable to instantiate plugin for class "+pluginClass.getName();
									throw new YADAPluginException(msg,e);
								}
								catch(ClassCastException e)
								{
									String msg = "Unable to instantiate plugin for class "+pluginClass.getName();
									throw new YADAPluginException(msg,e);
								} 
							}
							else
							{
								l.debug("Could not find a POSTPROCESS plugin with the classname ["+plugin+"]");
							}
						}
						catch(ClassNotFoundException e)
						{
							String msg = "Could not find any plugin with the classname ["+plugin+"]";
							l.error(msg);
						}
					}
				}
			}
		}
		//TODO if no class, url, or script, should return same 'result' passed in
	}
	
	/**
	 * Executes a request-level Bypass plugin
	 * @param lyadaReq the request configuration
	 * @return a result to return to the requesting client
	 * @throws YADAPluginException when the plugin fails to execute successfully
	 */
	private String engageBypass(YADARequest lyadaReq) throws YADAPluginException
	{
		String result = null;
		String[] plugins  = lyadaReq.getPlugin();
		if (null != plugins && plugins.length > 0)
		{
		  for (int i=0; i < plugins.length; i++)
      {
        String plugin = plugins[i];
				l.debug("possible Bypass plugin is["+plugin+"]");
				if (null != plugin && !"".equals(plugin))
				{
					try
					{
						Class<?> pluginClass = plugin.indexOf(YADARequest.PLUGIN_PKG) > -1 
								? Class.forName(plugin) 
								: Class.forName(YADARequest.PLUGIN_PKG + "." + plugin);
						Class<?> bypass = Class.forName(YADARequest.PLUGIN_PKG+"."+YADARequest.BYPASS);
						if (pluginClass != null)
						{
							if(bypass.isAssignableFrom(pluginClass)) // this checks plugin type
							{
								l.info("Found an BYPASS plugin with the classname ["+plugin+"]");
								try
								{
									Object plugObj = pluginClass.newInstance();
									if(getYADARequest().getPluginArgs().size() > 0) // api call might not set any args
									  getYADARequest().setArgs(getYADARequest().getPluginArgs().get(i));
									result = ((Bypass)plugObj).engage(getYADARequest());
								}
								catch(InstantiationException e)
								{
									String msg = "Unable to instantiate plugin for class "+pluginClass.getName();
									throw new YADAPluginException(msg,e);
								}
								catch(IllegalAccessException e)
								{
									String msg = "Unable to instantiate plugin for class "+pluginClass.getName();
									throw new YADAPluginException(msg,e);
								}
								catch(ClassCastException e)
								{
									String msg = "Unable to instantiate plugin for class "+pluginClass.getName();
									throw new YADAPluginException(msg,e);
								}
							}
							else
							{
								l.debug("Could not find a BYPASS plugin with the classname ["+plugin+"]");
							}
						}
					}
					catch(ClassNotFoundException e)
					{
						String msg = "Could not find any plugin with the classname ["+plugin+"]";
						l.error(msg);
//						throw new YADAPluginException(msg,e);
					}
				}
			}
		}
		return result; // returns a string 
	}
	
	/**
	 * Executes a request-level pre-processor plugin
	 * @param yReq the request configuration
	 * @throws YADAPluginException when the plugin fails to execute successfully
	 */
	private void engagePreprocess(YADARequest yReq) throws YADAPluginException
	{
		String[] plugins  = yReq.getPlugin();
		if ( null != plugins && plugins.length > 0)
		{
		  // TODO evaluate vulnerabilities to circumvent query-level security plugins
			for (int i=0; i < plugins.length; i++)
			{
			  String plugin = plugins[i];
				l.debug("possible preprocess plugin is["+plugin+"]");
				if (null != plugin && !"".equals(plugin))
				{
					try
					{
						Class<?> pluginClass = plugin.indexOf(YADARequest.PLUGIN_PKG) > -1 
								? Class.forName(plugin) 
								: Class.forName(YADARequest.PLUGIN_PKG + "." + plugin);
						Class<?> preproc = Class.forName(YADARequest.PLUGIN_PKG+"."+YADARequest.PREPROCESS);
						
						if(preproc.isAssignableFrom(pluginClass)) 
						{
							l.info("Found a request-level PREPROCESS plugin with the classname ["+plugin+"]");
							try
							{
								Object plugObj = pluginClass.newInstance();
								if(getYADARequest().getPluginArgs().size() > 0) // api call might not set any args
								  yReq.setArgs(yReq.getPluginArgs().get(i));
								setYADARequest(((Preprocess)plugObj).engage(yReq));
								// reset query manager, as service parameters may have changed
								try
								{
									// close existing connections first
									this.qMgr.releaseResources();
									this.qMgr = new QueryManager(getYADARequest());
								} 
								catch (YADAQueryConfigurationException e)
								{
									String msg = "Unable to reinitialize QueryManager with new parameters.";
									throw new YADAPluginException(msg, e);
								} 
								catch (YADAResourceException e)
								{
									String msg = "Unable to reinitialize QueryManager with new parameters.";
									throw new YADAPluginException(msg, e);
								} 
								catch (YADAConnectionException e)
								{
									String msg = "Unable to reinitialize QueryManager with new parameters.";
									throw new YADAPluginException(msg, e);
								} 
								catch (YADAFinderException e)
								{
									String msg = "Unable to reinitialize QueryManager with new parameters.";
									throw new YADAPluginException(msg, e);
								} 
								catch (YADAUnsupportedAdaptorException e)
								{
									String msg = "Unable to reinitialize QueryManager with new parameters.";
									throw new YADAPluginException(msg, e);
								} 
								catch (YADARequestException e)
								{
									String msg = "Unable to reinitialize QueryManager with new parameters.";
									throw new YADAPluginException(msg, e);
								} 
								catch (YADAAdaptorException e)
								{
									String msg = "Unable to reinitialize QueryManager with new parameters.";
									throw new YADAPluginException(msg, e);
								} 
								catch (YADAParserException e)
								{
									String msg = "Unable to reinitialize QueryManager with new parameters.";
									throw new YADAPluginException(msg, e);
								}
							}
							catch(InstantiationException e)
							{
								String msg = "Unable to instantiate plugin for class "+pluginClass.getName();
								throw new YADAPluginException(msg,e);
							}
							catch(IllegalAccessException e)
							{
								String msg = "Unable to instantiate plugin for class "+pluginClass.getName();
								throw new YADAPluginException(msg,e);
							}
							catch(ClassCastException e)
							{
								String msg = "Unable to instantiate plugin for class "+pluginClass.getName();
								throw new YADAPluginException(msg,e);
							} 
						}
						else
						{
							l.debug("Could not find a PREPROCESS plugin with the classname ["+plugin+"]");
						}
					}
					catch(ClassNotFoundException e)
					{
						String msg = "Could not find any plugin with the classname ["+plugin+"]"; 
						l.error(msg,e);
//						throw new YADAPluginException(msg,e);
					}
				}
			}
		}
	}
	
	/**
	 * Executes a request-level post-processor plugin
	 * @param result the raw result serving as plugin input
	 * @return a result to return to the requesting client
	 * @throws YADAPluginException when the plugin fails to execute successfully
	 */
	private String engagePostprocess(String result) throws YADAPluginException
	{
		String   lResult = result;
		String[] plugins = getYADARequest().getPlugin();
		if ( null != plugins && plugins.length > 0 )
		{
		  for (int i=0; i < plugins.length; i++)
      {
        String plugin = plugins[i];
				l.debug("possible postprocess plugin is["+plugin+"]");
				if (null != plugin && !"".equals(plugin))
				{
					try
					{
						Class<?> pluginClass = plugin.indexOf(YADARequest.PLUGIN_PKG) > -1 
								? Class.forName(plugin) 
								: Class.forName(YADARequest.PLUGIN_PKG + "." + plugin);
						Class<?> postproc = Class.forName(YADARequest.PLUGIN_PKG+"."+YADARequest.POSTPROCESS);
						if(postproc.isAssignableFrom(pluginClass)) 
						{
							l.info("Found a POSTPROCESS plugin with the classname ["+plugin+"]");
							try
							{
								Object plugObj = pluginClass.newInstance();
								if(getYADARequest().getPluginArgs().size() > 0) // api call might not set any args
								  getYADARequest().setArgs(getYADARequest().getPluginArgs().get(i));
								lResult = ((Postprocess)plugObj).engage(getYADARequest(), lResult);
							}
							catch(InstantiationException e)
							{
								String msg = "Unable to instantiate plugin for class "+pluginClass.getName();
								throw new YADAPluginException(msg,e);
							}
							catch(IllegalAccessException e)
							{
								String msg = "Unable to instantiate plugin for class "+pluginClass.getName();
								throw new YADAPluginException(msg,e);
							}
							catch(ClassCastException e)
							{
								String msg = "Unable to instantiate plugin for class "+pluginClass.getName();
								throw new YADAPluginException(msg,e);
							}
						}
						else
						{
							l.debug("Could not find a POSTPROCESS plugin with the classname ["+plugin+"]");
						}
					}
					catch(ClassNotFoundException e)
					{
						String msg = "Could not find any plugin with the classname ["+plugin+"]";
						l.error(msg);
//						throw new YADAPluginException(msg,e);
					}
				}
			}
		}
		// if no class, url, or script, should return same 'result' passed in
		return lResult;
	}
	
	/**
	 * Standard mutator for variable.
	 * @param yadaReq YADA request configuration
	 */
	public void setYADARequest(YADARequest yadaReq) {
		this.yadaReq = yadaReq;
	}
	
	/**
	 * Standard accessor for variable.
	 * @return the {@link YADARequest} object
	 */
	public YADARequest getYADARequest() {
		return this.yadaReq;
	}
	
	/**
	 * Takes the old-style argument parameters and appends them to the {@link YADARequest#PS_PLUGIN} parameter.
	 * The new config is then handled downstream during normal plugin parameter processing
	 * @param paraMap the {@link Map} passed in the {@link javax.servlet.http.HttpServletRequest}
	 * @param constant the {@link YADARequest} argument constant
	 */
	private void setDeprecatedPlugin(Map<String, String[]> paraMap, String constant)
	{
	  String[] plugins = paraMap.get(YADARequest.PS_PLUGIN);
    String plugin = "";
    if(plugins == null)
    {
      plugins = paraMap.get(YADARequest.PL_PLUGIN);
      if(plugins != null)
        paraMap.remove(YADARequest.PL_PLUGIN);
    }
    if(plugins != null)
    {
      plugin = plugins[0] + "," + paraMap.get(constant)[0]; 
    }
    paraMap.put(YADARequest.PS_PLUGIN,new String[] { plugin });
	}
}