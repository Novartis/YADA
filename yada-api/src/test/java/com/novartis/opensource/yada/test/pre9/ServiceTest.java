package com.novartis.opensource.yada.test.pre9;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import nl.javadude.assumeng.Assumption;
import nl.javadude.assumeng.AssumptionListener;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.novartis.opensource.yada.JSONParams;
import com.novartis.opensource.yada.JSONParamsEntry;
import com.novartis.opensource.yada.Service;
import com.novartis.opensource.yada.YADAExecutionException;
import com.novartis.opensource.yada.YADAParam;
import com.novartis.opensource.yada.YADAQueryConfigurationException;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADARequestException;
import com.novartis.opensource.yada.format.YADAResponseException;
import com.novartis.opensource.yada.test.QueryFile;
import com.novartis.opensource.yada.util.YADAUtils;

// TODO tests for query creation
// TODO tests for query updating
// TODO tests for query deletion

// TODO tests for query plugins (security handles pre, need post and bypass)
// TODO tests for pretty printing
// TODO tests for multiple plugins (there is only one currently, for a pre and post)
// TODO tests for e=true (export=true)
// TODO tests for exportLimit
// TODO validateIntegerResult method + break out these tests into separate files
// TODO validateJSONFormatted method + break out these tests into separate files
// TODO Exception tests, i.e, Finder exception (protector queries), Security exceptions, et al

/**
 * Tests all manner of queries via API and HTTP. Uses query parameter strings
 * and json strings stored in resources as test cases
 *
 * @author David Varon
 * @since 9.0.0
 */
@Listeners(AssumptionListener.class)
public class ServiceTest
{

  /**
   * Local logger handle
   */
  private static Logger l = Logger.getLogger(ServiceTest.class);
  /**
   * Constant ref to <code>log.stdout</code> system property.  When present or true, prints qname and result to console during testing.  This is useful for troubleshooting but otherwise a pain.
   */
  protected static final String LOG_STDOUT = "log.stdout";
  /**
   * Constant equal to:
   * <code>^(\"([A-Z]{1,2}(,[A-Z]*)*|[0-9]{1,2}|[0-9\\.]{3})\",){3}(\"[\\-\\s:0-9]+\",*){2}$</code>
   */
  protected static final Pattern CSV = Pattern.compile("^((\"([A-Z]{1,2}(,[A-Z]*)*|[0-9]{1,2}|[0-9\\.]{3})\"|null),){3}((\"[\\-\\s:0-9\\.]+\"|null),*){2}(\"((YADA)+|YO)\")?$");
  /**
   * Constant equal to:
   * <code>^(\"([A-Z]{1,2}(,[A-Z]*)*|[0-9]{1,2}|[0-9\\.]{3})\"\\t){3}(\"[\\-\\s:0-9]+\"\\t*){2}$</code>
   */
  protected static final Pattern TSV = Pattern.compile("^(\"([A-Z]{1,2}(,[A-Z]*)*|[0-9]{1,2}|[0-9\\.]{3})\"\\t){3}(\"[\\-\\s:0-9\\.]+\"\\t*){2}(\"((YADA)+|YO)\")?$");
  /**
   * Constant equal to:
   * <code>^(\"([A-Z]{1,2}(,[A-Z]*)*|[0-9]{1,2}|[0-9\\.]{3})\"\\|){3}(\"[\\-\\s:0-9]+\"\\|*){2}$</code>
   */
  protected static final Pattern PSV = Pattern.compile("^(\"([A-Z]{1,2}(,[A-Z]*)*|[0-9]{1,2}|[0-9\\.]{3})\"\\|){3}(\"[\\-\\s:0-9\\.]+\"\\|*){2}(\"((YADA)+|YO)\")?$");
  /**
   * Constant equal to: {@value}
   */
  protected static final String COL_STRING = "COL1";

  /**
   * Constant equal to: {@value}
   */
  protected static final String COL_INTEGER = "COL2";

  /**
   * Constant equal to: {@value}
   */
  protected static final String COL_NUMBER = "COL3";

  /**
   * Constant equal to: {@value}
   */
  protected static final String COL_DATE = "COL4";

  /**
   * Constant equal to: {@value}
   */
  protected static final String COL_TIME = "COL5";

  /**
   * Constant equal to: {@value}
   */
  protected static final String COL_STRING_LC = "col1";

  /**
   * Constant equal to: {@value}
   */
  protected static final String COL_INTEGER_LC = "col2";

  /**
   * Constant equal to: {@value}
   */
  protected static final String COL_NUMBER_LC = "col3";

  /**
   * Constant equal to: {@value}
   */
  protected static final String COL_DATE_LC = "col4";

  /**
   * Constant equal to: {@value}
   */
  protected static final String COL_TIME_LC = "col5";

  /**
   * Constant equal to: {@value}
   */
  protected static final String COL_HM_STRING = "STRING";

  /**
   * Constant equal to: {@value}
   */
  protected static final String COL_HM_INT    = "INT";

  /**
   * Constant equal to: {@value}
   */
  protected static final String COL_HM_FLOAT  = "FLOAT";

  /**
   * Constant equal to: {@value}
   */
  protected static final String COL_HM_DATE   = "DATE";

  /**
   * Constant equal to: {@value}
   */
  protected static final String COL_HM_TIME   = "TIME";


  /**
   * Constant equal to: {@value}
   */
  protected static final String LEFT_SQUARE = "[";

  /**
   * Constant equal to: {@value}
   */
  protected static final String AMP = "&";

  /**
   * Constant equal to: {@value}
   */
  protected static final String EQUAL = "=";

  /**
   * Constant equal to: {@value}
   */
  protected static final String RESULTSETS = "RESULTSETS";

  /**
   * Constant equal to: {@value}
   */
  protected static final String RESULTSET = "RESULTSET";

  /**
   * Constant equal to: {@value}
   */
  protected static final String ROWS = "ROWS";

  /**
   * Constant equal to: {@value}
   */
  protected static final String RECORDS = "records";

  /**
   * Constant equal to: {@value}
   */
  protected static final String UTF8 = "UTF-8";

  /**
   * The yada server used for http and soap tests. Defaults to:
   * <code>localhost:8080</code>
   */
  protected String host = "localhost:8080";

  /**
   * The yada resource for brokering the request. All requests are ultimately
   * forwarded to yada.jsp, but the framework can support access to the jsp
   * without the extension (<code>yada?param=val</code>), and also path-style
   * parameters with and without the 'yada' path element, e.g.,
   * <code>/yada/param/value</code> or simply <code>/param/value</code>
   */
  protected String uri = "/yada.jsp";

  /**
   * The proxy server to use in for external REST queries
   */
  protected String proxy = null;

  /**
   * The flag to use authentication
   */
  protected String auth = "false";
  
  /**
   * Container for tokens
   */
  protected JSONObject secData;
  
  /** 
   * 
   */
  protected String user = "";
  
  /**
   * 
   */
  protected String pass = "";

  /**
   * One-arg constructor passes in host from config file;
   */
  public ServiceTest()
  {

  }

  /**
   * The initialization method required by the {@code jsp} group to set the
   * pertinent ivars to property values.
   */
  @BeforeSuite(groups = { "jsp" })
  public void init()
  {
    Properties props = ConnectionFactoryTest.getProps();
    this.host = props.getProperty("YADA.host");
    this.uri = props.getProperty("YADA.uri");
    this.auth = props.getProperty("YADA.auth");
  }

  /**
   * Loads the resource at {@code path} containing query parameter or json
   * strings
   *
   * @param path the path to the test script
   * @return an array of query or json strings
   * @throws URISyntaxException when a handle can't be attached to the test file
   *         path
   * @throws IOException if the {@link InputStream} used for reading test files
   *         can't be closed
   */
  public String[] loadResource(String path) throws URISyntaxException, IOException
  {
    Scanner scanner = null;
    String[] queries = null;
    java.net.URL resource = getClass().getResource(path);
    InputStream in = null;
    try
    {
      scanner = new Scanner(new File(resource.toURI()), UTF8);
    }
    catch (Exception e)
    {
      in = getClass().getResourceAsStream(path);
      if (in != null)
      {
        scanner = new Scanner(in, UTF8);
      }
    }

    if (scanner != null)
    {
      scanner.useDelimiter("\\Z");
      queries = scanner.next().split("\\n");
      scanner.close();
    }

    if (in != null)
      in.close();

    return queries;
  }

  /**
   * Combines the queries from multiple resources into a single array to
   * facilitate execution of tests from multiple files
   *
   * @param paths the paths to the test scripts
   * @return an array of query or JSON strings
   * @throws URISyntaxException when a handle can't be attached to the test file
   *         path
   * @throws IOException if the {@link InputStream} used for reading test files
   *         can't be closed
   */
  public String[] mergeArrays(String[] paths) throws URISyntaxException, IOException
  {
    String   engine = ConnectionFactoryTest.getProps().getProperty("jdbc.engine");
    String[] params = null;
    for (int i = 0; i < paths.length; i++)
    {
      params = (String[]) ArrayUtils.addAll(params, loadResource(paths[i]));

      if (engine != null)
      {
        String enginePath = paths[i].replace(".txt", "_" + engine + ".txt");
        String[] engineResources = loadResource(enginePath);
        if (engineResources != null)
          params = (String[]) ArrayUtils.addAll(params, engineResources);
      }
    }
    List<String> list = new ArrayList<>(Arrays.asList(params));
    List<String> comments = new ArrayList<>();
    for (String entry : list)
    {
      if (isComment(entry) || entry.length() == 0)
      {
        comments.add(entry);
      }
    }
    list.removeAll(comments);
    params = list.toArray(new String[list.size()]);

    return params;
  }

