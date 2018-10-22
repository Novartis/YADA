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

import java.util.List;

import com.novartis.opensource.yada.YADASecurityException;

/**
 * @author Dave Varon
 * @since 7.0.0
 */
public interface SecurityPolicy {

  /**
   * Constant equal to {@value}.
   * Refers to a security query in the {@code YADA_A11N} table.
   * For the pre-process to approve running the original query, 
   * the security query must return 0 rows.
   */
  public static final String BLACKLIST = "blacklist";
  
  /**
   * Constant equal to {@value}.
   * Refers to a security query in the {@code YADA_A11N} table.
   * For the pre-process to approve running the original query, 
   * the security query must &gt;0 rows.
   */
  public static final String WHITELIST = "whitelist";
  
  /**
   * Constant equal to {@value}.
   * Refers to the value stored in the {@code YADA_A11N.POLICY} column pertaining to the protected qname.
   * @since 7.0.0
   */
  public static final String CONTENT_POLICY_CODE = "C";
  
  /**
   * Constant equal to {@value}
   * Refers to the value stored in the {@code YADA_A11N.POLICY} column pertaining to the protected qname.
   * @since 7.0.0
   */
  public static final String EXECUTION_POLICY_CODE = "E";
  /**
   * 
   * @throws YADASecurityException when the user is unauthorized or there is an error in policy processing
   */
  public void applyPolicy() throws YADASecurityException;
  
  /**
   * @param securityPolicyCode the variety of security policy (currently always {@code E}
   * @return a {@link java.util.HashMap} containing a policy key and query value.  
   * @throws YADASecurityException when the policy can't be processed
   */
  public List<SecurityPolicyRecord> getSecurityPolicyRecords(String securityPolicyCode) throws YADASecurityException;
  
  /**
   * Should returns {@code true} if the security target is 
   * associated to a {@link #WHITELIST} policy type in the 
   * {@code YADA_A11N} table
   * 
   * @param type
   *          the value of the {@code TYPE} field in the
   *          {@code YADA_A11N} table
   * @return {@code true} if {@code TYPE} is {@link #WHITELIST}
   */
  public boolean isWhitelist(String type);
  
  /**
   * Should return {@code true} if the security target is 
   * associated to a {@link #BLACKLIST} policy type in the 
   * {@code YADA_A11N} table
   * 
   * @param type
   *          the value of the {@code TYPE} field in the
   *          {@code YADA_A11N} table
   * @return {@code true} if {@code TYPE} is {@link #BLACKLIST}
   */
  public boolean isBlacklist(String type);
  
  /**
   * Provides route to inject HTTP header value into security policy.
   * @param header the name of the header from which to retrieve the value
   * @return the value of the request header, or {@code null} if the header was not included
   * @since 7.0.0
   */
  public String getHeader(String header);

  /**
   * Provides route to inject HTTP cookie value into the security policy.
   * @param cookie the name of the cookie for which to retrieve the value
   * @return the value of the desired cookie or {@code null} if the cookie was not included in the request
   * @since 7.0.0
   */
  public String getCookie(String cookie);
  
  /**
   * Provides route to inject session variables into the security policy.
   * @param attribute the name of the session attribute for which to retrieve the value
   * @return the value of the desired attribute or {@code null} if the attribute was not included in the session
   * @since 8.1.0
   */
  public Object getSessionAttribute(String attribute);
  
  /**
   * Provides route to inject data values passed in request into the security policy.
   * @param key the name of the column for which to retrieve the value
   * @return the value of the desired column or {@code null} if the column was not included in the request
   * @since 8.1.0
   */
  public String getValue(String key);
  
  /**
   * Provides route to inject data values passed in request into the security policy.
   * @param index the index of the column for which to retrieve the value
   * @return the value of the desired column or {@code null} if the column was not included in the request
   * @since 8.1.0
   */
  public String getValue(int index);
}
