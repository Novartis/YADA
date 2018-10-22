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

import com.novartis.opensource.yada.plugin.YADAPluginException;

/**
 * Throw from a security preprocessor or adaptor when authentication or authorization fails. 
 * Moved to main package from {@code plugin} for {@code 8.7.0}
 * @author Dave Varon
 * @since 7.0.0
 *
 */
public class YADASecurityException extends YADAPluginException {

  /**
   * Serialization
   */
  private static final long serialVersionUID = 5226150837033317104L;

  /**
   * Null constructor
   */
  public YADASecurityException() {
  }

  /**
   * @param message message the message to report using {@link #getMessage()}
   */
  public YADASecurityException(String message) {
    super(message);
  }

  /**
   * @param cause the {@link Throwable} that led to this exception
   */
  public YADASecurityException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message message the message to report using {@link #getMessage()}
   * @param cause the {@link Throwable} that led to this exception 
   */
  public YADASecurityException(String message, Throwable cause) {
    super(message, cause);
  }

}
