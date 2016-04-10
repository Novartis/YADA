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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.novartis.opensource.yada.YADAConnectionException;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADASQLException;
import com.novartis.opensource.yada.util.YADAUtils;

/**
 * @author David Varon
 * @since 0.4.2
 */
public abstract class AbstractPreprocessor implements Preprocess, Validation, TokenValidator, ExecutionPolicy, ContentPolicy
{
  /**
   * Constant equal to {@value}
   * @since 0.7.0.0
   */
  protected static final String AUTH_PATH_RX = "auth.path.rx";
  
  /**
   * Constant equal to {@value}
   * @since 0.7.0.0
   */
  protected static final String TOKEN_VALIDATOR = "token.validator";
  
  /**
   * Constant equal to {@value}
   * @since 0.7.0.0
   */
  protected static final String CONTENT_POLICY = "content.policy";
  
  /**
   * Constant equal to {@value}
   * @since 0.7.0.0
   */
  protected static final String EXECUTION_POLICY = "execution.policy";
  
  /**
   * Constant equal to {@value}
   * @since 0.7.0.0
   */  
  protected static final String EXECUTION_POLICY_INTERFACE = "ExecutionPolicy";
  
  /**
   * Constant equal to {@value}
   * @since 0.7.0.0
   */
  protected static final String CONTENT_POLICY_INTERFACE   = "ContentPolicy";
  
  /**
   * Constant equal to {@value}.
   * The query executed to evaluate authorization.
   */
  protected static final String YADA_A11N_QUERY = 
      "SELECT DISTINCT target, type, qname "
      + "FROM YADA_A11N a join YADA_QUERY b on  (a.target = b.qname OR a.target = b.app) "
      + "WHERE a.qname = ?";
    
  /**
   * Constant equal to {@value}
   * @since 0.7.0.0
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
   * @since 0.7.0.0
   */
  private Object token = null;
  
  /**
   * The {@link TokenValidator}
   * @since 0.7.0.0 
   */
  private TokenValidator tokenValidator;
  
  /**
   * The {@link SecurityPolicy}
   * @since 0.7.0.0 
   */
  private SecurityPolicy securityPolicy;  
  
  /**
   * The {@code args} {@link List} from {@link YADARequest#getArgs}
   * @since 0.7.0.0
   */
  private List<String> args;
  
