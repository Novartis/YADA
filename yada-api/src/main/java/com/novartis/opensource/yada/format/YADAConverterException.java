package com.novartis.opensource.yada.format;

import com.novartis.opensource.yada.YADAException;

/**
 * Thrown when a yada {@code Converter} implementation catches an error.
 * @author David Varon
 *
 */
public class YADAConverterException extends YADAException {

	/**
	 * Support for serialization
	 */
	private static final long serialVersionUID = -4289110005481744762L;

	/**
	 * Constructs a new exception with a null message.
	 */
	public YADAConverterException() {
		super();
	}

	/**
	 * Constructs a new exception with a specified message
	 * @param message the message to report using {@link #getMessage()}
	 */
	public YADAConverterException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a speificed cause
	 * @param cause the {@link Throwable} that led to this exception
	 */
	public YADAConverterException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with a speificed cause and message
	 * @param message message the message to report using {@link #getMessage()}
	 * @param cause the {@link Throwable} that led to this exception 
	 */
	public YADAConverterException(String message, Throwable cause) {
		super(message, cause);
		this.cause  = cause;
	}

}
