/**
 * Copyright 2015 Novartis Institutes for BioMedical Research Inc.
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
 * The top of the YADA exception handling hierarchy.
 * @author David Varon
 */
public class YADAException extends Exception {

	/**
	 * Support for serialization
	 */
	private static final long serialVersionUID = -1367563098856320981L;
	
	/**
	 * The original exception thrown at runtime, leading to the throwing of this one. 
	 */
	public Throwable cause = null;
	
	/**
	 * Constructs a new exception with a null message.
	 */
	public YADAException() {
		super();
	}

	/**
	 * Constructs a new exception with a specified message
	 * @param message the message to report using {@link #getMessage()}
	 */
	public YADAException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a speificed cause
	 * @param cause the {@link Throwable} that led to this exception
	 */
	public YADAException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with a speificed cause and message
	 * @param message message the message to report using {@link #getMessage()}
	 * @param cause the {@link Throwable} that led to this exception 
	 */
	public YADAException(String message, Throwable cause) {
		super(message,cause);
		this.cause = cause;
	}
	
	
	@Override
	public synchronized Throwable getCause() {
	    return this.cause;
	}
	
	
	@Override
	public void printStackTrace() {
		super.printStackTrace();
		if (this.cause != null) {
			System.err.println("Caused by:");
			this.cause.printStackTrace();
		}
	}
	
	
	@Override
	public void printStackTrace(java.io.PrintStream ps) {
		super.printStackTrace(ps);
    if (this.cause != null) {
      ps.println("Caused by:");
      this.cause.printStackTrace(ps);
    }
	}
	
	
	@Override
	public void printStackTrace(java.io.PrintWriter pw) {
		super.printStackTrace(pw);
	  if (this.cause != null) {
	    pw.println("Caused by:");
	    this.cause.printStackTrace(pw);
	  }
	}

}
