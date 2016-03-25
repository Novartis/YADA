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

import java.util.Arrays;
import java.util.List;

import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADARequest;

/**
 * @author David Varon
 * @since 0.4.2
 */
public abstract class AbstractPreprocessor implements Preprocess, Validation
{
  /**
   * Constant equal to {@value}
   * @since 0.6.3.0
   */
  private static final String AUTH_PATH_RX = "auth.path.rx";
  
  /**
   * Constant equal to {@value}
   * @since 0.6.3.0
   */
  private static final String TOKEN_VALIDATOR = "token.validator";
  
  /**
   * Constant equal to {@value}
   * @since 0.6.3.0
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
   * @since 0.6.3.0
   */
  private String token = null;
  
  /**
   * The {@link TokenValidator}
   * @since 0.6.3.0 
   */
  private TokenValidator tokenValidator;
  
  /**
   * The {@code args} {@link List} from {@link YADARequest#getArgs}
   * @since 0.6.3.0
   */
  private List<String> args;
  
  /**
   * The {@code preArgs} {@link List} from {@link YADARequest#getPreargs}
   * @since 0.6.3.0
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
	public void engage(YADARequest yadaReq, YADAQuery yq) throws YADAPluginException {
	  setYADARequest(yadaReq);
	  setYADAQuery(yq);
	  setArgs(Arrays.asList(getYADAQuery().getYADAQueryParamValue(YADARequest.PS_ARGS)));
	  setPreargs(Arrays.asList(getYADAQuery().getYADAQueryParamValue(YADARequest.PS_PREARGS)));
	}

	/**
	 * Convenience method with calls both {@link #validateURL()} and {@link #applyPolicy()}
	 * @since 0.6.3.0
	 */
	@Override
	public void validateYADARequest() throws YADAPluginException {
	   validateURL();
	   applyPolicy();
	}
	
	/**
	  * Throws an exception if the URL returned by {@code req.getRequestURL()} is unauthenticated.
	  * @throws YADAPluginException if a non-authenticated URL is requested
	  * @since 0.6.3.0
	  */
	@Override
	public void validateURL() throws YADAPluginException 
	{
	  String pathRx = getArgumentValue(AUTH_PATH_RX);
	  if(pathRx.length() > 0)
	  {
	    String reqUrl = getYADARequest().getRequest().getRequestURL().toString();
	    if(!reqUrl.matches(pathRx)) {
	      String msg = "Unauthorized.  This query requires use of an authenticated address.";
	      throw new YADAPluginException(msg);
	    }
	  }
	}
	
	/**
	 * Default implementation calls {@link TokenValidator#applyPolicy()} via injection
	 * @since 0.6.3.0
	 */
  @Override
  public void applyPolicy() throws YADAPluginException
  {
    setTokenValidator();
    getTokenValidator().prevalidate();
    getTokenValidator().validate();
  }
	
  /**
   * Default implementation calls {@link TokenValidator#applyPolicy(TokenValidator)} via injection
   * @since 0.6.3.0
   */
  @Override
  public void applyPolicy(TokenValidator tokenValidator) throws YADAPluginException
  {
    setTokenValidator(tokenValidator);
    getTokenValidator().prevalidate();
    getTokenValidator().validate();
  }
  
  /**
   * 
   * @param key
   * @return
   * @since 0.6.3.0
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
   * @since 0.6.3.0
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
   * Standard mutator for variable
   * @param token 
   * @since 0.6.3.0
   */
  public void setToken(String token)
  {
    this.token = token;
  }
  
  /**
   * Standard accessor for variable
   * @return the value of the validated {@code NIBR521} header
   * @since 0.6.3.0
   */
  public String getToken() 
  {
    return this.token;
  }
  
  
  /**
   * No arg mutator for variable, gets FQCN from args or properties 
   * @throws YADAPluginException 
   * @since 0.6.3.0
   */
  public void setTokenValidator() throws YADAPluginException
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
          name = getArgumentValue(PLUGIN_PKG+TOKEN_VALIDATOR);
          clazz = Class.forName(name);
        }
        catch (ClassNotFoundException e1)
        {
          String msg = "Could not find the specified TokenValidator class: " + name + ".";
          throw new YADAPluginException(msg,e1);
        }
      }
      
      try 
      {
        setTokenValidator((TokenValidator)clazz.newInstance());
      } 
      catch (InstantiationException | IllegalAccessException e) {
        String msg = "Could not instantiate the specified TokenValidator class: " + name;
        throw new YADAPluginException(msg,e);
      }
    }
  }
  
  /**
   * Standard mutator for variable
   * @param tokenValidator the {@link TokenValidator} instance
   * @since 0.6.3.0
   */
  public void setTokenValidator(TokenValidator tokenValidator)
  {
    this.tokenValidator = tokenValidator;
  }
  
  /**
   * Standard accessor for variable
   * @return the {@link TokenValidotor} instance
   * @since 0.6.3.0
   */
  public TokenValidator getTokenValidator() 
  {
    return this.tokenValidator;
  }

  /**
   * @return the args
   * @since 0.6.3.0
   */
  public List<String> getArgs() {
    return this.args;
  }

  /**
   * @param args the args to set
   * @since 0.6.3.0
   */
  public void setArgs(List<String> args) {
    this.args = args;
  }
  
  /**
   * @return the preargs
   * @since 0.6.3.0
   */
  public List<String> getPreArgs() {
    return this.preargs;
  }

  /**
   * @param preargs the preargs to set
   * @since 0.6.3.0
   */
  public void setPreargs(List<String> preargs) {
    this.preargs = preargs;
  }
}
