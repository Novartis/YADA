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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.adaptor.FileSystemAdaptor;
import com.novartis.opensource.yada.plugin.Bypass;
import com.novartis.opensource.yada.plugin.EmailBypassPlugin;
import com.novartis.opensource.yada.plugin.Postprocess;
import com.novartis.opensource.yada.plugin.Preprocess;
import com.novartis.opensource.yada.plugin.ScriptBypass;
import com.novartis.opensource.yada.plugin.ScriptPostprocessor;
import com.novartis.opensource.yada.plugin.ScriptPreprocessor;
import com.novartis.opensource.yada.util.YADAUtils;

/**
 * A utility class for marshalling url params into instance variables, and serving as a portable 
 * structure for passing parameter values across layers of the API. This class also sets default YADA 
 * parameter values, converts values into appropriate object types, or nestles values in data structures.
 * 
 * @author David Varon
 * @since 1.0.0
 *
 */
public class YADARequest {

	/**
	 * Local logger hangle
	 */
	private static Logger l = Logger.getLogger(YADARequest.class);
	// constants
	/**
	 * A constant equal to: {@value}. This is the default value for the {@code method} or {@code m} parameter.
	 * This value is the default method for both.
	 * Also Used for HTTP {@value} in [@link RESTAdaptor}
	 */
	public static final String METHOD_GET 				= "GET";
	/**
	 * A constant equal to: {@value}. This is the default value for the {@code method} or {@code m} parameter.
	 * Used for HTTP {@value} in [@link RESTAdaptor}
	 * @since 8.5.0
	 */
	public static final String METHOD_POST 				= "POST";
	/**
	 * A constant equal to: {@value}. This is the default value for the {@code method} or {@code m} parameter.
	 * Used for HTTP {@value} in [@link RESTAdaptor}
	 * @since 8.5.0
	 */
	public static final String METHOD_PUT 				= "PUT";
	/**
	 * A constant equal to: {@value}. This is the default value for the {@code method} or {@code m} parameter.
	 * Used for HTTP {@value} in [@link RESTAdaptor}
	 * @since 8.5.0
	 */
	public static final String METHOD_PATCH				= "PATCH";
	/**
	 * A constant equal to: {@value}. This is the default value for the {@code method} or {@code m} parameter.
	 * Used for HTTP {@value} in [@link RESTAdaptor}
	 * @since 8.5.0
	 */
	public static final String METHOD_DELETE 			= "DELETE";
	/**
	 * A constant equal to: {@value}. This is the default value for the {@code method} or {@code m} parameter.
	 * Used for HTTP {@value} in [@link RESTAdaptor}
	 * @since 8.5.0
	 */
	public static final String METHOD_OPTONS 			= "OPTIONS";
	/**
	 * A constant equal to: {@value}. This is the default value for the {@code method} or {@code m} parameter.
	 * Used for HTTP {@value} in [@link RESTAdaptor}
	 * @since 8.5.0
	 */
	public static final String METHOD_HEAD 				= "HEAD";
	/**
	 * A constant equal to: {@value}. This is the default value for the {@code method} or {@code m} parameter.
	 * Used for HTTP {@value} in [@link RESTAdaptor}
	 * @since 8.5.0
	 */
	public static final String METHOD_TRACE 			= "TRACE";
	/**
	 * A constant equal to: {@value}. This value is still necessary for backward compatiblity when expecting
	 * an update query to return an integer result, rather than a JSON object.
	 * @deprecated as of 4.0.0
	 */
	@Deprecated
	public static final String METHOD_UPDATE 			= "update";
	/**
	 * A constant equal to: {@value}. 
	 * @deprecated as of 4.0.0, as this is now detected automatically.
	 */
	@Deprecated
	public static final String METHOD_UPLOAD 			= "upload";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String JSON_KEY_DATA    	= "DATA";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String JSON_KEY_QNAME   	= "qname";
	/**
	 * A constant equal to: {@value}.  This is the default value of the {@code format} or {@code f} parameter.
	 */
	public static final String FORMAT_JSON 				= "json";
	/**
	 * A constant equal to: {@value}
	 * @since 8.5.0
	 */
	public static final String FORMAT_PLAINTEXT  	= "text";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String FORMAT_CSV 				= "csv";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String FORMAT_CSV_STRING  = ",";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String FORMAT_TSV 				= "tsv";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String FORMAT_TAB 				= "tab";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String FORMAT_TSV_STRING  = "\t";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String FORMAT_PSV 				= "psv";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String FORMAT_PIPE 				= "pipe";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String FORMAT_PIPE_STRING = "|";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String FORMAT_XML 				= "xml";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String FORMAT_HTML 				= "html";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String FORMAT_DELIMITED   = "delimited";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String SORT_ASC 					= "asc";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String SORT_DESC 					= "desc";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String PLUGIN_PKG 				= "com.novartis.opensource.yada.plugin";
	/**
	 * A constant equal to: {@value}.  {@code Override/override} was renamed {@code bypass}
	 */
	@Deprecated
	public static final String OVERRIDE 					= "Override";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0
	 */
	public static final String BYPASS             = "Bypass";
	/**
	 * A constant equal to: {@value}.  Included here for consistency. Use {@code QUERY_BYPASS} instead.
	 * @since 4.0.0
	 * @deprecated as of 4.0.0
	 */
	@Deprecated
	public static final String QUERY_OVERRIDE  	  = "QueryBypass";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0
	 * @deprecated as of 0.4.2.0
	 */
	@Deprecated
	public static final String QUERY_BYPASS    	  = "QueryBypass";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0
	 */
	public static final String OVERRIDE_LC 				= "override";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String PREPROCESS 				= "Preprocess";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String PREPROCESS_LC			= "preprocess";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String POSTPROCESS 				= "Postprocess";
	/**
	 * A constant equal to: {@value}
	 */
	@Deprecated
	public static final String QUERY_POSTPROCESS 	= "QueryPostprocess";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String POSTPROCESS_LC			= "postprocess";
	/**
	 * A constant equal to: {@value}.  This is the default value for the {@code qname} or {@code q} parameter.
	 * If this value is passed for {@code qname} or {@code q}, and there is no {@code plugin} or {@code pl} 
	 * parameter in the request query string, YADA will return an error.
	 */
	public static final String DEFAULT_QNAME 			= "YADA dummy";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String DEFAULT_USER 			= "YADABOT";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String DEFAULT_DELIMITER  = ",";
	/**
	 * A constant equal to: {@value} (newline)
	 */
	public static final String DEFAULT_ROW_DELIMITER = "\n";
	/**
	 * A constant equal to: {@value}
	 */
	//TODO make PARAM_DELIMITER a settable value
	public static final String PARAM_DELIMITER		= ",";
	/**
	 * A constant equal to: {@value}
	 */
	public static final int    DEFAULT_START_PAGE = 1;
	/**
	 * A constant equal to: {@value}
	 */
	public static final int    DEFAULT_PAGE_SIZE 	= 20;
	/**
	 * A constant equal to: {@value}
	 */
	public static final int    MAX_PAGE_SIZE    	= 1000000000;
	/**
	 * A constant equal to: {@code com.novartis.opensource.yada.plugin.ScriptPreprocessor}
	 */
	public static final String SCRIPT_PREPROCESSOR  = ScriptPreprocessor.class.getName();
	/**
	 * A constant equal to: {@code com.novartis.opensource.yada.plugin.ScriptPostprocessor}
	 */
	public static final String SCRIPT_POSTPROCESSOR = ScriptPostprocessor.class.getName();
	/**
	 * A constant equal to: {@code com.novartis.opensource.yada.plugin.ScriptBypass}
	 */
	public static final String SCRIPT_BYPASS        = ScriptBypass.class.getName();
  /**
   * A constant equal to {@value} for handling param value syntax
   */
	private static final String RX_NOTJSON = "^[^{].+$";
	
