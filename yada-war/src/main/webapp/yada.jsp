<%--

    Copyright 2016 Novartis Institutes for BioMedical Research Inc.
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%><%@page language="java" trimDirectiveWhitespaces="true" import="com.novartis.opensource.yada.*,java.io.*,java.util.*,java.util.regex.*,
                                     org.json.JSONObject,
 																		 org.apache.commons.fileupload.*,
 																		 org.apache.commons.fileupload.servlet.*,
 																		 org.apache.commons.fileupload.disk.*" %><%Service service  = new Service();
//   "Exception": "com.novartis.opensource.yada.YADAFinderException",
final String EXCEPTION = "(?s)^.*\\{\\n\\s+.+\"Exception\":.*$"; 
final String PACKAGE   = "com.novartis.opensource.yada.";
final String BASE_EXCEPTION                = PACKAGE + "YADAException";
final String EXECUTION_EXCEPTION           = PACKAGE + "YADAExecutionException";
final String PARSER_EXCEPTION              = PACKAGE + "YADAParserException";
final String CONNECTION_EXCEPTION          = PACKAGE + "YADAConnectionException";
final String FINDER_EXCEPTION              = PACKAGE + "YADAFinderException";
final String QUERY_CONFIGURATION_EXCEPTION = PACKAGE + "YADAQueryConfigurationException";
final String REQUEST_EXCEPTION             = PACKAGE + "YADARequestException";
final String UNSUPPORTED_ADAPTOR_EXCEPTION = PACKAGE + "YADAUnsupportedAdaptorException";
final String ADAPTOR_EXCEPTION             = PACKAGE + "adaptor.YADAAdaptorException";
final String ADAPTOR_EXECUTION_EXCEPTION   = PACKAGE + "adaptor.YADAAdaptorExecutionException";
final String CONVERTER_EXCEPTION           = PACKAGE + "format.YADAConverterException";
final String RESPONSE_EXCEPTION            = PACKAGE + "format.YADAResponseException";
final String IO_EXCEPTION                  = PACKAGE + "io.YADAIOException";
final String PLUGIN_EXCEPTION              = PACKAGE + "plugin.YADAPluginException";
final String SECURITY_EXCEPTION            = PACKAGE + "YADASecurityException";
final String UNHANDLED_EXCEPTION           = "java.lang\\.*";

final String HTTP_SC_NOT_FOUND       = "Not Found";
final String HTTP_SC_BAD_REQUEST     = "Bad Request";
final String HTTP_SC_NOT_IMPLEMENTED = "Not Implemented";
final String HTTP_SC_FORBIDDEN       = "Forbidden";
final String HTTP_SC_INTERNAL_SERVER_ERROR = "Internal Server Error";

final Hashtable<Integer,String> statusText = new Hashtable<>();
statusText.put(new Integer(HttpServletResponse.SC_NOT_FOUND),HTTP_SC_NOT_FOUND);
statusText.put(new Integer(HttpServletResponse.SC_BAD_REQUEST),HTTP_SC_BAD_REQUEST);
statusText.put(new Integer(HttpServletResponse.SC_NOT_IMPLEMENTED),HTTP_SC_NOT_IMPLEMENTED);
statusText.put(new Integer(HttpServletResponse.SC_FORBIDDEN),HTTP_SC_FORBIDDEN);
statusText.put(new Integer(HttpServletResponse.SC_INTERNAL_SERVER_ERROR),HTTP_SC_INTERNAL_SERVER_ERROR);

