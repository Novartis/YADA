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
package com.novartis.opensource.yada.format;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.YADAQueryResult;

/**
 * Primarily a tool to assist with query authoring, this response returns query results wrapped in a simple HTML table.
 * @author David Varon
 *
 */
public class HTMLResponse extends AbstractResponse {
	/**
	 * Local logger handle
	 */
	@SuppressWarnings("unused")
	private static Logger l = Logger.getLogger(HTMLResponse.class);
	/**
   * Ivar containing the result to be returned by this class's {@link #toString()} method
   */
  private StringBuffer buffer = new StringBuffer();
	
	
	@Override
  public Response compose(YADAQueryResult[] yqrs) throws YADAConverterException, YADAResponseException 
	{
	  Response delimitedResponse = new DelimitedResponse();
	  String csv = delimitedResponse.compose(yqrs).toString();

	  try(BufferedReader br = new BufferedReader(new StringReader(csv)))
	  {
	    String line = "";
	    int lineNum = 0;
	    int fields  = 0;
	    int hdrFields = 0;
	    String oTag = "<th>";
	    String cTag = "</th>";
	    String oRow = "<tr>";
	    String cRow = "</tr>\n";
	    this.buffer.append("<html>\n<head>\n\n</head>\n<body>\n");
	    this.buffer.append("<table border=\"1\" style=\"border-collapse:collapse;\">\n");
	    while((line = br.readLine()) != null)
	    {
	      fields = 0;
	      if(lineNum == 0)
	      {
	        this.buffer.append("<thead>\n");
	      }
	      else
	      {
	        oTag = "<td>"; 
	        cTag = "</td>";
	      }
  	    this.buffer.append(oRow);
	      char[] p = line.toCharArray(); // the values passed in
	      boolean escape = false, delim = false, inField = false;
	      // "Z,Z",,,"2013-04-03","2013-04-04 20:12:10" 
	      for(int i=0;i<p.length;i++)
	      {
	        char c = p[i];
	        // slash is probably escape char
	        if(c == '/')
	        {
	          this.buffer.append(c); // add it
	          escape = true;  // set state
	        }
	        // quote could be opener, closer, or escaped
	        else if(c == '"')  
	        {
	          if(inField) // already in a field
	          {
	            if(escape) // an escaped quote
	            {
	              this.buffer.append(c); // add it
	              escape = false; // unset state
	            }
	            else
	            {
	              this.buffer.append(cTag); // in a field and not escaped so close it
	              fields++;          // increment fields counter;
	              inField = false;   // unset state
	              delim = true;      // pending delimiter set state
	            }
	          }
	          else
	          {
	            this.buffer.append(oTag); // not in a field, so open it
	            inField = true;    // set state
	          }
	        }
	        else if(c == ',')
	        {
	          if(inField)
	          {
	            this.buffer.append(c); // in a field, so add it
	          }
	          else
	          {
	            if(delim)
	            {
	              delim = false; // not in field, and field just closed
	            }
	            else
	            {
	              this.buffer.append(oTag+cTag); // not in a field, no pending delim, so this is empty field
	              fields++;
	            }
	          } 
	        }
	        else
	        {
	          this.buffer.append(c); // any other char
	          escape = false;
	        }
	      }
	      if(fields < hdrFields)
	      {
	        for(int i=0;i<hdrFields-fields;i++)
	        {
	          this.buffer.append(oTag+cTag);
	        }
	      }
	      this.buffer.append(cRow);
        if(lineNum == 0)
        {
          this.buffer.append("</thead>\n<tbody>\n");
          hdrFields = fields;
          oTag = "<td>"; 
          cTag = "</td>";
        }
        lineNum++;
	    }
	    this.buffer.append("</tbody>\n</table>\n</body>\n</html>");
	  }
	  catch(IOException e)
	  {
	    String msg = "There was a problem parsing the result.";
	    throw new YADAResponseException(msg, e);
	  }
	  return this;
	}
	
	/**
	 * Returns the internal string
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.buffer.toString();
	}

	/**
	 * Returns the internal string
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(boolean prettyPrint) throws YADAResponseException {
		return this.toString();
	}


}
