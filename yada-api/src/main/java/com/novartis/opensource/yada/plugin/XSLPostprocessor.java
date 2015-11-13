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
package com.novartis.opensource.yada.plugin;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.Finder;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADAResourceException;

/**
 * A postprocess plugin for performing XSL transformations on XML results.
 * @author David Varon
 *
 */
public class XSLPostprocessor extends AbstractPostprocessor {

	/**
   * Local logger handle
   */
	private static Logger l = Logger.getLogger(XSLPostprocessor.class);
	/**
	 * Constant equal to: {@value}
	 */
	private final static String RX_EQL = "=";
	
	/**
	 * Instantiates a {@link TransformerFactory}, gets a new {@link Transformer} using the stylesheet
	 * passed in the request's {@link YADARequest#postArgs}, transforms {@code result} and returns the output.
	 * @see com.novartis.opensource.yada.plugin.Postprocess#engage(com.novartis.opensource.yada.YADARequest, java.lang.String)
	 */
	@SuppressWarnings("javadoc")
  @Override
	public String engage(YADARequest yadaReq, String result) throws YADAPluginException {
		String xmlInput  = result;
		String xslResult = "";
		String xsl       = "";
		StringWriter output = new StringWriter();
		// 1. Instantiate a TransformerFactory.
		TransformerFactory tFactory =  TransformerFactory.newInstance();

		// 2. Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
		Transformer transformer;
		try 
		{	
			List<String> args   = yadaReq.getPostArgs();
			for(String arg:args)
			{
				if (arg.endsWith(".xsl"))
					xsl = arg;
			}
			transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(Finder.getEnv("yada_util")+xsl));
			// 3. Use the Transformer to transform an XML Source and send the output to a Result object.
			setTransformerParameters(args, transformer);
			transformer.transform
		    	(new StreamSource(new StringReader(xmlInput)), 
		    	 new StreamResult(output));
			xslResult = output.getBuffer().toString();
			changeResultFormat(args, yadaReq);
		} 
		catch (TransformerConfigurationException e) 
		{
			e.printStackTrace();
			throw new YADAPluginException(e.getMessage());
		} 
		catch (TransformerException e) 
		{
			e.printStackTrace();
			throw new YADAPluginException(e.getMessage());
		} 
		catch (YADAResourceException e)
		{
			e.printStackTrace();
			throw new YADAPluginException(e.getMessage());
		} 
		
		return xslResult;
	}
	
	/**
	 * Parses XSL params out of the {@code args} and passes them to the {@code transformer}
	 * @param args the list of arguments passed in the request
	 * @param transformer the object on which to set the parameters in {@code args}
	 */
	public void setTransformerParameters(List<String> args, Transformer transformer)
	{
		for(String arg:args)
		{
			if(arg.startsWith("param="))
			{
				l.debug("arg is ["+arg+"]");
				String[] temp = arg.split(RX_EQL);
				transformer.setParameter(temp[1], temp[2]);
				l.debug("param ["+temp[1]+"] is ["+transformer.getParameter(temp[1])+"]");
			}
		}
		
	}
	
	/**
	 * Enables modification of return format in the event the XSL transformation returns a format other than XML.
	 * @param args the list of arguments passed in the request
	 * @param yadaReq YADA request configuration
	 */
	public void changeResultFormat(List<String> args, YADARequest yadaReq)
	{
		for(String arg:args)
		{
			if (arg.startsWith("format="))
			{
				l.debug("arg is ["+arg+"]");
				yadaReq.setFormat(new String[] {arg.substring(arg.indexOf("=")+1)});
			}
		}
	}
}
