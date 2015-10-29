package com.novartis.opensource.yada.adaptor;

import com.novartis.opensource.yada.YADAException;


/**
 * Typically thrown by any {@link com.novartis.opensource.yada.adaptor.Adaptor} 
 * subclass after catching any processing exception during
 * query prep or execution.
 * @author David Varon
 *
 */
public class YADAAdaptorException extends YADAException {
	
	/**
	 * Support for serialization
	 */
	private static final long serialVersionUID = -6989604074578312784L;

	/**
	 * Constructs a new exception with a null message.
	 */
	public YADAAdaptorException() {
		super();
	}

	/**
	 * Constructs a new exception with a specified message
	 * @param message the message to report using {@link #getMessage()}
	 */
	public YADAAdaptorException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a speificed cause
	 * @param cause the {@link Throwable} that led to this exception
	 */
	public YADAAdaptorException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with a speificed cause and message
	 * @param message message the message to report using {@link #getMessage()}
	 * @param cause the {@link Throwable} that led to this exception 
	 */
	public YADAAdaptorException(String message, Throwable cause) {
		super(message, cause);
		this.cause = cause;
	}
	
}
