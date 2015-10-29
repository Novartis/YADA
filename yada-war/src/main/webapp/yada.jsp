<%@page language="java" import="com.novartis.opensource.yada.*,java.io.*,java.util.*,java.util.regex.*,
 																		 org.apache.commons.fileupload.*,
 																		 org.apache.commons.fileupload.servlet.*,
 																		 org.apache.commons.fileupload.disk.*" %><%Service service  = new Service();
final String EXCEPTION = "(?s)^\\{\\n\\s+\"Exception\".*";
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
	// there's an 'unchecked' warning on the 'getParameterMap' call which will be resolved
	// by upgrading to Tomcat7/Servlet 3.0.
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
	result = service.execute();%><%=result%><%}
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
		//TODO confirm response content type defaults to json even though the call below follows
		// the call to execute
		if (YADARequest.FORMAT_JSON.equals(fmt)
				|| result.matches(EXCEPTION))
		{
			response.setContentType("application/json;charset=UTF-8");	
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
