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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.io.XMLFileHelper;

/**
 * Primarily a tool to assist with query authoring, this reponse returns query results wrapped in a simple HTML table.
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
	private String response;
	
	
	/**
	 * Obtains xml results and transforms them using {@code /utils/HTMLConverter.xsl}
	 * @see com.novartis.opensource.yada.format.AbstractResponse#compose(com.novartis.opensource.yada.YADAQueryResult[])
	 */
	@Override
	public Response compose(YADAQueryResult[] yqrs) throws YADAConverterException, YADAResponseException {
		
		Response xmlResponse = new XMLResponse();
		String xml = xmlResponse.compose(yqrs).toString();
		XMLFileHelper helper = new XMLFileHelper();
		try
		{
		  File xslSrc = File.createTempFile("xsl","");
			// XSL
			try(InputStream is = getClass().getResourceAsStream("/utils/HTMLConverter.xsl"))
			{
  			
  			try(FileWriter xslOut = new FileWriter(xslSrc))
  			{
    			int b = 0;
    			while((b = is.read()) != -1)
    			{
    				xslOut.write(b);
    			}
  			}
  	    helper.setXslSrc(xslSrc);
			}
			
			// XML
			File xmlSrc = File.createTempFile("xml","");
			try(FileWriter out = new FileWriter(xmlSrc))
			{
  			out.write(xml);
  			helper.setXmlSrc(xmlSrc);
			}
			// transform
			helper.transform();
			this.response = helper.getXslResult();
			
			xslSrc.delete();
			xmlSrc.delete();
		}  
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Returns the internal string
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.response;
	}

	/**
	 * Returns the internal string
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(boolean prettyPrint) throws YADAResponseException {
		return this.response;
	}


}
