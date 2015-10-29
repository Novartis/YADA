/**
 * 
 */
package com.novartis.opensource.yada;

/**
 * Thrown when there is an issue parsing the YADA request parameters.
 * @author David Varon
 *
 */
public class YADARequestException extends YADAException
{

	/**
	 * Support for serialization
	 */
	private static final long serialVersionUID = -4074733860268963337L;

	/**
	 * Constructs a new exception with a null message.
	 */
	public YADARequestException()
	{
	}

	/**
	 * Constructs a new exception with a specified message
	 * @param message the message to report using {@link #getMessage()}
	 */
	public YADARequestException(String message)
	{
		super(message);
	}

	/**
	 * Constructs a new exception with a speificed cause
	 * @param cause the {@link Throwable} that led to this exception
	 */
	public YADARequestException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Constructs a new exception with a speificed cause and message
	 * @param message message the message to report using {@link #getMessage()}
	 * @param cause the {@link Throwable} that led to this exception 
	 */
	public YADARequestException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
