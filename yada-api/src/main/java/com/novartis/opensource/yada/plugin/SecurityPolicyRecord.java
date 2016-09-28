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

/**
 * A simple POJO for transferring security policy metadata to the method applying the security policy.
 * @author varonda1
 * @since 7.0.0
 */
public class SecurityPolicyRecord {

  /**
   * The qname (or app, in a future version) to which the policy is applied
   */
  private String target;
  /**
   * {@code E} for execution policy, {@code C} for content policy.
   */
  private String policyCode;
  /**
   * {@code whitelist} for security queries that should return rows, {@code blacklist} for those that shouldn't
   */
  private String type;
  /**
   * The qname of the security query to be execution.
   */
  private String a11nQname;

  /**
   * Null constructor.
   */
  public SecurityPolicyRecord() {

  }
  
  /**
   * Create a security policy record
   * 
   * @param target The qname (or app, in a future version) to which the policy is applied
   * @param policyCode {@code E} for execution policy, {@code C} for content policy.
   * @param type {@code whitelist} for security queries that should return rows, {@code blacklist} for those that shouldn't
   * @param a11nQname The qname of the security query to be execution.
   */
  public SecurityPolicyRecord(String target, String policyCode, String type, String a11nQname) {
    this.setTarget(target);
    this.setPolicyCode(policyCode);
    this.setType(type);
    this.setA11nQname(a11nQname);
  }

  /**
   * Standard accessor for variable
   * @return the target
   */
  public String getTarget() {
    return this.target;
  }

  /**
   * Standard mutator for variable
   * @param target the target to set
   */
  public void setTarget(String target) {
    this.target = target;
  }

  /**
   * Standard accessor for variable
   * @return the policyCode
   */
  public String getPolicyCode() {
    return this.policyCode;
  }

  /**
   * Standard mutator for variable
   * @param policyCode the policyCode to set
   */
  public void setPolicyCode(String policyCode) {
    this.policyCode = policyCode;
  }

  /**
   * Standard accessor for variable
   * @return the type
   */
  public String getType() {
    return this.type;
  }

  /**
   * Standard mutator for variable
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Standard accessor for variable
   * @return the a11nQname
   */
  public String getA11nQname() {
    return this.a11nQname;
  }

  /**
   * Standard mutator for variable
   * @param a11nQname the a11nQname to set
   */
  public void setA11nQname(String a11nQname) {
    this.a11nQname = a11nQname;
  }

}