	// PL = Param Long
	// PS = Param Short
	/**
	 * A constant equal to: {@value}. Used internally for query parameter access and mutation.
	 * @since 7.1.0
	 */
	public static final String PS_ARGLIST     = "a";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 * @deprecated as of 7.1.0
	 */
	@Deprecated
	public static final String PS_ARGS        = "a";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 * @deprecated as of 7.1.0
	 */
	@Deprecated
	public static final String PS_BYPASSARGS  = "b";
	/**
   * A constant equal to: {@value}
   * @since 5.1.0
   */
  public static final String PS_COMPACT     = "cm";
	/**
   * A constant equal to: {@value}
   * @since PROVISIONAL
   */
	public static final String PS_COOKIES     = "ck";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this param is {@code true}.
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_COUNT       = "c";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_COMMITQUERY = "cq";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this param is {@code false}.
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_COUNTONLY   = "co";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_CONVERTER   = "cv";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this parameter is {@link YADARequest#DEFAULT_DELIMITER}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_DELIMITER   = "d";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_EXPORT      = "e";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_EXPORTLIMIT = "el";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this parameter is {@code null}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_FILTERS     = "fi";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this parameter is {@link YADARequest#FORMAT_JSON}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_FORMAT      = "f";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_JSONPARAMS  = "j";
	/**
   * A constant equal to: {@value}
   * @since 6.2.0
   */
  public static final String PS_JOIN        = "ij";
  /**
   * A constant equal to: {@value}
   * @since 6.2.0
   */
  public static final String PS_LEFTJOIN    = "lj";
  /**
   * A constant equal to: {@value}
   * @since 8.5.0
   */
  public static final String PS_HTTPHEADERS = "H";
  /**
   * A constant equal to: {@value}
   * This is a global parameter.
   * The default value associated to this parameter is {@code null}
   * @since 4.0.0 (Short param aliases were first added in 4.0.0)
   */
	public static final String PS_HARMONYMAP  = "h";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this parameter is {@link YADARequest#METHOD_GET}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 * No longer deprecated (4.0.0) as of 8.5.0
	 */
	public static final String PS_METHOD      = "m";
	/**
	 * a constant equals to: {@value}
	 * @since 8.7.0
	 */
	public static final String PS_OAUTH       = "o";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 * @deprecated as of 7.1.0
	 */
	@Deprecated
	public static final String PS_OVERARGS    = "o";	
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_PAGE        = "pg";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this param is {@link YADARequest#DEFAULT_PAGE_SIZE}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_PAGESIZE    = "pz";
	/**
	 * A constant equal to: {@value}
 	 * This is a global parameter.
	 * The default value associated to this param is {@link YADARequest#DEFAULT_START_PAGE}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_PAGESTART   = "pg";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_PARAMS      = "p";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_PARAMSET    = "ps";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_PLUGIN      = "pl";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 * @deprecated as of 4.0.0
	 */
	@Deprecated
	public static final String PS_PLUGINTYPE  = "pt";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
   * @deprecated as of 7.1.0
   */
  @Deprecated
	public static final String PS_POSTARGS    = "pa";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
   * @deprecated as of 7.1.0
   */
  @Deprecated
	public static final String PS_PREARGS     = "pr";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_PRETTY      = "py";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_PROTOCOL    = "pc";
	/**
	 * A constant equal to: {@value}
	 * @since 4.1.0
	 */
	public static final String PS_PROXY       = "px";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_QNAME       = "q";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this parameter is {@link YADARequest#DEFAULT_ROW_DELIMITER}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_ROW_DELIMITER = "rd";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_RESPONSE    = "r";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value is {@code null}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_SORTKEY     = "s";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this param is {@link YADARequest#SORT_ASC}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_SORTORDER   = "so";
	/**
	 * A constant equal to: {@value}
	 * @since 5.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_UPDATE_STATS = "us";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_USER        = "u";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0 (Short param aliases were first added in 4.0.0)
	 */
	public static final String PS_VIEWLIMIT   = "vl";
	/**
	 * A constant equal to: {@value}
	 * @deprecated as of 7.1.0
	 */
	@Deprecated
	public static final String PL_ARGS        = "args";
	/**
	 * A constant equal to: {@value}.  This constant has replaced {@link YADARequest#PL_OVERARGS} to avoid 
	 * java compiler annotation errors with the caused by name conficts, i.e., {@code @Override} 
	 * @since 4.0.0
	 * @deprecated as of 7.1.0
	 */
	@Deprecated
	public static final String PL_BYPASSARGS  = "bypassargs";
	/**
	 * A constant equal to: {@value}
	 */
	//TODO there is no alias for PL_COLHEAD
	public static final String PL_COLHEAD     = "colhead";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0
	 */
	public static final String PL_COMMITQUERY = "commitQuery";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String PL_COMPACT     = "compact";
	/**
   * A constant equal to: {@value}
   * @since 5.1.0
   */
	public static final String PL_COOKIES     = "cookies";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this param is {@code true}.
	 * @since 1.0.0
	 */
	public static final String PL_COUNT       = "count";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this param is {@code false}.
	 * @since 2.0.0
	 */
	public static final String PL_COUNTONLY   = "countOnly";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0
	 */
	public static final String PL_CONVERTER   = "converter";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this parameter is {@link YADARequest#DEFAULT_DELIMITER}
	 * @since 4.0.0
	 */
	public static final String PL_DELIMITER   = "delimiter";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String PL_EXPORT      = "export";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String PL_EXPORTLIMIT = "exportlimit";
	/**
	 * A constant equal to: {@value}
   * This is a global parameter.
	 * The default value associated to this parameter is {@code null}
	 */
	public static final String PL_FILTERS     = "filters";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this parameter is {@link YADARequest#FORMAT_JSON}
	 */
	public static final String PL_FORMAT      = "format";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this parameter is {@code null}
	 * @since 4.0.0
	 */
	public static final String PL_HARMONYMAP  = "harmonyMap";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String PL_HTTPHEADERS = "HTTPHeaders";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String PL_JSONPARAMS  = "JSONParams";
	/**
   * A constant equal to: {@value}
   * @since 6.2.0
   */
  public static final String PL_JOIN        = "join";
  /**
   * A constant equal to: {@value}
   * @since 6.2.0
   */
  public static final String PL_LEFTJOIN    = "leftJoin";
	/**
	 * A constant equal to: {@value}
	 */
	//TODO There is no alias for PL_LABELS
	public static final String PL_LABELS      = "labels";
	/**
	 * A constant equal to: {@value}
	 */
//TODO There is no alias for PL_MAIL
	public static final String PL_MAIL        = "mail";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this parameter is {@link YADARequest#METHOD_GET}
	 * No longer deprecated (4.0.0) as of 8.5.0
	 */
	public static final String PL_METHOD      = "method";
	/**
	 * A constant equals to: {@value}.
	 * 
	 * @since 8.7.0
	 */
	public static final String PL_OAUTH       = "oauth";
	/**
	 * A constant equal to: {@value}.  Use {@link YADARequest#PL_BYPASSARGS} instead.
	 * @deprecated as of 4.0.0
	 */
	@Deprecated
	public static final String PL_OVERARGS    = "overargs";
	/**
	 * A constant equal to: {@value}.  Defaults to {@link YADARequest#DEFAULT_START_PAGE}
	 */
	public static final String PL_PAGE        = "page";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this param is {@link YADARequest#DEFAULT_PAGE_SIZE}
	 * @since 1.0.0
	 */
	public static final String PL_PAGESIZE    = "pagesize";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this param is {@link YADARequest#DEFAULT_START_PAGE}
	 * @since 1.0.0
	 */
	public static final String PL_PAGESTART   = "pagestart";
	/**
	 * A constant equal to: {@value}
	 * @since 1.0.0
	 */
	public static final String PL_PARAMS      = "params";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String PL_PARAMSET    = "paramset";
	/**
	 * A constant equal to: {@value}
	 */
  //TODO There is no alias for PL_PATH
	public static final String PL_PATH        = "path";
	/**
	 * @deprecated as of 4.0.0
	 */
  //TODO There is no alias for PL_LABELS
	@Deprecated
	public static final String PL_PARALLEL    = "parallel";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String PL_PLUGIN      = "plugin";
	/**
	 * A constant equal to: {@value}
	 * @deprecated as of 4.0.0
	 */
	@Deprecated
	public static final String PL_PLUGINTYPE  = "plugintype";
	/**
	 * A constant equal to: {@value}
	 * @deprecated as of 7.1.0
	 */
	@Deprecated
	public static final String PL_POSTARGS    = "postargs";
	/**
	 * A constant equal to: {@value}
	 * @deprecated as of 7.1.0
	 */
	@Deprecated
	public static final String PL_PREARGS     = "preargs";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String PL_PRETTY      = "pretty";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0
	 */
	public static final String PL_PROTOCOL    = "protocol";
	/**
	 * A constant equal to: {@value}
	 * @since 4.1.0
	 */
	public static final String PL_PROXY       = "proxy";
	/**
	 * Required.
	 * A constant equal to: {@value}
	 * @since 1.0.0
	 */
	public static final String PL_QNAME       = "qname";
	/**
	 * A constant equal to: {@value}
	 * @since 4.0.0
	 */
	public static final String PL_RESPONSE    = "response";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this parameter is {@link YADARequest#DEFAULT_ROW_DELIMITER};
	 * @since 4.0.0
	 */
	public static final String PL_ROW_DELIMITER = "rowDelimiter";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value is {@code null}
	 * @since 1.0.0
	 */
	public static final String PL_SORTKEY     = "sortkey";
	/**
	 * A constant equal to: {@value}
	 * This is a global parameter.
	 * The default value associated to this param is {@link YADARequest#SORT_ASC}
	 * @since 1.0.0
	 */
	public static final String PL_SORTORDER   = "sortorder";
	/**
	 * A constant equal to: {@value}
	 * @since 5.0.0
	 */
	public static final String PL_UPDATE_STATS = "updateStats";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String PL_USER        = "user";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String PL_VIEWLIMIT   = "viewlimit";
	
	// utility vars
	/**
	 * The calling url
	 */
	private String               referer;
	/**
	 * The request object provided by Tomcat
	 */
	private HttpServletRequest   request;
	/**
	 * The map of parameters derived from the request
	 */
	private Map<String,String[]> parameterMap = new LinkedHashMap<>();
	/**
	 * A thesaurus for long and short parameter name synonyms
	 */
	private static final Map<String,String> fieldAliasMap;
	
	static {
		Map<String,String> map = new HashMap<>();
		map.put(PS_ARGS,PL_ARGS);
		map.put(PS_BYPASSARGS, PL_BYPASSARGS);
		map.put(PS_COUNT,PL_COUNT);
		map.put(PS_COUNTONLY,PL_COUNTONLY);
		map.put(PS_COMPACT,PL_COMPACT);
		map.put(PS_COMMITQUERY, PL_COMMITQUERY);
		map.put(PS_CONVERTER, PL_CONVERTER);
		map.put(PS_COOKIES, PL_COOKIES);
		map.put(PS_DELIMITER, PL_DELIMITER);
		map.put(PS_ROW_DELIMITER, PL_ROW_DELIMITER);
		map.put(PS_EXPORT,PL_EXPORT);
		map.put(PS_EXPORTLIMIT,PL_EXPORTLIMIT);
		map.put(PS_FILTERS,PL_FILTERS);
		map.put(PS_FORMAT,PL_FORMAT);
		map.put(PS_HARMONYMAP,PL_HARMONYMAP);
		map.put(PS_HTTPHEADERS,PL_HTTPHEADERS);
		map.put(PS_JSONPARAMS,PL_JSONPARAMS);
		map.put(PS_JOIN, PL_JOIN);
		map.put(PS_LEFTJOIN, PL_LEFTJOIN);
		map.put(PS_METHOD,PL_METHOD);
		map.put(PS_OAUTH,PL_OAUTH);
		map.put(PS_OVERARGS,PL_OVERARGS);
		map.put(PS_PAGE,PL_PAGE);
		map.put(PS_PAGESIZE,PL_PAGESIZE);
		map.put(PS_PAGESTART,PL_PAGESTART);
		map.put(PS_PARAMS,PL_PARAMS);
		map.put(PS_PARAMSET,PL_PARAMSET);
		map.put(PS_PLUGIN,PL_PLUGIN);
		map.put(PS_PLUGINTYPE,PL_PLUGINTYPE);
		map.put(PS_POSTARGS,PL_POSTARGS);
		map.put(PS_PREARGS,PL_PREARGS);
		map.put(PS_PRETTY, PL_PRETTY);
		map.put(PS_PROTOCOL,PL_PROTOCOL);
		map.put(PS_PROXY,PL_PROXY);
		map.put(PS_QNAME,PL_QNAME);
		map.put(PS_RESPONSE, PL_RESPONSE);
		map.put(PS_SORTKEY,PL_SORTKEY);
		map.put(PS_SORTORDER,PL_SORTORDER);
		map.put(PS_UPDATE_STATS, PL_UPDATE_STATS);
		map.put(PS_USER,PL_USER);
		map.put(PS_VIEWLIMIT,PL_VIEWLIMIT);
		map.put(PL_ARGS,PL_ARGS);
		map.put(PL_BYPASSARGS,PL_BYPASSARGS);
		map.put(PL_COLHEAD,PL_COLHEAD);
		map.put(PL_COMPACT,PL_COMPACT);
		map.put(PL_COMMITQUERY, PL_COMMITQUERY);
		map.put(PL_DELIMITER, PL_DELIMITER);
		map.put(PL_ROW_DELIMITER, PL_ROW_DELIMITER);
		map.put(PL_COUNT,PL_COUNT);
		map.put(PL_COUNTONLY,PL_COUNTONLY);
		map.put(PL_CONVERTER, PL_CONVERTER);
		map.put(PL_COOKIES, PL_COOKIES);
		map.put(PL_EXPORT,PL_EXPORT);
		map.put(PL_EXPORTLIMIT,PL_EXPORTLIMIT);
		map.put(PL_FILTERS,PL_FILTERS);
		map.put(PL_FORMAT,PL_FORMAT);
		map.put(PL_HARMONYMAP,PL_HARMONYMAP);
		map.put(PL_HTTPHEADERS,PL_HTTPHEADERS);
		map.put(PL_JSONPARAMS,PL_JSONPARAMS);
		map.put(PL_JOIN, PL_JOIN);
    map.put(PL_LEFTJOIN, PL_LEFTJOIN);
		map.put(PL_LABELS,PL_LABELS);
		map.put(PL_MAIL,PL_MAIL);
		map.put(PL_METHOD,PL_METHOD);
		map.put(PL_OAUTH,PL_OAUTH);
		map.put(PL_OVERARGS,PL_OVERARGS);
		map.put(PL_PAGE,PL_PAGE);
		map.put(PL_PAGESIZE,PL_PAGESIZE);
		map.put(PL_PAGESTART,PL_PAGESTART);
		map.put(PL_PARAMS,PL_PARAMS);
		map.put(PL_PARAMSET,PL_PARAMSET);
		map.put(PL_PATH,PL_PATH);
		map.put(PL_PARALLEL,PL_PARALLEL);
		map.put(PL_PLUGIN,PL_PLUGIN);
		map.put(PL_PLUGINTYPE,PL_PLUGINTYPE);
		map.put(PL_POSTARGS,PL_POSTARGS);
		map.put(PL_PREARGS,PL_PREARGS);
		map.put(PL_PRETTY,PL_PRETTY);
		map.put(PL_PROTOCOL,PL_PROTOCOL);
		map.put(PL_PROXY, PL_PROXY);
		map.put(PL_RESPONSE, PL_RESPONSE);
		map.put(PL_QNAME,PL_QNAME);
		map.put(PL_SORTKEY,PL_SORTKEY);
		map.put(PL_SORTORDER,PL_SORTORDER);
		map.put(PL_UPDATE_STATS, PL_UPDATE_STATS);
		map.put(PL_USER,PL_USER);
		map.put(PL_VIEWLIMIT,PL_VIEWLIMIT);
		fieldAliasMap = Collections.unmodifiableMap(map);
	}
	
	// URL Parameters
	/**
	 * Flag indicating whether or not to include the column header in delimited output. Defaults to {@code false}.
	 */
	private boolean    colhead      = false;
	/**
	 * Flag indicating whether or not to use compact notation in json results. Defaults to {@code false}
	 * @since PROVISIONAL
	 */
	private boolean    compact      = false;
	/**
	 * Flag indicating whether or not to execute JDBC commits after each query execution. Defaults to {@code false} (request-level commit)
	 */
	private boolean    commitQuery  = false;
	/**
	 * Flag indicating whether or not to execute a second query to return the total number results  
	 */
	private boolean    count        = true;  
	/**
	 * Flag indicating whether or not to execute ONLY the count query, and ignore the data query
	 */
	private boolean    countOnly    = false;
	/**
	 * The character for separating columns in delimited output 
	 */
	private String     delimiter    = DEFAULT_DELIMITER;
	/**
	 * Flag indicating whether or not to automatically dump results to delimited file, and return url to client. Defaults to {@code false}
	 */
	private boolean    export       = false;
	/**
	 * Maxium number of rows to include in export. Defaults to {@code -1}
	 */
	private int        exportLimit  = -1;
	/**
	 * Parsed filter criteria string.
	 */
	private JSONObject filters;
	/**
	 * Response format. Defaults to {@link #FORMAT_JSON}
	 */
	private String     format 	 	  = FORMAT_JSON;
	/**
	 * Parsed harmonyMap string
	 */
	private JSONArray harmonyMap;
	/**
	 * parsed HTTPHeaders string 
	 * @since 8.5.0
	 */
	private JSONObject httpHeaders;
	/**
	 * Flag indicating something about labels.
	 * @since PROVISIONAL
	 */
	private boolean    labels       = true;
	/**
	 * Mail specification
	 */
	private String     mail;
	/**
	 * YADA execution method. Defaults to {@link #METHOD_GET}
	 * @since 1.0.0, deprecated in v4.0.0, un-deprecated in 8.5.0 to support different HTTP methods for {@link com.novartis.opensource.yada.adaptor.RESTAdaptor}
	 */
	private String     method 	 	  = METHOD_GET;
	/**
	 * Number of rows to return.  Defaults to {@link #DEFAULT_PAGE_SIZE}
	 */
	private int        pageSize 	  = DEFAULT_PAGE_SIZE;
	/**
	 * The "page" subset of results to return (first record = {@code pageSize} * {@code pageStart}). Defaults to {@link #DEFAULT_START_PAGE} 
	 */
	private int        pageStart 	  = DEFAULT_START_PAGE;
	/**
	 * Flag indicating whether to process count query in separate thread.
	 * @deprecated as of 4.0.0: Never called.
	 */
	@Deprecated
	private boolean    parallel     = false;
	/**
	 * Container for query data parameter values.
	 */
	private String[][] params		    = null;
	/**
	 * Name of stored parameter set.
	 * @since PROVISIONAL
	 */
	private String     paramset;
	/**
	 * Filesystem location of target. Used by {@link FileSystemAdaptor}
	 */
	private String     path;
	/**
	 * Class name (default package,) or fully-qualified class name of plugin.  Type is auto-detected.
	 */
	private String[]   plugin 	    = null;
	/**
	 * Plugin class type, either {@link #PREPROCESS}, {@link #POSTPROCESS}, or {@link #BYPASS}
	 * @deprecated as of 4.0.0
	 */
	@Deprecated
	private String[]   pluginType   = {PREPROCESS};
	/**
	 * Flag indicating whether or not to pretty print JSON results
	 */
	private boolean    pretty       = false;
	/**
	 * Connection mode of query, defaults to {@link Parser#JDBC}
	 */
	private String     protocol     = Parser.JDBC;
	/**
	 * The proxy server to use for external REST queries
	 */
	private String     proxy        = null;
	/**
	 * A JSON object containing oauth key/value pairs needed for authenticated requests
	 * @since 8.7.0 
	 */
	private JSONObject oauth        = null;
	/**
	 * The name of the query to be executed, when coupled with {@link #params}, or {@link #plugin}.  Defaults to {@link #DEFAULT_QNAME}
	 */
	private String     qname 		    = DEFAULT_QNAME;
	/**
	 * The class name or fully-qualified classname of the {@link com.novartis.opensource.yada.format.Response} object desired to be returned.  Default is 
	 * determined dynamically using {@link #format}.
	 */
	private String     response;
	/**
   * The class name or fully-qualified classname of the {@link com.novartis.opensource.yada.format.Converter} object desired to be used to format data results.  Default is 
	 * determined dynamically using {@link #format} and {@link #protocol}
	 */
	private String     converter;
	/**
	 * The character for separating rows in delimited output. Defaults to {@link #DEFAULT_ROW_DELIMITER}
	 */
	private String     rowDelimiter = DEFAULT_ROW_DELIMITER;
	/**
	 * The column on which to sort results
	 */
	private String     sortKey;
	/**
	 * The order of sorted results.  Defaults to {@link #SORT_ASC}
	 */
	private String     sortOrder   	= SORT_ASC;
	/**
	 * Flag indicating whether or not the {@link Finder} should execute a parallel op to update the query access count and date  
	 */
	private boolean    updateStats = true;  
	/**
	 * The id of the requesting user.  Defaults to {@link #DEFAULT_USER}
	 */
	private String     user 		    = DEFAULT_USER;
	/**
	 * The maximum number of rows to return, when distinct from {@link #exportLimit} or {@link #pageSize}.  This is a performance enhancer for very large data sets.
	 */
	private int        viewLimit 	  = -1;
	/**
	 * A data structure for storing json params values
	 * @deprecated as of 4.0.0
	 */
	@Deprecated
	private Map<String,List<Map<String,String>>> 
					   JSONParams           = new LinkedHashMap<>();
	/**
	 * A data structure for storing json params values
	 */
	private JSONParams jsonParams;
	/**
	 * A boolean or comma-separated list of names to specify on which fields to join multiple result sets
	 * @since 6.2.0
	 */
	private String join = "";
	/**
   * A boolean or comma-separated list of names to specify on which fields to join multiple result sets
   * @since 6.2.0
   */
  private String leftJoin = "";
	/**
	 * The list of cookies names passed in the request
	 * @since 5.1.0
	 */
	private List<String> cookies    = new ArrayList<>();
	/**
	 * A data structure for storing content and form field values when uploading a file
	 */
	private List<FileItem> uploadItems;
	/**
	 * Plugin arguments
	 * @since 7.1.0
	 */
	private List<List<String>> argLists = new ArrayList<>();
	/**
	 * Plugin arguments
   */
	private List<String>   args 	  = new ArrayList<>();
	/**
	 * Preprocessor plugin arguments
   * @deprecated as of 7.1.0
   */
  @Deprecated
	private List<String>   preArgs 	= new ArrayList<>();
	/**
	 * Postprocessor plugin arguments
   * @deprecated as of 7.1.0
   */
  @Deprecated
	private List<String>   postArgs = new ArrayList<>();
	/**
	 * Bypass plugin arguments
   * @deprecated as of 7.1.0
   */
  @Deprecated
	private List<String>   bypassArgs = new ArrayList<>();
	
	/**
	 * Default constructor 
	 */
	public YADARequest() {	}
	
	/**
	 * Calls {@link #invokeSetter(String, String)} internally to dynamically map all request parameters
	 * @param paramList a list of parameters to set in this configuration object
	 * @throws YADARequestException when there is a method invocation problem related to a parameter
	 */
	public YADARequest(List<YADAParam> paramList) throws YADARequestException {
		for(YADAParam param : paramList)
		{
			this.invokeSetter(param.getName(), param.getValue());
		}
	}
	
	/**
	 * Generates a {@link java.util.List} of {@link YADAParam} objects from request parameter values.  All objects 
	 * are qualified as {@link YADAParam#QUERY}-targeted (as opposed to {@link YADAParam#APP}-targeted), 
	 * and {@link YADAParam#OVERRIDEABLE}
	 * 
	 * @return a list of parameter objects, all overrideable, and query-level, derived from request parameter values
	 * @throws YADARequestException when there is a mal
	 */
	public List<YADAParam> getAllParams() throws YADARequestException
	{
		List<YADAParam> lParams = new ArrayList<>();
		for (Iterator<String> iterator = fieldAliasMap.keySet().iterator(); iterator.hasNext();)
		{
			String key = iterator.next();
			String val = (String) invokeGetter(key);
			YADAParam param = new YADAParam(key,val,YADAParam.QUERY,YADAParam.OVERRIDEABLE);
			lParams.add(param);
		}
		return lParams;
	}
	
	/**
	 * This is an alias for {@link #getRequestParamsForQueries()}
	 * @return a {@link java.util.List} of {@link YADAParam}s which will be applied to the entire request
	 * @throws YADAQueryConfigurationException when a malformed parameter is encountered
	 * @see #getRequestParamsForQueries()
	 */
	public List<YADAParam> getGlobalParamsForQueries() throws YADAQueryConfigurationException
	{
		return getRequestParamsForQueries();
	}
	
	
	/**
	 * Returns a list with the following parameters:
	 * <ul>
	 *   <li>{@link #PS_COUNT}</li>
	 *   <li>{@link #PS_COUNTONLY}</li>
	 *   <li>{@link #PS_DELIMITER}</li>
	 *   <li>{@link #PS_FILTERS}</li>
	 *   <li>{@link #PS_FORMAT}</li>
	 *   <li>{@link #PS_HARMONYMAP}</li>
	 *   <li>{@link #PS_METHOD} (for backward compatibility)</li>
	 *   <li>{@link #PS_OAUTH}</li>
	 *   <li>{@link #PS_PAGESIZE}</li>
	 *   <li>{@link #PS_PAGESTART}</li>
	 *   <li>{@link #PS_ROW_DELIMITER}</li>
	 *   <li>{@link #PS_SORTKEY}</li>
	 *   <li>{@link #PS_SORTORDER}</li>
	 * </ul>
	 * @return a {@link java.util.List} of {@link YADAParam}s which will be applied to the entire request
	 * @throws YADAQueryConfigurationException when a malformed parameter is encountered
	 */
	public List<YADAParam> getRequestParamsForQueries() throws YADAQueryConfigurationException
	{
		List<YADAParam> lParams = new ArrayList<>();
		JSONObject jobj = new JSONObject();
		try
		{
			jobj.put(PS_OAUTH, getOAuth());
			jobj.put(PS_COUNT, getCount());
			jobj.put(PS_FILTERS, getFilters());
			jobj.put(PS_PAGESIZE, getPageSize());
			jobj.put(PS_PAGESTART, getPageStart());
			jobj.put(PS_SORTKEY, getSortKey());
			jobj.put(PS_SORTORDER, getSortOrder());
			jobj.put(PS_METHOD, getMethod());
			jobj.put(PS_FORMAT, getFormat());
			jobj.put(PS_DELIMITER, getDelimiter());
			jobj.put(PS_ROW_DELIMITER, getRowDelimiter());
			jobj.put(PS_HARMONYMAP, getHarmonyMap());
			jobj.put(PS_COUNTONLY, getCountOnly());
			jobj.put(PS_UPDATE_STATS, getUpdateStats());
			jobj.put(PS_JOIN, getJoin());
			jobj.put(PS_LEFTJOIN, getLeftJoin());
	
			for(String key : JSONObject.getNames(jobj))
			{
				if(key != null && String.valueOf(jobj.get(key)) != null)
				{
					YADAParam param = new YADAParam(key, 
					                                String.valueOf(jobj.get(key)), 
					                                YADAParam.QUERY, 
					                                YADAParam.OVERRIDEABLE);
					lParams.add(param);
				}
			}
		}
		catch(JSONException e)
		{
			String msg = "Unable to create global parameters list.";
			throw new YADAQueryConfigurationException(msg,e);
		}
		return lParams;
	}
	
	/**
	 * Uses a parameter name fragment to retrieve the desired parameter value regardless of whether a short or long 
	 * parameter name was used in the request.
	 * 
	 * @param q list of parameter objects
	 * @param frag fragment of parameter name to retrieve
	 * @return the value of the desired parameter
	 * @throws YADAQueryConfigurationException when a parameter containing {@code frag} cannot be found
	 */
	public static String getParamValueForKey(List<YADAParam> q, String frag) throws YADAQueryConfigurationException 
	{
		for (YADAParam param : q)
		{
			try
			{
				JSONObject j = new JSONObject();
				j.put(param.getName(),param.getValue());
				String key = getParamValueForKey(j,frag);
				if(param.getName().equals(key))
					return param.getValue();
			} 
			catch (JSONException e)
			{
				throw new YADAQueryConfigurationException(e.getMessage(),e);
			}
		}
		return null;
	}
		
	/**
	 * Uses a parameter name fragment to retrieve the desired parameter value regardless of whether a short or long 
	 * parameter name was used in the request.
	 * @param q a JSON object containing parameter object metadata and values
	 * @param frag a parameter name fragment
	 * @return the value of the desired parameter
	 * @throws YADAQueryConfigurationException when a parameter containing {@code frag} cannot be found 
	 */
	public static String getParamValueForKey(JSONObject q, String frag) throws YADAQueryConfigurationException {
		String PL  = "PL_";
		String PS  = "PS_";
		String keys[] = new String[] { PL+frag, PS+frag };
		for (String key : keys)
		{
			try 
			{
				String keyVal = (String) YADARequest.class.getField(key).get(null);
				if(q.has(keyVal))
				{
					try
					{
					  return q.getString(keyVal);
					}
					catch(JSONException e)
					{
					  return q.getJSONObject(keyVal).toString();
					}
				}
			} 
			catch (SecurityException e) 
			{
				throw new YADAQueryConfigurationException(e.getMessage(),e);
			} 
			catch (NoSuchFieldException e) 
			{
				throw new YADAQueryConfigurationException(e.getMessage(),e);
			} 
			catch (IllegalArgumentException e) 
			{
				throw new YADAQueryConfigurationException(e.getMessage(),e);
			} 
			catch (IllegalAccessException e) 
			{
				throw new YADAQueryConfigurationException(e.getMessage(),e);
			}
		}
		return null;
	}
	
	/**
	 * Returns the value of the constant {@code key}.  This is useful when processing {@link YADAUtils#PARAM_FRAGS}.
	 * @param key the name of a constant
	 * @return the value of the constant {@code key}
	 * @throws YADAQueryConfigurationException when {@code key} is not accessible for any reason.
	 */
	public static String getParamKeyVal(String key) throws YADAQueryConfigurationException 
	{
		String val = null;
		try
		{
			val = (String) YADARequest.class.getField(key).get(null); 
		}
		catch (SecurityException e) 
		{
			throw new YADAQueryConfigurationException(e.getMessage(),e);
		} 
		catch (NoSuchFieldException e) 
		{
			throw new YADAQueryConfigurationException(e.getMessage(),e);
		} 
		catch (IllegalArgumentException e) 
		{
			throw new YADAQueryConfigurationException(e.getMessage(),e);
		} 
		catch (IllegalAccessException e) 
		{
			throw new YADAQueryConfigurationException(e.getMessage(),e);
		}
		return val;
	}
	
	/**
	 * Gets the long parameter name mapped to the {@code shortcut} args
	 * @since 4.0.0
	 * @param shortcut a short parameter name, i.e., {@code PS_...}
	 * @return {@link String} Long parameter name alias mapped to {@code shortcut}
	 */
	private static String getFieldAlias(String shortcut) {
		return fieldAliasMap.get(shortcut);
	}
	
	/**
	 * Standard mutator for variable
	 * @param request the {@link javax.servlet.http.HttpServletRequest} object passed from the app server
	 */
	public void setRequest(HttpServletRequest request)
	{
		this.request = request;
	}
	
	/**
	 * Standard accessor for variable
	 * @return the {@link javax.servlet.http.HttpServletRequest} object passed from the app server
	 */
	public HttpServletRequest getRequest()
	{
		return this.request;
	}
	
	/* 
	 * Non params, utils
	 */
	/**
	 * Standard mutator for variable
	 * @param referer the referring page
	 */
	public void setReferer(String referer)
	{
		this.referer = referer;
		l.debug(getFormattedDebugString("referer", referer));
	}
	
	/**
	 * Standard accessor for variable
	 * @return the referring url
	 */
	public String getReferer()
	{
		return this.referer;
	}
	
	/**
	 * Standard mutator for variable.  Used by {@code yada.jsp} to pass the application context root to the framework 
	 * to assist with file uploads.
	 * @param path a portion of the requested url
	 */
	public void setPath(String path) {
		this.path = path;
		l.debug(getFormattedDebugString("path", path));
	}
	
	/**
	 * Standard accessor for variable
	 * @return the path value, usually the application context root as set in {@code yada.jsp}
	 */
	public String getPath() {
		return this.path;
	}
	
	/**
	 * Returns a json formatted string derived from {@link #getParameterMap()}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		l.debug(getParameterMap().toString());
		return new JSONObject(getParameterMap()).toString();
	}
	
	/**
	 * Adds a parameter to the parameter maps
	 * @param key the parameter name
	 * @param value the parameter value
	 */
	public void addToMap(String key, String[] value)
	{
		getParameterMap().put(key,value);
	}
	
	/**
	 * Uses java reflection to invoke the "getter" method associated to the {@code alias} arg.
	 * 
	 * @since 4.0.0
	 * @param alias the parameter name
	 * @return Object value of parameter corresponding to getter method 
	 * @throws YADARequestException when method invocation fails
	 */
	public Object invokeGetter(String alias) throws YADARequestException
	{
		// input
		String field = getFieldAlias(alias);
		// return value
		Object o = null;
		// list of methods
		Method[] methods = this.getClass().getMethods();
		// currently inquired method name
		String mName = "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
		// iterate over methods
		for (int i = 0; i < methods.length; i++) 
		{
			String methName = methods[i].getName();
			if(methName.equals(mName))
			{
				l.debug("methName:" + methName + ":" + mName);
				try 
				{
					o = methods[i].invoke(this, new Object[] {});
				} 
				catch (InvocationTargetException e) 
				{
					throw new YADARequestException(e.getMessage(),e);
				} 
				catch (IllegalAccessException e) 
				{
					throw new YADARequestException(e.getMessage(),e);
				}
			}
		}
		return o;
	}
	
	/**
	 * Uses java reflection to invoke the "setter" method associated to {@code alias}
	 * @since 4.0.0
	 * @param alias the parameter name
	 * @param value the parameter value(s)
	 * @throws YADARequestException when there is a method invocation problem with the setter for {@code alias} 
	 */
	public void invokeSetter(String alias, String[] value) throws YADARequestException
	{
		//input
		String field = getFieldAlias(alias);
		// list of methods
		Method[] methods = this.getClass().getMethods();
		// currently inquired method name
		String mName = "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
		// iterate over methods
		for (int i = 0; i < methods.length; i++) {
			String methName = methods[i].getName();
			
			if (methName.equals(mName) && methods[i].getParameterTypes()[0].isArray()) {
				l.debug("methName:" + methName + ":" + mName);
				try 
				{
					methods[i].invoke(this, new Object[] {value});
				} 
				catch (InvocationTargetException e) 
				{
					throw new YADARequestException(e.getMessage(),e);
				} 
				catch (IllegalAccessException e) 
				{
					throw new YADARequestException(e.getMessage(),e);
				}
			}
		}
	}
	
	/**
	 * Uses java reflection to invoke the "setter" method associated to {@code alias}
	 * @param alias the parameter name
	 * @param value the parameter value
	 * @throws YADARequestException when there is a method invocation problem with the setter for {@code alias}
	 */
	public void invokeSetter(String alias, String value) throws YADARequestException
	{
		//input
		String field = getFieldAlias(alias);
		// list of methods
		Method[] methods = this.getClass().getMethods();
		// currently inquired method name
		String mName = "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
		// iterate over methods
		for (int i = 0; i < methods.length; i++) {
			String[] arglist = new String[1];
			String methName = methods[i].getName();

			if (methName.equals(mName)) {
				l.debug("methName:" + methName + ":" + mName);
				arglist[0] = value;
				try 
				{
					if(methods[i].getParameterTypes()[0].isArray())
					{
						methods[i].invoke(this, (Object)arglist);
					}
					//l.debug("qname: " + getQname());
				} 
				catch (InvocationTargetException e) 
				{
					throw new YADARequestException(e.getMessage(),e);
				} 
				catch (IllegalAccessException e) 
				{
					throw new YADARequestException(e.getMessage(),e);
				}
			}
		}
	}
	
	/**
	 * Adds a value to the {@code #args} list
	 * @param arg the value to add to the list
   * @deprecated as of 7.1.0
   */
  @Deprecated
	public void addArg(String arg) {
		this.args.add(arg);
	}

	/**
	 * Adds a value to the {@code #preArgs} list
	 * @param arg the value to add to the list
   * @deprecated as of 7.1.0
   */
  @Deprecated
	public void addPreArg(String arg) {
		this.preArgs.add(arg);
	}

	/**
	 * Adds a value to the {@code #postArgs} list
	 * @param arg the value to add to the list
	 * @deprecated as of 7.1.0
	 */
	@Deprecated
	public void addPostArg(String arg) {
		this.postArgs.add(arg);
	}

	/**
   * Adds a value to the {@code #cookies} list
   * @param cookie the value to add to the list
   * @since 5.1.0
   */
  public void addCookie(String cookie) {
    this.cookies.add(cookie);
  }
  
	/**
	 * Adds a value to the {@code #bypassArgs} list
	 * @param arg the value to add to the list
	 * @deprecated use {@link #addBypassArg(String)}
	 */
	@Deprecated
	public void addOverArg(String arg) {
		this.bypassArgs.add(arg);
	}
	
	/**
	 * Adds a value to the {@code #bypassArgs} list
	 * @param arg the value to add to the list
   * @deprecated as of 7.1.0
   */
  @Deprecated
	public void addBypassArg(String arg) {
		this.bypassArgs.add(arg);
	}

	/*
	 * Parameter Setters
	 */
	
	/**
	 * Used internally by {@link Service}{@code .engage} methods to pin an 
	 * argument list at a specific index in {@link #getArgLists()} to 
	 * the plugin at that index in {@link #getPlugin()}.
	 * 
	 * <p><strong>Undeprecated</strong> in 7.1.0 to use internally.</p>
	 * @param args the list of args to pass to the plugin
	 */
	void setArgs(List<String> args) {
		this.args = args;
		if(args != null)
		  l.debug(getFormattedDebugString("args", args.toString()));
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with 
	 * {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * This method is used internally by {@link #invokeSetter(String,String[])} when args are passed in a query
	 * string to the api, as in some test classes. The method effectively mandates that only a single
	 * plugin will be handled, which is ok because the version 7.1.0 upgrade, which rendered this method
	 * as deprecated, is what effectively enabled multiple plugins. In other words, pre-7.1.0, multiple plugins 
	 * probably wouldn't work, so using the deprecated syntax shouldn't affect deprecated expectations. 
	 * @param argArr the array of args to pass to the plugin
	 * @deprecated as of 7.1.0
	 */
	@Deprecated
	public void setArgs(String[] argArr)
	{
	  
	  // this operation will compensate for url strings that place plugin parameters first and 
	  // argument parameters later, e.g.,
	  // q=YADA test SELECT&pl=com.novartis.opensource.yada.plugin.ScriptBypass&a=scriptPluginBypassTest.pl&c=false
	  // 
	  String[] lArgArr = argArr;
	  if(getPluginArgs().size() > 0)
	    getPluginArgs().set(0,new LinkedList<>(Arrays.asList(argArr[0].split(PARAM_DELIMITER))));
	  else
	  {
	    if(lArgArr.length == 1)
	      lArgArr = lArgArr[0].split(PARAM_DELIMITER);
	    addPluginArgs(new LinkedList<>(Arrays.asList(lArgArr)));
	  }
		l.debug(getFormattedDebugString("args", lArgArr.toString()));
	}
	
	/**
	 * Adds the {@link List} of plugin {@link String}-arguments to the {@link List} of {@link List}s
	 * @param args an indexed {@link List} of {@link String} objects containing the 
	 * @since 7.1.0
	 */
	public void addPluginArgs(List<String> args)
	{
	  getPluginArgs().add(args);  
	}
	
	/**
	 * Sets the column header flag.
	 * @deprecated As of YADA 4.0.0. Use {@link #setColhead(String[])} or {@link #setColHead(String[])}
	 * @param colhead flag for inclusion or exclusion of column headers in delimited responses
	 */
	@Deprecated
	public void setColhead(Boolean colhead) {
		this.colhead = colhead.booleanValue();
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}.
	 * Calls {@link #setColhead(String[])}. 
	 * @since 4.0.0
	 * @param colHead flag for inclusion or exclusion of column headers in delimited responses
	 */
	public void setColHead(String[] colHead) {
		this.setColhead(colHead);
	}

	/** 
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @since 4.0.0
	 * @param colhead flag for inclusion or exclusion of column headers in delimited responses
	 */
	public void setColhead(String[] colhead) {
		boolean b = Boolean.parseBoolean(colhead[0]); 
		if (b)
		{
			this.colhead = b;
		}
		l.debug(getFormattedDebugString("colhead", String.valueOf(this.colhead)));
	}
	
	/**
	 * Standard mutator for variable
	 * @deprecated As of YADA 4.0.0. Use {@link #setCompact(String[])}
	 * @param compact flag to indicate whether or not to return default json response with short keys
	 */
	@Deprecated
	public void setCompact(boolean compact) {
		this.compact = compact;
		l.debug(getFormattedDebugString("compact", String.valueOf(compact)));
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @since 4.0.0
	 * @param compact flag to indicate whether or not to return default json response with short keys
	 */
	public void setCompact(String[] compact) {
		boolean b = Boolean.parseBoolean(compact[0]); 
		if (b)
		{
			this.compact = b;
		}
		l.debug(getFormattedDebugString("compact", String.valueOf(this.compact)));
	}
	
	/**
   * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
   * @param cookies the array of cookie names to pass to the REST query
   * @since 5.1.0
   */
  public void setCookies(String[] cookies)
  {
    String[] lCook = cookies[0].split(PARAM_DELIMITER);
    if (lCook.length > 0)
    {
      for (String cook : lCook)
      {
        this.addCookie(cook);
      }
    }
    l.debug(getFormattedDebugString("args", lCook.toString()));
  }
	
	/**
	 * Standard mutatar for variable
	 * @deprecated As of YADA 4.0.0. Use {@link #setCount(String[])}
	 * @param count flag to indicate whether or not to execute the second query for counting results
	 */
	@Deprecated
	public void setCount(boolean count) {
		this.count = count;
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @since 4.0.0
	 * @param count flag to indicate whether or not to execute the second query for counting results
	 */
	public void setCount(String[] count) {
		boolean b = Boolean.parseBoolean(count[0]); 
		if (!b)
		{
			this.count = b;
		}
		l.debug(getFormattedDebugString("count", String.valueOf(this.count)));
	}
	
	/**
	 * Standard mutator for variable
	 * @deprecated As of YADA 4.0.0. Use {@link #setCountOnly(String[])}
	 * @param countOnly flag to indicate whether or not to skip the data query and execute only the count query
	 */
	@Deprecated
	public void setCountOnly(boolean countOnly) {
		this.countOnly = countOnly;
		l.debug(getFormattedDebugString("countOnly", String.valueOf(countOnly)));
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @since 4.0.0
	 * @param countOnly flag to indicate whether or not to skip the data query and execute only the count query
	 */
	public void setCountOnly(String[] countOnly) {
		
		boolean b = Boolean.parseBoolean(countOnly[0]); 
		if (b)
		{
			this.countOnly = b;
		}
		l.debug(getFormattedDebugString("countOnly", String.valueOf(this.countOnly)));
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @since 4.0.0
	 * @param commitQuery flag to indicate whether or not to commit after each query execution, or at the end of all query executions 
	 */
	public void setCommitQuery(String[] commitQuery) {
		
		boolean b = Boolean.parseBoolean(commitQuery[0]); 
		if (!b)
		{
			this.commitQuery = b;
		}
		l.debug(getFormattedDebugString("commitQuery", String.valueOf(this.commitQuery)));
	}
	
	/** 
	 * Standard mutator for variable
	 * @deprecated As of YADA 4.0.0. Use {@link #setExport(String[])}
	 * @param export flag to indicate whether or not to dump results to a file and return it's url to the client
	 */
	@Deprecated
	public void setExport(boolean export) {
		this.export = export;
		l.debug(getFormattedDebugString("export", String.valueOf(export)));
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @since 4.0.0
	 * @param export flag to indicate whether or not to dump results to a file and return it's url to the client
	 */
	public void setExport(String[] export) {
		boolean b = Boolean.parseBoolean(export[0]); 
		if (b)
		{
			this.export = b;
		}
		l.debug(getFormattedDebugString("export", String.valueOf(this.export)));
	}
	
	/**
	 * Standard mutator for variable
	 * @deprecated As of YADA 4.0.0. Use {@link #setExportLimit(String[])}
	 * @param exportLimit maximim number of results to export
	 */
	@Deprecated
	public void setExportLimit(int exportLimit) {
		this.exportLimit = exportLimit;
		l.debug(getFormattedDebugString("exportLimit", String.valueOf(this.exportLimit)));
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @since 4.0.0
	 * @param exportLimit maximim number of results to export
	 */
	public void setExportLimit(String[] exportLimit) {
		int i = new Integer(exportLimit[0]).intValue();
		if (i > -1)
		{
			this.exportLimit = i;
		}
		l.debug(getFormattedDebugString("exportLimit", String.valueOf(this.exportLimit)));
	}
	
	/**
	 * Mutator for variable
	 * @deprecated As of YADA 4.0.0.  Use {@link #setFilters(String[])}
	 * @param filters json object conforming to filter spec
	 */
	@Deprecated
	public void setFilters(JSONObject filters) {
		this.filters = filters;
		l.debug(getFormattedDebugString("filters", this.filters.toString()));
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @since 4.0.0
	 * @param filters json object conforming to filter spec
	 * @throws YADARequestException when {@code filters} contains a malformed json string
	 */
	public void setFilters(String[] filters) throws YADARequestException {
		try
		{
			this.filters = new JSONObject(filters[0]);
		}
		catch(JSONException e)
		{
			throw new YADARequestException(e.getMessage(),e);
		}
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * The implementation supports passage of a JSONObject or JSONArray. See the Harmony Map Specification for details.
	 * 
	 * @since 4.0.0
	 * @param harmonyMap json object conforming to harmony map spec
	 * @throws YADARequestException when {@code harmonyMap} contains a malformed json string
	 */
	public void setHarmonyMap(String[] harmonyMap) throws YADARequestException {
	  //TODO convert to ArrayList and LinkedHashMaps rather than JSONArray,JSONObject to preserve ordering
		try
		{
			this.harmonyMap = new JSONArray(harmonyMap[0]); 
		}
		catch(JSONException e)
		{
		  try
		  {
  		  this.harmonyMap = new JSONArray();
  		  this.harmonyMap.put(new JSONObject(harmonyMap[0]));
		  }
		  catch(JSONException e1)
		  {
		    throw new YADARequestException(e1.getMessage(),e1);
		  }
		}
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * Converts parameter string into {@link JSONObject}
	 * @param httpHeaders the {@link String} array originating in the {@link HttpServletRequest}
	 * @throws YADARequestException when the header string is malformed
	 * @since 8.5.0
	 */
	public void setHTTPHeaders(String[] httpHeaders) throws YADARequestException {
		String  hdrStr  = httpHeaders[0];
		Matcher m1      = Pattern.compile(RX_NOTJSON).matcher(hdrStr);
		Map<String,String> reqHeaders = new HashMap<String,String>();
		
		// api circumvents http request so check for null
		if(null != getRequest())
		{	@SuppressWarnings("unchecked")
		  Enumeration<String> hdrNames = getRequest().getHeaderNames();
			while(hdrNames.hasMoreElements())
			{
				String name = hdrNames.nextElement(); 
				reqHeaders.put(name, getRequest().getHeader(name));
			}
		}
		
		if (m1.matches()) // it's a list of header names
		{
			String[] hdrList = hdrStr.split(",");
			this.httpHeaders = new JSONObject();
			for(String name : hdrList)
			{
				this.httpHeaders.put(name, reqHeaders.get(name));
			}
		}
		else // it's a json object
		{			
			try
			{
				this.httpHeaders = new JSONObject(hdrStr);
				JSONArray names = this.httpHeaders.names();
				JSONArray vals = this.httpHeaders.toJSONArray(names);
				for(int i=0;i<vals.length();i++)
				{
					if(vals.optBoolean(i))
					{
						String name = names.getString(i);
						this.httpHeaders.put(name,reqHeaders.get(name));
					}
				}
			}
			catch(JSONException e)
			{
				String msg = "The HTTPHeaders specification is not valid JSON:\n\n"+httpHeaders[0];
				throw new YADARequestException(msg,e); 
		  }			
		}
		
	}

	/**
	 * Standard mutator for variable
	 * @deprecated As of YADA 4.0.0
	 * @param format the output type
	 */
	@Deprecated
	public void setFormat(String format) {
		this.format = format;
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * If {@code format} equals {@link #FORMAT_PIPE} or {@link #FORMAT_TSV}, then {@link #delimiter} will 
	 * be set with either {@link #FORMAT_PIPE_STRING} or {@link #FORMAT_TSV_STRING}, accordingly 
	 * @since 4.0.0
	 * @param format the output type
	 */
	public void setFormat(String[] format) {
		this.format = format[0];
		// reset delimiter string 
		if(this.format.equals(FORMAT_TSV) || this.format.equals(FORMAT_TAB))
			this.delimiter = FORMAT_TSV_STRING;
		else if(this.format.equals(FORMAT_PIPE) || this.format.equals(FORMAT_PSV))
			this.delimiter = FORMAT_PIPE_STRING;
		l.debug(getFormattedDebugString("format", this.format));
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @since 4.0.0
	 * @param delimiter the column separator for delimited output
	 */
	public void setDelimiter(String[] delimiter) {
		this.delimiter = delimiter[0];
		setFormat(new String[] { FORMAT_DELIMITED }); 
		l.debug(getFormattedDebugString("delimiter", this.delimiter));
		l.debug(getFormattedDebugString("format", this.format));
	}

	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @since 4.0.0
	 * @param rowDelimiter the line separator for delimited output
	 */
	public void setRowDelimiter(String[] rowDelimiter) {
		this.rowDelimiter = rowDelimiter[0];
		l.debug(getFormattedDebugString("rowDelimiter", this.rowDelimiter));
	}

	/**
	 * Useful for plugins, rather than being forced to construct strings
	 * to pass to its overloaded twin. 
	 * 
	 * The JSONArray must be an array of JSON objects which reflects the 
	 * following format:
	 * 
	 * [
	 *   {"qname":"query name 1",
	 *    "DATA":[{"field1":"value1","field2":"value2",..."field_n":"value_n"},...
	 *            {...}]
	 *   },
	 *   {"qname":"query name 2",
	 *    "DATA":[...]
	 *   },
	 *   ...
	 * ]
	 * @deprecated as of 4.0.0
	 * @param ja a json array conforming to the JSONParams spec
	 * @throws YADARequestException when {@code ja} contains a malformed json string
	 */
	@Deprecated
	public void setJSONParams(JSONArray ja) throws YADARequestException {
		setJSONParams(new String[] {ja.toString()});
	}
	
	/**
	 * The JSONArray must be an array of JSON objects which reflects the 
	 * following format:
	 * 
	 * [
	 *   {"qname":"query name 1",
	 *    "DATA":[{"field1":"value1","field2":"value2",..."field_n":"value_n"},...
	 *            {...}]
	 *   },
	 *   {"qname":"query name 2",
	 *    "DATA":[...]
	 *   },
	 *   ...
	 * ]
	 * @deprecated as of 4.0.0
	 * @since 4.0.0
	 * @param JSONParams an array containing a string conforming to the JSONParams specification
	 * @throws YADARequestException  when {@code JSONParams} contains a malformed json string
	 */
	@Deprecated
	public void setJSONParams(String[] JSONParams) throws YADARequestException {
		try 
		{
			JSONArray jsonArray = new JSONArray(JSONParams[0]);
			for (int i = 0; i < jsonArray.length(); i++) // multiple queries
			{
				// object with the query and data
				JSONObject query = jsonArray.getJSONObject(i); 
				String lQname    = query.getString(JSON_KEY_QNAME); // query name 
				if (!this.JSONParams.containsKey(lQname)) 
				{
					this.JSONParams.put(lQname,
							// store the query and a structure for data
							new ArrayList<Map<String, String>>()); 
				}
				
				// multiple rows of data
				JSONArray rows = query.getJSONArray(JSON_KEY_DATA); 

				for (int j = 0; j < rows.length(); j++) 
				{
					JSONObject row = rows.getJSONObject(j); // row
					Iterator<?> iter = row.keys();
					Map<String, String> dataForRow = new HashMap<>();
					while (iter.hasNext()) 
					{
						String column = ((String) iter.next()).toUpperCase();
						String value = ""; 
						JSONArray valIsArray;
						try
						{
							valIsArray = row.getJSONArray(column);
							for (int k=0;k<valIsArray.length();k++)
							{
								value += valIsArray.getString(k);
								if (k < valIsArray.length() -1)
								{
									value += ",";
								}
							}
							l.debug("JSONArray passed in is now ["+value+"]");
						}
						catch(JSONException e)
						{
							value = row.getString(column);
						}
						dataForRow.put(column, value);
					}
					// store the col/val hash in the arraylist for the qname
					this.JSONParams.get(lQname).add(dataForRow); 
				}
			}
			l.debug(getFormattedDebugString("JSONParams",jsonArray.toString()));
		} 
		catch (JSONException e) 
		{
			throw new YADARequestException(e.getMessage(),e);
		}
		
	}
	
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @param jp json string compliant with the JSONParams specification
	 * @throws YADARequestException when the JSONParams string passed in the request is malformed  
	 * @since 4.0.0
	 * @see com.novartis.opensource.yada.JSONParams
	 */
	public void setJsonParams(String[] jp) throws YADARequestException
	{
		try
		{
			this.jsonParams = new JSONParams(jp);
		}
		catch(JSONException|YADAQueryConfigurationException e)
		{
			String msg = "The JSONParams string appears to be malformed.";
			throw new YADARequestException(msg, e);
		}
	}
	
	/**
	 * @since 4.0.0
	 * @param jp the configuration object
	 */
	public void setJsonParams(JSONParams jp)
	{
		this.jsonParams = jp;
	}
	
	
	/**
	 * @deprecated As of YADA 4.0.0
	 * @param labels {@code true} to include labels
	 */
	@Deprecated
	public void setLabels(boolean labels) {
		this.labels = labels;
		l.debug(getFormattedDebugString("labels", String.valueOf(labels)));
	}
	
	/**
	 * @since 4.0.0
	 * @param labels <code>{"true"}</code> to include labels
	 */
	public void setLabels(String[] labels) {
		boolean b = Boolean.parseBoolean(labels[0]); 
		if (b)
		{
			this.labels = b;
		}
		l.debug(getFormattedDebugString("labels", String.valueOf(this.labels)));
	}
	
	/**
	 * @deprecated As of YADA 4.0.0
	 * @param mail the mail spec
	 */
	@Deprecated
	public void setMail(String mail) {
		this.mail = mail;
		l.debug(getFormattedDebugString("mail", mail));
	}
	
	/**
	 * @since 4.0.0
	 * @param mail the mail spec
	 */
	public void setMail(String[] mail) {
		this.mail = mail[0];
		l.debug(getFormattedDebugString("mail", this.mail));
	}
	
	/**
	 * @deprecated As of YADA 4.0.0
	 * @param method the yada request type
	 */
	@Deprecated
	public void setMethod(String method) {
		this.method = method;
	}
	
	/**
	 * @since 4.0.0
	 * @param method the yada request type
	 */
	public void setMethod(String[] method) {
		this.method = method[0];
		l.debug(getFormattedDebugString("method", this.method));
	}
	
	/**
	 * @since 8.7.0
	 * @param oauth oauth parameters
	 * @throws YADARequestException if the parameter string is not malformed
	 */
	public void setOAuth(String[] oauth) throws YADARequestException
	{
		try
		{
			this.oauth = new JSONObject(oauth[0]);
		}
		catch(JSONException e)
		{
			String msg = "The OAuth parameter JSON string appears to be malformed.";
			throw new YADARequestException(msg, e);
		}
	}
	
	/**
	 * @since 4.0.0
	 * @param argArr bypass plugin arguments
   * @deprecated as of 7.1.0
   */
  @Deprecated
	public void setBypassargs(String[] argArr)
	{
		if (argArr.length > 0 && this.plugin == null)
		{
			setPlugin(new String[] {SCRIPT_BYPASS});
		}
		String[] lArgs = argArr[0].split(PARAM_DELIMITER);
		if (lArgs.length > 0)
		{
			for (String arg : lArgs)
			{
				this.addBypassArg(arg);
			}
		}
		l.debug(getFormattedDebugString("bypassargs", lArgs.toString()));
	}
	
	/**
	 * @deprecated As of YADA 4.0.0
	 * @param pretty flag indicating whether or not to pretty print JSON results
	 */
	@Deprecated
	public void setPretty(boolean pretty) {
		this.pretty = pretty;
		l.debug(getFormattedDebugString("pretty", String.valueOf(pretty)));
	}
	
	/**
	 * @since 4.0.0
	 * @param protocol adaptor type
	 */
	public void setProtocol(String[] protocol) {
		this.protocol = protocol[0];
		l.debug(getFormattedDebugString("protocol", this.protocol));
	}
	
	/**
	 * @param proxy the proxy to set
	 * @since 4.1.0
	 */
	public void setProxy(String[] proxy)
	{
		this.proxy = proxy[0];
		l.debug(getFormattedDebugString("proxy", this.proxy));
	}

	
	/**
	 * @since 4.0.0
	 * @param pretty flag indicating whether or not to pretty print JSON results
	 */
	public void setPretty(String[] pretty) {
		boolean b = Boolean.parseBoolean(pretty[0]); 
		if (b)
		{
			this.pretty = b;
		}
		l.debug(getFormattedDebugString("pretty", String.valueOf(this.pretty)));
	}
	
	/**
	 * Alias for setPageStart
	 * @since 4.0.0
	 * @param page the subset of results
	 */
	public void setPage(String[] page) {
		this.setPageStart(page);
	}
	
	/**
	 * @deprecated As of YADA 4.0.0
	 * @param pageSize the number of result "rows" to return
	 */
	@Deprecated
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	/**
	 * @since 4.0.0
	 * @param pageSize the number of result "rows" to return
	 */
	public void setPageSize(String[] pageSize) {
		int i = new Integer(pageSize[0]).intValue();
		if (i != YADARequest.DEFAULT_PAGE_SIZE)
		{
			if(i == -1)
			{
				this.pageSize = MAX_PAGE_SIZE;
			}
			else
			{
				this.pageSize = i;
			}
		}
		l.debug(getFormattedDebugString("pageSize", String.valueOf(this.pageSize)));
	}
	
	/**
	 * @deprecated As of YADA 4.0.0
	 * @param pageStart the subset of results
	 */
	@Deprecated
	public void setPageStart(int pageStart) {
		this.pageStart = pageStart;
		l.debug(getFormattedDebugString("pageStart", String.valueOf(pageStart)));
	}
	
	/**
	 * @since 4.0.0
	 * @param pageStart the subset of results
	 */
	public void setPageStart(String[] pageStart) {
		int i = new Integer(pageStart[0]).intValue();
		if (i != YADARequest.DEFAULT_START_PAGE)
		{
			this.pageStart = i;
		}
		l.debug(getFormattedDebugString("pageStart", String.valueOf(this.pageStart)));
	}
	
	/**
	 * @deprecated as of 4.0.0
	 * @param parallel flag indicating whether or not to execute the count query in a separate thread
	 */
	@Deprecated
	public void setParallel(boolean parallel) {
		this.parallel = parallel;
		l.debug(getFormattedDebugString("parallel", String.valueOf(parallel)));
	}
	
	/**
	 * @since 4.0.0
	 * @param parallel flag indicating whether or not to execute the count query in a separate thread
	 * @deprecated as of 4.0.0
	 */
	@Deprecated
	public void setParallel(String[] parallel) {
		boolean b = Boolean.parseBoolean(parallel[0]); 
		if (b)
		{
			this.parallel = b;
		}
		
		l.debug(getFormattedDebugString("parallel", String.valueOf(parallel)));
	}
	
	/**
	 * @since 1.0.0
	 * @param params list of data values to plug into query parameters
	 */
	public void setParams(String[] params) {
		char[] p = params[0].toCharArray(); // the values passed in
		List<String[]> paramList = new ArrayList<>();
		StringBuffer param     = new StringBuffer(), 
								 array     = null;
		for(int i=0;i<p.length;i++)
		{
			char c = p[i];
			// first char of array param
			if(c == '[')  
			{
				// create the array
				array = new StringBuffer(); 
			}
			// last char of array param
			else if(c == ']')
			{
				// add the current param to the list
				if(array != null)
				{
					paramList.add(array.toString().split(","));
					array = null;
				}
			}
			// just a comma, and not in an array
			else if(c == ',' && array == null && param.length() > 0)
			{
			  // add the current param to the list
				paramList.add(new String[] {param.toString()});
				// reset the param to null
				param = new StringBuffer();
			}
			// in the array
			else if(array != null)
			{
				// add the char to the stringbuffer
				array.append(c);
			}
			else if(c != ',')
			{
				// add the char to the stringbuffer
				param.append(c);
				// if end of param string
				if(i == p.length-1)
				{
					// add the current param to the list
					paramList.add(new String[] {param.toString()});
				}
			}
		}
		this.params = new String[paramList.size()][];
		for (int j = 0; j < paramList.size(); j++)
		{
			this.params[j] = paramList.get(j);
		}
//		Pattern ARRAY_RX = Pattern.compile("([^\\[\\]],[^\\[\\]]+)+"); // this pattern matches the contents inside brackets	
		l.debug(getFormattedDebugString("params", Arrays.toString(params)));
	}
	
	/**
	 * @since PROVISIONAL
	 * @param paramset a stored set of parameters
	 */
	public void setParamset(String[] paramset) {
		l.debug(paramset[0]);
		this.paramset = paramset[0];
		l.debug(getFormattedDebugString("paramset", this.paramset));
	}
	
	/** 
	 * Sets the class name or fully-qualified class name of the plugin.
	 * @deprecated as of 4.0.0
	 * @param plugin the class name or fully-qualified class name of the plugin
	 */
	@Deprecated
	public void setPlugin(String plugin) {
		setPlugin(new String[] {plugin});
	}
	
	/**
	 * Sets the class name or fully-qualified class name of the plugin.  The plugin type is auto-detected.
	 * @since 4.0.0
	 * @param plugin the class name or fully-qualified class name of the plugin
	 */
//	public void setPlugin(String[] plugin) {
//	  
//		this.plugin = plugin;
//		l.debug(getFormattedDebugString("plugin", ArrayUtils.toString(plugin)));
//	}
	
	/**
   * Deconstructs the {@link YADARequest#PS_PLUGIN} string into a {@link Preprocess}, {@link Postprocess},
   * or {@link Bypass} plugin, and {@link List} of plugin argument {@link List}s.  The original version
   * of this method was a simple mutator for the {@link #plugin} variable.
   * @param configs the {@link String}[] array passed in the url 
   * @since 7.1.0
   */
  public void setPlugin(String[] configs) {
    int length = configs.length;
    this.plugin = new String[length];
    for(int i=0;i<configs.length;i++) //each plugin parameter
    {
      String   config       = configs[i];
      String[] pluginConfig = config.split(","); //separate the pl and args
      this.plugin[i] = pluginConfig[0];
      if(pluginConfig.length > 1) // there's args
      {
        String[] args = (String[])ArrayUtils.remove(pluginConfig, 0);
        this.addPluginArgs(new LinkedList<>(Arrays.asList(args)));
      }
      else
      {
        this.addPluginArgs(null); // placeholder
      }
    }
  }
  
  /**
   * Utility method to enable passing of plugin configuration from one {@link YADARequest} to another. 
   * This method assumes no plugin configuration exists in the calling {@link YADARequest}.
   * 
   * @param pluginConfig a {@link Map} containing the plugin configuration for the request
   */
  public void setPlugin(Map<String,List<String>> pluginConfig) {
    int i = 0;
    for(String plug : pluginConfig.keySet())
    {
      if(this.plugin == null || this.plugin.length == 0)
        this.plugin = new String[pluginConfig.size()];
      this.plugin[i] = plug;
      this.addPluginArgs(pluginConfig.get(plug));
    }
  }
	
	/**
	 * Sets the class name or fully-qualified class name of the response.  The default is 
	 * determined dynamically from the value of {@code format}
	 * @since 4.0.0
	 * @param response the class name or fully-qualified class name of the response
	 */
	public void setResponse(String[] response) {
		l.debug(response[0]);
		this.response = response[0];
		l.debug(getFormattedDebugString("response", response));
	}
	
	/**
	 * Sets the class name or fully-qualified class name of the converter.  The default is 
	 * determined dynamically from the value of {@code format} and {@code protocol}.
	 * @since 4.0.0
	 * @param converter the class name or fully-qualified class name of the converter
	 */
	public void setConverter(String[] converter) {
		l.debug(converter[0]);
		this.converter = converter[0];
		l.debug(getFormattedDebugString("converter", converter));
	}
	
	/**
	 * @deprecated As of YADA 4.0.0
	 * @param pluginType the type of the plugin, i.e., preprocessor, postprocessor, or bypass
	 */
	@Deprecated
	public void setPluginType(String pluginType) {
		setPluginType(new String[] {pluginType});
	}
	
	/**
	 * @deprecated As of YADA 4.0.0
	 * @since 4.0.0
	 * @param pluginType the type of the plugin, i.e., preprocessor, postprocessor, or bypass
	 */
	@Deprecated
	public void setPluginType(String[] pluginType) {
		// only 1 plugin param, and a comma delimted string, rather than multiple params
		if (pluginType.length == 1 && pluginType[0].indexOf(PARAM_DELIMITER)>-1)
		{
			this.pluginType = pluginType[0].split(PARAM_DELIMITER);
		}
		else
		{
			this.pluginType = pluginType;
		}
		l.debug(getFormattedDebugString("pluginType", pluginType));
	}
	
	/**
	 * @deprecated As of YADA 4.0.0
	 * @param postArgs the args to pass to the post processor plugin
	 */
	@Deprecated
	public void setPostArgs(List<String> postArgs)
	{
		setPostArgs((String[])postArgs.toArray());
	}
	
	/**
	 * @since 4.0.0
	 * @param postArgArr the args to pass to the post processor plugin
   * @deprecated as of 7.1.0
   */
  @Deprecated
	public void setPostArgs(String[] postArgArr)
	{
		if (postArgArr.length > 0 && this.plugin == null)
		{
			setPlugin(new String[] {SCRIPT_POSTPROCESSOR});
		}

		String[] lArgs = postArgArr[0].split(PARAM_DELIMITER);
		if (lArgs.length > 0)
		{
			for (String arg : lArgs)
			{
				this.addPostArg(arg);
			}
		}
		l.debug(getFormattedDebugString("postargs", lArgs.toString()));
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @param postArgArr list of arguments to pass to the post processor plugin
   * @deprecated as of 7.1.0
   */
  @Deprecated
	public void setPostargs(String[] postArgArr)
	{
		this.setPostArgs(postArgArr);
	}
	
	/**
	 * Standard mutator for variable
	 * @deprecated As of YADA 4.0.0. Use {@link #setPreargs(String[])}
	 * @param preArgs list of arguments to pass to the preprocessor
	 */
	@Deprecated
	public void setPreArgs(List<String> preArgs) {
		this.preArgs = preArgs;
		l.debug(getFormattedDebugString("preArgs", preArgs.toString()));
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @param preArgArr list of arguments to pass to the preprocessor 
	 * @since 4.0.0
   * @deprecated as of 7.1.0
   */
  @Deprecated
	public void setPreargs(String[] preArgArr) {
		this.setPreArgs(preArgArr);
	}
	
	/** 
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @param preArgArr list of arguments to pass to the preprocessor
	 * @since 4.0.0
   * @deprecated as of 7.1.0
   */
  @Deprecated
	public void setPreArgs(String[] preArgArr)
	{
		if (preArgArr.length > 0 && this.getPlugin() == null)
		{
			this.setPlugin(new String[] {SCRIPT_PREPROCESSOR});
		}
		String[] lArgs = preArgArr[0].split(PARAM_DELIMITER);
		if (lArgs.length > 0)
		{
			for (String arg : lArgs)
			{
				this.addPreArg(arg);
			}
		}
		l.debug(getFormattedDebugString("preargs", lArgs.toString()));
	}
	
	/**
	 * Standard mutator for variable
	 * @deprecated As of YADA 4.0.0
	 * @param qname the query name
	 */
	@Deprecated
	public void setQname(String qname) {
		this.qname = qname;
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @since 4.0.0
	 * @param qname the query name
	 */
	public void setQname(String[] qname) {
		l.debug(qname[0]);
		this.qname = qname[0];
		l.debug(getFormattedDebugString("qname", this.qname));
	}
	
	/**
	 * Standard mutator for variable
	 * @deprecated As of YADA 4.0.0. Use {@link #setSortKey(String[])}.
	 * @param sortKey c
	 */
	@Deprecated
	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @since 4.0.0
	 * @param sortKey the column on which to sort
	 */
	public void setSortKey(String[] sortKey) {
		this.sortKey = sortKey[0];
		l.debug(getFormattedDebugString("sortKey", this.sortKey));
	}
	
	/**
	 * Standard mutator for variable
	 * @deprecated As of YADA 4.0.0. Use {@link #setSortOrder(String[])}
	 * @param sortOrder the sort order, defaults to {@link #SORT_ASC}
	 */
	@Deprecated
	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder; 
	}

	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @since 4.0.0
	 * @param sortOrder the sort order, defaults to {@link #SORT_ASC}
	 */
	public void setSortOrder(String[] sortOrder) {
		this.sortOrder = sortOrder[0];
		l.debug(getFormattedDebugString("sortOrder", this.sortOrder));
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @param updateStats flag indicating whether or not to execute the query access statistics when the query is included in a request
	 * @since 4.0.0
	 */
	public void setUpdateStats(String[] updateStats) {
		boolean b = Boolean.parseBoolean(updateStats[0]); 
		if (!b)
		{
			this.updateStats = b;
		}
		l.debug(getFormattedDebugString("updateStats", String.valueOf(this.updateStats)));
	}
	
	/**
	 * Standard mutator for variable
	 * @deprecated As of YADA 4.0.0
	 * @param user the user id passed in the request, defaults to {@link #DEFAULT_USER} 
	 */
	@Deprecated
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @since 4.0.0
	 * @param user the user id passed in the request, defaults to {@link #DEFAULT_USER}
	 */
	public void setUser(String[] user) {
		this.user = user[0];
		l.debug(getFormattedDebugString("user", this.user));
	}
	
	/**
	 * Standard mutator for variable
	 * @deprecated As of YADA 4.0.0
	 * @param viewLimit the maximum number of rows to retrieve, independent of {@code pageSize}
	 */
	@Deprecated
	public void setViewLimit(int viewLimit) {
		this.viewLimit = viewLimit;
		l.debug(getFormattedDebugString("viewLimit", String.valueOf(this.viewLimit)));
	}
	
	/**
	 * Array mutator for variable, preferred for compatibility with {@link javax.servlet.http.HttpServletRequest#getParameterMap()}
	 * @since 4.0.0
	 * @param viewLimit the maximum number of rows to retrieve, independent of {@code pageSize}
	 */
	public void setViewLimit(String[] viewLimit) {
		int i = new Integer(viewLimit[0]).intValue();
		if (i > -1)
		{
			this.viewLimit = i;
		}
		l.debug(getFormattedDebugString("viewLimit", String.valueOf(this.viewLimit)));
	}
	
	
	/**
	 * Used internally by {@link Service}{@code .engage} methods.
	 * @return the list containing the plugin args
   */
	public List<String> getArgs() {
		return this.args;
	}
	
	/**
	 * Standard accessor for variable.  Usually returns a mutable {@link LinkedList} 
	 * @return the {@link List} of plugin arguments {@link List}s
	 */
	public List<List<String>> getArgLists() {
	  return getPluginArgs();
	}
  
  /**
   * Standard accessor for variable
   * @return the list of arg lists for each plugin
   */
  public List<List<String>> getPluginArgs() {
    return this.argLists;
  }
  
  /**
   * A utility method for obtaining the plugin configuration
   * @return a {@link Map} containing the plugin configuration
   * @since 7.1.0
   */
  public Map<String,List<String>> getPluginConfig() {
    //TODO consider a better queueing mech for plugins, so those processed can be
    // popped off the stack so to speak, but also to retain original order.
    Map<String,List<String>> pluginMap = new LinkedHashMap<>();
    int i = 0;
    if(null != this.plugin && this.plugin.length > 0)
    {
	    for(String plugin : this.plugin)
	    {
	      List<String> args = getPluginArgs() != null ? getPluginArgs().get(i++) : null; 
	      pluginMap.put(plugin, args);
	    }
    }
    return pluginMap;
  }
	
	/**
   * Standard accessor for variable
   * @return the list containing the cookie names
   * @since 5.1.0
   */
  public List<String> getCookies() {
    return this.cookies;
  }
  
	
	/**
	 * Standard accessor for variable
	 * @return boolean value of {@code colhead}
	 */
	public boolean getColHead() {
		return this.colhead;
	}
	
	/**
	 * Standard accessor for variable.  If {code true} (the default
	 * @return boolean value of {@code colhead}
	 */
	public boolean getColhead() {
		return this.colhead;
	}
	
	/**
	 * Standard accessor for variable.  The {@code compact} parameter is currently unimplemented.
	 * @return boolean value of {@code compact}
	 */
	public boolean getCompact() {
		return this.compact;
	}
	
	/**
	 * Standard accessor for variable.  If {@code true}, and the query protocol is {@link Parser#JDBC},
	 * the core code is wrapped in a {@code SELECT COUNT(*)} query, 
	 * which is added to the request in the processing phase, to return the total result count of the query.  
	 * This is particularly useful when executing the query from a GUI client which uses pagination.  
	 * IF set to {@code false}, only the original query is  executed. For more details see the <a href="../../../../../../guide.html">Users' Guide</a>
	 * If the query protocol is {@link Parser#REST} or {@link Parser#SOAP}, {@code count} is automatically set to {@code false}. 
	 * @return boolean value of {@code count}
	 */
	public boolean getCount() {
		return this.count;
	}
	
	/**
	 * Standard accessor for variable. If {@code true}, and the query protocol is {@link Parser#JDBC}, 
	 * the core code is wrapped in a {@code SELECT COUNT(*)} query, and 
	 * only this query is executed in the processing phase. Only the record count is returned.  No data is returned. 
	 * If the query protocol is {@link Parser#REST} or {@link Parser#SOAP}, {@code countOnly} is ignored.
	 * For more details see the <a href="../../../../../../guide.html">Users' Guide</a>
	 * @return boolean value of {@code countOnly}
	 */
	public boolean getCountOnly() {
		return this.countOnly;
	}
	
	/**
	 * Standard accessor for variable. If {@code true}, the results are automatically formatted as {@link #FORMAT_CSV}
	 * and written to a file.  The url of the file is then returned to facilitate retrieval of the file.
	 *
	 * @return boolean value of {@code export}
	 */
	public boolean getExport() {
		return this.export;
	}
	
	/**
	 * @return maximum number of rows to export
	 */
	public int getExportLimit() {
		return this.exportLimit;
	}
	
	/**
	 * Returns the {@code filter} or {@code fi} parameter value as a {@link JSONObject}
	 * @return a {@link JSONObject} built from the {@code filter} or {@code fi} parameter value
	 */
	public JSONObject getFilters() {
		return this.filters;
	}
	
	/**
	 * Returns the value of the {@code format} or {@code f} parameter, a value equal to one of the following:
	 * <ul>
	 * <li>{@link #FORMAT_CSV}</li>
	 * <li>{@link #FORMAT_TSV}</li>
	 * <li>{@link #FORMAT_PIPE}</li>
	 * <li>{@link #FORMAT_JSON} (the default)</li>
	 * <li>{@link #FORMAT_XML}</li>
	 * <li>{@link #FORMAT_HTML} (useful as a tool for query authoring)</li>
	 * </ul>
	 * @return the desired result format
	 */
	public String getFormat() {
		return this.format;
	}
	
	/**
	 * Returns the value of the column delimiter used by delimited result formats.  The default is comma.
	 * @return the string value of the column delimiter, defaults to comma
	 */
	public String getDelimiter() {
		return this.delimiter;
	}
	
	/**
	 * Returns the {@code HTTPHeaders} or {@code H} parameter value as a {@link JSONObject}
	 * @return a {@link JSONObject} built from the value of the {@code HTTPHeaders} or {@code H} parameter value
	 * @since 8.5.0
	 */
	public JSONObject getHttpHeaders() {
		return this.httpHeaders;
	}
	/**
	 * Returns the {@code harmonyMap} or {@code h} parameter value as a {@link JSONObject}
	 * @return a {@link JSONArray} built from the value of the {@code harmonyMap} or {@code h} parameter value
	 */
	public JSONArray getHarmonyMap() {
		return this.harmonyMap;
	}
	
	/**
	 * Standard accessor for variable, a newline by default.
	 * @return the character string for delimiting rows in the response
	 */
	public String getRowDelimiter() {
		return this.rowDelimiter;
	}
	
	/**
	 * Standard accessor for variable. If {@code true}, YADA will execute a commit after each all rows in each query 
	 * have been processed. When {@code false}, as is the default, YADA will execute a commit on each connection after
	 * all queries have been executed. 
	 * @return a boolean indicating whether to commit transactions after each query, or at the end of the request.
	 */
	public boolean getCommitQuery() {
		return this.commitQuery;
	}
	
	/**
	 * Returns the old internal {@code JSONParams} object.  This object should no longer be used.
	 * @deprecated as of 4.0.0
	 * @return {@link java.util.Map}&lt;{@link String}, {@link java.util.List}&lt;{@link java.util.Map}&lt;{@link String},{@link String}&gt;&gt;&gt;
	 */
	@Deprecated
	public Map<String, List<Map<String, String>>> getJSONParams() {
		return this.JSONParams;
	}
	
	/**
	 * Returns the internal {@code jsonParams} object.
	 * @since 4.0.0
	 * @return {@link JSONParams}
	 */
	public JSONParams getJsonParams() {
		return this.jsonParams;
	}
	
	/**
	 * This is currently unsupported.
	 * @return the labels
	 */
	public boolean getLabels() {
		return this.labels;
	}
	
	/**
	 * Retuns the mail spec used by the builtin {@link EmailBypassPlugin}
	 * @return the mail specification, used by the builtin {@link EmailBypassPlugin}
	 */
	public String getMail() {
		return this.mail;
	}

	/**
	 * Returns the value of the {@code method} or {@code m} parameter: either {@link #METHOD_GET}, {@link #METHOD_UPDATE},
	 * or {@link #METHOD_UPLOAD}.
	 * @return the value of the {@code method} or {@code m} parameter
	 */
	public String getMethod() {
		return this.method;
	}
	
	/**
	 * Returns the list of arguments to be passed to the Bypass plugin
	 * @return list of arguments to pass to the Bypass plugin
	 * @see Bypass
	 * @deprecated as of 7.1.0
	 */
	@Deprecated
	public List<String> getBypassArgs() {
		return this.bypassArgs;
	}
	
	/**
	 * Returns the number of rows to return from the current set of request. 
	 * Note: there are a number of possible permutations of results, particularly when multiple queries are
	 * submitted in a single request.  Currently YADA only returns the first {@code pageSize} rows. See <a href="../../../../../../params.html#pagesize">YADA Parameters Reference</a>
	 * for more details.  
	 * @return the number of rows to return from the current request
	 */
	public int getPageSize() {
		return this.pageSize;
	}
	
	/**
	 * Returns an {@link JSONObject} containing oauth parameters such as <code>consumer_key</code>, 
	 * <code>secret</code>, etc. See the YADA OAuth 1.0a specification or YADA parameter documentation
	 * for details. 
	 * @return the oauth parameters object
	 * @since 8.7.0
	 */
	//TODO Add link to YADA OAuth 1.0a spec 
	public JSONObject getOAuth() {
		return this.oauth;
	}
	
	/**
	 * Returns the results from page {@code pageStart}, using {@link #getPageSize()} to determine which row should
	 * be the first row.  This is a common pagination implementation--nothing fancy here.  For example, if {@code pageSize} is 
	 * {@code 20}, and {@code pageStart} is {@code 3}, then the results returned would be rows 41-60.
	 * @return the first, or current, page of results to return
	 */
	public int getPageStart() {
		return this.pageStart;
	}
	
	/**
	 * Returns a {@code boolean} value corresponding to the {@code parallel} url parameter
	 * @deprecated as of 4.0.0
	 * @return true if url parameter {@code parallel} was set to true, otherwise false 
	 */
	@Deprecated
	public boolean getParallel() {
		return this.parallel;
	}
	
	/**
	 * {@code params} is an array of arrays to support the json-derived array syntax 
	 * in a list of param values, e.g., {@code p=val1,[val_a,val_b,val_c],val3}
	 * @return the array of param arrays
	 */
	public String[][] getParams() {
		return this.params;
	}
	
	/**
	 * This is currently unsupported.
	 * @return a parameter set
	 */
	public String getParamset() {
		return this.paramset;
	}
	
	/**
	 * 
	 * @return the plugin class name
	 */
	public String[] getPlugin() {
		return this.plugin;
	}
	
	/**
	 * @deprecated As of YADA 4.0.0
	 * @return {@link String}[] array of {@code pluginType} parameter values
	 */
	@Deprecated
	public String[] getPluginType() {
		return this.pluginType;
	}
	
	/**
	 * Returns the list of arguments passed to the {@link Postprocess} plugin
	 * @return list of arguments passed to the {@link Postprocess} plugin
	 * @deprecated as of 7.1.0
	 */
	@Deprecated
	public List<String> getPostArgs() {
		return this.postArgs;
	}
	
	/**
	 * Returns the list of arguments passed to the {@link Preprocess} plugin
	 * @return list of arguments passed to the {@link Preprocess} plugin
   * @deprecated as of 7.1.0
   */
  @Deprecated
  public List<String> getPreArgs() {
		return this.preArgs;
	}

	/**
	 * If {@code true}, json-formatted results should return with proper indentation and line feeds.
	 * @return boolean value indicating whether or not to pretty print json output (i.e., indent)
	 */
	public boolean getPretty() {
		return this.pretty;
	}
	
	/**
	 * Returns the value of the {@code protocol} variable, as set in {@link QueryManager}.  Value will be one of:
	 * {@link Parser#JDBC}, {@link Parser#SOAP}, {@link Parser#REST}, or {@link Parser#FILE}
	 * @return the value of {@code protocol}
	 */
	public String getProtocol() {
		return this.protocol;
	}
	
	/**
	 * Returns the value of the proxy server to use for external REST queries
	 * @return the proxy
	 */
	public String getProxy()
	{
		return this.proxy;
	}
	
	/**
	 * Returns the current query name.  This value defaults to {@code YADA dummy}. 
	 * If the url contains the {@code qname} or {@code q}
	 * parameter and the default value, the request will only be processed if the url query string 
	 * also contains a {@code plugin} parameter referencing a {@link #BYPASS} or {@link #PREPROCESS} plugin.
	 * 
	 * @return the query name
	 */
	public String getQname() {
		return this.qname;
	}
	
	/**
	 * Returns the name of the the {@link com.novartis.opensource.yada.format.Response} implementation class. The default response is
	 * determined dynamically based on the value of {@code format}.
	 * @return the name of the {@link com.novartis.opensource.yada.format.Response} class
	 */
	public String getResponse() {
		return this.response;
	}
	
	/**
	 * Returns the name of the the {@link com.novartis.opensource.yada.format.Converter} implementation class. The default converter is
	 * determined dynamically based on the value of {@code format} and {@code protocol}.
	 * 
	 * @return the name of the {@link com.novartis.opensource.yada.format.Converter} class
	 */
	public String getConverter() {
		return this.converter;
	}
	
	/**
	 * Standard accessor for variable.
	 * @return the value of the parameter
	 */
	public String getSortKey() {
		return this.sortKey;
	}

	/**
	 * Standard accessor for variable. Default is {@link #SORT_ASC}.
	 * @return the value of the parameter
	 */
	public String getSortOrder() {
		return this.sortOrder;
	}
	/**
	 * Standard accessor for variable.  If {@code true}, the default, the {@link Finder} will execute a
	 * parallel operation to increment the query access count value and the last accessed date in the YADA index. 
	 * IF set to {@code false}, the parallel operation is skipped.
	 * @return boolean value of {@code count}
	 * @since 5.0.0
	 */
	public boolean getUpdateStats() {
		return this.updateStats;
	}
	/**
	 * Standard accessor for variable. Default is {@link #DEFAULT_USER}
	 * @return the value of the parameter
	 */
	public String getUser() {
		return this.user;
	}
	
	/**
	 * {@code viewLimit} is a performance related parameter which truncates a query result at {@code viewLimit} rows.
	 * In other words it is a "maximum number of rows" setting. It is useful in GUI client applications which enable 
	 * filtering of very large result sets, but also enable browsing of entire data sets.  
	 * @return the value of the parameter
	 */
	public int getViewLimit() {
		return this.viewLimit;
	}

	/**
   * @return the join specification
   * @since 6.2.0
   * @see #setJoin(String[])
   */
  public String getJoin() {
    return this.join;
  }

  /**
   * Set to {@code true} to join on any matching keys in across queries.
   * Set to a comma-separated list of keys to join only on matching values of those keys.
   * When {@link #harmonyMap} accompanies this param, the target keys are used
   * @param join the join spec, either {@code true} or a comma-separated list of keys
   * @since 6.2.0
   */
  public void setJoin(String[] join) {
    this.join = join[0];
  }

  /**
   * @return the leftjoin specification
   * @since 6.2.0
   * @see #setLeftJoin(String[])
   */
  public String getLeftJoin() {
    return this.leftJoin;
  }

  /**
   * Set to {@code true} to left join on any matching keys across queries.
   * Set to a comma-separated list of keys to join only on matching values of those keys.
   * When {@link #harmonyMap} accompanies this param, the target keys are used
   * @param leftjoin leftjoin spec, either {@code true} or a comma-separated list of keys
   * @since 6.2.0
   */
  public void setLeftJoin(String[] leftjoin) {
    this.leftJoin = leftjoin[0];
  }

  /** 
	 * @since 4.0.0
	 */
	public void resetJsonParams() {
		this.jsonParams = new JSONParams();
	}
	
	
	/**
	 * @deprecated as of 4.0.0
	 */
	@Deprecated
	public void resetJSONParams() {
		this.JSONParams = new LinkedHashMap<>();
	}
	
	/**
	 * Sets the upload items which will then be dervied from the
	 * YADARequest by the preprocessor plugin
	 * 
	 * @param uploadItems list of form field name/value pairs and upload content
	 * @throws YADARequestException when a method invocation problem occurs while setting parameter values
	 */
	public void setUploadItems(List<FileItem> uploadItems) throws YADARequestException 
	{
		this.uploadItems = uploadItems;
		Iterator<FileItem> iter = uploadItems.iterator();
		while (iter.hasNext()) {
			DiskFileItem fi = (DiskFileItem) iter.next();
			l.debug(fi);
			// set parameters from form fields
			if (fi.isFormField()) 
			{
				String field = fi.getFieldName();
				String value = fi.getString();
				l.debug("field is ["+field+"], value is ["+value+"]");
				invokeSetter(field,new String[] {value});
				addToMap(field, new String[] {value});
			} 
			else 
			{
				File f;
				String fPath;
				String fName;
				try 
				{
					// execute plugin, as we are not calling a WS
					f = fi.getStoreLocation(); // tmp dir
					fPath = f.getAbsolutePath(); // uploaded file
					fName = fi.getName(); // original file name
				} 
				finally 
				{
					f = null;
				}
				addArg(fPath);
				addArg(fName);
			}
		}
		l.debug(getFormattedDebugString("uploadItems", uploadItems.toString()));
	}

	
	/**
	 * Returns a list of form field values and content items from the form submission when processing an uploads
	 * @return list of form fields and content items
	 */
	public List<FileItem> getUploadItems() {
		return this.uploadItems;
	}

	/**
	 * Takes all parameters and values from {@code paraMap}, typically set in the {@link javax.servlet.http.HttpServletRequest}, and 
	 * adds them to a local {@link java.util.Map}.  This is called by {@link Service#handleRequest(HttpServletRequest)}.
	 * The inclusion of the map enables the use of otherwise unsupported url parameters in plugins. 
	 * @param paraMap the parameter map originally set in the request
	 * @see Service#handleRequest(String, Map)
	 */
	public void setParameterMap(Map<String, String[]> paraMap) {
		this.getParameterMap().putAll(paraMap);
		l.debug(getFormattedDebugString("parameterMap", paraMap.toString()));
	}

	/**
	 * Returns the {@link java.util.Map} of parameters originally set in the {@link javax.servlet.http.HttpServletRequest}.
	 * @return the map of parameters derived from the request
	 */
	public Map<String, String[]> getParameterMap() {
		return this.parameterMap;
	}
	
	/**
	 * Returns {@code true} if format is {@link #FORMAT_JSON}, {@link #FORMAT_HTML}, or {@link #FORMAT_XML}, otherwise {@code false}.
	 * @since 4.0.0
	 * @return true if format is JSON, XML, or HTML, otherwise false
	 */
	public boolean isFormatStructured()
	{
		if(this.getFormat().equals(FORMAT_JSON) || this.getFormat().equals(FORMAT_XML) || this.getFormat().equals(FORMAT_HTML))
			return true;
		return false;
	}
	
	/**
	 * Returns {@code true} if request contains "h" or "harmonize" parameter with a value other than an empty string or null
	 * @since 4.0.0
	 * @return true if request contains "h" or "harmonize" parameter with a value other than an empty string or null
	 */
	public boolean hasHarmonyMap()
	{
		if(null == getHarmonyMap() || "".equals(getHarmonyMap()))
			return false;
		return true;
	}
	
	/**
	 * Returns {@code true} if {@link #cookies} contains a cookie name, otherwise {@code false}
	 * @return {@code true} if {@link #cookies} contains a cookie name, otherwise {@code false}
	 * @since 5.1.0
	 */
	public boolean hasCookies() {
	  if(null == this.getCookies() || this.getCookies().size() == 0)
	  {
	    return false;
	  }
	  return true;
	}
	
	/**
	 * Returns {@code true} if {@link #httpHeaders} contains a header array entry, otherwise {@code false}
	 * @return {@code true} if {@link #httpHeaders} contains a header array entry, otherwise {@code false}
	 * @since 8.5.0
	 */
	public boolean hasHttpHeaders() {
	  if(null == this.getHttpHeaders() || this.getHttpHeaders().length() == 0)
	  {
	    return false;
	  }
	  return true;
	}

	/**
	 * A utility method for pretty-printing debug info in the form like:
	 * <p>
	 * {@code Set [name] to [val]}
	 * </p>
	 * @param name a parameter name
	 * @param val the parameter value
	 * @return a formatted string
	 */
	public String getFormattedDebugString(String name, String val) {
		String sSet = "Set [" + name + "]";
		String sVal = "["+val+"]";
		return String.format("%25s to %s", sSet, sVal);
	}
	
	/**
	 * A utility method for pretty-printing debug info in the form like:
	 * <p>
	 * {@code Set [name] to [string,string...]}
	 * </p>
	 * @param name a parameter name
	 * @param strings the parameter value
	 * @return a formatted string
	 */
	@SuppressWarnings("static-method")
	private String getFormattedDebugString(String name, String[] strings) {
		String sSet = "Set [" + name + "]";
		String sVal = "[" + ArrayUtils.toString(strings) + "]";
		return String.format("%25s to %s", sSet, sVal);
	}
}
