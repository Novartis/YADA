package com.novartis.opensource.yada;

/**
 * Thrown when an error is encountered by the {@link com.novartis.opensource.yada.Parser} 
 * while parsing SQL.
 * @author David Varon
 *
 */
public class YADAParserException extends YADAException {
	
	/**
	 * Support for serialization
	 */
	private static final long serialVersionUID = 5663264602127850769L;

	/**
	 * Constructs a new exception with a null message.
	 */
	public YADAParserException() {
		super();
	}

	/**
	 * Constructs a new exception with a specified message
	 * @param message the message to report using {@link #getMessage()}
	 */
	public YADAParserException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a speificed cause
	 * @param cause the {@link Throwable} that led to this exception
	 */
	public YADAParserException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with a speificed cause and message
	 * @param message message the message to report using {@link #getMessage()}
	 * @param cause the {@link Throwable} that led to this exception 
	 */
	public YADAParserException(String message, Throwable cause) {
		super(message, cause);
		this.cause = cause;
	}
}
