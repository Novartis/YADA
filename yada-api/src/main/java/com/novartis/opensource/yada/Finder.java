/**
 * 
 */
package com.novartis.opensource.yada;

/**
 * @author dvaron
 * @since 9.0.0
 */
interface Finder {
	public static String getEnv(String property) throws YADAResourceException {
		String result = System.getProperty(property);
		if(result == null || "".equals(result))
		{
			String msg = "The property [" + property + "] was not found.";
			throw new YADAResourceException(msg);
		} 
		return result;
	}
	
	public YADAQuery getQueryFromIndex(String q) throws YADAConnectionException, YADAFinderException, YADAQueryConfigurationException;
	public YADAQuery getQuery(String q) throws YADAConnectionException, YADAFinderException, YADAQueryConfigurationException;
	public YADAQuery getQuery(String q, boolean updateStats) throws YADAConnectionException, YADAFinderException, YADAQueryConfigurationException;
	void updateQueryStatistics(String qname, int accessCount) throws YADAConnectionException, YADAFinderException;
}
