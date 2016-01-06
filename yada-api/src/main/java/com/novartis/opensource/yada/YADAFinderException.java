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
package com.novartis.opensource.yada;

/**
 * Thrown when {@link com.novartis.opensource.yada.Finder} encounters a runtime exception,
 * usually due to an attempt to retrieve an unknown query.
 * @author David Varon
 *
 */
public class YADAFinderException extends YADAException {
	
	/**
	 * Support for serialization
	 */
	private static final long serialVersionUID = -2037191253423467700L;

	/**
	 * Constructs a new exception with a null message.
	 */
	public YADAFinderException() {
		super();
	}

	/**
	 * Constructs a new exception with a specified message
	 * @param message the message to report using {@link #getMessage()}
	 */
	public YADAFinderException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a speificed cause
	 * @param cause the {@link Throwable} that led to this exception
	 */
	public YADAFinderException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with a speificed cause and message
	 * @param message message the message to report using {@link #getMessage()}
	 * @param cause the {@link Throwable} that led to this exception 
	 */
	public YADAFinderException(String message, Throwable cause) {
		super(message, cause);
		this.cause = cause;
	}
}
