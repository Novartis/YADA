/**
 * 
 */
package com.novartis.opensource.yada;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author dvaron
 * @since 9.0.0
 */
public class YADASecuritySpec extends HashMap<String, Object> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  /**
   * 
   */
  public final static String KEY_AUTH_PATH_RX = "auth.path.rx";
  /**
   * 
   */
  public final static String KEY_POLICY = "policy";
  /**
   * 
   */
  public final static String KEY_TYPE = "type";
  /**
   * 
   */
  public final static String KEY_QUALIFIER = "qualifier";
  /**
   * 
   */
  public final static String KEY_PREDICATE = "predicate";
  /**
   * 
   */
  public final static String KEY_PROTECTOR = "protector";
  /**
   * 
   */
  public final static String KEY_COLUMNS = "columns";
  /**
   * 
   */
  public final static String KEY_INDEXES = "indexes";
  /**
   * 
   */
  public final static String KEY_INDICES = "indices";
  /**
   * 
   */
  public final static String KEY_TOKEN_VALIDATOR = "token.validator";
  /**
   * 
   */
  public final static String POLICY_EXECUTION = "E";
  /**
   * 
   */
  public final static String POLICY_CONTENT = "C";
  /**
   * 
   */
  public final static String POLICY_AUTHORIZATION  = "A";
  /**
   * 
   */
  public final static String TYPE_ALLOWLIST = "whitelist";
  /**
   * 
   */
  public final static String TYPE_DENYLIST = "blacklist";
  /**
   * Constant equal to {@value}
   * 
   * @since 9.0.0 (moved from {@link com.novartis.opensource.yada.plugin.Gatekeeper}
   */
  public static final String RX_COL_INJECTION = "(([a-zA-Z0-9_]+):)?(get[A-Z][a-zA-Z0-9_]+\\([A-Za-z0-9_]*\\))";

  
  /**
   * Constant equal to {@value}
   * 
   * @since 9.0.0 (moved from {@link com.novartis.opensource.yada.plugin.Gatekeeper}
   */
  public static final String RX_IDX_INJECTION = "(([0-9]+):)?(get[A-Z][a-zA-Z0-9_]+\\([A-Za-z0-9_]*\\))";
  
  
  /**
   * Constant equal to {@value}
   * @since 9.0.0
   */
  public final static String RX_COL = "^(("+RX_COL_INJECTION+"|[A-Za-z0-9_]+)\\s?)+$";
  
  /**
   * Constant equal to {@value}
   * @since 9.0.0
   */
  public final static String RX_IDX = "^(("+RX_IDX_INJECTION+"|[\\d]+)\\s?)+$";
  
  /**
   * 
   */
  public final static List<String> keyFields = getKeyFields(); 
  

  
  /**
   * 
   */
  public YADASecuritySpec() {}
  
  /**
   * @param spec
   */
  public YADASecuritySpec(JSONObject spec) throws YADAQueryConfigurationException
  {    
    for(String key : JSONObject.getNames(spec))
    {      
      // only add key to spec if it matches a private "KEY" field
      if(keyFields.contains(key))
      {
        try
        {
          this.put(key, spec.get(key));
        }
        catch(JSONException e)
        {
          String msg = String.format("%s key caused an error", key);
          throw new YADAQueryConfigurationException(msg);
        }        
      }
      else
      {
        String msg = String.format("%s is not permitted in YADASecuritySpec", key);
        throw new YADAQueryConfigurationException(msg);
      }
    }
  }
  
  /**
   * @return
   */
  public final static List<String> getKeyFields() 
  {
    List<String> names = new ArrayList<String>();
    for(Field field : YADASecuritySpec.class.getDeclaredFields())
    {
      if(field.getName().startsWith("KEY"))
      {
        names.add(field.getName());
      }
    }
    return names;
  }
  
  /**
   * @param value
   * @throws YADAQueryConfigurationException
   */
  public void setTokenValidator(String value) throws YADAQueryConfigurationException 
  {
    if(null == value)
      throw new YADAQueryConfigurationException("Token validator value must be non-null");
    this.put(KEY_TOKEN_VALIDATOR, value);
  }
  
  /**
   * @return
   */
  public String getTokenValidator()
  {
    return (String) this.get(KEY_TOKEN_VALIDATOR);
  }
  
  /**
   * @param value
   * @throws YADAQueryConfigurationException
   */
  public void setURLSpec(String value) throws YADAQueryConfigurationException 
  {
    if(null == value)
      throw new YADAQueryConfigurationException("Authorized URL path expression must be non-null");
    this.put(KEY_AUTH_PATH_RX, value);
  }
  
  /**
   * @return
   */
  public String getURLSpec() 
  {
    return (String) this.get(KEY_AUTH_PATH_RX);
  }
  
  /**
   * @param type
   * @param protector
   * @param columnsOrIndexes
   * @throws YADAQueryConfigurationException
   */
  public void setExecutionPolicy(String type, String protector, String columnsOrIndexes) throws YADAQueryConfigurationException 
  {
    if(null == type || null == protector || (!columnsOrIndexes.matches(RX_COL) && !columnsOrIndexes.matches(RX_IDX)))
      throw new YADAQueryConfigurationException("policy, type, protector, and columns, indexes, or indices must be non-null");
    this.put(KEY_POLICY, POLICY_EXECUTION);
    this.put(KEY_TYPE, type);
    this.put(KEY_PROTECTOR, protector);
    if(columnsOrIndexes.matches(RX_IDX))
    {
      this.put(KEY_INDEXES, columnsOrIndexes);
    }
    else
    {
      this.put(KEY_COLUMNS, columnsOrIndexes);
    }
  }
  
  /**
   * @return
   */
  public Map<String, Object> getExecutionPolicy()  
  {
    Map<String,Object> policy = null;
    if(this.get(KEY_POLICY) == POLICY_EXECUTION)
    {
      policy = new HashMap<String,Object>();
      policy.put(KEY_TYPE, (String) this.get(KEY_TYPE));
      policy.put(KEY_PROTECTOR, (String) this.get(KEY_PROTECTOR));
      if(this.get(KEY_COLUMNS) != null)
        policy.put(KEY_COLUMNS, (List<?>) this.get(KEY_COLUMNS));
      else if(this.get(KEY_INDEXES) != null)
        policy.put(KEY_INDEXES, (List<?>) this.get(KEY_INDEXES));
      else
        policy.put(KEY_INDEXES, (List<?>) this.get(KEY_INDICES));
      
    }
    return policy;
  }
  
  /**
   * @param type
   * @param qualifier
   * @throws YADAQueryConfigurationException
   */
  public void setAuthorizationPolicy(String type, String qualifier) throws YADAQueryConfigurationException 
  {
    if(null == type || null == qualifier)
      throw new YADAQueryConfigurationException("policy, type, protector, and columns, indexes, or indices must be non-null");
    this.put(KEY_POLICY, POLICY_AUTHORIZATION);
    this.put(KEY_TYPE, type);
    this.put(KEY_QUALIFIER, qualifier);    
  }
  
  /**
   * @return
   */
  public Map<String,Object> getAuthorizationPolicy()  
  {
    Map<String,Object> policy = null;
    if(this.get(KEY_POLICY) == POLICY_AUTHORIZATION)
    {
      policy = new HashMap<String,Object>();
      policy.put(KEY_TYPE, (String) this.get(KEY_TYPE));
      policy.put(KEY_QUALIFIER, (List<?>) this.get(KEY_QUALIFIER));
    }
    return policy;
  }
  
  /**
   * @param type
   * @param predicate
   * @throws YADAQueryConfigurationException
   */
  public void setContentPolicy(String type, String predicate) throws YADAQueryConfigurationException 
  {
    if(null == type || null == predicate)
      throw new YADAQueryConfigurationException("policy, type, protector, and columns, indexes, or indices must be non-null");
    this.put(KEY_POLICY, POLICY_CONTENT);
    this.put(KEY_TYPE, type);
    this.put(KEY_PREDICATE, predicate);    
  }
  
  /**
   * @return
   */
  public Map<String,String> getContentPolicy()  
  {
    Map<String,String> policy = null;
    if(this.get(KEY_POLICY) == POLICY_CONTENT)
    {
      policy = new HashMap<String,String>();
      policy.put(KEY_TYPE, (String) this.get(KEY_TYPE));
      policy.put(KEY_PREDICATE, (String) this.get(KEY_PREDICATE));
    }
    return policy;
  }
}
