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
