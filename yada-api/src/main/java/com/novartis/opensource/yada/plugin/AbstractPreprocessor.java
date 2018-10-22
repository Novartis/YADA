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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;

import org.json.JSONArray;

import com.novartis.opensource.yada.ConnectionFactory;
import com.novartis.opensource.yada.YADAConnectionException;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADASQLException;
import com.novartis.opensource.yada.YADASecurityException;
import com.novartis.opensource.yada.util.YADAUtils;

/**
 * @author David Varon
 * @since 0.4.2
 */
public abstract class AbstractPreprocessor implements Preprocess, Validation, TokenValidator, ExecutionPolicy, ContentPolicy
{
  /**
   * Constant equal to {@value}
   * @since 7.0.0
   */
  protected static final String AUTH_PATH_RX = "auth.path.rx";

  /**
   * Constant equal to {@value}
   * @since 7.0.0
   */
  protected static final String AUTH_TOKEN = "auth.token";

  /**
   * Constant equal to {@value}
   * @since 7.0.0
   */
  protected static final String TOKEN_VALIDATOR = "token.validator";

  /**
   * Constant equal to {@value}
   * @since 7.0.0
   */
  protected static final String CONTENT_POLICY = "content.policy";

  /**
   * Constant equal to {@value}
   * @since 7.0.0
   */
  protected static final String EXECUTION_POLICY = "execution.policy";

  /**
   * Constant equal to {@value}
   * @since 7.0.0
   */
  protected static final String EXECUTION_POLICY_INTERFACE = "ExecutionPolicy";

  /**
   * Constant equal to {@value}
   * @since 7.0.0
   */
  protected static final String CONTENT_POLICY_INTERFACE   = "ContentPolicy";

  /**
   * Constant equal to {@value}
   * @since 7.0.0
   */
  protected static final String VOID   = "void";

  /**
   * Constant equal to {@value}.
   * The query executed to evaluate authorization.
   */
  protected static final String YADA_A11N_QUERY =
      "SELECT DISTINCT a.target, a.policy, a.type, a.qname "
      + "FROM YADA_A11N a " // join YADA_QUERY b on  (a.target = b.qname OR a.target = b.app) "
      + "WHERE a.target = ?";

  /**
   * Constant equal to {@value}
   * @since 7.0.0
   */
  private static final String PLUGIN_PKG = "com.novartis.opensource.yada.plugin.";

  /**
   * The {@link YADARequest} object processed by the plugin
   */
  private YADARequest yadaReq;

  /**
   * The {@link YADAQuery} object processed by the plugin
   */
  private YADAQuery yq;

  /**
   * The authentication token, e.g., user id
   * @since 7.0.0
   */
  private Object token = null;

  /**
   * The {@link TokenValidator}
   * @since 7.0.0
   */
  private TokenValidator tokenValidator;

  /**
   * The {@link SecurityPolicy}
   * @since 7.0.0
   */
  private SecurityPolicy securityPolicy;

  /**
   * The {@code args} {@link List} from {@link YADARequest#getArgs}
   * @since 7.0.0
   */
  private List<String> args;

  /**
   * The {@code preArgs} {@link List} from {@link YADARequest#getPreArgs}
   * @since 7.0.0
   * @deprecated as of 7.1.0
   */
  @Deprecated
  private List<String> preargs;

	/**
	 * Base implementation, calls {@link #setYADARequest(YADARequest)},
	 * {@link #setArgs(List)}, {@link #setPreargs(List)}
	 * and returns {@link #getYADARequest()}
	 * @see com.novartis.opensource.yada.plugin.Preprocess#engage(com.novartis.opensource.yada.YADARequest)
	 */
	@Override
	public YADARequest engage(YADARequest yadaReq) throws YADAPluginException
	{
	  setYADARequest(yadaReq);
	  String[] args = getYADAQuery().getYADAQueryParamValue(YADARequest.PS_ARGLIST);
	  if(args != null && args.length > 0)
      setArgs(Arrays.asList(args));
	  else
	  {
  	  args = getYADAQuery().getYADAQueryParamValue(YADARequest.PS_PREARGS);
      if(args != null && args.length > 0)
        setPreargs(Arrays.asList(args));
	  }

		return getYADARequest();
	}

