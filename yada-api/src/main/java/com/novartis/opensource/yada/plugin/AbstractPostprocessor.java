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
package com.novartis.opensource.yada.plugin;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADARequestException;
import com.novartis.opensource.yada.YADASecurityException;

/**
 * @author David Varon
 * @since 0.4.2 Preprocess, Validation, Authorization, TokenValidator,
 *        ExecutionPolicy, ContentPolicy
 */
public abstract class AbstractPostprocessor implements Postprocess, Authorization, TokenValidator {

  /**
   * A constant equal to {@value} for handling param value syntax
   */
  private static final String RX_NOTJSON = "^[^{].+$";

  /**
   * Constant with value: {@value}
   *
   * @since 8.7.6
   */
  protected final static String RESULT_KEY_RESULTSET = "RESULTSET";

  /**
   * Constant with value: {@value}
   *
   * @since 8.7.6
   */
  protected final static String RESULT_KEY_RECORDS = "records";

  /**
   * Constant with value: {@value}
   *
   * @since 8.7.6
   */
  protected final static String RESULT_KEY_ROWS = "ROWS";

  /**
   * Constant with value: {@value}
   *
   * @since 8.7.6
   */
  protected final static String RESULT_KEY_RESOURCE = "resource";

  /**
   * The {@link YADARequest} object processed by the plugin
   */
  private YADARequest yadaReq;

  /**
   * The {@link YADAQuery} object processed by the plugin
   */
  private YADAQuery yq;

  /**
   * parsed HTTPHeaders string
   * 
   * @since 8.7.6
   */
  private JSONObject httpHeaders;

  /**
   * The authentication token, e.g., user id
   * 
   * @since 8.7.6
   */
  private Object token = null;

  /**
   * The {@link TokenValidator}
   * 
   * @since 8.7.6
   */
  private TokenValidator tokenValidator;

  /**
   * The {@code args} {@link List} from {@link YADARequest#getArgs}
   * 
   * @since 8.7.6
   */
  private List<String> args;

  /**
   * The request object provided by Tomcat
   */
  private HttpServletRequest request;

  /**
   * Null implementation
   * 
   * @see com.novartis.opensource.yada.plugin.Postprocess#engage(com.novartis.opensource.yada.YADAQuery)
   */
  @Override
  public void engage(YADAQuery yq) throws YADAPluginException {
    /* nothing to do */ }

  /**
   * Base implementation returns {@code #result}
   * 
   * @throws YADAPluginException when there is a processing error
   * @see com.novartis.opensource.yada.plugin.Postprocess#engage(com.novartis.opensource.yada.YADARequest,
   *      java.lang.String)
   */
  @Override
  public String engage(YADARequest yadaReq, String result) throws YADAPluginException {
    // return the query result
    return result;
  }

  /**
   * Convenience method with calls {@link #validateToken()},
   * {@link #authorize(String)},
   * 
   * @since 8.7.6
   */
  @Override
  public void authorizeYADARequest(YADARequest yadaReq, String result) throws YADASecurityException {

    // default impl does nothing - override in authorizer plugin
    obtainToken(yadaReq);

    // default impl does nothing - override in authorizer plugin
    validateToken();

    // default impl does nothing - override in authorizer plugin
    authorize(result);

  }

  /**
   * Authorization of query use for given context
   * {@link Authorization#authorize()}
   * 
   * @since 8.7.6
   */
  @Override
  public void authorize() throws YADASecurityException {
    /* nothing to do */ }

  /**
   * Authorization of general use for given context
   * {@link Authorization#authorize()} Not implemented in preprocessor
   * 
   * @since 8.7.6
   */
  @Override
  public void authorize(String payload) throws YADASecurityException {
    // nothing to do
  }

  /**
   * Default implementation calls {@link TokenValidator#validateToken()} via
   * injection
   * 
   * @since 8.7.6
   */
  @Override
  public void validateToken() throws YADASecurityException {
    // nothing to do
  }

  /**
   * @param cookie
   * @return the requested <code>cookie</code>
   * 
   * @since 8.7.6
   */
  public String getCookie(String cookie) {
    Cookie[] cookies = getYADARequest().getRequest().getCookies();
    if (cookies != null)
    {
      for (Cookie c: cookies)
      {
        if (c.getName().equals(cookie))
        {
          return c.getValue();
        }
      }
    }
    return null;
  }

  /**
   * Default implementation intended for override
   * 
   * @throws YADASecurityException
   * 
   * @since 8.7.6
   */
  @Override
  public void setToken() throws YADASecurityException {
    // nothing to do
  }

  /**
   * Standard mutator for variable
   * 
   * @param token the value of the authentication token
   * @throws YADASecurityException
   * @since 8.7.6
   */
  @Override
  public void setToken(Object token) throws YADASecurityException {
    this.token = token;
  }

  /**
   * Standard accessor for variable
   * 
   * @return the value of the validated header
   * @since 8.7.6
   */
  @Override
  public Object getToken() {
    return this.token;
  }

