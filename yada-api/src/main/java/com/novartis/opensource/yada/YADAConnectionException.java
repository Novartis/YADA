/**
 * 
 */
package com.novartis.opensource.yada;

/**
 * Thrown when the attempt to open or close a source connection fails.
 * @author David Varon
 *
 */
public class YADAConnectionException extends YADAException {

	/**
	 * Support for serialization
	 */
	private static final long serialVersionUID = -2209508639365222872L;

	/**
	 * Constructs a new exception with a null message.
	 */
	public YADAConnectionException() {
		super();
	}

	/**
	 * Constructs a new exception with a specified message
	 * @param message the message to report using {@link #getMessage()} 
	 */
	public YADAConnectionException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a speificed cause
	 * @param cause the {@link Throwable} that led to this exception
	 */
	public YADAConnectionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with a speificed cause and message
	 * @param message message the message to report using {@link #getMessage()}
	 * @param cause the {@link Throwable} that led to this exception 
 */
	public YADAConnectionException(String message, Throwable cause) {
		super(message, cause);
		this.cause = cause;
	}

}
