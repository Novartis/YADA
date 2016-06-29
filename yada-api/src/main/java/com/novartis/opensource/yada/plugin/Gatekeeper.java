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
package com.novartis.opensource.yada.plugin;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.novartis.opensource.yada.Finder;
import com.novartis.opensource.yada.JSONParams;
import com.novartis.opensource.yada.JSONParamsEntry;
import com.novartis.opensource.yada.YADAConnectionException;
import com.novartis.opensource.yada.YADAFinderException;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADAQueryConfigurationException;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.plugin.AbstractPreprocessor;
import com.novartis.opensource.yada.plugin.YADASecurityException;
import com.novartis.opensource.yada.util.YADAUtils;

/**
 * A Preprocess plugin to evaluate user authorization for query execution.
 * 
 * @author David Varon
 * 
 */
public class Gatekeeper extends AbstractPreprocessor {

  /**
   * Local logger handle
   */
  private static final Logger LOG = Logger.getLogger(Gatekeeper.class);
  /**
   * Constant equal to {@value}
   */
  protected static final String DEFAULT_AUTH_TOKEN_PROPERTY = "security.token";
  /**
   * Constant equal to {@value}
   */
  protected static final String EXECUTION_POLICY_COLUMNS = "execution.policy.columns";
  /**
   * Constant equal to {@value}
   */
  protected static final String EXECUTION_POLICY_INDICES = "execution.policy.indices";
  /**
   * Constant equal to {@value}
   */
  protected static final String EXECUTION_POLICY_INDEXES = "execution.policy.indexes";

  
 
  /**
   * Validates the request host, user, security params, and security query
   * execution results
   * 
   * @throws YADAPluginException
   *           , YADASecurityException
   * @see com.novartis.opensource.yada.plugin.AbstractPreprocessor#engage(com.novartis.opensource.yada.YADARequest,
   *      com.novartis.opensource.yada.YADAQuery)
   */
  @Override
  public void engage(YADARequest yReq, YADAQuery yq) throws YADAPluginException, YADASecurityException {
    super.engage(yReq, yq);
    try 
    {
      validateYADARequest();
    } 
    catch (Exception e) 
    {
      String msg = "Unable to process security spec";
      throw new YADASecurityException(msg, e);
    }
  }

  /**
   * Overrides {@link TokenValidator#validate()}. Default sets token to value of
   * {@link #DEFAULT_AUTH_TOKEN_PROPERTY} system property.
   * 
   * @throws YADASecurityException
   *           when the {@link #DEFAULT_AUTH_TOKEN_PROPERTY} is not set
   */
  @Override
  public void validateToken() throws YADASecurityException 
  {
    String token = System.getProperty(DEFAULT_AUTH_TOKEN_PROPERTY);
    if(token == null || token.equals(""))
      throw new YADASecurityException("Unauthorized. "+DEFAULT_AUTH_TOKEN_PROPERTY+" system property not set.");
    setToken(token);
  }

  /**
   * Returns {@code true} if {@link #WHITELIST} or {@link #BLACKLIST} is stored
   * in the {@code YSEC_PARAMS} table corresponding to the security target
   * 
   * @param policy
   *          the value of the {@code YSEC_PARAM_NAME} field in the
   *          {@code YSEC_PARAMS} table
   * @return {@code true} if {@link #WHITELIST} or {@link #BLACKLIST} is set
   */
  protected boolean hasValidPolicy(String policy) {
    return isWhitelist(policy) || isBlacklist(policy);
  }

