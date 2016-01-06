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

import java.io.File;
import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

/**
 * Another {@code yada.io} base class for handling XML files.
 * @author David Varon
 *
 */
public class XMLFileHelper {
	/** 
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(XMLFileHelper.class);
	/**
	 * Constant equal to: {@value}
	 */
	private final static String RX_EQL = "=";
	/**
	 * XML source file
	 */
	private File xmlSrc;
	/**
	 * XSL stylesheet for transforming source
	 */
	private File xslSrc;
	/**
	 * java.io component for handling transformation
	 */
	private StringWriter output = new StringWriter();
	/**
	 * Object containing result to be output
	 */
	private String xslResult;
	/**
	 * Container for arguments to pass to transformer
	 */
	private List<String> args;
	
	/**
	 * Standard mutator for variable
	 * @param xmlSrc source xml file
	 */
	public void setXmlSrc(File xmlSrc) { this.xmlSrc = xmlSrc; }
	/**
	 * Standard accessor for variable
	 * @return the xml source file
	 */
	public File getXmlSrc()            { return this.xmlSrc; } 
	
	/**
	 * Standard mutator for variable
	 * @param xslSrc the xsl source file
	 */
	public void setXslSrc(File xslSrc) { this.xslSrc = xslSrc; }
	/**
	 * Standard accessor for variable
	 * @return the xsl source file
	 */
	public File getXslSrc()            { return this.xslSrc; }
	
	/**
	 * Standard mutator for variable
	 * @param output the internal java.io object to handle output from the transformer
	 */
	public void         setOutput(StringWriter output) { this.output = output; }
	/**
	 * Standard accessor for variable
	 * @return the object into which to write the transformation
	 */
	public StringWriter getOutput()                    { return this.output; }
	
	/**
	 * Standard mutator for variable
	 * @param xslResult the output of the transformation
	 */
	public void   setXslResult(String xslResult) { this.xslResult = xslResult; }
	/**
	 * Standard accessor for variable
	 * @return the transformation result
	 */
	public String getXslResult()                 { return this.xslResult; }
	
	/**
	 * Standard mutator for variable
	 * @param args the args containing xsl params to qualify the transformation
	 */
	public void setArgs(List<String> args) { this.args = args; }
	/**
	 * Standard accessor for variable
	 * @return the arguments, including xsl parameters
	 */
	public List<String> getArgs()          { return this.args; }
	
	/**
	 * Parses xsl parameters out of {@code args} list and sets them on the {@code transformer}
	 * @param args the arguments containing xsl params to qualify the transformation
	 * @param transformer the transformer object to which to apply the args
	 */
	public void setTransformerParameters(List<String> args, Transformer transformer)
	{
		if (args != null && args.size() > 0)
		{
			for(String arg : args)
			{
				if(arg.startsWith("param="))
				{
					String[] temp = arg.split(RX_EQL);
					transformer.setParameter(temp[1], temp[2]);
				}
			}
		}
	}
	
	/**
	 * Performs an xsl transformation on the content at {@link #getXmlSrc()} using the stylesheet at {@link #getXslSrc()}
	 * Stores results via {@link #setXslResult(String)}
	 */
	public void transform()
	{
		TransformerFactory tf = TransformerFactory.newInstance();
		try {
			Transformer trans = tf.newTransformer(new StreamSource(getXslSrc()));
			setTransformerParameters(getArgs(), trans);
			trans.transform(new StreamSource(getXmlSrc()), new StreamResult(getOutput()));
			setXslResult(getOutput().getBuffer().toString());
			l.debug("\n"+getXslResult());
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}	
	}
}
