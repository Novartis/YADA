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
package com.novartis.opensource.yada.io;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * An extension of {@link FileHelper} specifically for parsing tab-delimited source files.
 * @author David Varon
 *
 */
public class TabHelper extends FileHelper {

	/**
   * Local logger handle
   */
	private static Logger l = Logger.getLogger(TabHelper.class);
	
	/**
	 * Splits the column header into an array on {@link FileHelper#TAB}
	 * @see com.novartis.opensource.yada.io.FileHelper#setColHeaderArray()
	 */
	@Override
	protected void setColHeaderArray() throws YADAIOException
	{
		l.debug("Setting tab column headers...");
		String[] ch = getColumnHeader().split(TAB);
		super.setColHeaderArray(ch);
	}
		
	/**
	 * Processes file headers and column headers in tabular files
	 * @see com.novartis.opensource.yada.io.FileHelper#setHeaders()
	 */
	@Override
	protected void setHeaders() throws YADAIOException
	{ 
		l.debug("Setting headers...");
		try 
		{
			setHeaderLineNumber(); // this does nothing here but could be overridden in a subclass
			setHeaderByteOffset(); // this does nothing here but could be overridden in a subclass
			int chr               = -1;
			StringBuffer head     = new StringBuffer(); 
			if (getHeaderLineNumber() >= 0 && getHeaderByteOffset() == 0)  // line num and not bytes
			{
				int    lineNum = 0;
				String line    = "";
				l.debug("Header line number is ["+getHeaderLineNumber()+"]");  // defaults to 0
				// build fileheader, line by line
				while(lineNum <= getHeaderLineNumber() && (line = ((BufferedReader)this.reader).readLine()) != null)
				{
					l.debug("Header line ["+line+"] @ lineNum ["+lineNum+"]");
					// fileheader
					if (getHeaderLineNumber() > 0)
					{
						head.append(line);
					}
					lineNum++;
					l.debug("Line num ["+lineNum+"]");
				}
				// fileheader
				if (getHeaderLineNumber() > 0)
				{
					setFileHeader(head.toString());
				}
				l.debug(line);
				setColumnHeader(line);
			}
			else if(getHeaderLineNumber() == 0 && getHeaderByteOffset() > 0) // bytes and not line num
			{
				char[] chars = null;
				int numChars = this.reader.read(chars, 0, getHeaderByteOffset());
				if (numChars > -1)
				{
					setFileHeader(String.valueOf(chars));
				}
				while((chr = this.reader.read()) > -1 && chr != Integer.parseInt(getLineSeparator()))
				{
					head.append(String.valueOf((char)chr));
				}
				setColumnHeader(head.toString());
			}
			else if (getHeaderLineNumber() > 0 && getHeaderByteOffset() > 0)
			{
				// throw an error here.
			}
			else // defaults
			{
				// handled by 1st condition
			}
			setFileHeaderMap();  // this does nothing here but could be overridden in a subclass
			setColHeaderArray(); // this does nothing here but could be overridden in a subclass
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
