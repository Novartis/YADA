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

import com.novartis.opensource.yada.YADASecurityException;

/**
 * 
 * @author David Varon
 * @since 7.0.0
 */
public interface TokenValidator {
  
  /**
   * Approve or not the credentials or actions based on the credentials
   * @throws YADASecurityException when unable to validate the token
   */
  public void validateToken() throws YADASecurityException;
  /**
   * Standard accessor for variable
   * @return the security token object
   * @throws YADASecurityException when the token can't be retrieved
   */
  public Object getToken() throws YADASecurityException;
  /**
   * Standard mutator for variable
   * @param token tho security token
   * @throws YADASecurityException when the token can't be set
   */
  public void setToken(Object token) throws YADASecurityException;
  
  /**
   * No arg mutator for variable, gets FQCN from args or properties 
   * @throws YADASecurityException when there is an issue setting the token
   * @since 7.0.0
   */
  public void setToken() throws YADASecurityException;
}

