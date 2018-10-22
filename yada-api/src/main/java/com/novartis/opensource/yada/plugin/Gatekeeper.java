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

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.novartis.opensource.yada.Finder;
import com.novartis.opensource.yada.JSONParams;
import com.novartis.opensource.yada.JSONParamsEntry;
import com.novartis.opensource.yada.QueryManager;
import com.novartis.opensource.yada.Service;
import com.novartis.opensource.yada.YADAConnectionException;
import com.novartis.opensource.yada.YADAFinderException;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADAQueryConfigurationException;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADASecurityException;
import com.novartis.opensource.yada.plugin.AbstractPreprocessor;
import com.novartis.opensource.yada.util.YADAUtils;

/**
 * A Preprocess plugin to evaluate user authorization for query execution.
 *
 * @author David Varon
 * @since 7.0.0
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
   * Constant equal to {@value}
   */
  protected static final String CONTENT_POLICY_PREDICATE = "content.policy.predicate";
  /**
   * Constant equal to {@value}
   * @since 8.1.0
   */
  protected static final String RX_COL_INJECTION  = "(([a-zA-Z0-9_]+):)?(get[A-Z][a-zA-Z0-9_]+\\([A-Za-z0-9_]*\\))";
  /**
   * Constant equal to {@value}
   * @since 8.1.0
   */
  protected static final String RX_IDX_INJECTION  = "(([0-9]+):)?(get[A-Z][a-zA-Z0-9_]+\\([A-Za-z0-9_]*\\))";
  /**
   * Validates the request host, user, security params, and security query
   * execution results
   *
   * @throws YADAPluginException when plugin processing fails
   * @throws YADASecurityException when the user is unauthorized or there is an error in policy processing
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
   * Overrides {@link TokenValidator#validateToken()}. Default sets token to value of
   * {@link #DEFAULT_AUTH_TOKEN_PROPERTY} system property.
   *
   * @throws YADASecurityException when the {@link #DEFAULT_AUTH_TOKEN_PROPERTY} is not set
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
   * @throws YADASecurityException when there is an issue retrieving or processing the security
   *           query
   */
  @Override
  public void applyExecutionPolicy() throws YADASecurityException
  {

    //TODO the security query executes for every iteration of the qname
    // in the current request. a flag needs to be set somewhere to indicate
    // clearance has already been granted.  This can't be in YADAQuery because of caching.

    //TODO needs to support app targets as well as qname targets

    //TODO tests for auth failure, i.e., unauthorized
    //TODO tests for ignoring attempted plugin overrides
    //TODO make it impossible to execute a protector query as a primary query without a server-side flag set, or
    //  perhaps some authorization (i.e., for testing, maybe with a content policy)
    //  This will close an attack vector.
    //TODO support dependency injection for other methods in addition to token for execution policy

    List<SecurityPolicyRecord> spec = getSecurityPolicyRecords(EXECUTION_POLICY_CODE);
    List<SecurityPolicyRecord> prunedSpec = new ArrayList<>();
    // process security spec

    // query can be standard or json
    //  if json, need name of column to map to token
    //  if standard, need list of relevant indices

    String  policyColumns       = getArgumentValue(EXECUTION_POLICY_COLUMNS);
    String  policyIndices       = getArgumentValue(EXECUTION_POLICY_INDICES);
    policyIndices = policyIndices == null ? getArgumentValue(EXECUTION_POLICY_INDEXES) : policyIndices;
    String  polColParams_rx     = "^(("+RX_IDX_INJECTION+"|[\\d]+)\\s?)+$";
    String  polColJSONParams_rx = "^(("+RX_COL_INJECTION+"|[A-Za-z0-9_]+)\\s?)+$";
    String  result              = "";
    int     index               = -1;
    String  injectedIndex       = "";
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
        String[]      polCols = policyIndices.split("\\s");
        StringBuilder polVals = new StringBuilder();

        if(reqHasParams)
        {
          for (int i = 0; i < polCols.length; i++)
          {
            // handle as params
            // 1. get params from query
            List<String> vals = getYADAQuery().getVals(0);
            try
            {
              index = Integer.parseInt(polCols[i]);
            }
            catch(NumberFormatException e)
            {
              injectedIndex = polCols[i];
            }
            // 2. pass user column
            if(polVals.length() > 0)
              polVals.append(",");

            if(injectedIndex.equals("") && index > -1)
            {
              if(index >= vals.size())
                polVals.append((String)getToken());
              else
                polVals.append(vals.get(index));
            }
            else
            {
              Pattern  rxInjection = Pattern.compile(RX_IDX_INJECTION);
              Matcher  m1          = rxInjection.matcher(injectedIndex);
              if(m1.matches() && m1.groupCount() == 3) // injection
              {
                // parse regex: this is where the method value is injected
                //String   colIdx      = m1.group(2);
                String   colval      = m1.group(3);

                // find and execute injected method
                String method = colval.substring(0,colval.indexOf('('));
                String arg    = colval.substring(colval.indexOf('(')+1,colval.indexOf(')'));
                Object val    = null;
                try
                {
                  if(arg.equals(""))
                    val = getClass().getMethod(method).invoke(this, new Object[] {});
                  else
                    val = getClass().getMethod(method, new Class[] { java.lang.String.class }).invoke(this, new Object[] {arg});
                }
                catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
                {
                  String msg = "Unathorized. Injected method invocation failed.";
                  throw new YADASecurityException(msg, e);
                }

                // add/replace item in dataRow
                polVals.append(val);
              }
            }
            index = -1;
            injectedIndex = "";
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
        LOG.debug("Could not parse protector column value into integer, assuming it's a String");
        // handle as JSONParams
        // 1. get JSONParams from query (params)
        LinkedHashMap<String, String[]> dataRow = getYADAQuery().getDataRow(0);
        // 2. add user column if necessary
        String[] polCols = policyColumns.split("\\s");
        for(String colspec : polCols)
        {
          // dataRow can look like, e.g.: {COL1:val1,COL2:val2}
          // polCols can look like, e.g.: COL2 APP:getValue(TARGET)

          Pattern  rxInjection = Pattern.compile(RX_COL_INJECTION);
          Matcher  m1          = rxInjection.matcher(colspec);
          if(m1.matches() && m1.groupCount() == 3) // injection
          {
            // parse regex: this is where the method value is injected
            String   colname     = m1.group(2);
            String   colval      = m1.group(3);

            // find and execute injected method
            String method = colval.substring(0,colval.indexOf('('));
            String arg    = colval.substring(colval.indexOf('(')+1,colval.indexOf(')'));
            Object val    = null;
            try
            {
              if(arg.equals(""))
                val = getClass().getMethod(method).invoke(this, new Object[] {});
              else
                val = getClass().getMethod(method, new Class[] { java.lang.String.class }).invoke(this, new Object[] {arg});
            }
            catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
            {
              String msg = "Unathorized. Injected method invocation failed.";
              throw new YADASecurityException(msg, e);
            }

            // add/replace item in dataRow
            dataRow.put(colname, new String[] {(String)val});
          }
          else
          {
            if(!dataRow.containsKey(colspec)) // no injection AND no parameter
            {
              String msg = "Unathorized. Injected method invocation failed.";
              throw new YADASecurityException(msg);
            }
          }
        }

        // 3. execute the security query
        JSONParamsEntry jpe = new JSONParamsEntry();
        // dataRow now contains injected values () or passed values
        // if values were injected, they've overwritten the passed in version
        jpe.addData(dataRow);
        JSONParams jp = new JSONParams(a11nQname, jpe);
        result = YADAUtils.executeYADAGetWithJSONParamsNoStats(jp);
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
   * Modifies the original query by appending a dynamic predicate
   * <p>Recall the {@link Service}{@code .engagePreprocess} method
   * will recall {@link QueryManager}{@code .endowQuery()} to
   * reconform the code after this {@link Preprocess}
   * disengages.
   *
   *
   * @throws YADASecurityException when token retrieval fails
   */
  @Override
  public void applyContentPolicy() throws YADASecurityException
  {

    // TODO make it impossible to reset args and preargs dynamically if pl class implements SecurityPolicy
    //   this will close an attack vector

    String SPACE                = " ";
    StringBuilder contentPolicy = new StringBuilder();
    Pattern       rxInjection   = Pattern.compile(RX_COL_INJECTION);
    String        rawPolicy     = getArgumentValue(CONTENT_POLICY_PREDICATE);
    Matcher       m1            = rxInjection.matcher(rawPolicy);
    int           start         = 0;

    // field = getToken
    // field = getCookie(string)
    // field = getHeader(string)
    // field = getUser()
    // field = getRandom(string)

    if(!m1.find())
    {
      String msg = "Unathorized. Injected method invocation failed.";
      throw new YADASecurityException(msg);
    }

    m1.reset();

    while(m1.find())
    {
      int rxStart   = m1.start();
      int rxEnd     = m1.end();

      contentPolicy.append(rawPolicy.substring(start,rxStart));

      String frag   = rawPolicy.substring(rxStart,rxEnd);
      String method = frag.substring(0,frag.indexOf('('));
      String arg    = frag.substring(frag.indexOf('(')+1,frag.indexOf(')'));
      Object val    = null;
      try
      {
        if(arg.equals(""))
          val = getClass().getMethod(method).invoke(this, new Object[] {});
        else
          val = getClass().getMethod(method, new Class[] { java.lang.String.class }).invoke(this, new Object[] {arg});
      }
      catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
      {
        String msg = "Unathorized. Injected method invocation failed.";
        throw new YADASecurityException(msg, e);
      }
      contentPolicy.append((String)val +SPACE);

      start = rxEnd;
    }

    Expression parsedContentPolicy;
    try
    {
      parsedContentPolicy = CCJSqlParserUtil.parseCondExpression(contentPolicy.toString());
    }
    catch (JSQLParserException e)
    {
      String msg = "Unauthorized. Content policy is not valid.";
      throw new YADASecurityException(msg, e);
    }

    PlainSelect sql = (PlainSelect)((Select)getYADAQuery().getStatement()).getSelectBody();
    Expression  where  = sql.getWhere();

    if(where != null)
    {
      AndExpression and = new AndExpression(where,parsedContentPolicy);
      sql.setWhere(and);
    }
    else
    {
      sql.setWhere(parsedContentPolicy);
    }
    try
    {
      CCJSqlParserManager parserManager = new CCJSqlParserManager();
      sql = (PlainSelect)((Select) parserManager.parse(new StringReader(sql.toString()))).getSelectBody();
    }
    catch (JSQLParserException e)
    {
      String msg = "Unauthorized. Content policy is not valid.";
      throw new YADASecurityException(msg, e);
    }

    getYADAQuery().setCoreCode(sql.toString());
    this.clearSecurityPolicy();
  }

  /**
   * Utility function for content policy
   * @return the auth token wrapped in single quotes
   * @throws YADASecurityException when the token can't retrieved
   */
  public String getQToken() throws YADASecurityException
  {
    String quote = "'";
    return quote + getToken() + quote;
  }

  /**
   * Utility function for content policy
   * @return the auth token wrapped in single quotes
   * @since 8.1.0
   */
  public String getQLoggedUser()
  {
    String user = ((JSONArray)getSessionAttribute("YADA.user.privs")).getJSONObject(0).getString("USERID");
    String quote = "'";
    return quote + user + quote;
  }

  /**
   * Utility function for content policy
   */

  /**
   * Utility function for content policy
   * @param cookie the desired HTTP request cookie
   * @return the value of {@code cookie} wrapped in single quotes
   */
  public String getQCookie(String cookie)
  {
    String quote = "'";
    String val = super.getCookie(cookie);
    return quote + val + quote;
  }

  /**
   * Utility function for content policy
   * @param header the desired HTTP request header
   * @return the value of {@code header} wrapped in single quotes
   */
  public String getQHeader(String header)
  {
    String quote = "'";
    String val = super.getHeader(header);
    return quote + val + quote;
  }

  /**
   * Sets the local {@link TokenValidator} to {@code this}
   */
  @Override
  public void setTokenValidator() throws YADASecurityException {
    setTokenValidator(this);
  }

}
