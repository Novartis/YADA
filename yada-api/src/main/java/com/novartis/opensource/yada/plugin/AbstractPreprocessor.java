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

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.novartis.opensource.yada.ConnectionFactory;
import com.novartis.opensource.yada.YADAConnectionException;
import com.novartis.opensource.yada.YADAParam;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADARequestException;
import com.novartis.opensource.yada.YADASQLException;
import com.novartis.opensource.yada.YADASecurityException;
import com.novartis.opensource.yada.YADASecuritySpec;
import com.novartis.opensource.yada.util.YADAUtils;

/**
 * @author David Varon
 * @since 0.4.2
 */
public abstract class AbstractPreprocessor
    implements Preprocess, Validation, Authorization, TokenValidator, ExecutionPolicy, ContentPolicy {

  /**
   * Constant equal to {@value}
   * 
   * @since 7.0.0
   */
  private static final String PLUGIN_PKG = "com.novartis.opensource.yada.plugin.";

  /**
   * Constant equal to {@value}
   * 
   * @since 7.0.0
   */
  protected static final String AUTH_PATH_RX = "auth.path.rx";

  /**
   * Constant equal to {@value}
   * 
   * @since 7.0.0
   */
  protected static final String AUTH_TOKEN = "auth.token";

  /**
   * Constant equal to {@value}
   * 
   * @since 7.0.0
   */
  protected static final String TOKEN_VALIDATOR = "token.validator";

  /**
   * Constant equal to {@value}
   * 
   * @since 7.0.0
   */
  protected static final String CONTENT_POLICY = "content.policy";

  /**
   * Constant equal to {@value}
   * 
   * @since 7.0.0
   */
  protected static final String EXECUTION_POLICY = "execution.policy";

  /**
   * Constant equal to {@value}
   * 
   * @since 7.0.0
   */
  protected static final String EXECUTION_POLICY_INTERFACE = "ExecutionPolicy";

  /**
   * Constant equal to {@value}
   * 
   * @since 7.0.0
   */
  protected static final String CONTENT_POLICY_INTERFACE = "ContentPolicy";

  /**
   * Constant equal to {@value}
   * 
   * @since 7.0.0
   */
  protected static final String VOID = "void";

  /**
   * A constant equal to {@value} for handling param value syntax
   */
  private static final String RX_NOTJSON = "^[^{].+$";

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
   * @since 8.5.0
   */
  private JSONObject httpHeaders;

  /**
   * The authentication token, e.g., user id
   * 
   * @since 7.0.0
   */
  private Object token = null;

  /**
   * The {@link TokenValidator}
   * 
   * @since 7.0.0
   */
  private TokenValidator tokenValidator;

  /**
   * The {@link SecurityPolicy}
   * 
   * @since 7.0.0
   */
  private SecurityPolicy securityPolicy;
  
  /**
   * @since 9.0.0
   */
  private YADASecuritySpec securitySpec;

  /**
   * The {@code args} {@link List} from {@link YADARequest#getArgs}
   * 
   * @since 7.0.0
   */
  private List<String> args;

  /**
   * The request object provided by Tomcat
   */
  private HttpServletRequest request;
  /**
   * The {@code preArgs} {@link List} from {@link YADARequest#getPreArgs}
   * 
   * @since 7.0.0
   * @deprecated as of 7.1.0
   */
  @Deprecated
  private List<String>       preargs;

  /**
   * Base implementation, calls {@link #setYADARequest(YADARequest)},
   * {@link #setRequest(HttpServletRequest)}, {@link #setArgs(List)},
   * {@link #setPreargs(List)} and returns {@link #getYADARequest()}
   * 
   * @see com.novartis.opensource.yada.plugin.Preprocess#engage(com.novartis.opensource.yada.YADARequest)
   */
  @Override
  public YADARequest engage(YADARequest yadaReq) throws YADAPluginException {
    setYADARequest(yadaReq);
    setRequest(yadaReq.getRequest());
    String[] args = getYADAQuery().getYADAQueryParamValue(YADARequest.PS_ARGLIST);
    if (args != null && args.length > 0)
      setArgs(Arrays.asList(args));
    else
    {
      args = getYADAQuery().getYADAQueryParamValue(YADARequest.PS_PREARGS);
      if (args != null && args.length > 0)
        setPreargs(Arrays.asList(args));
    }

    return getYADARequest();
  }

  /**
   * Base implementation, calls {@link #setYADARequest(YADARequest)},
   * {@link #setRequest(HttpServletRequest)}, {@link #setArgs(List)},
   * {@link #setPreargs(List)} using
   * {@link YADAQuery#getYADAQueryParamValue(String)} for the last two.
   * 
   * @see com.novartis.opensource.yada.plugin.Preprocess#engage(com.novartis.opensource.yada.YADARequest,
   *      com.novartis.opensource.yada.YADAQuery)
   */
  @Override
  public void engage(YADARequest yadaReq, YADAQuery yq) throws YADAPluginException, YADASecurityException {
    setYADARequest(yadaReq);
    setRequest(yadaReq.getRequest());
    setYADAQuery(yq);
    // test for specs
    //TODO is there a genuine way to use the SecurityPreprocessor Annotation here
    for (YADAParam param: yq.getYADAQueryParamsForKey(YADARequest.PS_PLUGIN))
    {
      String value  = param.getValue();
      String simple = this.getClass().getSimpleName();
      String name   = this.getClass().getName();
      // is there sec spec for this param, if so it's the one
      if (param.getSecuritySpec() != null 
          && (value.contentEquals(simple) || value.contentEquals(name)))
      {
        this.setSecuritySpec(param.getSecuritySpec());
        break;
      }
    }
    
    // extract args for plugin if no secspec is present
    // this implies normal processing for non-security plugins
    // but also enables backwards-compatibility for pre-9.0.0 configs 
    
    // args are split at commas
    if (this.getSecuritySpec() == null)
    {
      String[] args = getYADAQuery().getYADAQueryParamValuesForTarget(YADARequest.PS_ARGLIST,
          this.getClass().getSimpleName());
      if (args != null && args.length > 0)
      {
        setArgs(Arrays.asList(args[0].split(",")));
      }
      else
      {
        args = getYADAQuery().getYADAQueryParamValuesForTarget(YADARequest.PS_ARGLIST, this.getClass().getName());
        if (args != null && args.length > 0)
        {
          setArgs(Arrays.asList(args[0].split(",")));
        }
        else
        {
          args = getYADAQuery().getYADAQueryParamValue(YADARequest.PS_ARGS);
          if (args != null && args.length > 0)
            setArgs(Arrays.asList(args[0].split(",")));
          else
            args = getYADAQuery().getYADAQueryParamValue(YADARequest.PS_PREARGS);
          if (args != null && args.length > 0)
            setPreargs(Arrays.asList(args[0].split(",")));
        }
      }
    }
  }

  /**
   * Convenience method with calls {@link #validateURL()},
   * {@link #validateToken()}, {@link #authorize()},
   * {@link #applyExecutionPolicy()}, and {@link #applyContentPolicy()}
   * 
   * @since 7.0.0
   */
  @Override
  public void validateYADARequest() throws YADASecurityException {
    // default impl here looks for arg/prearg
    validateURL();

    // will use argument to inject, if present, or return gracefully
    setTokenValidator();

    // default impl does nothing - override in gatekeeper plugin
    obtainToken(getYADARequest());

    // default impl does nothing - override in gatekeeper plugin
    validateToken();

    // default impl does nothing - override in gatekeeper plugin
    authorize();
    
    if(this.hasSecuritySpec())
    {
      for(String iface : new String[] {EXECUTION_POLICY_INTERFACE, CONTENT_POLICY_INTERFACE})
      {
        Class<?> plugin = null;
        String   method = null;
        try
        {
          YADASecuritySpec spec = this.getSecuritySpec();
          method = "get"+iface;
          // test for existing policy config
          if(spec.getClass().getMethod(method).invoke(spec) != null)
          {
            plugin = Class.forName(PLUGIN_PKG + iface);
            if (plugin.isAssignableFrom(getClass()))
            {
              setSecurityPolicy(this);
            }
            SecurityPolicy policy = getSecurityPolicy();
            if (policy != null)
            {
              // cast the current policy object to the current iface,
              // get the 'apply+iface' (e.g., applyExecutionPolicy) method and
              // invoke it on the current plugin
              policy.getClass().cast(plugin.getClass()).getClass().getMethod("apply"+iface).invoke(this);
            }
          }
        }        
        catch (ClassNotFoundException e)
        {
          // TODO support additional policy types like in `setSecurityPolicy`
          String msg = String.format("Can't find implementing class for %s", PLUGIN_PKG + iface);          
          throw new YADASecurityException(msg,e);
        }
        catch ( InvocationTargetException e)
        {
          String msg = String.format("Unable to invoke %s method on plugin.", method);
          throw new YADASecurityException(msg,e);
        }
        catch (IllegalAccessException e)
        {
          String msg = String.format("Unable to invoke %s method on plugin: check encapsulation", method);
          throw new YADASecurityException(msg,e);
        }
        catch (IllegalArgumentException e)
        {
          String msg = String.format("Unable to invoke %s method on plugin: check arguments", method);
          throw new YADASecurityException(msg,e);
        }
        catch (NoSuchMethodException e)
        {
          String msg = String.format("Unable to invoke %s method on plugin: confirm method name.", method);
          throw new YADASecurityException(msg,e);
        }
        catch (SecurityException e)
        {
          String msg = String.format("Unable to invoke %s method on plugin: java security issue.", method);
          throw new YADASecurityException(msg,e);
        }
      }      
    }
    else
    {
      // if interface is implemented, or return gracefully
      setSecurityPolicy(EXECUTION_POLICY);
      SecurityPolicy policy = getSecurityPolicy();
      if (policy != null)
        ((ExecutionPolicy) policy).applyExecutionPolicy();

      // will use argument to inject, if present, or use current class,
      // if interface is implemented, or return gracefully
      setSecurityPolicy(CONTENT_POLICY);
      policy = getSecurityPolicy();
      if (policy != null)
        ((ContentPolicy) policy).applyContentPolicy();
    }
    // will use argument to inject, if present, or use current class,    
  }

  /**
   * Throws an exception if the URL returned by {@code req.getRequestURL()} is
   * unauthenticated.
   * 
   * @throws YADASecurityException if a non-authenticated URL is requested
   * @since 7.0.0
   */
  @Override
  public void validateURL() throws YADASecurityException {
    String pathRx, reqUrl;
    if(this.hasSecuritySpec())
    {
      pathRx = this.getSecuritySpec().getURLSpec();
    }
    else
    {
      pathRx = getArgumentValue(AUTH_PATH_RX);      
    }
    if (pathRx != null && pathRx.length() > 0)
    {
      reqUrl = getYADARequest().getRequest().getRequestURL().toString();
      if (!reqUrl.matches("^(http://)*" + pathRx + "$"))
      {
        String msg = "Unauthorized.  This query requires use of an authenticated address.";
        throw new YADASecurityException(msg);
      }
    }
  }

  /**
   * Default implementation calls {@link TokenValidator#validateToken()} via
   * injection
   * 
   * @since 7.0.0
   */
  @Override
  public void validateToken() throws YADASecurityException {
    // nothing to do
  }

  /**
   * Authorization of query use for given context
   * {@link Authorization#authorize()}
   * 
   * @since 8.7.6
   */
  @Override
  public void authorize() throws YADASecurityException {
    // nothing to do
  }

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
   * Returns the value of {@code key} from {@link #args} or {@link #preargs} or
   * from {@link System#getProperty(String)}
   * 
   * @param key the name of the argument for which the value is desired
   * @return the value mapped to {@code key} or an empty {@link String}
   * @since 7.0.0
   */
  protected String getArgumentValue(String key) {
    String val = "";

    if (this.args == null || this.args.size() == 0)
    {
      if (this.preargs == null || this.preargs.size() == 0)
      {
        val = System.getProperty(key);
      }
      else
      {
        val = getListEntry(this.preargs, key);
      }
    }
    else
    {
      val = getListEntry(this.args, key);
    }
    return val;
  }

  /**
   * Retrieves the value part of an argument passed as a name=value pair
   *
   * @param list the argument parameter list to review
   * @param key  the name in the argument name=value pair to retrieve
   * @return the value mapped to {@code key} or {@code null}
   * @since 7.0.0
   */
  private String getListEntry(List<String> list, String key) {
    for (int i = 0; i < list.size(); i++)
    {
      Pattern rxKeyVal = Pattern.compile("^" + key + "=(.+)$");
      Matcher m        = rxKeyVal.matcher(list.get(i));
      if (m.matches())
      {
        return m.group(1);
      }
    }
    return null;
  }

  /**
   * Standard mutator for variable
   * 
   * @param yadaReq the currently executing {@link YADARequest}
   */
  public void setYADARequest(YADARequest yadaReq) {
    this.yadaReq = yadaReq;
  }

  /**
   * Standard accessor for variable
   * 
   * @return the {@link YADARequest} being executed
   */
  public YADARequest getYADARequest() {
    return this.yadaReq;
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
   * Default implementation intended for override
   * 
   * @since 7.0.0
   */
  @Override
  public void setToken() {
    // nothing to do
  }

  /**
   * Standard mutator for variable
   * 
   * @param token the value of the authentication token
   * @since 7.0.0
   */
  @Override
  public void setToken(Object token) {
    this.token = token;
  }

  /**
   * Standard accessor for variable
   * 
   * @return the value of the validated {@code NIBR521} header
   * @throws YADASecurityException when token retrieval fails
   * @since 7.0.0
   */
  @Override
  public Object getToken() throws YADASecurityException {
    return this.token;
  }

  @Override
  public String getHeader(String header) {
    return getYADARequest().getRequest().getHeader(header);
  }

  @Override
  public Object getSessionAttribute(String attribute) {
    return getYADARequest().getRequest().getSession().getAttribute(attribute);
  }

  /**
   * Convenience method to get the user id value for security policy application.
   * Base implementation returns {@code null}
   * 
   * @return the current user id
   * @throws YADASecurityException
   */
  public String getLoggedUser() throws YADASecurityException {
    return null;
  }

  @Override
  public String getValue(String key) {
    return getYADAQuery().getDataRow(0).get(key)[0];
  }

  @Override
  public String getValue(int index) {
    return getYADAQuery().getDataRow(0).get("YADA_" + index)[0];
  }

  @Override
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
   * No arg mutator for variable, gets FQCN from args or properties
   * 
   * @throws YADASecurityException when the validator can't be loaded
   * @since 7.0.0
   */
  public void setTokenValidator() throws YADASecurityException {
    if (getTokenValidator() == null)
    {
      Class<?> clazz = null;
      String   name  = "";
      try
      {
        if(this.hasSecuritySpec())
        {
          name = this.getSecuritySpec().getTokenValidator();
        }
        else
        {
          name = getArgumentValue(TOKEN_VALIDATOR);
        }
        if (name.length() == 0)
          return;
        clazz = Class.forName(name);
      }
      catch (ClassNotFoundException e)
      {
        try
        {
          name  = PLUGIN_PKG + getArgumentValue(TOKEN_VALIDATOR);
          clazz = Class.forName(name);
        }
        catch (ClassNotFoundException e1)
        {
          String msg = "Could not find the specified TokenValidator class: " + name + ".";
          throw new YADASecurityException(msg, e1);
        }
      }

      try
      {
        setTokenValidator((TokenValidator) clazz.newInstance());
      }
      catch (InstantiationException | IllegalAccessException e)
      {
        String msg = "Could not instantiate the specified TokenValidator class: " + name;
        throw new YADASecurityException(msg, e);
      }
    }
  }

  /**
   * Standard mutator for variable
   * 
   * @param tokenValidator the {@link TokenValidator} instance
   * @since 7.0.0
   */
  public void setTokenValidator(TokenValidator tokenValidator) {
    this.tokenValidator = tokenValidator;
  }

  /**
   * Standard accessor for variable
   * 
   * @return the {@link TokenValidator} instance
   * @since 7.0.0
   */
  public TokenValidator getTokenValidator() {
    return this.tokenValidator;
  }

  /**
   * Default implementation of {@link SecurityPolicy#applyPolicy()}, intended for
   * override
   * 
   * @throws YADASecurityException when the user is unauthorized or there is an
   *                               error in policy processing
   * @since 7.0.0
   */
  @Override
  public void applyPolicy() throws YADASecurityException {
    // nothing to do
  }

  /**
   * Default implementation of {@link SecurityPolicy#applyPolicy()}, intended for
   * override
   * 
   * @throws YADASecurityException when the user is unauthorized or there is an
   *                               error in policy processing
   * @since 7.0.0
   */
  @Override
  public void applyPolicy(SecurityPolicy securityPolicy) throws YADASecurityException {
    // nothing to do
  }

  /**
   * Default implementation of does nothing, intended for override
   * 
   * @throws YADASecurityException when the user is unauthorized or there is an
   *                               error in policy processing
   * @since 7.0.0
   */
  @Override
  public void applyExecutionPolicy() throws YADASecurityException {
    // nothing to do
  }

  /**
   * Default implementation of does nothing, intended for override
   * 
   * @throws YADASecurityException when the user is unauthorized or there is an
   *                               error in policy processing
   * @since 7.0.0
   */
  @Override
  public void applyContentPolicy() throws YADASecurityException {
    // nothing to do
  }

  /**
   * @return the args
   * @since 7.0.0
   */
  public List<String> getArgs() {
    return this.args;
  }

  /**
   * @param args the args to set
   * @since 7.0.0
   */
  public void setArgs(List<String> args) {
    this.args = args;
  }

  /**
   * @return the preargs
   * @since 7.0.0
   */
  public List<String> getPreArgs() {
    return this.preargs;
  }

  /**
   * @param preargs the preargs to set
   * @since 7.0.0
   * @deprecated as of 7.1.0
   */
  @Deprecated
  public void setPreargs(List<String> preargs) {
    this.preargs = preargs;
  }

  /**
   * @return the securityPolicy
   */
  public SecurityPolicy getSecurityPolicy() {
    return this.securityPolicy;
  }

  /**
   * @param securityPolicy the securityPolicy to set
   */
  public void setSecurityPolicy(SecurityPolicy securityPolicy) {
    this.securityPolicy = securityPolicy;
  }

  /**
   * No arg mutator for variable, gets FQCN from args or properties
   * 
   * @param policyType either {@link #CONTENT_POLICY} or {@link #EXECUTION_POLICY}
   * @throws YADASecurityException when the policy can't be loaded
   * @since 7.0.0
   */
  public void setSecurityPolicy(String policyType) throws YADASecurityException {
    if (getSecurityPolicy() == null)
    {
      Class<?> clazz = null;
      String   name  = "";
      try
      {
        // this is where the default query param implementation meets execution.
        // the value of the 'execution.policy' or 'content.policy' argument is
        // the implementing class name
        name = getArgumentValue(policyType);
        if (name != null && name.equals(VOID))
        {
          return;
        }
        // next blokc == no arguments == this is the default case 
        // for pre-9.0.0 configurations, where the arg string looks like:
        //    plugin,content.policy=void,execution.policy.columns=...
        // there is no classname associated to the .policy keys
        else if (name == null || name.length() == 0)
        {
          try
          {
            String   iface  = policyType.equals(EXECUTION_POLICY) 
                ? EXECUTION_POLICY_INTERFACE
                : CONTENT_POLICY_INTERFACE;
            Class<?> plugin = Class.forName(PLUGIN_PKG + iface);
            if (plugin.isAssignableFrom(getClass()))
              setSecurityPolicy(this);
          }
          catch (ClassNotFoundException e)
          {
            return;
          }
          return; // we'll usually return here
        }
        // not null, not "void"
        clazz = Class.forName(name);
      }
      catch (ClassNotFoundException e)
      {
        //TODO What does this try/catch do
        //     it seems like a setting for an arbitrary policy
        try
        {
          name  = PLUGIN_PKG + getArgumentValue(policyType);
          clazz = Class.forName(name);
        }
        catch (ClassNotFoundException e1)
        {
          String msg = "Could not find the specified SecurityPolicy class: " + name + ".";
          throw new YADASecurityException(msg, e1);
        }
      }

      try
      {
        setSecurityPolicy((SecurityPolicy) clazz.newInstance());
      }
      catch (InstantiationException | IllegalAccessException e)
      {
        String msg = "Could not instantiate the specified SecurityPolicy class: " + name;
        throw new YADASecurityException(msg, e);
      }
    }
  }

  /**
   * Returns {@code true} if the security target is associated to a
   * {@link #BLACKLIST} policy type in the {@code YADA_A11N} table
   *
   * @param type the value of the {@code TYPE} field in the {@code YADA_A11N}
   *             table
   * @return {@code true} if {@code TYPE} is {@link #BLACKLIST}
   */
  @Override
  public boolean isBlacklist(String type) {
    return BLACKLIST.equals(type);
  }

  /**
   * Returns {@code true} if the security target is associated to a
   * {@link #WHITELIST} policy type in the {@code YADA_A11N} table
   *
   * @param type the value of the {@code TYPE} field in the {@code YADA_A11N}
   *             table
   * @return {@code true} if {@code TYPE} is {@link #WHITELIST}
   */
  @Override
  public boolean isWhitelist(String type) {
    return WHITELIST.equals(type);
  }

  /**
   * Retrieves the row from {@code YADA_A11N} for the desired query.
   * 
   * @param securityPolicyCode the policy code {@code E}
   * @return a {@link HashMap} contaning the security config for the {@code qname}
   * @throws YADASecurityException when the security query can't be retrieved
   */
  @Override
  public List<SecurityPolicyRecord> getSecurityPolicyRecords(String securityPolicyCode) throws YADASecurityException {
    List<SecurityPolicyRecord> policy = new ArrayList<>();

    // get the security params associated to the query
    String qname = getYADAQuery().getQname();
    try (ResultSet rs = YADAUtils.executePreparedStatement(YADA_A11N_QUERY, new Object[] { qname });)
    {
      while (rs.next())
      {
        String tgt        = rs.getString(1); // YADA_A11N.TARGET (this is in the query
                                             // paramaters, no need to pass)
        String policyCode = rs.getString(2); // YADA_A11N.POLICY (this is in the
                                             // method parameters, no need to pass)
        String type       = rs.getString(3); // YADA_A11N.TYPE
        String a11nQname  = rs.getString(4); // YADA_A11N.QNAME (a query name)

        if (qname.equals(tgt) && policyCode.equals(securityPolicyCode))
        {
          policy.add(new SecurityPolicyRecord(tgt, policyCode, type, a11nQname));
        }
      }
      ConnectionFactory.releaseResources(rs);
    }
    catch (SQLException | YADAConnectionException | YADASQLException e)
    {
      String msg = "Unauthorized. Could not obtain security query. This could be a temporary problem.";
      throw new YADASecurityException(msg, e);
    }

    if (policy.size() == 0)
    {
      String msg = "Unauthorized. A security check was configured by has no policy associated to it.";
      throw new YADASecurityException(msg);
    }

    return policy;
  }

  /**
   * Sets the current security policy to {@code null}.
   * 
   * @since 7.0.0
   */
  public void clearSecurityPolicy() {
    this.securityPolicy = null;
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

    if (m1.matches()) // it's a list of header names
    {
      this.httpHeaders = new JSONObject();
      for (String name: httpHeaders)
      {
        this.httpHeaders.put(name, reqHeaders.get(name.toLowerCase()));
      }
    }
    else // it's a json object
    {
      try
      {
        this.httpHeaders = new JSONObject(httpHeaders);
        JSONArray names = this.httpHeaders.names();
        JSONArray vals  = this.httpHeaders.toJSONArray(names);
        for (int i = 0; i < vals.length(); i++)
        {
          if (vals.optBoolean(i))
          {
            String name = names.getString(i);
            this.httpHeaders.put(name, reqHeaders.get(name.toLowerCase()));
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
   * @since 8.5.0
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
   * @since 8.5.0
   */
  public boolean hasHttpHeaders() {
    if (null == this.getHttpHeaders() || this.getHttpHeaders().length() == 0)
    {
      return false;
    }
    return true;
  }
  
  public boolean hasSecuritySpec() {
    return this.getSecuritySpec() != null;
  }

  /**
   * Standard mutator for variable
   * 
   * @param request the {@link javax.servlet.http.HttpServletRequest} object
   *                passed from the app server
   */
  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }

  /**
   * Standard accessor for variable
   * 
   * @return the {@link javax.servlet.http.HttpServletRequest} object passed from
   *         the app server
   */
  public HttpServletRequest getRequest() {
    return this.request;
  }

  /**
   * Default credential check to YADA
   * 
   * @return {@code false} by default
   */
  public Boolean checkCredentials() {
    return false;
  }

  /**
   * Generate YADA token
   * 
   * @return an empty string by default
   */
  public String generateToken() {
    return "";
  }

  @Override
  public void authorizeYADARequest(YADARequest yadaReq, String result) throws YADASecurityException {
    // nothing to do
  }

  /**
   * @since 8.7.6
   */
  @Override
  public void obtainToken(YADARequest yadaReq) throws YADASecurityException {
    // nothing to do
  }

  /**
   * Standard accessor
   * @return the {@link YADASecuritySpec} object
   * @since 9.0.0
   */
  public YADASecuritySpec getSecuritySpec() {
    return this.securitySpec;
  }

  /**
   * Standard mutator
   * @param securitySpec
   * @since 9.0.0
   */
  public void setSecuritySpec(YADASecuritySpec securitySpec) {
    this.securitySpec = securitySpec;
  }
}
