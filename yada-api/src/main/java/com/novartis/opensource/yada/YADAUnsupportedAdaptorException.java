package com.novartis.opensource.yada;

/**
 * Thrown when attempting to instantiate an {@link com.novartis.opensource.yada.adaptor.Adaptor}
 * subclass that doesn't exist, can't be found, or another exception is thrown while 
 * so doing.
 * @author David Varon
 *
 */
public class YADAUnsupportedAdaptorException extends YADAException {
	/**
	 * Support for serialization.
	 */
	private static final long serialVersionUID = -8084957677745033435L;
	
	/**
	 * Constructs a new exception with a null message.
	 */
	public YADAUnsupportedAdaptorException() {
		super();
	}

	/**
	 * Constructs a new exception with a specified message
	 * @param message the message to report using {@link #getMessage()}
	 */
	public YADAUnsupportedAdaptorException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a speificed cause
	 * @param cause the {@link Throwable} that led to this exception
	 */
	public YADAUnsupportedAdaptorException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with a speificed cause and message
	 * @param message message the message to report using {@link #getMessage()}
	 * @param cause the {@link Throwable} that led to this exception 
	 */
	public YADAUnsupportedAdaptorException(String message, Throwable cause) {
		super(message, cause);
		this.cause = cause;
	}
}
