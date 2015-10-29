package com.novartis.opensource.yada.adaptor;

import com.novartis.opensource.yada.YADAException;


/**
 * Typically thrown by any {@link com.novartis.opensource.yada.adaptor.Adaptor} 
 * subclass after catching any processing exception during
 * query execution.
 * @author David Varon
 *
 */
public class YADAAdaptorExecutionException extends YADAException {
	
	/**
	 * Support for serialization
	 */
	private static final long serialVersionUID = -6989604074578312784L;

	/**
	 * Constructs a new exception with a null message.
	 */
	public YADAAdaptorExecutionException() {
		super();
	}

	/**
	 * Constructs a new exception with a specified message
	 * @param message the message to report using {@link #getMessage()}
	 */
	public YADAAdaptorExecutionException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a speificed cause
	 * @param cause the {@link Throwable} that led to this exception
	 */
	public YADAAdaptorExecutionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with a speificed cause and message
	 * @param message message the message to report using {@link #getMessage()}
	 * @param cause the {@link Throwable} that led to this exception 
	 */
	public YADAAdaptorExecutionException(String message, Throwable cause) {
		super(message, cause);
		this.cause = cause;
	}
	
}
