/**
 * 
 */
package com.novartis.opensource.yada;

/**
 * @author David Varon
 *
 */
public class YADAQueryConfigurationException extends YADAException
{

	/**
	 * Support for serialization
	 */
	private static final long serialVersionUID = 827149356732000123L;

	/**
	 * Constructs a new exception with a null message.
	 */
	public YADAQueryConfigurationException()
	{
	}

	/**
	 * Constructs a new exception with a specified message
	 * @param message the message to report using {@link #getMessage()}
	 */
	public YADAQueryConfigurationException(String message)
	{
		super(message);
	}

	/**
	 * Constructs a new exception with a speificed cause
	 * @param cause the {@link Throwable} that led to this exception
	 */
	public YADAQueryConfigurationException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Constructs a new exception with a speificed cause and message
	 * @param message message the message to report using {@link #getMessage()}
	 * @param cause the {@link Throwable} that led to this exception 
	 */
	public YADAQueryConfigurationException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
