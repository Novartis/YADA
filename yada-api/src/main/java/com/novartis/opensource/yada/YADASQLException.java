/**
 * 
 */
package com.novartis.opensource.yada;

/**
 * Thrown when encountering an error while processing SQL.
 * @author David Varon
 *
 */
public class YADASQLException extends YADAException
{

	/**
	 * Support for serialization
	 */
	private static final long serialVersionUID = -6530875641148645080L;

	/**
	 * Constructs a new exception with a null message.
	 */
	public YADASQLException()
	{
	}

	/**
	 * Constructs a new exception with a specified message
	 * @param message the message to report using {@link #getMessage()}
	 */
	public YADASQLException(String message)
	{
		super(message);
	}

	/**
	 * Constructs a new exception with a speificed cause
	 * @param cause the {@link Throwable} that led to this exception
	 */
	public YADASQLException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Constructs a new exception with a speificed cause and message
	 * @param message message the message to report using {@link #getMessage()}
	 * @param cause the {@link Throwable} that led to this exception 
	 */
	public YADASQLException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