  /**
   * Retrieves and processes the security query, and validates the results per
   * the security specification
   * 
   * @param spec
   *          the security specification for the requested query
   * @throws YADASecurityException
   *           when there is an issue retrieving or processing the security
   *           query
   */
  @Override
  public void applyExecutionPolicy() throws YADASecurityException 
  {
    
    //TODO the security query executes for every iteration of the qname 
    // in the current request. a flag needs to be set somewhere to indicate
    // clearance has already been granted.  This can't be in YADAQuery because of caching.
    
    //TODO needs to support app targets as well as qname targets
    
    List<SecurityPolicyRecord> spec = getSecurityPolicyRecords(EXECUTION_POLICY_CODE);
    List<SecurityPolicyRecord> prunedSpec = new ArrayList<>(); 
    // process security spec
    
    // query can be standard or json
    //  if json, need name of column to map to token
    //  if standard, need list of relevant indices
    
    String  policyColumns       = getArgumentValue(EXECUTION_POLICY_COLUMNS);
    String  policyIndices       = getArgumentValue(EXECUTION_POLICY_INDICES);
    policyIndices = policyIndices == null ? getArgumentValue(EXECUTION_POLICY_INDEXES) : policyIndices;
    String  polColParams_rx     = "^([\\d]+\\s?)+$";
    String  polColJSONParams_rx = "^([A-Za-z0-9_]+\\s?)+$";
    String  result              = "";
    int     index               = -1;
    boolean policyHasParams     = false;
    boolean policyHasJSONParams = false;
    boolean reqHasParams        = getYADARequest().getParams() == null || getYADARequest().getParams().length == 0 ? false : true;
    boolean reqHasJSONParams    = YADAUtils.hasJSONParams(getYADARequest());
    
    
    for (SecurityPolicyRecord secRec : spec) 
    {
      // Are params required for security query?
      if(policyIndices != null && policyIndices.matches(polColParams_rx))
      {
        policyHasParams = true;
      }
      
      if(policyColumns != null && policyColumns.matches(polColJSONParams_rx))
      {
        policyHasJSONParams = true;
      }
      // (hasParams || hasJSONParams) == true or false
      
      // request and policy must have syntax compatibility, i.e., matching param syntax, or no params
      if((policyHasParams && !reqHasJSONParams) || (policyHasJSONParams && !reqHasParams) 
          || (!policyHasParams && reqHasJSONParams) || (!policyHasJSONParams && reqHasParams)
          || !(policyHasParams || reqHasParams || policyHasJSONParams || reqHasJSONParams))
      {
        // confirm sec spec is config properly
        if (hasValidPolicy(secRec.getType())) // whitelist or blacklist
        { 
          // confirm sec spec is mapped to requested query
          try 
          {
            new Finder().getQuery(secRec.getA11nQname());
          } 
          catch (YADAFinderException e)
          {
            String msg = "Unauthorized. Authorization qname not found.";
            throw new YADASecurityException(msg);
          }
          catch (YADAConnectionException | YADAQueryConfigurationException e) 
          {
            String msg = "Unauthorized. Unable to check for security query. This could be a temporary issue.";
            throw new YADASecurityException(msg, e);
          }
          // security query exists
        }
        else
        {
          String msg = "Unauthorized, due to policy misconfiguration. Must be \"blacklist\" or \"whitelist.\"";
          throw new YADASecurityException(msg);
        }
        prunedSpec.add(secRec);
      }
    }

    // kill the query if there aren't any compatible specs
    if(prunedSpec.size() == 0)
    {
      String msg = "Unauthorized. Request parameter syntax is incompatible with policy.";
      throw new YADASecurityException(msg);
    }
    
    // process the relevant specs
    for (SecurityPolicyRecord secRec : prunedSpec) // policy code (E,C), policy type (white,black), target (qname), A11nqname
    {
      String   a11nQname  = secRec.getA11nQname();
      String   policyType = secRec.getType();
      
      
      // policy has params and req has compatible params 
      if(policyHasParams && !reqHasJSONParams)
      {
        String[] polCols    = policyIndices.split("\\s");
        //String[] polVals    = new String[polCols.length];
        StringBuilder polVals = new StringBuilder();

        if(reqHasParams)
        {
          for (int i = 0; i < polCols.length; i++) 
          {
            // handle as params
            // 1. get params from query
            List<String> vals = getYADAQuery().getVals(0);
            index = Integer.parseInt(polCols[i]);
            // 2. pass user column
            if(polVals.length() > 0)
              polVals.append(",");
            if(index >= vals.size())
              polVals.append((String)getToken());
            else
              polVals.append(vals.get(index));
          }
          // 3. execute the security query
          result = YADAUtils.executeYADAGet(new String[] { a11nQname }, new String[] {polVals.toString()});
        }
        else
        {
          for (int i = 0; i < polCols.length; i++) 
          {
            polVals.append((String) getToken());
          }
          result = YADAUtils.executeYADAGet(new String[] { a11nQname },new String[] {polVals.toString()});
        }
      }
      // policy has JSONParams and req has compatible JSONParams
      else if(policyHasJSONParams && reqHasJSONParams) 
      { 
        LOG.warn("Could not parse column value into integer -- it's probably a String");
        // handle as JSONParams
        // 1. get JSONParams from query (params)
        LinkedHashMap<String, String[]> dataRow = getYADAQuery().getDataRow(0);
        // 2. add user column if necessary
        //dataRow.put(policyColumns, new String[] { (String) getToken() });
        String[] polCols = policyColumns.split("\\s");
        for(String colname : polCols) 
        {
          if(!dataRow.containsKey(colname))
          {
            dataRow.put(colname, new String[] {(String)getToken()});
            break;
          }
        }
        // 3. execute the security query
        JSONParamsEntry jpe = new JSONParamsEntry();
        jpe.addData(dataRow);
        JSONParams jp = new JSONParams(a11nQname, jpe);
        result = YADAUtils.executeYADAGetWithJSONParams(jp);
      }
      else
      {
        // no parameters to pass to execution.policy query
        result = YADAUtils.executeYADAGet(new String[] { a11nQname }, new String[0]);
      }
      // parse result
      int count = new JSONObject(result).getJSONObject("RESULTSET").getInt("records");

      // Reject if necessary
      if ((isWhitelist(policyType) && count == 0) || (isBlacklist(policyType) && count > 0))
        throw new YADASecurityException("Unauthorized.");
    } 
      
    this.clearSecurityPolicy();
  }
  
