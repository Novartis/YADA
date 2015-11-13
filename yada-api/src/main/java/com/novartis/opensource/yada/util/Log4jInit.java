/**
 * Copyright 2015 Novartis Institutes for BioMedical Research Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
