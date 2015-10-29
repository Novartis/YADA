package com.novartis.opensource.yada.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * ADFHelper is for files in Adnan Derti format.  These are tab-delimited files with the following fileheader:
 * 
<pre>
__sample name: 22RV1
__species: human
__sample type: cell.line
__alignment program: TopHat v1.3.KornMod
__expression program: Cufflinks v2.0
__contents: FPKM
__no transcript: ---
__BEGINDATA
</pre>
 * 
 * The column header is on the next line, line 9.
 * 
 * @author David Varon
 *
 */
public class ADFHelper extends TabHelper {
	
	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(ADFHelper.class);
	
	//  header regex basic matches this:  __(header): (value)
	/**
	 * Constant equal to: {@code __(.+):\\s*(.+)}
	 */
	private final static Pattern HEADER_RX   = Pattern.compile("__(.+):\\s*(.+)");
	/**
	 * Constant equal to: {@value}
	 */
	private final static String  H_BEGINDATA = "__BEGINDATA";
	
	/**
	 * Sets file header values until encountering {@link #H_BEGINDATA}, then sets the column header line. 
	 * @see com.novartis.opensource.yada.io.TabHelper#setHeaders()
	 */
	@Override
	protected void setHeaders() throws YADAIOException 
	{
		l.debug("Setting headers dynamically...");
		String line       = "";
		StringBuffer fh   = new StringBuffer();
		
		boolean beginData     = false;
		boolean areHeadersSet = false;
		
		try
		{
			while(!areHeadersSet && (line = ((BufferedReader)this.reader).readLine()) != null)
			{
				if (!beginData)
				{
					if(!H_BEGINDATA.equals(line))
					{
						fh.append(line);
						fh.append(NEWLINE);
					}
					else
					{
						beginData = true;
					}
				}
				else
				{
					setColumnHeader(line);
					setFileHeader(fh.toString());
					areHeadersSet = true;
				}
			}
			setColHeaderArray();
			setFileHeaderMap();
		}
		catch (IOException e)
		{
			throw new YADAIOException(e.getMessage(),e);
		}
	}
	
	/**
	 * Builds a map from the values in the file header buffer.
	 * @see com.novartis.opensource.yada.io.FileHelper#setFileHeaderMap()
	 */
	@Override
	protected void setFileHeaderMap() 
	{
		l.debug("Setting ADF file header...");
		try(Scanner s = new Scanner(getFileHeader()))
		{
  		String  h = "";
  		if (null == this.fileHeaderMap)
  		{
  			this.fileHeaderMap = new HashMap<>();
  		}
  		try 
  		{
  			while(s.hasNextLine())
  			{
  				h = s.nextLine();
  				Matcher m = HEADER_RX.matcher(h);
  				if (m.matches())
  				{
  					this.fileHeaderMap.put(m.group(1),m.group(2));
  				}
  			}
  		} 
  		catch (NoSuchElementException e) 
  		{
  			e.printStackTrace();
  		}
		}
	}
}