  /**
   * Evaluates text line in resource, returning {@code true} if line starts with
   * a hashmark: {@code #}
   *
   * @param query the line from the test script to test
   * @return {@code true} if line starts with {@code #}
   */
  public boolean isComment(String query)
  {
    if (query.startsWith("#"))
    {
      return true;
    }
    return false;
  }

  /**
   * Tests if the current execution environment is a variant of Windows, which
   * will cause some tests to fail.
   *
   * @return {@code true} if the {@code os.name} property does not contain the
   *         string {@code win}, otherwise {@code false}
   */
  public boolean isNotWindows()
  {
    return System.getProperty("os.name").toLowerCase().indexOf("win") == -1;
  }

  /**
   * Creates a {@link com.novartis.opensource.yada.YADARequest} object and
   * populates it with values in {@code query}
   *
   * @param query the query to use in the request
   * @return a {@link com.novartis.opensource.yada.YADARequest} object
   * @throws YADAQueryConfigurationException when request creation fails
   */
  public YADARequest getYADAReq(String query) throws YADAQueryConfigurationException
  {

    logQuery(query);
    YADARequest yadaReq = new YADARequest();
    yadaReq.setUpdateStats(new String[] { "false" });
    StringBuilder b = new StringBuilder();
    boolean inQuotes = false;
    List<String> names  = new ArrayList<>();
    List<String> values = new ArrayList<>();
    for(char c : query.toCharArray())
    {
    	switch(c) {
    		case '&':
    			if(inQuotes)
    			{
    				b.append(c);
    			}
    			else
    			{
    				values.add(b.toString());
    				b = new StringBuilder();
    			}
    			break;
    		case '=':
    			if(inQuotes)
    			{
    				b.append(c);
    			}
    			else
    			{
    				names.add(b.toString());
    				b = new StringBuilder();
    			}
    			break;
    		case '\"':
    			inQuotes = !inQuotes;
    		default:
    			b.append(c);
    	}
    }
    values.add(b.toString()); // the last one

    //String[] array = query.split(AMP);
    Map<String, String[]> paraMap = new HashMap<>();
    int i = 0;
    for (String param : names)
    {
//      String[] pair = param.split(EQUAL);
      String[] vals = paraMap.get(param);
      if(vals == null)
      {
        paraMap.put(param, new String[] { values.get(i) });
      }
      else
      {
        paraMap.put(param, (String[])ArrayUtils.add(vals, values.get(i)));
      }
      i++;
    }


    for (String key : paraMap.keySet())
    {
      String[] val = paraMap.get(key);//null;
      if (key.equals(YADARequest.PL_JSONPARAMS) || key.equals(YADARequest.PS_JSONPARAMS) && val != null)
      {
      	System.out.println(val[0]);
      	yadaReq.setJsonParams(new JSONParams(new JSONArray(val[0])));
      }
      else
      {
        try
        {
          if (val != null)
            yadaReq.invokeSetter(key, val);
        }
        catch (YADARequestException e)
        {
          throw new YADAQueryConfigurationException("Problem invoking setter.", e);
        }
      }
    }
    return yadaReq;
  }

  /**
   * Creates a {@link com.novartis.opensource.yada.YADARequest} object and
   * populates it with values in {@code ja}
   *
   * @param ja the parameter to use in the request
   * @return a {@link com.novartis.opensource.yada.YADARequest} object
   * @throws YADAQueryConfigurationException when request creation fails
   */
  public YADARequest getYADAReq(JSONArray ja) throws YADAQueryConfigurationException
  {
    YADARequest yadaReq = new YADARequest();
    yadaReq.setUpdateStats(new String[] { "false" });
    yadaReq.setJsonParams(new JSONParams(ja));
    return yadaReq;
  }

  /**
   * The main DataProvider of the test class
   *
   * @param theMethod the test to run
   * @return the array of Object arrays required of DataProviders by the TestNG
   *         framework
   * @throws URISyntaxException when a handle can't be attached to the test file
   *         path
   * @throws IOException if the {@link InputStream} used for reading test files
   *         can't be closed
   */
  @DataProvider(name = "QueryTests")
  public Object[][] initDataProvider(Method theMethod) throws URISyntaxException, IOException
  {
    String[] paths = theMethod.getAnnotation(QueryFile.class).list();
    String[] params = mergeArrays(paths);
    Object[][] result = new Object[params.length][];
    for (int i = 0; i < params.length; i++)
    {
      result[i] = new Object[] { params[i] };
    }
    return result;
  }

  /**
   * Sets the proxy string
   *
   * @param proxy the host:port of the proxy server passed in xml config file
   */
  @BeforeMethod(groups = { "proxy" })
  @Parameters({ "YADA_proxy" })
  public void setProxy(@Optional("") String proxy)
  {
    this.proxy = ConnectionFactoryTest.getProps().getProperty("YADA.proxy");
  }

  /**
   * Unsets the proxy string, just to be a good citizen
   */
  @AfterMethod(groups = { "proxy" })
  public void unsetProxy()
  {
    this.proxy = null;
  }

  /**
   * Convenience method to stitch together url-encoded versions of params passed
   * in queries
   *
   * @param param the name/value pair representing the url parameter
   * @return the encoded name/value pair
   * @throws YADAExecutionException when the encoder throws an exception
   */
  public String encodeParam(String[] param) throws YADAExecutionException
  {
    String p = "";
    if (param.length > 1)
    {
      try
      {
        p += param[0] + "=" + URLEncoder.encode(param[1], UTF8);
      }
      catch (UnsupportedEncodingException e)
      {
        throw new YADAExecutionException(e.getMessage(), e);
      }
    }
    return p;
  }

  /**
   * Preps the db before each test execution by populating the test table with a
   * mix of data from the resaurce {@code /test/inserts_single_json_prep.txt}
   *
   * @throws URISyntaxException when a handle can't be attached to the test file
   *         path
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws IOException if the {@link InputStream} used for reading test files
   *         can't be closed
   */
  @BeforeMethod(groups = { "json", "standard", "options", "api", "jsp", "plugins", "sqlite_debug" })
  public void dbPrep() throws URISyntaxException, YADAQueryConfigurationException, IOException
  {
    prepOrClean(new String[] { "/test_pre9/inserts_single_json_prep.txt" });
  }

  /**
   * @throws URISyntaxException if the test files content can't be loaded
   * @throws YADAQueryConfigurationException if the queries in the test are
   *         malformed
   * @throws IOException if the {@link InputStream} used for reading test files
   *         can't be closed
   */
  @BeforeMethod(groups = { "filesystem" })
  public void fsPrep() throws URISyntaxException, YADAQueryConfigurationException, IOException
  {
    prepOrClean(new String[] { "/test_pre9/filesystem_insert_single_json_prep.txt" });
  }

  /**
   * Cleans the db after each test execution by executing the queries in
   * {@code /test/deletes_single_json.txt}
   *
   * @throws URISyntaxException when a handle can't be attached to the test file
   *         path
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws IOException if the {@link InputStream} used for reading test files
   *         can't be closed
   */
  @AfterMethod(groups = { "json", "standard", "options", "api", "jsp", "plugins", "sqlite_debug" })
  public void dbClean() throws URISyntaxException, YADAQueryConfigurationException, IOException
  {
    prepOrClean(new String[] { "/test_pre9/deletes_single_json.txt" });
  }

  /**
   * Utility method to prep or clean the db before or after each test execution
   * by executing the queries in {@code paths}
   *
   * @param paths the list of files containing the queries to process
   *
   * @throws URISyntaxException when a handle can't be attached to the test file
   *         path
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws IOException if the {@link InputStream} used for reading test files
   *         can't be closed
   */
  public void prepOrClean(String[] paths) throws URISyntaxException, IOException, YADAQueryConfigurationException
  {
    String[] params = mergeArrays(paths);
    JSONArray jarray = new JSONArray(params[0].substring(params[0].indexOf(LEFT_SQUARE)));
    YADARequest yadaReq = getYADAReq(jarray);
    Service svc = new Service(yadaReq);
    svc.execute();
  }

  /**
   * Convenience method to instantiate a {@link Service} object for processing
   * {@code query}.
   *
   * @param query the test string
   * @return the {@link Service} object for query execution or further
   *         modification
   * @throws YADAQueryConfigurationException when the query string is malformed
   */
  public Service prepareTest(String query) throws YADAQueryConfigurationException
  {
    YADARequest yadaReq = getYADAReq(query);
    Service svc = new Service(yadaReq);
    return svc;
  }

