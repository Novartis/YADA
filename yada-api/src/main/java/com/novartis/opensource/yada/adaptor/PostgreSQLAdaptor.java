package com.novartis.opensource.yada.adaptor;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.YADARequest;

/**
 * For connecting to PostgreSQL databases via JDBC.
 * 
 * @author David Varon
 * @since 0.4.1.0
 */
public class PostgreSQLAdaptor extends JDBCAdaptor {
	
	/**
   * Local logger handle
   */
	private static Logger l = Logger.getLogger(PostgreSQLAdaptor.class);
	
	/**
	 * Default subclass constructor (calls {@code super()}
	 */
	public PostgreSQLAdaptor() {
		super();
		l.debug("Initializing");
	}
	
	/**
	 * Subclass constructor, calls {@code super(yadaReq)}
	 * @param yadaReq YADA request configuration
	 */
	public PostgreSQLAdaptor(YADARequest yadaReq)
	{
		super(yadaReq);
	}
}
