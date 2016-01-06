/**
 * Copyright 2016 Novartis Institutes for BioMedical Research Inc.
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

import java.io.File;
import java.io.FilenameFilter;

import org.apache.log4j.Logger;

/**
 * An implementation of {@link java.io.FilenameFilter} enabling use of a Regular Expression as filter criteria.
 * @author David Varon
 * @since PROVISIONAL
 *
 */
public class RegexFilenameFilter implements FilenameFilter {

	/**
   * Local logger handle
   */
	private static Logger l = Logger.getLogger(RegexFilenameFilter.class);
	
	/**
	 * Default regular expression (any string): {@code "^.*$"}
	 */
	private String regex = "^.*$";
	
	/**
	 * Constructor.
	 * @param regex the regular expression to use for filtering
	 */
	public RegexFilenameFilter(String regex)
	{
		if (null != regex && !"".equals(regex))
		{
			this.regex = regex;
		}
		l.debug("Regex: ["+this.regex+"]");
	}
	
	/**
	 * Returns {@code true} if the {@code name} of the file in {@code dir} matches {@link #regex}
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	@Override
	public boolean accept(File dir, String name)
	{
		return name.matches(this.regex);
	}
}