	/**
	 * Base implementation, calls {@link #setYADARequest(YADARequest)},
	 *  {@link #setArgs(List)}, {@link #setPreargs(List)} using {@link YADAQuery#getYADAQueryParamValue(String)}
	 *  for the last two.
	 * @see com.novartis.opensource.yada.plugin.Preprocess#engage(com.novartis.opensource.yada.YADARequest, com.novartis.opensource.yada.YADAQuery)
	 */
	@Override
	public void engage(YADARequest yadaReq, YADAQuery yq) throws YADAPluginException, YADASecurityException
	{
	  setYADARequest(yadaReq);
	  setYADAQuery(yq);
	  String[] args = getYADAQuery().getYADAQueryParamValuesForTarget(YADARequest.PS_ARGLIST, this.getClass().getSimpleName());
    if(args != null && args.length > 0)
      setArgs(Arrays.asList(args[0].split(",")));
	  else
	  {
	  	args = getYADAQuery().getYADAQueryParamValuesForTarget(YADARequest.PS_ARGLIST, this.getClass().getName());
      if(args != null && args.length > 0)
        setArgs(Arrays.asList(args[0].split(",")));
      else
      {
      	args = getYADAQuery().getYADAQueryParamValue(YADARequest.PS_ARGS);
        if(args != null && args.length > 0)
          setArgs(Arrays.asList(args[0].split(",")));
        else
	        args = getYADAQuery().getYADAQueryParamValue(YADARequest.PS_PREARGS);
	        if(args != null && args.length > 0)
	          setPreargs(Arrays.asList(args[0].split(",")));
      }
	  }
	}

	/**
	 * Convenience method with calls 
	 * {@link #validateURL()}, 
	 * {@link #validateToken()}, 
	 * {@link #applyExecutionPolicy()}, 
	 * and {@link #applyContentPolicy()}
	 * @since 7.0.0
	 */
	@Override
	public void validateYADARequest() throws YADASecurityException
	{
	  // default impl here looks for arg/prearg
	  validateURL();

	  // will use argument to inject, if present, or return gracefully
	  setTokenValidator();
	  // default impl does nothing
	  validateToken();

	  // will use argument to inject, if present, or use current class,
	  // if interface is implemented, or return gracefully
	  setSecurityPolicy(EXECUTION_POLICY);
	  SecurityPolicy policy = getSecurityPolicy();
	  if(policy != null)
	    ((ExecutionPolicy)policy).applyExecutionPolicy();

    // will use argument to inject, if present, or use current class,
    // if interface is implemented, or return gracefully
	  setSecurityPolicy(CONTENT_POLICY);
	  policy = getSecurityPolicy();
    if(policy != null)
      ((ContentPolicy)policy).applyContentPolicy();
	}

	/**
	  * Throws an exception if the URL returned by {@code req.getRequestURL()} is unauthenticated.
	  * @throws YADASecurityException if a non-authenticated URL is requested
	  * @since 7.0.0
	  */
	@Override
	public void validateURL() throws YADASecurityException
	{
	  String pathRx = getArgumentValue(AUTH_PATH_RX);
	  if(pathRx != null && pathRx.length() > 0)
	  {
	    String reqUrl = getYADARequest().getRequest().getRequestURL().toString();
	    if(!reqUrl.matches("^(http://)*"+pathRx+"$")) {
	      String msg = "Unauthorized.  This query requires use of an authenticated address.";
	      throw new YADASecurityException(msg);
	    }
	  }
	}

	/**
   * Default implementation calls {@link TokenValidator#validateToken()} via injection
   * @since 7.0.0
   */
  @Override
  public void validateToken() throws YADASecurityException
  {
    // nothing to do
  }

  /**
   * Returns the value of {@code key} from {@link #args}
   * or {@link #preargs} or from {@link System#getProperty(String)}
   * @param key the name of the argument for which the value is desired
   * @return the value mapped to {@code key} or an empty {@link String}
   * @since 7.0.0
   */
  protected String getArgumentValue(String key)
  {
    String val = "";

    if(this.args == null || this.args.size() == 0)
    {
      if(this.preargs == null || this.preargs.size() == 0)
      {
        val = System.getProperty(key);
      }
      else
      {
        val = getListEntry(this.preargs,key);
      }
    }
    else
    {
      val = getListEntry(this.args,key);
    }
    return val;
  }


	/**
   * Retrieves the value part of an argument passed as a name=value pair
   *
   * @param list the argument parameter list to review
   * @param key the name in the argument name=value pair to retrieve
   * @return the value mapped to {@code key} or {@code null}
   * @since 7.0.0
   */
  private String getListEntry(List<String> list, String key)
  {
    for(int i=0;i<list.size();i++)
    {
      Pattern rxKeyVal = Pattern.compile("^"+key+"=(.+)$");
      Matcher m = rxKeyVal.matcher(list.get(i));
      if(m.matches())
      {
        return m.group(1);
      }
    }
    return null;
  }

	/**
	 * Standard mutator for variable
	 * @param yadaReq the currently executing {@link YADARequest}
	 */
	public void setYADARequest(YADARequest yadaReq) {
	  this.yadaReq = yadaReq;
	}

  /**
   * Standard accessor for variable
   * @return the {@link YADARequest} being executed
   */
  public YADARequest getYADARequest() {
    return this.yadaReq;
  }

