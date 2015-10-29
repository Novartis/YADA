package com.novartis.opensource.yada.plugin;

import com.novartis.opensource.yada.YADAException;

/**
 * Thrown when a plugin class encounters an exception.
 * @author David Varon
 *
 */
public class YADAPluginException extends YADAException {
	
	/**
	 * Support for serialization
	 */
	private static final long serialVersionUID = 5853178560749643355L;

	/**
	 * Constructs a new exception with a null message.
	 */
	public YADAPluginException() {
		super();
	}

	/**
	 * Constructs a new exception with a specified message
	 * @param message the message to report using {@link #getMessage()}
	 */
	public YADAPluginException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a speificed cause
	 * @param cause the {@link Throwable} that led to this exception
	 */
	public YADAPluginException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with a speificed cause and message
	 * @param message message the message to report using {@link #getMessage()}
	 * @param cause the {@link Throwable} that led to this exception 
	 */
	public YADAPluginException(String message, Throwable cause) {
		super(message + " (Original cause: " + cause.getMessage() + ")", cause);
		this.cause = cause;
	}
}
