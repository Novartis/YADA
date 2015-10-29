package com.novartis.opensource.yada;


/**
 * Thrown by various methods when catching a variety of other execution exceptions.
 * @author David Varon
 *
 */
public class YADAExecutionException extends YADAException {
	/**
	 * Support for serialization.
	 */
	private static final long serialVersionUID = 560391989171922065L;
	
	/**
	 * Constructs a new exception with a null message.
	 */
	public YADAExecutionException() {
		super();
	}

	/**
	 * Constructs a new exception with a specified message
	 * @param message the message to report using {@link #getMessage()}
	 */
	public YADAExecutionException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a speificed cause
	 * @param cause the {@link Throwable} that led to this exception
	 */
	public YADAExecutionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with a speificed cause and message
	 * @param message message the message to report using {@link #getMessage()}
	 * @param cause the {@link Throwable} that led to this exception 
	 */
	public YADAExecutionException(String message, Throwable cause) {
		super(message, cause);
		this.cause = cause;
	}
}