  /**
   * @param header
   * @return the requested <code>header</code>
   * 
   * @since 8.7.6
   */
  public String getHeader(String header) {
    return getYADARequest().getRequest().getHeader(header);
  }

  /**
   * @return the yadaReq
   */
  public YADARequest getYADARequest() {
    return yadaReq;
  }

  /**
   * @param yadaReq the yadaReq to set
   */
  public void setYADARequest(YADARequest yadaReq) {
    this.yadaReq = yadaReq;
  }

  /**
   * Standard mutator for variable
   * 
   * @param yq the {@link YADAQuery} to which this preprocessor is attached
   */
  public void setYADAQuery(YADAQuery yq) {
    this.yq = yq;
  }

  /**
   * Standard accessor for variable
   * 
   * @return the {@link YADAQuery} to which this preprocessor is attached
   */
  public YADAQuery getYADAQuery() {
    return this.yq;
  }

  /**
   * Array mutator for variable, preferred for compatibility with
   * {@link javax.servlet.http.HttpServletRequest#getParameterMap()} Converts
   * parameter string into {@link JSONObject}
   * 
   * @param httpHeaders
   * @throws YADARequestException when the header string is malformed
   * @since 8.7.6
   */
  public void setHTTPHeaders(String[] httpHeaders) throws YADARequestException {
    Matcher             m1         = Pattern.compile(RX_NOTJSON).matcher(httpHeaders.toString());
    Map<String, String> reqHeaders = new HashMap<String, String>();
    // ignore key case
    // api circumvents http request so check for null
    if (null != getRequest())
    {
      @SuppressWarnings("unchecked")
      Enumeration<String> hdrNames = getRequest().getHeaderNames();
      while (hdrNames.hasMoreElements())
      {
        String name = hdrNames.nextElement();
        reqHeaders.put(name.toLowerCase(), getRequest().getHeader(name));
      }
    }
    else // extract headers from YADARequest--it's an api call 
    {
      for(String name: JSONObject.getNames(this.getYADARequest().getHttpHeaders()))
      {
        reqHeaders.put(name.toLowerCase(), this.getYADARequest().getHttpHeaders().getString(name));
      }
    }

    if (m1.matches()) // it's a list of header names
    {
      this.httpHeaders = new JSONObject();
      for (String name: httpHeaders)
      {
        String value = reqHeaders.get(name.toLowerCase());// != null ? reqHeaders.get(name.toLowerCase()) : "";
        this.httpHeaders.put(name, value);
      }
    }
    else // it's a json object
    {
      try
      {
        this.httpHeaders = new JSONObject(httpHeaders);
        JSONArray names  = this.httpHeaders.names();
        JSONArray vals   = this.httpHeaders.toJSONArray(names);
        for (int i = 0; i < vals.length(); i++)
        {
          if (vals.optBoolean(i))
          {
            String name = names.getString(i);
            String value = reqHeaders.get(name.toLowerCase()); // != null ? reqHeaders.get(name.toLowerCase()) : "";
            this.httpHeaders.put(name, value);
          }
        }
      }
      catch (JSONException e)
      {
        String msg = "The HTTPHeaders specification is not valid JSON:\n\n" + httpHeaders[0];
        throw new YADARequestException(msg, e);
      }
    }

  }

  /**
   * Returns the {@code HTTPHeaders} or {@code H} parameter value as a
   * {@link JSONObject}
   * 
   * @return a {@link JSONObject} built from the value of the {@code HTTPHeaders}
   *         or {@code H} parameter value
   * @since 8.7.6
   */
  public JSONObject getHttpHeaders() {
    return this.httpHeaders;
  }

  /**
   * Returns {@code true} if {@link #httpHeaders} contains a header array entry,
   * otherwise {@code false}
   * 
   * @return {@code true} if {@link #httpHeaders} contains a header array entry,
   *         otherwise {@code false}
   * @since 8.7.6
   */
  public boolean hasHttpHeaders() {
    if (null == this.getHttpHeaders() || this.getHttpHeaders().length() == 0)
    {
      return false;
    }
    return true;
  }

  /**
   * @return the request
   */
  public HttpServletRequest getRequest() {
    return this.request;
  }

  /**
   * @param request the request to set
   */
  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }

  /**
   * @return the args
   */
  public List<String> getArgs() {
    return args;
  }

  /**
   * @param args the args to set
   */
  public void setArgs(List<String> args) {
    this.args = args;
  }

  /**
   * @return the tokenValidator
   */
  public TokenValidator getTokenValidator() {
    return tokenValidator;
  }

  /**
   * @param tokenValidator the tokenValidator to set
   */
  public void setTokenValidator(TokenValidator tokenValidator) {
    this.tokenValidator = tokenValidator;
  }

  /**
   * @since 8.7.6
   */
  @Override
  public void obtainToken(YADARequest yadaReq) throws YADASecurityException {
    // nothing to do
  }

}