final Hashtable<String,Integer> statusCodes = new Hashtable<>();
// FinderExcepion 404
statusCodes.put(FINDER_EXCEPTION, HttpServletResponse.SC_NOT_FOUND);
// QueryConfigurationException, RequestException 403
statusCodes.put(QUERY_CONFIGURATION_EXCEPTION, HttpServletResponse.SC_BAD_REQUEST);
statusCodes.put(REQUEST_EXCEPTION, HttpServletResponse.SC_BAD_REQUEST);
// UnsupportedAdaptorException 501
statusCodes.put(UNSUPPORTED_ADAPTOR_EXCEPTION, HttpServletResponse.SC_NOT_IMPLEMENTED);
// SecurityException 403
statusCodes.put(SECURITY_EXCEPTION, new Integer(HttpServletResponse.SC_FORBIDDEN));
// All others 500
statusCodes.put(BASE_EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
statusCodes.put(EXECUTION_EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
statusCodes.put(PARSER_EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
statusCodes.put(CONNECTION_EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
statusCodes.put(ADAPTOR_EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
statusCodes.put(ADAPTOR_EXECUTION_EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
statusCodes.put(CONVERTER_EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
statusCodes.put(RESPONSE_EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
statusCodes.put(IO_EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
statusCodes.put(PLUGIN_EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
statusCodes.put(UNHANDLED_EXCEPTION, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);



String  result   = "";
String  YADAPath = request.getParameter("yp"); // yp = YADAPath (path-style yada parameters)
String  URLPath  = request.getParameter("yu"); // up = URLPath (path aliases)
if( null != URLPath && !("").equals(URLPath))
{
	System.out.println(URLPath);
}
else if (null != YADAPath && !("").equals(YADAPath))
{
	service.handleRequest(request, YADAPath);
}
else
{
	service.handleRequest(request);
}

if(ServletFileUpload.isMultipartContent(request))
{
	FileItemFactory factory = new DiskFileItemFactory();
	ServletFileUpload upload = new ServletFileUpload(factory);
	((DiskFileItemFactory)factory).setSizeThreshold(1);
	List<FileItem> items = upload.parseRequest(request);
	YADARequest svcParams = service.getYADARequest();
	svcParams.setPath(request.getSession().getServletContext().getRealPath("/"));
	svcParams.setUploadItems(items);
	result = service.execute();
	boolean exception = result.matches(EXCEPTION);
	if(exception)
  {
	  response.setContentType("application/json;charset=UTF-8");
	  JSONObject e = new JSONObject(result);
	  String exceptionClass = e.getString("Exception");
	  Integer errorCode = statusCodes.get(UNHANDLED_EXCEPTION);
	  if(statusCodes.containsKey(exceptionClass)) 
	  {
	    errorCode = statusCodes.get(exceptionClass);
	  }
    int ec = errorCode.intValue();
    e.put("Status",ec);
    e.put("StatusText",statusText.get(errorCode));
    request.getSession().setAttribute("YADAException",e.toString());
    response.sendError(errorCode); 
  }%><%=result%><%
}
else
{
/*	String help = request.getParameter("help");
	if (new Boolean(help).booleanValue() 
			|| "yes".equals(help) 
			|| "1".equals(help)
	{  */
%><%-- link to user guide --%><%// get and prep result
	if(request.getParameter("method") == null
			|| !request.getParameter("method").equals("upload"))
	{
		result = service.execute();
		String fmt    = service.getYADARequest().getFormat();
		// TODO confirm response content type defaults to json even though the call below follows
		// the call to execute
		boolean exception = result.matches(EXCEPTION);
		if (service.getYADARequest().getExport())
    {
      response.setStatus(HttpServletResponse.SC_CREATED);
      response.addHeader("Location", result);
      response.setContentType("text/plain");
      fmt = YADARequest.FORMAT_PLAINTEXT;
    }
		if (YADARequest.FORMAT_JSON.equals(fmt)
				|| exception)
		{
			response.setContentType("application/json;charset=UTF-8");	
			if(exception)
			{
			  JSONObject e = new JSONObject(result);
			  String exceptionClass = e.getString("Exception");
		    Integer errorCode = statusCodes.get(UNHANDLED_EXCEPTION);
		    if(statusCodes.containsKey(exceptionClass)) 
		    {
		      errorCode = statusCodes.get(exceptionClass);
		    }
			  int ec = errorCode.intValue();
			  e.put("Status",ec);
			  e.put("StatusText",statusText.get(errorCode));
			  request.getSession().setAttribute("YADAException",e.toString());
			  response.sendError(errorCode); 
			}
		}
		else if (YADARequest.FORMAT_XML.equals(fmt))
		{
			response.setContentType("text/xml");
		}
		else if (YADARequest.FORMAT_CSV.equals(fmt))
		{
			response.setContentType("text/csv");
		}
		else if (YADARequest.FORMAT_TSV.equals(fmt) || YADARequest.FORMAT_TAB.equals(fmt))
		{
			response.setContentType("text/tab-separated-values");
		}
		else if (YADARequest.FORMAT_PIPE.equals(fmt))
		{
			response.setContentType("text/pipe-separated-values");
		}
		else if (YADARequest.FORMAT_HTML.equals(fmt))
		{
			response.setContentType("text/html");
		}
	}%><%=result%><%
}
%>