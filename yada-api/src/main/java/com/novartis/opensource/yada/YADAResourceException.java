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