	/**
	 * Standard mutator for variable
	 * @param yq the {@link YADAQuery} to which this preprocessor is attached
	 */
	public void setYADAQuery(YADAQuery yq) {
    this.yq = yq;
  }

  /**
   * Standard accessor for variable
   * @return the {@link YADAQuery} to which this preprocessor is attached
   */
  public YADAQuery getYADAQuery() {
    return this.yq;
  }

  /**
   * Default implementation intended for override
   * @since 7.0.0
   */
  @Override
  public void setToken()
  {
    // nothing to do
  }

  /**
   * Standard mutator for variable
   * @param token the value of the authentication token
   * @since 7.0.0
   */
  @Override
  public void setToken(Object token)
  {
    this.token = token;
  }

  /**
   * Standard accessor for variable
   * @return the value of the validated {@code NIBR521} header
   * @throws YADASecurityException when token retrieval fails
   * @since 7.0.0
   */
  @Override
  public Object getToken() throws YADASecurityException
  {
    return this.token;
  }

  @Override
  public String getHeader(String header)
  {
    return getYADARequest().getRequest().getHeader(header);
  }

  @Override
  public Object getSessionAttribute(String attribute)
  {
    return getYADARequest().getRequest().getSession().getAttribute(attribute);
  }

  /**
   * Convenience method to get the user id value for security policy application
   * @return a json string containing the {@code UID} value stored in the {@code YADA.user.privs} session attribute
   */
  public String getLoggedUser()
  {
    return ((JSONArray)getSessionAttribute("YADA.user.privs")).getJSONObject(0).getString("USERID");
  }

  @Override
  public String getValue(String key)
  {
    return getYADAQuery().getDataRow(0).get(key)[0];
  }

  @Override
  public String getValue(int index)
  {
    return getYADAQuery().getDataRow(0).get("YADA_"+index)[0];
  }

