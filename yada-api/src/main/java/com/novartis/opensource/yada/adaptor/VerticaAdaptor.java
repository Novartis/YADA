package com.novartis.opensource.yada.adaptor;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.YADARequest;

/**
 * For connecting to Vertica databases via JDBC. See {@link "https://my.vertica.com/docs/CE/6.0.1/HTML/index.htm#1395.htm"} for configuration info.
 * 
 * @author David Varon
 * @since 0.4.1.0
 */
public class VerticaAdaptor extends JDBCAdaptor {
	
	/**
   * Local logger handle
   */
	private static Logger l = Logger.getLogger(VerticaAdaptor.class);
	
	/**
	 * Default subclass constructor (calls {@code super()}
	 */
	public VerticaAdaptor() {
		super();
		l.debug("Initializing");
	}
	
	/**
	 * Subclass constructor, calls {@code super(yadaReq)}
	 * @param yadaReq YADA request configuration
	 */
	public VerticaAdaptor(YADARequest yadaReq)
	{
		super(yadaReq);
	}
	
}
