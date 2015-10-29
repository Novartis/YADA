package com.novartis.opensource.yada.io;

import com.novartis.opensource.yada.YADAException;


/**
 * Thrown whenever a YADA class interaction with the file system causes an error.
 * @author David Varon
 *
 */
public class YADAIOException extends YADAException 
{
	/**
	 * Support for serialization
	 */
	private static final long serialVersionUID = -4526753554677295506L;

	/**
	 * Constructs a new exception with a null message.
	 */
	public YADAIOException() {
		super();
	}

	/**
	 * Constructs a new exception with a specified message
	 * @param message the message to report using {@link #getMessage()}
	 */
	public YADAIOException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a speificed cause
	 * @param cause the {@link Throwable} that led to this exception
	 */
	public YADAIOException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with a speificed cause and message
	 * @param message message the message to report using {@link #getMessage()}
	 * @param cause the {@link Throwable} that led to this exception 
	 */
	public YADAIOException(String message, Throwable cause) {
		super(message, cause);
		this.cause = cause;
	}
	
}