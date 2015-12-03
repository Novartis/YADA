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

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.novartis.opensource.yada.YADAQueryResult;

/**
 * A converter subclass for traversal and appending of SOAP results to the calling Response object.
 * @author David Varon
 *
 */
public class SOAPResultXMLConverter extends AbstractConverter
{

	/**
	 * Local logger handle
	 */
	@SuppressWarnings("unused")
	private static Logger l = Logger.getLogger(RESTResultJSONConverter.class);

  /**
   * Java XML API component
   */
	private DocumentBuilderFactory docFactory;
  /**
   * Java XML API component
   */
	private DocumentBuilder        docBuilder;
  /**
   * Java XML API component
   */
	private Document               doc;
  /**
   * Java XML API component
   */
	private DocumentFragment       frag;
	
	/**
   * Default constructor
   */
  public SOAPResultXMLConverter() {
    // default constructor
  }
  
  /**
   * Constructor with {@link YADAQueryResult}
   * @param yqr the container for result processing artifacts
   */
  public SOAPResultXMLConverter(YADAQueryResult yqr) {
    this.setYADAQueryResult(yqr);
  }

	/**
	 * Returns {@code true} if the {@code node} is a {@link org.w3c.dom.Node#TEXT_NODE} and contains only whitespace
	 * @param node the node to check for whitespace only
	 * @return {@code true} if the {@code node} is a {@link org.w3c.dom.Node#TEXT_NODE} and contains only whitespace
	 */
	public boolean isWhitespace(Node node)
	{
		if(node.getNodeType() == Node.TEXT_NODE)
		{
			Pattern p = Pattern.compile("^[\\s]+$");
			Matcher m = p.matcher(node.getTextContent());
			if (m.matches())
				return true;
		}
		return false;	
	}
	
	/**
	 * Recurses through document, starting with {@code node}, incrementally appending nodes to the 
	 * {@link org.w3c.dom.DocumentFragment} which is returned by {@link #convert(Object)}
	 * @param node the node into which to drill down
	 */
	public void visitNodes(Node node) //soap:Body, frag
	{
		NodeList list = node.getChildNodes();
		if(list != null)
		{
			for (int i=0;i<list.getLength();i++)
			{
				Node child = list.item(i); //GetListResponse
				if(!isWhitespace(child))
				{
					if(node.getNodeName().equals("soap:Body"))
						this.frag.appendChild(child);
					else
						node.appendChild(child); //frag.append(GetListResponse)
					visitNodes(child);
				}
			}
		}
	}
	
	/**
	 * Gets {@code soap:Body} element as a {@link Node} from {@code result} after converting it to 
	 * an xml {@link Document}, then drills into it, via {@link #visitNodes(Node)}
	 * @see com.novartis.opensource.yada.format.AbstractConverter#convert(java.lang.Object)
	 */
	@Override
	public Object convert(Object result) throws YADAConverterException
	{
		Object res = ((String)result).replace("#RowsetSchema", "http://www.microsoft.com/RowsetSchema");
		try
		{
			this.docFactory = DocumentBuilderFactory.newInstance();  
			this.docFactory.setIgnoringElementContentWhitespace(true);
			this.docBuilder = this.docFactory.newDocumentBuilder();
    	this.doc = this.docBuilder.parse(new InputSource(new StringReader((String)res)));
    	this.frag = this.doc.createDocumentFragment();
    	Node node = this.doc.getElementsByTagName("soap:Body").item(0);
    	visitNodes(node);
    } 
    catch (ParserConfigurationException e)
		{
			String msg = "Error while creating xml document.";
			throw new YADAConverterException(msg,e);
		}
    catch (SAXException e)
		{
			String msg = "Could not create xml document from SOAP result";
			throw new YADAConverterException(msg,e);
		} 
    catch (IOException e)
		{
    	String msg = "Error reading input string from SOAP result.";
			throw new YADAConverterException(msg,e);
		} 
		return this.frag;
	}
}