  /**
   * Modified the original query by appending 
   * the authenticated token value in a dynamic predicate.
   * <p>Recall the {@link Service#engagePreprocess} method
   * will recall {@link QueryManager#endowQuery} to 
   * reconform the code after this {@link Preprocess} 
   * disengages.
   * 
   * 
   * @throws YADASecurityException when token retrieval fails
   * @since 0.7.0.0
   */
  @Override
  public void applyContentPolicy() throws YADASecurityException 
  {
    String SPACE      = " ";
    String AND        = "AND";
    String WHERE      = "WHERE";
    String RX_SQL     = "^\\s*(SELECT.+FROM.+)(WHERE.+)?(GROUP BY.+)?(ORDER BY.+)?$";
    StringBuilder q   = new StringBuilder();
    String query      = getYADAQuery().getYADACode();
    int     modifiers = Pattern.DOTALL|Pattern.CASE_INSENSITIVE;
    Pattern rxWhere   = Pattern.compile(RX_SQL,modifiers);
    Matcher m         = rxWhere.matcher(query.toUpperCase());
    if(m.matches() && m.group(2) != null)
    {
      q.append(m.group(1)); // SELECT.+FROM.+
      q.append(m.group(2)); // WHERE.+
      q.append(SPACE+AND);
    }
    else
    {
      q.append(m.group(1)); // SELECT.+FROM.+
      q.append(SPACE+WHERE);
    }
    q.append(SPACE+getToken().toString()); // TOKEN
    if(m.group(3) != null)
      q.append(m.group(3)); // GROUP BY.+
    if(m.group(4) != null)
      q.append(m.group(4)); // ORDER BY.+
    this.clearSecurityPolicy();
  }

  /**
   * Sets the local {@link TokenValidator} to {@code this}
   */
  @Override
  public void setTokenValidator() throws YADASecurityException {
    setTokenValidator(this);
  }
  
}
