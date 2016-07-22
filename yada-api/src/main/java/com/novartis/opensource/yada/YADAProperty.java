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
package com.novartis.opensource.yada;

/**
 * @author varonda1
 * @since 7.1.0
 */
public class YADAProperty {

  /**
   * Identity of object to which the property is attached 
   */
  private String target;
  
  /**
   * Identity of property
   */
  private String name;
  
  /**
   * Value of property
   */
  private String value;
  
  /**
   * Default constructor
   */
  public YADAProperty() {
    
  }
  
  /**
   * Value-setting constructor
   * @param target identity of object to which the property is attached
   * @param name identity of property
   * @param value value of property
   */
  public YADAProperty(String target, String name, String value) {
    setTarget(target);
    setName(name);
    setValue(value);
  }
  /**
   * Standard accessor for variable
   * @return the target
   */
  public String getTarget() {
    return this.target;
  }
  /**
   * @param target the target to set
   */
  public void setTarget(String target) {
    this.target = target;
  }
  /**
   * Standard accessor for variable
   * @return the name
   */
  public String getName() {
    return this.name;
  }
  /**
   * Standard accessor for variable
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }
  /**
   * Standard accessor for variable
   * @return the value
   */
  public String getValue() {
    return this.value;
  }
  /**
   * Standard accessor for variable
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

}
