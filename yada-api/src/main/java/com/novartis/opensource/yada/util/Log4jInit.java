package com.novartis.opensource.yada.util;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.PropertyConfigurator;

/**
 * YADA-specific implementation to enable dynamic monitoring of properties file.
 * @author David Varon
 * @see org.apache.log4j.PropertyConfigurator
 */
public class Log4jInit extends HttpServlet 
{

	/**
	 * For serialization
	 */
	private static final long serialVersionUID = -6064583716642478769L;

	@Override
	public void init() {
		String file   = getInitParameter("log4j-init-file");
	  // if the log4j-init-file is not set, then no point in trying
	  if(file != null) {
	  	System.out.println("log4j config is at ["+file+"]");
	  	PropertyConfigurator.configureAndWatch(file);
	  }
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) {
	 	//nothing to do
	}
}