  @Override
  public String getCookie(String cookie)
  {
    Cookie[] cookies = getYADARequest().getRequest().getCookies();
    if (cookies != null)
    {
      for (Cookie c : cookies)
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
   * @throws YADASecurityException when the validator can't be loaded
   * @since 7.0.0
   */
  public void setTokenValidator() throws YADASecurityException
  {
    if(getTokenValidator() == null)
    {
      Class<?> clazz = null;
      String name = "";
      try
      {
        name = getArgumentValue(TOKEN_VALIDATOR);
        if(name.length() == 0) return;
        clazz = Class.forName(name);
      }
      catch (ClassNotFoundException e)
      {
        try
        {
          name = PLUGIN_PKG + getArgumentValue(TOKEN_VALIDATOR);
          clazz = Class.forName(name);
        }
        catch (ClassNotFoundException e1)
        {
          String msg = "Could not find the specified TokenValidator class: " + name + ".";
          throw new YADASecurityException(msg,e1);
        }
      }

      try
      {
        setTokenValidator((TokenValidator)clazz.newInstance());
      }
      catch (InstantiationException | IllegalAccessException e) {
        String msg = "Could not instantiate the specified TokenValidator class: " + name;
        throw new YADASecurityException(msg,e);
      }
    }
  }

  /**
   * Standard mutator for variable
   * @param tokenValidator the {@link TokenValidator} instance
   * @since 7.0.0
   */
  public void setTokenValidator(TokenValidator tokenValidator)
  {
    this.tokenValidator = tokenValidator;
  }

  /**
   * Standard accessor for variable
   * @return the {@link TokenValidator} instance
   * @since 7.0.0
   */
  public TokenValidator getTokenValidator()
  {
    return this.tokenValidator;
  }

  /**
   * Default implementation of {@link SecurityPolicy#applyPolicy()}, intended for override
   * @throws YADASecurityException when the user is unauthorized or there is an error in policy processing
   * @since 7.0.0
   */
  @Override
  public void applyPolicy() throws YADASecurityException
  {
    // nothing to do
  }

  /**
   * Default implementation of {@link SecurityPolicy#applyPolicy()}, intended for override
   * @throws YADASecurityException when the user is unauthorized or there is an error in policy processing
   * @since 7.0.0
   */
  @Override
  public void applyPolicy(SecurityPolicy securityPolicy) throws YADASecurityException
  {
    // nothing to do
  }

  /**
   * Default implementation of does nothing, intended for override
   * @throws YADASecurityException when the user is unauthorized or there is an error in policy processing
   * @since 7.0.0
   */
  @Override
  public void applyExecutionPolicy() throws YADASecurityException
  {
    // nothing to do
  }

  /**
   * Default implementation of does nothing, intended for override
   * @throws YADASecurityException when the user is unauthorized or there is an error in policy processing
   * @since 7.0.0
   */
  @Override
  public void applyContentPolicy() throws YADASecurityException
  {
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
  public void setArgs(List<String> args)
  {
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
  public void setPreargs(List<String> preargs)
  {
    this.preargs = preargs;
  }

  /**
   * @return the securityPolicy
   */
  public SecurityPolicy getSecurityPolicy()
  {
    return this.securityPolicy;
  }

  /**
   * @param securityPolicy the securityPolicy to set
   */
  public void setSecurityPolicy(SecurityPolicy securityPolicy)
  {
    this.securityPolicy = securityPolicy;
  }

  /**
   * No arg mutator for variable, gets FQCN from args or properties
   * @param policyType either {@link #CONTENT_POLICY} or {@link #EXECUTION_POLICY}
   * @throws YADASecurityException when the policy can't be loaded
   * @since 7.0.0
   */
  public void setSecurityPolicy(String policyType) throws YADASecurityException
  {
    if(getSecurityPolicy() == null)
    {
      Class<?> clazz = null;
      String name = "";
      try
      {
        // this is where the default query param implementation meets execution.
        // the value of the 'execution.policy' or 'content.policy' argument is the
        // implementing class name
        name = getArgumentValue(policyType);
        if(name != null && name.equals(VOID))
        {
          return;
        }
        else if(name == null || name.length() == 0)
        {
          try
          {
            String   iface  = policyType.equals(EXECUTION_POLICY) ? EXECUTION_POLICY_INTERFACE : CONTENT_POLICY_INTERFACE;
            Class<?> plugin = Class.forName(PLUGIN_PKG+iface);
            if(plugin.isAssignableFrom(getClass()))
              setSecurityPolicy(this);
          }
          catch(ClassNotFoundException e)
          {
            return;
          }
          return;
        }
        clazz = Class.forName(name);
      }
      catch (ClassNotFoundException e)
      {
        try
        {
          name = PLUGIN_PKG + getArgumentValue(policyType);
          clazz = Class.forName(name);
        }
        catch (ClassNotFoundException e1)
        {
          String msg = "Could not find the specified SecurityPolicy class: " + name + ".";
          throw new YADASecurityException(msg,e1);
        }
      }

      try
      {
        setSecurityPolicy((SecurityPolicy)clazz.newInstance());
      }
      catch (InstantiationException | IllegalAccessException e) {
        String msg = "Could not instantiate the specified SecurityPolicy class: " + name;
        throw new YADASecurityException(msg,e);
      }
    }
  }

  /**
   * Returns {@code true} if the security target is
   * associated to a {@link #BLACKLIST} policy type in the
   * {@code YADA_A11N} table
   *
   * @param type
   *          the value of the {@code TYPE} field in the
   *          {@code YADA_A11N} table
   * @return {@code true} if {@code TYPE} is {@link #BLACKLIST}
   */
  @Override
  public boolean isBlacklist(String type)
  {
    return BLACKLIST.equals(type);
  }

  /**
   * Returns {@code true} if the security target is
   * associated to a {@link #WHITELIST} policy type in the
   * {@code YADA_A11N} table
   *
   * @param type
   *          the value of the {@code TYPE} field in the
   *          {@code YADA_A11N} table
   * @return {@code true} if {@code TYPE} is {@link #WHITELIST}
   */
  @Override
  public boolean isWhitelist(String type)
  {
    return WHITELIST.equals(type);
  }

  /**
   * Retrieves the row from {@code YADA_A11N} for the desired query.
   * @param securityPolicyCode the policy code {@code E}
   * @return a {@link HashMap} contaning the security config for the {@code qname}
   * @throws YADASecurityException when the security query can't be retrieved
   */
  @Override
  public List<SecurityPolicyRecord> getSecurityPolicyRecords(String securityPolicyCode) throws YADASecurityException
  {
    List<SecurityPolicyRecord> policy = new ArrayList<>();

    // get the security params associated to the query
    String qname = getYADAQuery().getQname();
    try (ResultSet rs = YADAUtils.executePreparedStatement(YADA_A11N_QUERY, new Object[] { qname });)
    {
      while (rs.next()) {
        String tgt        = rs.getString(1); // YADA_A11N.TARGET  (this is in the query paramaters, no need to pass)
        String policyCode = rs.getString(2); // YADA_A11N.POLICY  (this is in the method parameters, no need to pass`)
        String type       = rs.getString(3); // YADA_A11N.TYPE
        String a11nQname  = rs.getString(4); // YADA_A11N.QNAME (a query name)

        if (qname.equals(tgt) && policyCode.equals(securityPolicyCode))
        {
          policy.add(new SecurityPolicyRecord(tgt,policyCode,type,a11nQname));
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
   * @since 7.0.0
   */
  public void clearSecurityPolicy() {
    this.securityPolicy = null;
  }
}