  /**
   * Method stub for testing with environment-specific authentication credential
   * @throws YADAExecutionException when any authentication method fails. Any exception thrown internally by authentication methods can be caught and rethrown as {@link YADAExecutionException}s.
   */
  public void setAuthentication() throws YADAExecutionException
  {
    if(null != this.secData && this.secData.length() > 0)
    {
      return;
    }
    
    byte[] toEncode = null;
    try
    {
      toEncode = (this.user+":"+this.pass).getBytes("utf-8");
    }
    catch (UnsupportedEncodingException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    String basic    = "Basic " + Base64.getEncoder().encodeToString(toEncode);
    String query = "YADA/q/YADA resource access/pl/Authorizer,YADA";
    String[] splits = query.split("/");    
    String encQuery = "";
    for(String s : splits)
    {
      try
      {
//        if(encQuery.length() > 0)
//          encQuery += "/";
        encQuery += "/" + URLEncoder.encode(s,UTF8);
      }
      catch (UnsupportedEncodingException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
       
    
    String target = "http://" + this.host + encQuery;
    URL url = null;
    try
    {
      url = new URL(target);
    }
    catch (MalformedURLException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    HttpURLConnection connection = null;
    try
    {
      connection = (HttpURLConnection) url.openConnection();
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
        
    connection.setRequestProperty("Authorization", basic);
    connection.setUseCaches(false);
    connection.setDoInput(true);
    connection.setDoOutput(true);
    

    // Get Response
    try(InputStream is = connection.getInputStream())
    {
      try(BufferedReader rd = new BufferedReader(new InputStreamReader(is)))
      {
        String line;
        StringBuffer result = new StringBuffer();
        while ((line = rd.readLine()) != null)
        {
          result.append(line);
        }
        l.debug(result);
        this.secData = new JSONObject(result.toString());        
      }
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * This test is a workaround for https://github.com/cbeust/testng/issues/787
   * See the readme at https://github.com/varontron/testng-annoxform2-bug for
   * details.
   *
   * @param query a null string
   */
  @Test(enabled = true, dataProvider = "QueryTests")
  @QueryFile(list = {})
  public void testWorkaroundFor787(String query)
  {
    System.out.println("This test is a workaround for testng issue 787: https://github.com/cbeust/testng/issues/787");
  }

  /**
   * Executes json-based requests
   *
   * @param query query to test
   * @throws YADAResponseException when the test result is invalid
   * @throws YADAQueryConfigurationException when request creation fails
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "json", "api" })
  @QueryFile(list = {})
  public void testWithJSONParams(String query) throws YADAResponseException, YADAQueryConfigurationException
  {
    Service svc = prepareTest(query);
    Assert.assertTrue(validate(svc.getYADARequest(), svc.execute()) ,  "Data invalid for query: "+query);
  }

  /**
   * Execute standard parameter tests
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws YADAResponseException when the test result is invalid
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "standard", "api" })
  @QueryFile(list = {})
  public void testWithStandardParams(String query) throws YADAResponseException, YADAQueryConfigurationException
  {
    Service svc = prepareTest(query);
    Assert.assertTrue(validate(svc.getYADARequest(), svc.execute()) ,  "Data invalid for query: "+query);
  }

  /**
   * Tests all json and standard queries using HTTP POST
   *
   * @param query the query to execute
   * @throws YADAExecutionException when the test fails
   */
  @SuppressWarnings("deprecation")
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "jsp" })
  @QueryFile(list = {})
  public void testWithHttpPost(String query) throws YADAExecutionException
  {
    logQuery(query);
    String method = null;
    HttpURLConnection connection = null;
    String target = "http://" + this.host + this.uri;
    try
    {
      URL url = new URL(target);
      String urlParameters = "";

      String[] params = query.split(AMP);
      for (int i = 0; i < params.length; i++)
      {
        String pair = params[i];
        String[] param = pair.split(EQUAL);
        urlParameters += encodeParam(param);
        if (i < params.length - 1)
          urlParameters += AMP;
        if (param[0].equals(YADARequest.PL_METHOD) || param[0].equals(YADARequest.PS_METHOD))
          method = param[1];
      }
      // }
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
      connection.setRequestProperty("Content-Language", "en-US");
      connection.setUseCaches(false);
      connection.setDoInput(true);
      connection.setDoOutput(true);

      // auth
      if (Boolean.parseBoolean(this.auth))
      {
        setAuthentication();
        connection.setRequestProperty("X-CSRF-Token", (String) this.secData.get("X-CSRF-Token"));
        connection.setRequestProperty("Authorization", (String) this.secData.get("Bearer"));
      }

      // Send request
      try(DataOutputStream wr = new DataOutputStream(connection.getOutputStream()))
      {
        wr.writeBytes(urlParameters);
        wr.flush();
      }

      // Get Response
      try(InputStream is = connection.getInputStream())
      {
        try(BufferedReader rd = new BufferedReader(new InputStreamReader(is)))
        {
          String line;
          StringBuffer result = new StringBuffer();
          while ((line = rd.readLine()) != null)
          {
            result.append(line);
          }
          try
          {
            if (method != null && method.equals(YADARequest.METHOD_UPDATE))
              Assert.assertTrue(validateInteger(result.toString(),true) ,  "Data invalid for query: "+query);
            else
              Assert.assertTrue(validateJSONResult(result.toString()) ,  "Data invalid for query: "+query);
          }
          catch (Exception e)
          {
            Assert.assertTrue(validateThirdPartyJSONResult(result.toString()) ,  "Data invalid for query: "+query);
          }
        }
      }
    }
    catch (MalformedURLException e)
    {
      throw new YADAExecutionException(e.getMessage(), e);
    }
    catch (IOException e)
    {
      throw new YADAExecutionException(e.getMessage(), e);
    }
    finally
    {
      if (connection != null)
      {
        connection.disconnect();
      }
    }
  }

  /**
   * Tests all json and standard queries using HTTP GET
   *
   * @param query the query to execute
   * @throws YADAExecutionException when the test fails
   */
  @SuppressWarnings("deprecation")
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "jsp" })
  @QueryFile(list = {})
  public void testWithHttpGet(String query) throws YADAExecutionException
  {
    logQuery(query);
    String method = null;
    HttpURLConnection connection = null;
    boolean pathStyle = query.startsWith("/");
    String target = "http://" + this.host + this.uri;
    try
    {
      if (pathStyle)
      {
        String[] params = query.split("/");
        String encQuery = "";
        for (int i = 0; i < params.length; i++)
        {
          if (params[i].length() > 0)
            encQuery += "/";
          encQuery += URLEncoder.encode(params[i], UTF8);
        }
        if (Boolean.parseBoolean(this.auth))
        {
          target = "http://" + this.host + this.uri.substring(0, this.uri.lastIndexOf('/')) + encQuery;
        }
        else
        {
          target = "http://" + this.host + encQuery;
        }
      }
      else
      {
        target += "?";
        String[] params = query.split(AMP);
        for (int i = 0; i < params.length; i++)
        {
          String pair = params[i];
          String[] param = pair.split(EQUAL);
          target += encodeParam(param);
          if (i < params.length - 1)
            target += AMP;
          if (param[0].equals(YADARequest.PL_METHOD) || param[0].equals(YADARequest.PS_METHOD))
            method = param[1];
        }
        // }
      }
      l.debug(target);
      URL url = new URL(target);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");


      // auth
      if (Boolean.parseBoolean(this.auth))
      {
        setAuthentication();
        connection.setRequestProperty("X-CSRF-Token", (String) this.secData.get("X-CSRF-Token"));
        connection.setRequestProperty("Authorization", (String) this.secData.get("Bearer"));
      }

      // Get Response
      try(InputStream is = connection.getInputStream())
      {
        try(BufferedReader rd = new BufferedReader(new InputStreamReader(is)))
        {
          String line;
          StringBuffer result = new StringBuffer();
          while ((line = rd.readLine()) != null)
          {
            result.append(line);
          }
          try
          {
            if (method != null && method.equals(YADARequest.METHOD_UPDATE))
              Assert.assertTrue(validateInteger(result.toString(),true) ,  "Data invalid for query: "+query);
            else
              Assert.assertTrue(validateJSONResult(result.toString()) ,  "Data invalid for query: "+query);
          }
          catch (Exception e)
          {
            Assert.assertTrue(validateThirdPartyJSONResult(result.toString()) ,  "Data invalid for query: "+query);
          }
        }
      }
    }
    catch (MalformedURLException e)
    {
      throw new YADAExecutionException(e.getMessage(), e);
    }
    catch (IOException e)
    {
      throw new YADAExecutionException(e.getMessage(), e);
    }
    finally
    {
      if (connection != null)
      {
        connection.disconnect();
      }
    }
  }

  /**
   * Tests CSV response format with json params
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws YADAResponseException when the test result is invalid
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "json", "options", "api", "sqlite_debug" })
  @QueryFile(list = {})
  public void testForCSV(String query) throws YADAResponseException, YADAQueryConfigurationException
  {
    Assert.assertTrue(validateCSVResult(prepareTest(query).execute()) ,  "Data invalid for query: "+query);
  }

  /**
   * Tests tab-delimited response with json params
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws YADAResponseException when the test result is invalid
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "json", "options", "api" })
  @QueryFile(list = {})
  public void testForTSV(String query) throws YADAResponseException, YADAQueryConfigurationException
  {
    Assert.assertTrue(validateTSVResult(prepareTest(query).execute()) ,  "Data invalid for query: "+query);
  }

  /**
   * Tests pipe-delimited response with json params
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws YADAResponseException when the test result is invalid
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "json", "options", "api" })
  @QueryFile(list = {})
  public void testForPSV(String query) throws YADAResponseException, YADAQueryConfigurationException
  {
    Assert.assertTrue(validatePSVResult(prepareTest(query).execute()) ,  "Data invalid for query: "+query);
  }

  /**
   * Tests XML response with json params
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws YADAResponseException when the test result is invalid
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "json", "options", "api" })
  @QueryFile(list = {})
  public void testForXML(String query) throws YADAResponseException, YADAQueryConfigurationException
  {
    Assert.assertTrue(validateXMLResult(prepareTest(query).execute()) ,  "Data invalid for query: "+query);
  }

  /**
   * Tests HTML response format with json params
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws YADAResponseException when the test result is invalid
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "json", "options", "api" })
  @QueryFile(list = {})
  public void testForHTML(String query) throws YADAResponseException, YADAQueryConfigurationException
  {
    Assert.assertTrue(validateHTMLResult(prepareTest(query).execute()) ,  "Data invalid for query: "+query);
  }

  /**
   * Tests Preprocessor plugin API
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws YADAResponseException when the test result is invalid
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "api", "plugins" })
  @QueryFile(list = {})
  public void testPreprocessor(String query) throws YADAResponseException, YADAQueryConfigurationException
  {
    Service svc = prepareTest(query);
    Assert.assertTrue(validate(svc.getYADARequest(), svc.execute()) ,  "Data invalid for query: "+query);
  }

  /**
   * Tests execution of a REST query using YADA as a proxy, and wraps results in
   * standard YADA JSON.
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws JSONException when the result does not conform
   * @throws YADAResponseException when the test result is invalid
   * @throws YADAExecutionException when the test execution fails
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "noproxy" })
  @QueryFile(list = {})
  public void testRESTExternal(String query) throws YADAResponseException, YADAQueryConfigurationException, YADAExecutionException
  {
    Service svc = prepareTest(query);
    Assert.assertTrue(validateThirdPartyJSONResult(svc.execute()) ,  "Data invalid for query: "+query);
  }

  /**
   * Tests execution of a REST query using YADA as a proxy, and using HTTP POST
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws JSONException when the result does not conform
   * @throws YADAResponseException when the test result is invalid
   * @throws YADAExecutionException when the test execution fails
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "api","noproxy" })
  @QueryFile(list = {})
  public void testRESTExternalPOST(String query) throws YADAResponseException, YADAQueryConfigurationException, YADAExecutionException
  {
    Service svc = prepareTest(query);
    YADARequest yadaReq = svc.getYADARequest();
    yadaReq.setMethod(new String[] {YADARequest.METHOD_POST});
    String result = svc.execute();
    Assert.assertTrue(validateThirdPartyJSONResult(result) ,  "Data invalid for query: "+query);
  }

  /**
   * Tests execution of a REST query using YADA as a proxy, and using HTTP PUT
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws JSONException when the result does not conform
   * @throws YADAResponseException when the test result is invalid
   * @throws YADAExecutionException when the test execution fails
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "api","noproxy" })
  @QueryFile(list = {})
  public void testRESTExternalPUT(String query) throws YADAResponseException, YADAQueryConfigurationException, YADAExecutionException
  {
    Service svc = prepareTest(query);
    YADARequest yadaReq = svc.getYADARequest();
    yadaReq.setMethod(new String[] {YADARequest.METHOD_PUT});
    String result = svc.execute();
    Assert.assertTrue(validateThirdPartyJSONResult(result) ,  "Data invalid for query: "+query);
  }

  /**
   * Tests execution of a REST query using YADA as a proxy, and using HTTP PATCH
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws JSONException when the result does not conform
   * @throws YADAResponseException when the test result is invalid
   * @throws YADAExecutionException when the test execution fails
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "api","noproxy" })
  @QueryFile(list = {})
  public void testRESTExternalPATCH(String query) throws YADAResponseException, YADAQueryConfigurationException, YADAExecutionException
  {
    Service svc = prepareTest(query);
    YADARequest yadaReq = svc.getYADARequest();
    yadaReq.setMethod(new String[] {YADARequest.METHOD_PATCH});
    String result = svc.execute();
    Assert.assertTrue(validateThirdPartyJSONResult(result) ,  "Data invalid for query: "+query);
  }

  /**
   * Tests execution of a REST query using YADA as a proxy, and using HTTP DELETE
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws JSONException when the result does not conform
   * @throws YADAResponseException when the test result is invalid
   * @throws YADAExecutionException when the test execution fails
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "api","noproxy" })
  @QueryFile(list = {})
  public void testRESTExternalDELETE(String query) throws YADAResponseException, YADAQueryConfigurationException, YADAExecutionException
  {
    Service svc = prepareTest(query);
    YADARequest yadaReq = svc.getYADARequest();
    yadaReq.setMethod(new String[] {YADARequest.METHOD_DELETE});
    String result = svc.execute();
    Assert.assertTrue(validateThirdPartyJSONResult(result) ,  "Data invalid for query: "+query);
  }

  /**
   * Tests execution of a REST query using YADA as a proxy, and wraps results in
   * standard YADA JSON.
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws JSONException when the result does not conform
   * @throws YADAResponseException when the test result is invalid
   * @throws YADAExecutionException when the test execution fails
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "proxy" })
  @QueryFile(list = {})
  public void testRESTExternalWithProxy(String query) throws YADAResponseException, YADAQueryConfigurationException, YADAExecutionException
  {
    Service svc = prepareTest(query);
    YADARequest yadaReq = svc.getYADARequest();
    yadaReq.setProxy(new String[] { this.proxy });
    Assert.assertTrue(validateThirdPartyJSONResult(svc.execute()) ,  "Data invalid for query: "+query);
  }

  /**
   * Tests execution of a REST query using YADA as a proxy, passing through the
   * query result unchanged
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws JSONException when the result does not conform
   * @throws YADAExecutionException when the test fails
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "noproxy" })
  @QueryFile(list = {})
  public void testRESTExternalPassThru(String query) throws YADAQueryConfigurationException, JSONException, YADAExecutionException
  {
    Service svc = prepareTest(query);
    YADARequest yadaReq = svc.getYADARequest();
    yadaReq.setResponse(new String[] { "RESTPassThruResponse" });
    Assert.assertTrue(validateThirdPartyJSONResult(svc.execute()) ,  "Data invalid for query: "+query);
  }

  /**
   * Tests execution of a REST query using YADA as a proxy, passing through the
   * query result unchanged
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws JSONException when the result does not conform
   * @throws YADAExecutionException when the test fails
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "proxy" })
  @QueryFile(list = {})
  public void testRESTExternalPassThruWithProxy(String query) throws YADAQueryConfigurationException, JSONException, YADAExecutionException
  {
    Service svc = prepareTest(query);
    YADARequest yadaReq = svc.getYADARequest();
    yadaReq.setProxy(new String[] { this.proxy });
    yadaReq.setResponse(new String[] { "RESTPassThruResponse" });
    Assert.assertTrue(validateThirdPartyJSONResult(svc.execute()) ,  "Data invalid for query: "+query);
  }

  /**
   * Tests Postprocessor plugin api
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "api", "plugins" })
  @QueryFile(list = {})
  public void testPostprocessor(String query) throws YADAQueryConfigurationException
  {
    YADARequest yadaReq = getYADAReq(query);
    Service svc = new Service(yadaReq);
    String result = svc.execute();
    Assert.assertTrue(result.equals("It worked."));
  }

  /**
   * Executes a Bypass plugin test
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "api", "plugins" })
  @QueryFile(list = {""})
  public void testBypass(String query) throws YADAQueryConfigurationException
  {
    Service svc = prepareTest(query);
    Assert.assertTrue(svc.execute().equals("Bypass worked."));
  }

  /**
   * Tests the ScriptPostprocessor API
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "api", "plugins" })
  @Assumption(methods = "isNotWindows")
  @QueryFile(list = {})
  public void testScriptPostprocessor(String query) throws YADAQueryConfigurationException
  {
    Service svc = prepareTest(query);
    JSONObject j = new JSONObject(svc.execute());
    logJSONResult(j);
    Assert.assertTrue(j.has("PLUGINWASHERE"),"The plugin appears to have failed.");
  }

  /**
   * Test the ScriptBypass API
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "api", "plugins" })
  @Assumption(methods = "isNotWindows")
  @QueryFile(list = {})
  public void testScriptBypass(String query) throws YADAQueryConfigurationException
  {
    Service svc = prepareTest(query);
    String result = svc.execute();
    logStringResult(result);
    Assert.assertEquals(result,"What is this, velvet?");
  }

  /**
   * Tests Script Preprocessor API
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "api", "plugins" })
  @Assumption(methods = "isNotWindows")
  @QueryFile(list = {})
  public void testScriptPreprocessor(String query) throws YADAQueryConfigurationException
  {
    Service svc = prepareTest(query);
    String result = svc.execute();
    JSONObject j = new JSONObject(result).getJSONObject(RESULTSET).getJSONArray(ROWS).getJSONObject(0);
    logJSONResult(j);
    Assert.assertTrue(j.has("APP") || j.has("app"),"The plugin appears to have failed."); // postgres lowercases without quotes around alias
  }

  /**
   * Tests Security API by causing failures (negative tests)
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   */
  @Test(enabled = false, dataProvider = "QueryTests", groups = { "api", "plugins" })
  @Assumption(methods = "isNotWindows")
  @QueryFile(list = {})
  public void testSecurityExceptions(String query) throws YADAQueryConfigurationException
  {
    if(query.equals("q=YADATEST test sec app property"))
      YADAUtils.executeYADAGet(new String[] {"YADA insert prop"}, new String[] {"YADATEST,protected,true"});
    else if(query.equals("q=YADATEST test sec query property"))
      YADAUtils.executeYADAGet(new String[] {"YADA insert prop"}, new String[] {"YADATEST test sec query property,protected,true"});

    Service svc = prepareTest(query);
    String result = svc.execute();

    if(query.equals("q=YADATEST test sec app property"))
      YADAUtils.executeYADAGet(new String[] {"YADA delete prop for target"}, new String[] {"YADATEST"});
    else if(query.equals("q=YADATEST test sec query property"))
      YADAUtils.executeYADAGet(new String[] {"YADA delete prop for target"}, new String[] {"YADATEST test sec query property"});

    JSONObject j = new JSONObject(result);
    logJSONResult(j);
    Assert.assertTrue(j.has("Exception") && j.get("Exception").equals("com.novartis.opensource.yada.plugin.YADASecurityException")
        && j.has("Message") && j.getString("Message").startsWith("Unable to process security spec"), "The Security API appears to have failed.");
  }

  /**
   * Tests Security API using default parameters
   *
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws YADAResponseException when the test result is invalid
   */
  @Test(enabled = false, dataProvider = "QueryTests", groups = { "api", "plugins" })
  @Assumption(methods = "isNotWindows")
  @QueryFile(list = {})
  public void testSecurityPlugins(String query) throws YADAQueryConfigurationException, YADAResponseException
  {
    Service svc = prepareTest(query);
    Assert.assertTrue(validate(svc.getYADARequest(), svc.execute()) ,  "Data invalid for query: "+query);
  }


  /**
   * Tests listing the files in the yada io/in mapped directory.
   *
   * @param query the parameter string issue by the data provider
   * @throws YADAQueryConfigurationException when there is a malformed query
   * @throws UnsupportedEncodingException when the query is not decodable as
   *         UTF-8
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "filesystem" })
  @QueryFile(list = {})
  public void testFileSystemDirectory(String query) throws YADAQueryConfigurationException, UnsupportedEncodingException
  {
    Service svc = prepareTest(query);
    String result = svc.execute();
    JSONObject jRes = new JSONObject(result);
    logJSONResult(jRes);
    JSONObject j = jRes.getJSONObject(RESULTSET).getJSONArray(ROWS).getJSONObject(0);
    Assert.assertTrue(j.has("path"),"The json result does not contain the expected key.");
  }

  /**
   * Tests reading text files in the yada io/in mapped directory.
   *
   * @param query the parameter string issue by the data provider
   * @throws YADAQueryConfigurationException when there is a malformed query
   * @throws UnsupportedEncodingException when the query is not decodable as
   *         UTF-8
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "filesystem" })
  @QueryFile(list = {})
  public void testFileSystemContentRead(String query) throws YADAQueryConfigurationException, UnsupportedEncodingException
  {
    Service svc = prepareTest(query);
    String result = svc.execute();
    JSONObject jRes = new JSONObject(result);
    logJSONResult(jRes);
    JSONObject j = jRes.getJSONObject(RESULTSET).getJSONArray(ROWS).getJSONObject(0);
    Assert.assertTrue(j.has("content"),"The json result does not contain the expected key.");
  }

  /**
   * Tests appending text to files in the yada io/in mapped directory.
   *
   * @param query the parameter string issue by the data provider
   * @throws YADAQueryConfigurationException when there is a malformed query
   * @throws UnsupportedEncodingException when the query is not decodable as
   *         UTF-8
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "filesystem" })
  @QueryFile(list = {})
  public void testFileSystemUpdate(String query) throws YADAQueryConfigurationException, UnsupportedEncodingException
  {
    Service svc = prepareTest(query);
    String result = svc.execute();

    String q = "q=YADAFSIN test read content&p=test.txt";
    svc = prepareTest(q);
    result = svc.execute();

    JSONObject jRes = new JSONObject(result);
    logJSONResult(jRes);
    String s = jRes.getJSONObject(RESULTSET).getJSONArray(ROWS).getJSONObject(0).getString("content");
    Assert.assertEquals(s,"writingappend","The json result does not contain the expected content.");
  }

  /**
   * Tests {@code harmonyMap} specs on REST queries using literal results for validation
   * @param query the parameter string issue by the data provider
   * @throws YADAQueryConfigurationException when there is a malformed query
   * @throws YADAResponseException when the test result is invalid
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "proxy" })
  @QueryFile(list = {})
  public void testHarmonizerWithREST(String query) throws YADAQueryConfigurationException, YADAResponseException
  {
    String[] qv = query.split("VSTR");
    String q = qv[0];
    // the doubly-escaped backslashes are just wierd
    // because there's only one in the src string, i.e. \n
    // I guess the vm is escaping the backlash, rather than
    // interpreting the newline.
    String expected = qv[1].replaceAll("\\\\n", "\n");
    Service svc = prepareTest(q);
    String actual = svc.execute();
    if(svc.getYADARequest().getFormat().equals(YADARequest.FORMAT_CSV))
      logStringResult(actual);
    else
      logJSONResult(new JSONObject(actual));
    Assert.assertEquals(actual, expected);
  }

  /**
   * Tests a miscellaneous string literal, e.g., question mark (?)
   * @param query the parameter string issue by the data provider
   * @throws YADAQueryConfigurationException when there is a malformed query
   * @throws YADAResponseException when the test result is invalid
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "standard", "api" })
  @QueryFile(list = {})
  public void testMiscellaneousStringLiteralStandard(String query) throws YADAQueryConfigurationException, YADAResponseException
  {
    String[] qv = query.split("VSTR");
    String q = qv[0];
    // the doubly-escaped backslashes are just wierd
    // because there's only one in the src string, i.e. \n
    // I guess the vm is escaping the backlash, rather than
    // interpreting the newline.
    String expected = qv[1].replaceAll("\\\\n", "\n");
    Service svc = prepareTest(q);
    String actual = svc.execute();
    if(svc.getYADARequest().getFormat().equals(YADARequest.FORMAT_CSV))
      logStringResult(actual);
    else
      logJSONResult(new JSONObject(actual));
    Assert.assertEquals(actual, expected);
  }

  /**
   * Tests many aspects of {@code harmonyMap} or {@code h} YADA parameter results including
   * for CSV: column counts, header values, row counts, row/column content; and for JSON:
   * singular result set, correct mapped/unmapped keys and values, record count.
   * @param query the query to execute
   * @throws YADAQueryConfigurationException when request creation fails
   * @throws YADAResponseException when the test result is invalid
   */
  @Test(enabled = true, dataProvider = "QueryTests", groups = { "options" })
  @QueryFile(list = {})
  public void testHarmonizer(String query) throws YADAQueryConfigurationException, YADAResponseException
  {
    String[] allKeys   = {COL_INTEGER, COL_INTEGER_LC, COL_HM_INT, COL_NUMBER, COL_NUMBER_LC, COL_HM_FLOAT, COL_DATE, COL_DATE_LC, COL_HM_DATE, COL_TIME, COL_TIME_LC, COL_HM_TIME};
    String[] intKeys   = {COL_INTEGER, COL_INTEGER_LC, COL_HM_INT};
    String[] floatKeys = {COL_NUMBER, COL_NUMBER_LC, COL_HM_FLOAT};
    String[] dateKeys  = {COL_DATE, COL_DATE_LC, COL_HM_DATE};
    String[] timeKeys  = {COL_TIME, COL_TIME_LC, COL_HM_TIME};
    Service svc        = prepareTest(query);
    YADARequest req    = svc.getYADARequest();
    req.setPageSize(new String[] {"-1"});
    JSONArray spec     = req.getHarmonyMap();
    String result      = svc.execute();

    int qCount = StringUtils.countMatches(query,"qname") + StringUtils.countMatches(query,"q=");
    String line = null;
    int lineCount = 0;
    if (req.getFormat().equals(YADARequest.FORMAT_CSV))
    {
      logStringResult(result);
      Pattern rx = Pattern.compile("^(\"([A-Z,]+)\"),(\"([0-9]+)\")?,(\"([0-9.]+)\")?,?(\"(201[3-5]-0[0-9]-[0-9]{2}(\\s00:00:00)?|1362373200|1396584000)\")?,?(\"(201[3-5]-0[0-9]-[0-9]{2} ([0-9]{2}:){2}[0-9]{2}|1441500273000)(\\.0+)?\")?$");
      // count columns
      // check for correct values in mapped columns

      try(BufferedReader br = new BufferedReader(new StringReader(result)))
      {
        while((line = br.readLine()) != null)
        {
          if(lineCount > 0)
          {
            Matcher m = rx.matcher(line);
            Assert.assertTrue(m.matches());
            // first query only returns three columns
            if(lineCount<9)
            {
              Assert.assertTrue(validateInteger(m.group(4))); // col 2
              Assert.assertTrue(validateNumber(m.group(6)));  // col 3
              Assert.assertNull(m.group(8)); // col 4
              Assert.assertNull(m.group(11)); // col 5
            }
            else if(lineCount > 8 && lineCount < 17)
            // 2nd query
            {
              Assert.assertNull(m.group(4));  // col 2
              Assert.assertNull(m.group(6));  // col 3
              Assert.assertTrue(validateDate(m.group(8)));  // col4
              Assert.assertTrue(validateTime(m.group(11))); // col5
            }
            else
            // 3rd query
            {
              Assert.assertNull(m.group(4));  // col 2
              Assert.assertNull(m.group(6));  // col 3
              Assert.assertNull(m.group(8));  // col 4
              Assert.assertTrue(validateTime(m.group(11))); // col5
            }
          }
          lineCount++;
        }
      }
      catch (IOException e)
      {
        throw new YADAResponseException("Result was unreadable.",e);
      }
      catch (ParseException e)
      {
        throw new YADAResponseException("Result was unparsable.",e);
      }

      //TODO confirm correct mapped/unmapped column headers

      // count rows
      Assert.assertEquals(lineCount - 1, qCount*8);

      //TODO check for "empty values" in unmapped columns

    }
    else if(req.getFormat().equals(YADARequest.FORMAT_XML))
    {
      //TODO harmony map xml validation
      logMarkupResult(result);
    }
    else if(req.getFormat().equals(YADARequest.FORMAT_HTML))
    {
      logMarkupResult(result);
      Pattern rx = Pattern.compile("^<tr>(<td>([A-Z,]+)</td>)(<td>([0-9]+)?</td>)(<td>([0-9.]+)?</td>)(<td>(201[3-5]-0[0-9]-[0-9]{2}(\\s00:00:00)?|1362373200|1396584000)?</td>)?(<td>((201[3-5]-0[0-9]-[0-9]{2} ([0-9]{2}:){2}[0-9]{2}|1441500273000)(\\.0+)?)?</td>)?</tr>$");
      Pattern end = Pattern.compile("^</tbody>|</table>|</body>|</html>$");
      try(BufferedReader br = new BufferedReader(new StringReader(result)))
      {
        while((line = br.readLine()) != null)
        {
          Matcher mEnd = end.matcher(line);
          if(lineCount > 9 && !mEnd.matches())
          {
            Matcher m = rx.matcher(line);
            Assert.assertTrue(m.matches());
            // first query only returns three columns
            if(lineCount < 18)
            {
              Assert.assertTrue(validateInteger(m.group(4))); // col 2
              Assert.assertTrue(validateNumber(m.group(6)));  // col 3
              Assert.assertNull(m.group(8)); // col 4
              Assert.assertNull(m.group(11)); // col 5
            }
            else if(lineCount > 17 && lineCount < 26)
            // 2nd query
            {
              Assert.assertNull(m.group(4));  // col 2
              Assert.assertNull(m.group(6));  // col 3
              Assert.assertTrue(validateDate(m.group(8)));  // col4
              Assert.assertTrue(validateTime(m.group(11))); // col5
            }
            else
            // 3rd query
            {
              Assert.assertNull(m.group(4));  // col 2
              Assert.assertNull(m.group(6));  // col 3
              Assert.assertNull(m.group(8));  // col 4
              Assert.assertTrue(validateTime(m.group(11))); // col5
            }
          }
          else
          {
            //TODO confirm correct mapped/unmapped column headers
          }
          lineCount++;
        }
      }
      catch (IOException e)
      {
        throw new YADAResponseException("Result was unreadable.",e);
      }
      catch (ParseException e)
      {
        throw new YADAResponseException("Result was unparsable.",e);
      }

      // count rows
      Assert.assertEquals(lineCount - 1, (qCount*8)+13); // adding 13 for non-data row html tags
    }
    else // JSON
    {
      JSONParamsEntry q;
      YADAParam p;
      qCount = 1;
      int resultCount = 8;
      if(YADAUtils.useJSONParams(req))
      {
        JSONParams jp = req.getJsonParams();
        String[] qnameKeys = jp.getKeys();
        qCount = qnameKeys.length;
        for(String qname : qnameKeys)
        {
          q = jp.get(qname);
          p = q.getParam(YADARequest.PS_HARMONYMAP);
          if(null == spec)
            spec = new JSONArray();
          if(null != p)
            spec.put(new JSONObject(p.getValue()));
        }
      }
      JSONObject jo = new JSONObject(result);
      logJSONResult(jo);

      // confirm singular result set
      Assert.assertNull(jo.optJSONObject(RESULTSETS));
      Assert.assertTrue(jo.has(RESULTSET));
//      // check record count
      int actualRecCount = jo.getJSONObject(RESULTSET).getInt(RECORDS);
      int expectRecCount = qCount*resultCount;
      Assert.assertEquals(actualRecCount, expectRecCount, "Result count invalid for query: "+query);

      // confirm correct mapped/unmapped keys
      JSONArray rows = jo.getJSONObject(RESULTSET).getJSONArray(ROWS);

      // For each query, find the hmap
      // test 8 records corresponding to query index
      // NOTE: This does not test for presence of unmapped keys, but does test all values
      for(int i=0;i<rows.length()/8;i++)          // 1-3 sets of 8
      {
        JSONObject currentSpec = new JSONObject();  // the hmap spec
        if(spec.length()==1)
          currentSpec = spec.getJSONObject(0);    // it's a global request param
        else
        {
          for(int j=spec.length()-1;j>=0;j--)
          {
            currentSpec = spec.getJSONObject(j);    // it's an embedded param, and JSONArray returns in reverse order
          }
        }


        // Deconstruct spec into keys and vals
        String[] currentSpecKeys = new String[currentSpec.length()];
        String[] currentSpecVals = new String[currentSpec.length()];
        int j=0;
        if(currentSpec.length() > 0)
        {
          for(String key : JSONObject.getNames(currentSpec))
          {
            currentSpecKeys[j] = key;
            currentSpecVals[j] = currentSpec.getString(key);
            j++;
          }
        }

        // check results
        for(j=0;j<resultCount;j++)            // for each set of results
        {
          JSONObject row = rows.getJSONObject(j); // the "row"
          String[] rowKeys = JSONObject.getNames(row);
          for(String key : rowKeys)               // iterate over the row keys
          {
            if(key.matches("[A-Z]+"))            // upper case are spec vals
              Assert.assertTrue(ArrayUtils.contains(currentSpecVals, key));  // row key is in current spec vals
            else
            {
              Assert.assertFalse(ArrayUtils.contains(currentSpecVals, key)); // row key is not current spec vals
              Assert.assertFalse(ArrayUtils.contains(currentSpecKeys, key)); // row key is in current spec keys
            }
          }

          for(String col : allKeys)             // confirm datatype of values
          {
            if(row.has(col))
            {
              try
              {
                if(ArrayUtils.contains(intKeys, col))
                  Assert.assertTrue(validateInteger(row.getString(col)));
                else if(ArrayUtils.contains(floatKeys, col))
                  Assert.assertTrue(validateNumber(row.getString(col)));
                else if(ArrayUtils.contains(dateKeys, col))
                  Assert.assertTrue(validateDate(row.getString(col)));
                else if(ArrayUtils.contains(timeKeys, col))
                  Assert.assertTrue(validateTime(row.getString(col)));
              }
              catch(ParseException e)
              {
                String msg = "Unable to validate result.";
                throw new YADAResponseException(msg, e);
              }
            }
          }
        }
      }
    }
  }

  /**
   * @param req the {@link YADARequest} containing the test query
   * @param result the {@link String} returned by {@link Service#execute()}
   * @return {@code true} if {@code result} conforms to the regular expression
   *         pattern corresponding to the value of
   *         {@link YADARequest#getFormat()}
   * @throws YADAResponseException when the test result is invalid
   */
  @SuppressWarnings("deprecation")
  public boolean validate(YADARequest req, String result) throws YADAResponseException
  {
    boolean isValid = false;
    if (req.getMethod().equals(YADARequest.METHOD_UPDATE))
    {
      isValid = validateInteger(result);
    }
    else if (req.getFormat().equals(YADARequest.FORMAT_CSV))
    {
      isValid = validateCSVResult(result);
    }
    else if (req.getFormat().equals(YADARequest.FORMAT_TSV))
    {
      isValid = validateTSVResult(result);
    }
    else if (req.getFormat().equals(YADARequest.FORMAT_PSV))
    {
      isValid = validatePSVResult(result);
    }
    else if (req.getFormat().equals(YADARequest.FORMAT_XML))
    {
      isValid = validateXMLResult(result);
    }
    else if (req.getFormat().equals(YADARequest.FORMAT_HTML))
    {
      isValid = validateHTMLResult(result);
    }
    else
    {
      isValid = validateJSONResult(result);
    }
    return isValid;
  }

  /**
   * As the name suggests, confirms that the value of {@code result} is an
   * integer, or throws an exception.
   *
   * @param result the {@link String} returned by {@link Service#execute()}
   * @param log {@code true} to print the value of the integer, {@code false} to suppress
   * @return {@code true} when the value of {@code result} can be converted to
   *         an {@link Integer}
   * @throws NumberFormatException when the value of {@code result} cannot be
   *         converted to an {@link Integer}
   */
  public boolean validateInteger(String result, boolean log) throws NumberFormatException
  {
    Integer.valueOf(result);
    if(log)
      logStringResult(result);
    return true;
  }

  /**
   * Calls {@link #validateInteger(String, boolean)} with {@code log=false}
   * @param result the {@link String} returned by {@link Service#execute()}
   * @return {@code true} when the value of {@code result} can be converted to
   *         an {@link Integer}
   * @throws NumberFormatException when the value of {@code result} cannot be
   *         converted to an {@link Integer}
   */
  public boolean validateInteger(String result) throws NumberFormatException
  {
    return validateInteger(result,false);
  }

  /**
   * As the name suggests, confirms that the value of {@code result} is a float,
   * or throws an exception.
   *
   * @param result the {@link String} returned by {@link Service#execute()}
   * @return {@code true} when the value of {@code result} can be converted to a
   *         {@link Float}
   * @throws NumberFormatException when the value of {@code result} cannot be
   *         converted to a {@link Float}
   */
  public boolean validateNumber(String result) throws NumberFormatException
  {
    if (!result.contains("."))
      return false;
    Float.valueOf(result);
    return true;
  }

  /**
   * As the name suggests, confirms that the value of {@code result} is a
   * parsable date, or throws an exception. The method uses {@link DateFormat}
   * with the default leniency ({@code true})
   * <p>
   * Valid values for date fields are {@code 2015-03-04 00:00:00 (1362373200000L)}
   * and {@code 2015-04-14 00:00:00 (1365912000000L)}
   * </p>
   *
   * @param result the {@link String} returned by {@link Service#execute()}
   * @return {@code true} when the value of {@code result} can be converted to a
   *         {@link java.util.Date}
   * @throws ParseException when the value of {@code result} cannot be converted
   *         to a {@link java.util.Date}
   */
  public boolean validateDate(String result) throws ParseException
  {
    java.util.Date d;
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    try
    {
      d = df.parse(result);
    }
    catch (ParseException e)
    {
      long millis = Long.valueOf(result).longValue()*1000;
      d = new java.util.Date(millis);
      df.format(d);
    }
    // Use `date -jf %Y%m%d%H%M%S YYYYmmdd000000 +%s` in shell to find seconds,
    // then convert to milliseconds for below values, e.g.: date -jf %Y%m%d%H%M%S 20130304000000 +%s
    return d.getTime() == 1362373200000L || d.getTime() == 1365912000000L || d.getTime() == 1396584000000L;
  }

  /**
   * As the name suggests, confirms that the value of {@code result} is a
   * parsable time, or throws an exception. The method uses {@link DateFormat}
   * with the default leniency ({@code true})
   * <p>
   * Valid values for time fields are {@code 2015-09-05 20:44:33 (1441500273000L)}
   * and {@code 2015-09-05 20:44:46 (1441500286000L)}
   * </p>
   * @param result the {@link String} returned by {@link Service#execute()}
   * @return {@code true} when the value of {@code result} can be converted to a
   *         {@link java.util.Date}
   * @throws ParseException when the value of {@code result} cannot be converted
   *         to a {@link java.util.Date}
   */
  public boolean validateTime(String result) throws ParseException
  {
    java.util.Date d;
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    try
    {
      d = df.parse(result);
    }
    catch(ParseException e)
    {
      df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S"); // mysql returns the 100-milli precision
      try
      {
        d = df.parse(result);
      }
      catch(ParseException e1)
      {
        long millis = Long.valueOf(result).longValue();
        d = new java.util.Date(millis);
        df.format(d);
      }
    }
    // Use `date -jf %Y%m%d%H%M%S YYYYmmdd000000 +%s` in shell to find seconds,
    // then convert to milliseconds for below values, e.g.: date -jf %Y%m%d%H%M%S 20150905204433 +%s
    return d.getTime() == 1441500273000L || d.getTime() == 1441500286000L || d.getTime() == 1378428299000L;
  }


  /**
   * Evaluates the query result content to ensure data values are consistent with expected type and
   * for date and time, with value.
   *
   * @param j the {@link JSONObject} to evaluate
   * @return {@code true} if each column's data type is preserved and values are parsable
   * @throws YADAResponseException when {@code j} is malformed or contains invalid data
   */
  public boolean validateJSONData(JSONObject j) throws YADAResponseException
  {
    try
    {
      if (j.has(COL_INTEGER))
        return validateInteger(j.getString(COL_INTEGER))
            && validateNumber(j.getString(COL_NUMBER))
            && validateDate(j.getString(COL_DATE))
            && validateTime(j.getString(COL_TIME));
      return validateInteger(j.getString(COL_INTEGER_LC))
          && validateNumber(j.getString(COL_NUMBER_LC))
          && validateDate(j.getString(COL_DATE_LC))
          && validateTime(j.getString(COL_TIME_LC));
    }
    catch (JSONException e)
    {
      String msg = "Unable to parse JSON result.";
      throw new YADAResponseException(msg, e);
    }
    catch (ParseException e)
    {
      String msg = "Unable to validate result content.";
      throw new YADAResponseException(msg, e);
    }
  }

  /**
   * Evaluates the query result content to ensure data values are consistent with expected type and
   * for date and time, with value.
   * @param result the subset of data (usu. one line) returned by the query
   * @param delimiter the character separating columns, i.e., tab, pipe, comma, etc.
   *
   * @return {@code true} if each column's data type is preserved and values are parsable
   * @throws YADAResponseException when {@code result} is malformed or contains invalid data
   */
  public boolean validateDelimitedData(String result, String delimiter) throws YADAResponseException
  {
    try
    {
      String[] res = result.split(delimiter);
      for (int i = 1; i < res.length; i++)
      {
        String val = res[i].replaceAll("^\"|\"$", "");
        switch (i)
        {
        case 1:
          validateInteger(val);
          break;
        case 2:
          validateNumber(val);
          break;
        case 3:
          validateDate(val);
          break;
        case 4:
          validateTime(val);
          break;
        default:
          break;
        }
      }
    }
    catch (ParseException e)
    {
      String msg = "Unable to validate result content.";
      throw new YADAResponseException(msg, e);
    }
    return true;
  }

  /**
   * Validation method for JSON response. Confirms the following:
   * <ul>
   * <li>Result is well-formed JSON</li>
   * <li>Result has "RESULTSETS" key, and has "total" key with value > 0, or</li>
   * <li>Result has "RESULTSET" key, and has "total" key with value > 0, or</li>
   * <li>Result has "RESULTSET" key (but no "total" key)</li>
   * </ul>
   *
   * @param result the query result
   * @return {@code true} if result complies with expected output spec
   * @throws YADAResponseException when the test produces malformed results
   */
  public boolean validateJSONResult(String result) throws YADAResponseException
  {
    JSONObject res = new JSONObject(result);
    logJSONResult(res);
    if (res.has("Exception"))
      throw new YADAResponseException(res.getString("StackTrace"));
    else if (res.has(RESULTSETS))
    {
      JSONArray sets = res.getJSONArray(RESULTSETS);
      if (!sets.getJSONObject(0).has(RESULTSET)
          || !sets.getJSONObject(0).getJSONObject(RESULTSET).has("total")
          || sets.getJSONObject(0).getJSONObject(RESULTSET).getInt("total") == 0)
        throw new YADAResponseException("Test produced malformed or zero results.  Check query parameter values first.");
      for(int i=0;i<sets.length();i++)
      {
        if(sets.getJSONObject(i).getJSONObject(RESULTSET).has(ROWS)
          && sets.getJSONObject(i).getJSONObject(RESULTSET).getJSONArray(ROWS).length() > 0)
          return validateJSONData(sets.getJSONObject(i).getJSONObject(RESULTSET).getJSONArray(ROWS).getJSONObject(0));
      }
      return sets.getJSONObject(0).getJSONObject(RESULTSET).getInt("total") > 0;
    }
    else if (!res.has(RESULTSET) || (res.getJSONObject(RESULTSET).has("total") && res.getJSONObject(RESULTSET).getInt("total") == 0))
      throw new YADAResponseException("Test produced malformed or zero results.  Check query parameter values first.");
    else if (res.has(RESULTSET))
    {
      if(res.getJSONObject(RESULTSET).has(ROWS))
      {
      	try
      	{
      		return validateJSONData(res.getJSONObject(RESULTSET).getJSONArray(ROWS).getJSONObject(0));
      	}
      	catch(JSONException e)
      	{
      		throw new YADAResponseException("Test produced results that do not contain valid JSON objects in ROWS.  Try using validateThirdPartyJSONResult.");
      	}
      }

      return res.getJSONObject(RESULTSET).getInt("total") > 0;
    }
    else
      throw new YADAResponseException("Test produced malformed results:" + result);
  }

  /**
   * Validation method for external REST result
   *
   * @param result the query result
   * @return {@code true} if result complies with expected output spec
   * @throws YADAExecutionException when the test fails
   */
  public boolean validateThirdPartyJSONResult(String result) throws YADAExecutionException
  {
    try
    {
      JSONArray res = new JSONArray(result);
      logJSONResult(res);
    }
    catch (JSONException e)
    {
      try
      {
        JSONObject res = new JSONObject(result);
        if (res.has("Exception"))
          throw new YADAExecutionException(res.toString());
        logJSONResult(res);
      }
      catch (JSONException e1)
      {
        throw new YADAExecutionException();
      }
    }
    return true;
  }

  /**
   * Validation method for CSV response
   *
   * @param result the query result
   * @return {@code true} if result complies with expected output spec
   * @throws YADAResponseException when the result is unreadable or does not
   *         conform to the expected syntax
   * */
  public boolean validateCSVResult(String result) throws YADAResponseException
  {
    try(BufferedReader rd = new BufferedReader(new StringReader(result)))
    {
      String line;
      int lineNum = 0;
      String header = "";

      while ((line = rd.readLine()) != null)
      {
        if (lineNum == 0)
        {
          header = line;
        }
        else if (lineNum > 0)
        {
          Matcher m = CSV.matcher(line);
          if (!(line.equals(header) || m.matches()))
          {
            String msg = "Results do not conform to CSV format.\n" + line;
            throw new YADAResponseException(msg);
          }
          if (lineNum == 1)
            validateDelimitedData(line, YADARequest.FORMAT_CSV_STRING);
        }
        logStringResult(line);
        lineNum++;
      }
    }
    catch (IOException e1)
    {
      String msg = "Unable to read the result.";
      throw new YADAResponseException(msg, e1);
    }
    return true;
  }

  /**
   * Validation method for tab-delimited response
   *
   * @param result the query result
   * @return {@code true} if result complies with expected output spec
   * @throws YADAResponseException when the test result is invalid
   */
  public boolean validateTSVResult(String result) throws YADAResponseException
  {
    try(BufferedReader rd = new BufferedReader(new StringReader(result)))
    {
      String line;
      int lineNum = 0;
      String header = "";
      while ((line = rd.readLine()) != null)
      {
        if (lineNum == 0)
        {
          header = line;
        }
        else if (lineNum > 0)
        {
          Matcher m = TSV.matcher(line);
          if (!(line.equals(header) || m.matches()))
          {
            String msg = "Results do not conform to TSV/tab format.\n" + line;
            throw new YADAResponseException(msg);
          }
          if (lineNum == 1)
            validateDelimitedData(line, YADARequest.FORMAT_TSV_STRING);
        }
        logStringResult(line);
        lineNum++;
      }
    }
    catch (IOException e1)
    {
      String msg = "Unable to read the result.";
      throw new YADAResponseException(msg, e1);
    }
    return true;
  }

  /**
   * Validation method for pipe-delimited response
   *
   * @param result the query result
   * @return {@code true} if result complies with expected output spec
   * @throws YADAResponseException when the test result is invalid
   */
  public boolean validatePSVResult(String result) throws YADAResponseException
  {
    try(BufferedReader rd = new BufferedReader(new StringReader(result)))
    {
      String line;
      int lineNum = 0;
      String header = "";
      while ((line = rd.readLine()) != null)
      {
        if (lineNum == 0)
        {
          header = line;
        }
        else if (lineNum > 0)
        {
          Matcher m = PSV.matcher(line);
          if (!(line.equals(header) || m.matches()))
          {
            String msg = "Results do not conform to PSV/Pipe format.\n" + line;
            throw new YADAResponseException(msg);
          }
          if (lineNum == 1)
            // need to escape the pipe and it's preceding backslash in the regex
            validateDelimitedData(line, "\\\\\\"+YADARequest.FORMAT_PIPE_STRING);
        }
        logStringResult(line);
        lineNum++;
      }
    }
    catch (IOException e1)
    {
      String msg = "Unable to read the result.";
      throw new YADAResponseException(msg, e1);
    }
    return true;
  }

  /**
   * Validation method for HTML response.  Unlike other formats, HTML is still validated only on syntax and not content.
   *
   * @param result the query result
   * @return {@code true} if result complies with expected output spec
   * @throws YADAResponseException when the test result is invalid
   */
  public boolean validateHTMLResult(String result) throws YADAResponseException
  {
    logMarkupResult(result);
    String error = "Results do not conform to HTML format";
    try(BufferedReader rd = new BufferedReader(new StringReader(result)))
    {
      String line;
      int counter = 0;
      try
      {
        while ((line = rd.readLine()) != null)
        {
          switch (counter++)
          {
          case 0:
            if (!line.startsWith("<html>"))
              throw new YADAResponseException(error);
            break;
          case 1:
            if (!line.startsWith("<head>"))
              throw new YADAResponseException(error);
            break;
          case 3:
            if (!line.startsWith("</head>"))
              throw new YADAResponseException(error);
            break;
          case 4:
            if (!line.startsWith("<body>"))
              throw new YADAResponseException(error);
            break;
          case 5:
            if (!line.startsWith("<table"))
              throw new YADAResponseException(error);
            break;
          default:
            break;
          }
        }
      }
      catch (IOException e)
      {
        String msg = "Unable to read the result.";
        throw new YADAResponseException(msg, e);
      }
    }
    catch (IOException e1)
    {
      String msg = "Unable to read the result.";
      throw new YADAResponseException(msg, e1);
    }
    return true;
  }

  /**
   * Validation method for XML response
   *
   * @param result the query result
   * @return {@code true} if result complies with expected output spec
   * @throws YADAResponseException when the test result is invalid
   */
  public boolean validateXMLResult(String result) throws YADAResponseException
  {
    try
    {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(new InputSource(new StringReader(result)));
      XPathFactory xPathfactory = XPathFactory.newInstance();
      XPath xpath = xPathfactory.newXPath();
      XPathExpression resultSets = xpath.compile("/RESULTSETS");
      XPathExpression nestedTotal = xpath.compile("//RESULTSET/@total[1]");
      XPathExpression resultSet = xpath.compile("/RESULTSET");
      XPathExpression colCase   = xpath.compile("//"+COL_STRING+"[1]");
      String evalSets = resultSets.evaluate(doc);
      String evalSet  = resultSet.evaluate(doc);
      XPathExpression colInt = xpath.compile("//"+COL_INTEGER+"[1]");
      XPathExpression colNum = xpath.compile("//"+COL_NUMBER+"[1]");
      XPathExpression colDat = xpath.compile("//"+COL_DATE+"[1]");
      XPathExpression colTim = xpath.compile("//"+COL_TIME+"[1]");
      if ((evalSets != null && evalSets.length() > 0) || (evalSet != null && evalSet.length() > 0))
      {
        String eval = nestedTotal.evaluate(doc);
        if (eval != null && eval.length() > 0 && Integer.parseInt(eval) > 0)
        {
          logMarkupResult(result);
          String evalCol  = colCase.evaluate(doc);
          if(evalCol == null || evalCol.length() == 0)
          {
            colInt = xpath.compile("//"+COL_INTEGER_LC+"[1]");
            colNum = xpath.compile("//"+COL_NUMBER_LC+"[1]");
            colDat = xpath.compile("//"+COL_DATE_LC+"[1]");
            colTim = xpath.compile("//"+COL_TIME_LC+"[1]");
          }
          try
          {
            return validateInteger(colInt.evaluate(doc))
                && validateNumber(colNum.evaluate(doc))
                && validateDate(colDat.evaluate(doc))
                && validateTime(colTim.evaluate(doc));
          }
          catch (NumberFormatException e)
          {
            String msg = "Unable to validate result content.";
            throw new YADAResponseException(msg, e);
          }
          catch (ParseException e)
          {
            String msg = "Unable to validate result content.";
            throw new YADAResponseException(msg, e);
          }
        }
      }
    }
    catch (ParserConfigurationException e)
    {
      throw new YADAResponseException("XML Document creation failed.", e);
    }
    catch (SAXException e)
    {
      throw new YADAResponseException("XML Document creation failed.", e);
    }
    catch (IOException e)
    {
      throw new YADAResponseException("XML Document creation failed.", e);
    }
    catch (XPathExpressionException e)
    {
      throw new YADAResponseException("Results do not contain expected XML content", e);
    }
    return true;
  }

  /**
   * Convenience method for printing out query name during processing.
   * @param query the query string to process
   */
  public static void logQuery(String query)
  {
    //if(System.getProperty(LOG_STDOUT) != null )
    System.out.println("\nQuery: " + query + " ");
    System.out.print(".");
  }

  /**
   * Convenience method to output results from tests
   *
   * @param res the JSON array containing the query result
   */
  public static void logJSONResult(JSONArray res)
  {
    logStringResult(res.toString(2));
  }

  /**
   * Convenience method to output results from tests
   *
   * @param res the JSON object containing the query result
   */
  public static void logJSONResult(JSONObject res)
  {
    logStringResult(res.toString(2));
  }

  /**
   * Convenience method to output results from tests
   *
   * @param res the String object containing the query result
   */
  public static void logStringResult(String res)
  {
    if(System.getProperty(LOG_STDOUT) != null )
      System.out.println(res);
    Reporter.log("<pre>" + res + "</pre>");
  }

  /**
   * Convenience method to output results from tests
   *
   * @param res the String object containing the query result
   */
  public static void logMarkupResult(String res)
  {
    if(System.getProperty(LOG_STDOUT) != null )
      System.out.println(res);
    Reporter.log("<textarea style=\"border:none;\">" + res + "</textarea>");
  }
}
