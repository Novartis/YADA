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
package com.novartis.opensource.yada.io;

import java.io.Reader;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * The abstract root of the {@code yada.io} package.  It contains constants and method stubs and implementations
 * to facilitate parsing of files, primarily by plugins.
 * @author David Varon
 *
 */
public abstract class FileHelper 
{
	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(FileHelper.class);
	
	/**
	 * Constant equal to: {@value} 
	 */
	public final static String TAB     = "\t";
	/**
	 * Constant equal to: {@value} 
	 */
	public final static String COMMA   = ",";
	/**
	 * Constant equal to: {@value} 
	 */
	public final static String PIPE    = "|";
	/**
	 * Constant equal to: {@value} 
	 */
	public final static String NEWLINE = "\n";
	/**
	 * Constant equal to: {@value} 
	 */
	public final static int    DEFAULT_HEADER_LINE_NUMBER = 0;
	/**
	 * Constant equal to: {@value} 
	 */
	public final static int    DEFAULT_BYTE_OFFSET        = 0;
	/**
	 * Constant equal to: {@value} 
	 */
	public final static String DEFAULT_DELIMITER          = TAB;
	/**
	 * Constant equal to: {@code System.getProperty("line.separotor")} usually a newline.
	 */
	public final static String DEFAULT_LINE_SEPARATOR     = System.getProperty("line.separator");
	
	/**
	 * Instance variable for {@code java.io} utilization
	 */
	protected Reader		         reader;
	/**
	 * Instance variable for storing file header
	 */
	private   String             fileHeader       = null;
	/**
	 * Instance variable for storing column header
	 */
	private   String             columnHeader     = null;
	/**
	 * Instance variable for storing line in file occupied by column header row. Defaults to {@link #DEFAULT_HEADER_LINE_NUMBER}
	 */
	protected int                headerLineNumber = DEFAULT_HEADER_LINE_NUMBER;
	/**
	 * Instance variable for storing byte offset in file where header row begins. Defaults to {@link #DEFAULT_BYTE_OFFSET}
	 */
	protected int                headerByteOffset = DEFAULT_BYTE_OFFSET;
	/**
	 * Instance variable for storage of name/value pairs found in file header
	 */
	protected Map<String,String> fileHeaderMap  = null;
	/**
	 * Indexed instance variable for storage of column header values and positions
	 */
	protected String[]           colHeaderArray = null;
	
	/**
	 * Method stub for mutator.  Requires subclass implementation.
	 */
	protected void               setHeaderLineNumber()          { l.debug("Nothing to do."); }
	/**
	 * Standard accessor for variable
	 * @return {@code int} value of {@link #headerLineNumber}
	 */
	public    int                getHeaderLineNumber()          { return this.headerLineNumber; }
	
	/**
	 * Method stub for mutator.  Requires subclass implementation.
	 */
	protected void               setHeaderByteOffset()          { l.debug("Nothing to do."); }
	/**
	 * Standard accessor for variable
	 * @return the {@code int} value of the offset, set to {@link #DEFAULT_BYTE_OFFSET} by default
	 */
	public    int                getHeaderByteOffset()          { return DEFAULT_BYTE_OFFSET; }
	
	/**
	 * Standard accessor for variable
	 * @return the {@code java.lang.String} value the delimiter, set to {@link #DEFAULT_DELIMITER} by default
	 */
	public    String             getDelimiter()                 { return DEFAULT_DELIMITER; }
	/**
	 * Standard accessor for variable
	 * @return the {@code java.lang.String} value the separator, set to {@link #DEFAULT_LINE_SEPARATOR} by default
	 */
	public    String             getLineSeparator()             { return DEFAULT_LINE_SEPARATOR; }
	
	/**
	 * Standard mutator for variable
	 * @param fileHeader string containing file header data
	 */
	protected void               setFileHeader(String fileHeader)       { this.fileHeader = fileHeader; }
	/**
	 * Standard accessor for variable
	 * @return the {@code java.lang.String} value the file header
	 */

	public    String             getFileHeader()                { return this.fileHeader; }
	
	/**
	 * Method stub for mutator.  Requires subclass implementation.
	 */
	protected void               setFileHeaderMap()                      { l.debug("Nothing to do."); }
	/**
	 * Standard mutator for variable
	 * @param fileHeaderMap map containing file header key/value pairs
	 */
	protected void               setFileHeaderMap(Map<String,String> fileHeaderMap) { this.fileHeaderMap = fileHeaderMap; }
	/**
	 * Returns the map of name/value pairs from the file header
	 * @return the map of name/value pairs from the file header
	 */
	public    Map<String,String> getFileHeaderMap()                      { return this.fileHeaderMap; }
	
	/**
	 * Standard mutator for variable
	 * @param columnHeader string containing column header line
	 */
	protected void               setColumnHeader(String columnHeader)     { this.columnHeader = columnHeader; }
	/**
	 * Returns the column header row as a {@link String}
	 * @return the column header row as a {@link String}
	 */
	public    String             getColumnHeader()              { return this.columnHeader; }
	
	/**
	 * Method stub for mutator.  Requires subclass implementation.
	 * @throws YADAIOException  when the column header can't be parsed into an array
	 */
	protected void               setColHeaderArray() throws YADAIOException  { l.debug("Nothing to do."); }
	/**
	 * Standard mutator for variable
	 * @param colHeaderArray the array of values constituting column headers
	 */
	protected void               setColHeaderArray(String[] colHeaderArray) { this.colHeaderArray = colHeaderArray; }
	/**
	 * Returns the column header row as an array of {@link String} values.
	 * @return the column header row as an array of {@link String} values
	 */
	public    String[]           getColHeaderArray()    		{ return this.colHeaderArray; }
	
	/**
	 * Standard mutator for variable
	 * @param reader the java.io component for processing the file
	 */
	protected void               setReader(Reader reader)       { this.reader = reader; }
	/**
	 * Returns the {@code java.io} object for processing the file.
	 * @return the {@code java.io} object for processing the file
	 */
	public    Reader             getReader()              		{ return this.reader; }
	
	/**
	 * @throws YADAIOException  when the file headers can't be read successfully
	 */
	protected void				 			 setHeaders() throws YADAIOException { l.debug("Nothing to do."); }
	
}
