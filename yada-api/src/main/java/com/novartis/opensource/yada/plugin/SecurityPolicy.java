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

import java.util.HashMap;

/**
 * @author Dave Varon
 * @since 0.7.0.0
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
   * the security query must >0 rows.
   */
  public static final String WHITELIST = "whitelist";
  
  /**
   * Constant equal to {@value}.
   * Refers to the value stored in the {@code YADA_A11N.POLICY} column pertaining to the protected qname.
   * @since 0.7.0.0
   */
  public static final String CONTENT_POLICY_CODE = "C";
  
  /**
   * Constant equal to {@value}
   * Refers to the value stored in the {@code YADA_A11N.POLICY} column pertaining to the protected qname.
   * @since 0.7.0.0
   */
  public static final String EXECUTION_POLICY_CODE = "E";
  /**
   * 
   * @throws YADASecurityException
   */
  public void applyPolicy() throws YADASecurityException;
  
  /**
   * @param securityPolicyCode 
   * @return a {@link HashMap} containing a policy key and query value.  
   * @throws YADASecurityException 
   */
  public HashMap<String,String> getSecurityPolicyMap(String securityPolicyCode) throws YADASecurityException;
  
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
}
