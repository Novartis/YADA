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
 * Thrown when an attempt to access a object in the application context or system
 * properties fails.
 * @author David Varon
 */
public class YADAResourceException extends YADAException
{

	/**
	 * Support for serialization
	 */
	private static final long serialVersionUID = -2724171655930025152L;

	/**
	 * Constructs a new exception with a null message.
	 */
	public YADAResourceException()
	{
	}

	/**
	 * Constructs a new exception with a specified message
	 * @param message the message to report using {@link #getMessage()}
	 */
	public YADAResourceException(String message)
	{
		super(message);
	}

	/**
	 * Constructs a new exception with a speificed cause
	 * @param cause the {@link Throwable} that led to this exception
	 */
	public YADAResourceException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Constructs a new exception with a speificed cause and message
	 * @param message message the message to report using {@link #getMessage()}
	 * @param cause the {@link Throwable} that led to this exception 
	 */
	public YADAResourceException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
