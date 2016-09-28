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
package com.novartis.opensource.yada.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

/**
 * Facilitates comparison and evaluation of xml sources.
 * @author David Varon
 *
 */
public class XmlUtils {

		/**
		 * Returns {@code true} if {@code xml1} is equal to {@code xml2}
		 * @param xml1 element list to compare
		 * @param xml2 element list to compare
		 * @return {@code true} if {@code xml1} is equal to {@code xml2}
		 */
		public boolean compareElementLists(String xml1, String xml2)
		{
			String xmlStr1 = getNormalElementList(xml1);
			String xmlStr2 = getNormalElementList(xml2);
			return xmlStr1.equals(xmlStr2);
		}
		
		/**
		 * Transforms {@code xml} into a simple list of elements, using {@code yada.util/elementlist.xsl}
		 * @param xml xml string from which to extract element names
		 * @return a list of elements
		 */
		public String getNormalElementList(String xml)
		{
			String result = "";
			StringWriter output = new StringWriter();
			// 1. Instantiate a TransformerFactory.
			TransformerFactory tFactory =  TransformerFactory.newInstance();

			// 2. Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
			Transformer transformer;
			try {
				transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(System.getenv("yada.util")+"/elementlist.xsl"));
				// 3. Use the Transformer to transform an XML Source and send the output to a Result object.
				transformer.transform
			    	(new StreamSource(new StringReader(xml)), 
			    	 new StreamResult(output));
				result = output.getBuffer().toString();
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			}
			return result;
		}
		
		/**
		 * Returns the text content of {@code elementName} from {@code xml}
		 * @param xml xml string containing content
		 * @param elementName the element from which to get the content
		 * @return text content
		 */
		public String getContentByElementName(String xml, String elementName)
		{
			String result = "";
			String xpath = "//"+elementName;
			try
			{
				XPath xpathObj = XPathFactory.newInstance().newXPath();
				result = xpathObj.evaluate(xpath,new InputSource(new StringReader(xml)));
			}
			catch (XPathExpressionException e)
			{
				e.printStackTrace();
			}
			return result;
		}
}
