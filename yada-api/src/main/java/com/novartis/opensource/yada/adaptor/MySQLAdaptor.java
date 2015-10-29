package com.novartis.opensource.yada.adaptor;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.YADARequest;

/**
 * For connecting to MySQL databases via JDBC.
 * 
 * @author David Varon
 * @since 0.3.0.0
 */
public class MySQLAdaptor extends JDBCAdaptor {
	
	/**
   * Local logger handle
   */
	private static Logger l = Logger.getLogger(MySQLAdaptor.class);
	
	/**
	 * Default subclass constructor (calls {@code super()}
	 */
	public MySQLAdaptor() {
		super();
		l.debug("Initializing");
	}
	
	/**
	 * Subclass constructor, calls {@code super(yadaReq)}
	 * @param yadaReq YADA request configuration
	 */
	public MySQLAdaptor(YADARequest yadaReq)
	{
		super(yadaReq);
	}
}