  /**
   * The {@code preArgs} {@link List} from {@link YADARequest#getPreargs}
   * @since 0.7.0.0
   */
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
	  setArgs(getYADARequest().getArgs());
	  setPreargs(getYADARequest().getPreArgs());
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
	  setArgs(Arrays.asList(getYADAQuery().getYADAQueryParamValue(YADARequest.PS_ARGS)));
	  setPreargs(Arrays.asList(getYADAQuery().getYADAQueryParamValue(YADARequest.PS_PREARGS)));
	}

	/**
	 * Convenience method with calls {@link #validateURL()}, {@link #validateToken()}, {@link #applyExecutionPolicy()}, and {@link #applyContentPolicy()}
	 * @since 0.7.0.0
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
	  * @throws YADAPluginException if a non-authenticated URL is requested
	  * @since 0.7.0.0
	  */
	@Override
	public void validateURL() throws YADASecurityException 
	{
	  String pathRx = getArgumentValue(AUTH_PATH_RX);
	  if(pathRx.length() > 0)
	  {
	    String reqUrl = getYADARequest().getRequest().getRequestURL().toString();
	    if(!reqUrl.matches(pathRx)) {
	      String msg = "Unauthorized.  This query requires use of an authenticated address.";
	      throw new YADASecurityException(msg);
	    }
	  }
	}
	
	/**
   * Default implementation calls {@link TokenValidator#validate()} via injection
   * @since 0.7.0.0
   */
  @Override
  public void validateToken() throws YADASecurityException
  {
    // nothing to do
  }
  
  /**
   * Returns the value of {@code key} from {@link #args} 
   * or {@link #preargs} or from {@link System#getProperty(String)}
   * @param key
   * @return the value mapped to {@code key} or an empty {@link String}
   * @since 0.7.0.0
   */
  protected String getArgumentValue(String key) 
  {
    String val = "";
    if(this.args.size() == 0)
    {
      if(this.preargs.size() == 0)
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
   * @since 0.7.0.0
   */
  private String getListEntry(List<String> list, String key)
  {
    for(int i=0;i<list.size();i++)
    {
      if(list.get(i).matches(key))
      {
        String[] split = list.get(i).split("=");
        return split[1];
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
   * @since 0.7.0.0
   */
  @Override
  public void setToken() 
  {
    // nothing to do
  }
  
  /**
   * Standard mutator for variable
   * @param token 
   * @since 0.7.0.0
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
   * @since 0.7.0.0
   */
  @Override
  public Object getToken() throws YADASecurityException 
  {
    return this.token;
  }
  
  
  /**
   * No arg mutator for variable, gets FQCN from args or properties 
   * @throws YADASecurityException 
   * @since 0.7.0.0
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
   * @since 0.7.0.0
   */
  public void setTokenValidator(TokenValidator tokenValidator)
  {
    this.tokenValidator = tokenValidator;
  }
  
  /**
   * Standard accessor for variable
   * @return the {@link TokenValidotor} instance
   * @since 0.7.0.0
   */
  public TokenValidator getTokenValidator() 
  {
    return this.tokenValidator;
  }
  
  /**
   * Default implementation of {@link SecurityPolicy#applyPolicy()}, intended for override
   * @throws YADASecurityException
   * @since 0.7.0.0
   */
  @Override
  public void applyPolicy() throws YADASecurityException
  {
    // nothing to do
  }
  
  /**
   * Default implementation of {@link SecurityPolicy#applyPolicy(SecurityPolicy)}, intended for override
   * @throws YADASecurityException
   * @since 0.7.0.0
   */
  @Override
  public void applyPolicy(SecurityPolicy securityPolicy) throws YADASecurityException
  {
    // nothing to do
  }
  
  /**
   * Default implementation of does nothing, intended for override
   * @throws YADASecurityException
   * @since 0.7.0.0
   */
  @Override
  public void applyExecutionPolicy() throws YADASecurityException
  {
    // nothing to do
  }
 
  /**
   * Default implementation of does nothing, intended for override
   * @throws YADASecurityException
   * @since 0.7.0.0
   */
  @Override
  public void applyContentPolicy() throws YADASecurityException
  {
    // nothing to do
  }
  
  /**
   * @return the args
   * @since 0.7.0.0
   */
  public List<String> getArgs() {
    return this.args;
  }

  /**
   * @param args the args to set
   * @since 0.7.0.0
   */
  public void setArgs(List<String> args) 
  {
    this.args = args;
  }
  
  /**
   * @return the preargs
   * @since 0.7.0.0
   */
  public List<String> getPreArgs() {
    return this.preargs;
  }

  /**
   * @param preargs the preargs to set
   * @since 0.7.0.0
   */
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
   * @throws YADASecurityException 
   * @since 0.7.0.0
   */
  public void setSecurityPolicy(String policyType) throws YADASecurityException
  {
    if(getSecurityPolicy() == null)
    {
      Class<?> clazz = null;
      String name = "";
      try 
      {
        name = getArgumentValue(policyType);
        if(name.length() == 0)
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
   * @param qname the request query
   * @return a {@link HashMap} contaning the security config for the {@code qname}
   * @throws YADASecurityException when the security query can't be retrieved
   */  
  @Override
  public HashMap<String, String> getSecurityPolicyMap() throws YADASecurityException 
  {
    HashMap<String, String> policyMap = new HashMap<>();

    // get the security params associated to the query
    String qname = getYADAQuery().getQname();
    try (ResultSet rs = YADAUtils.executePreparedStatement(YADA_A11N_QUERY, new Object[] { qname });) 
    {
      while (rs.next()) {
        String tgt       = rs.getString(1); // YADA_A11N.TARGET
        String type    = rs.getString(2); // YADA_A11N.POLICY
        String a11nQname = rs.getString(3); // YADA_A11N.QNAME (a query name)
        
        if (qname.equals(tgt)) 
        {
          policyMap.put(type, a11nQname);
        }
      }
    } 
    catch (SQLException | YADAConnectionException | YADASQLException e) 
    {
      String msg = "Unauthorized. Could not obtain security query. This could be a temporary problem.";
      throw new YADASecurityException(msg, e);
    } 
    
    if (policyMap.size() == 0)
    {
      String msg = "Unauthorized. A security check was configured by has no policy associated to it.";
      throw new YADASecurityException(msg);
    }
    
    return policyMap;
  }
}
