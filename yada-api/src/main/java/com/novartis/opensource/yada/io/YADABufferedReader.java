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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A subclass of {@link java.io.BufferedReader} which contains methods for returning a line of input 
 * as a {@link JSONObject}
 * @author David Varon
 *
 */
public class YADABufferedReader extends BufferedReader {
	
	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(YADABufferedReader.class);
	
	/**
	 * Instance variable.  The {@link FileHelper} enables functionality essential to {@link #readLine2JSON()} 
	 * such as access to the index of column headers which are required to be used as json keys.
	 */
	private FileHelper helper;
	
	/**
	 * Override of super-constructor with additional {@code helper} argument.
	 * @param reader a java i/o object
	 * @param helper a YADA FileHelper to handle headers
	 * @throws YADAIOException when the {@code helper} can't be set
	 */
	public YADABufferedReader(Reader reader, FileHelper helper) throws YADAIOException 
	{
		super(reader);
		setFileHelper(helper);
	}
	
	/**
	 * Override of super-constructor with additional {@code helper} argument.
	 * @param reader java i/o component
	 * @param bufferSize the size of the internal buffer to store processed content
	 * @param helper the YADA FileHelper for handling headers
	 * @throws YADAIOException when the {@code helper} can't be set
	 */
	public YADABufferedReader(Reader reader, int bufferSize, FileHelper helper) throws YADAIOException 
	{
		super(reader, bufferSize);
		setFileHelper(helper);
	}
	
	/**
	 * Called by the constructor to set the instance variable, this method also, in turn sets the 
	 * the current object (a subclass of {@link java.io.Reader}) as an instance variable in the {@code helper}
	 * (via {@link FileHelper#setReader(Reader)}). It also calls {@link FileHelper#setHeaders()} 
	 * @param helper the FileHelper subclass to handle headers
	 * @throws YADAIOException when helper can't set the headers
	 * @see FileHelper
	 */
	private void setFileHelper(FileHelper helper) throws YADAIOException {
		this.helper = helper;
		helper.setReader(this);
		helper.setHeaders();
	}
		
	/**
	 * Alternative to {@link java.io.BufferedReader#readLine()} which will wrap content of a line in JSON using corresponding 
	 * indexed column headers as keys.
	 * @return a json object containing a line of content with corresponding column headers as keys and column values as values.
	 * @throws YADAIOException when the content can't be read, or wrapped in JSON successfully
	 */
	public JSONObject readLine2JSON() throws YADAIOException 
	{
		JSONObject result = null;
		try 
		{
			String   orig = this.readLine();
			l.debug(orig);
			if (null != orig)
			{
				String[] line = orig.split(this.helper.getDelimiter());
				String[] head = this.helper.getColHeaderArray();
				if (line.length == head.length)
				{
					result = new JSONObject();
					for (int i=0;i<line.length;i++)
					{
						result.put(head[i],line[i]);
					}
					l.debug(result);
				}
				else
				{
					String msg = "Header/Line of different lengths: "+head.length+"/"+line.length;
					throw new YADAIOException(msg);
				}
			}
		} 
		catch (IOException e) 
		{
			l.error(e.getMessage());
			e.printStackTrace();
			throw new YADAIOException(e.getMessage(),e);
		} 
		catch (JSONException e) 
		{
			l.error(e.getMessage());
			e.printStackTrace();
			throw new YADAIOException(e.getMessage(),e);
		}
		return result;
	}
	
}
