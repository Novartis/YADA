/**
 * 
 */
package com.novartis.opensource.yada.plugin;

/**
 * A simple POJO for transferring security policy metadata to the method applying the security policy.
 * @author varonda1
 * @since 0.7.0.0
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
    return target;
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
    return policyCode;
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
    return type;
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
    return a11nQname;
  }

  /**
   * Standard mutator for variable
   * @param a11nQname the a11nQname to set
   */
  public void setA11nQname(String a11nQname) {
    this.a11nQname = a11nQname;
  }

}
