/**
 * 
 */
package com.novartis.opensource.yada.format;

import com.novartis.opensource.yada.YADAException;

/**
 * Caused by failure to process in a {@code Response} subclass.
 * @author David Varon
 *
 */
public class YADAResponseException extends YADAException {

	/**
	 * Support for serialization
	 */
	private static final long serialVersionUID = -4969891037052263822L;
	
	/**
	 * Constructs a new exception with a null message.
	 */
	public YADAResponseException() {
		super();
	}

	/**
	 * Constructs a new exception with a specified message
	 * @param message the message to report using {@link #getMessage()}
	 */
	public YADAResponseException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a speificed cause
	 * @param cause the {@link Throwable} that led to this exception
	 */
	public YADAResponseException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with a speificed cause and message
	 * @param message message the message to report using {@link #getMessage()}
	 * @param cause the {@link Throwable} that led to this exception 
	 */
	public YADAResponseException(String message, Throwable cause) {
		super(message, cause);
		this.cause = cause;
	}
}
