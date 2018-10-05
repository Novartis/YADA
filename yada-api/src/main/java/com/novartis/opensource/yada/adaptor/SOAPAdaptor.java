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
package com.novartis.opensource.yada.adaptor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.novartis.opensource.yada.ConnectionFactory;
import com.novartis.opensource.yada.Finder;
import com.novartis.opensource.yada.YADAConnectionException;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADAResourceException;

/**
 * For execution of SOAP requests
 * @author David Varon
 *
 */
public class SOAPAdaptor extends Adaptor {

	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(SOAPAdaptor.class);
	/**
	 * Constant equal to {@value}
	 */
	private static final String AUTH_BASIC          = "Basic";
	/**
	 * Constant equal to {@value}
	 */
	private static final String AUTH_NTLM           = "ntlm";
	/**
	 * Constant equal to: {@value}
	 */
	private static final String SOAP_USER_KEY 		  = "SOAPUser";
	/**
	 * Constant equal to: {@value}
	 */
	private static final String SOAP_PASSWORD_KEY 	= "SOAPPassword";
	/**
	 * Constant equal to: {@value}
	 */
	private static final String SOAP_AUTH_KEY 		  = "SOAPAuth";
	/**
	 * Constant equal to: {@value}
	 */
	private static final String SOAP_ACTION_KEY 	  = "SOAPAction";
	/**
	 * Constant equal to: {@value}
	 */
	private static final String SOAP_DATA_KEY 		  = "SOAPData";
	/**
	 * Constant equal to: {@value}
	 */
	private static final String SOAP_DOMAIN_KEY   	= "SOAPDomain";
	/**
	 * Constant equal to: {@value}
	 */
	private static final String SOAP_PATH_KEY		    = "SOAPPath";
	/**
	 * Constant equal to: {@value}
	 */
	private static final String YADA_BIN            = "yada.bin";
	/**
	 * Constant equal to: {@value}
	 */
	public  static final String PROTOCOL_SOAP       = "soap";
	/**
	 * Constant equal to: {@value}
	 */
	public  static final String PROTOCOL_HTTP       = "http";
	/**
	 * Constant equal to: {@value}
	 */
	private static final String CURL_EXEC           = "/curlerWS.sh";
	//private static final String NTLM_ADAPTOR		= Finder.getEnv("yada.bin")+"curlerWS.sh";
	/**
	 * Soap endpoint username
	 */
	private String soapUser     = "";
	/**
	 * Soap endpoint password
	 */
	private String soapPass     = "";
	/**
	 * Soap endpoint domain
	 */
	private String soapDomain   = "NANET";
	/**
	 * Soap endpoint authorization type, defaults to {@link #AUTH_BASIC}
	 */
	private String soapAuth     = AUTH_BASIC;
	/**
	 * Instance variable for soap action, derived from query spec
	 */
	private String soapAction   = "";
	/**
	 * Instance variable for soap data, derived from query spec
	 */
	private String soapData     = "";
	/**
	 * Instance variable for soap path, derived from query spec
	 */
	private String soapPath		= "";
	/**
	 * Instance variable for soap endpoint, derived from query spec
	 */
	private URL    endpoint;
	/**
	 * Instance variable for soap source, derived from query spec
	 */
	private String soapSource;
	/**
	 * Instance variable for soap querystring, derived from query spec
	 */
	private String queryString;
	
	/**
	 * Inner class for facilation of SOAP authentication 
	 * @author David Varon
	 * @see java.net.Authenticator
	 */
	class YadaSoapAuthenticator extends Authenticator {
		/**
		 * Instance variable for username
		 */
		String user;
		/**
		 * Instance variable for password
		 */
		String pass;
		/**
		 * Instance variable for domain
		 */
		String domain = null;
		
		/**
		 * Constructor with domain
		 * @param user username to authenticate
		 * @param pass password to use for authentication
		 * @param domain NTML domain for user
		 */
		public YadaSoapAuthenticator(String user, String pass, String domain)
		{
			this.user = user;
			this.pass = pass;
			this.domain = domain;
		}
		
		/**
		 * Constructor without domain
		 * @param user username to authenticate
		 * @param pass password to use for authentication
		 */
		public YadaSoapAuthenticator(String user, String pass)
		{
			
			this.user = user;
			this.pass = pass;
		}
		
		@Override
		public PasswordAuthentication getPasswordAuthentication() {
        // I haven't checked getRequestingScheme() here, since for NTLM
        // and Negotiate, the usrname and password are all the same.
        System.err.println("Feeding username and password for " + getRequestingScheme());
        if (null == this.domain)
        {
        	return (new PasswordAuthentication(this.user, this.pass.toCharArray()));
        }
       	return (new PasswordAuthentication(this.domain+"\\"+this.user, this.pass.toCharArray()));
    }
	}
	
	/**
	 * Default constructor
	 */
	public SOAPAdaptor() {
		super();
		l.debug("Initializing");
	}
	
	/**
	 * Preferred "YADARequest" constructor
	 * @param yadaReq YADA request configuration
	 */
	public SOAPAdaptor(YADARequest yadaReq) {
		super(yadaReq);
	}
	
	/**
	 * Sets soap request authentication, action, connection, and request metadata.
	 * @param yq the {@link YADAQuery} containing the source code and metadata required to construct an executable query
	 * @return SOAP request source and path
	 * @throws YADAAdaptorException when the stored SOAP query specs cannot be parsed into a SOAP message
	 */
	@Override
	public String build(YADAQuery yq) throws YADAAdaptorException {
	  String conf     = ConnectionFactory.getConnectionFactory().getWsSourceMap().get(yq.getApp());
		String queryStr = yq.getYADACode();
		try {
			JSONObject querySpec = new JSONObject(queryStr);
			this.soapUser     = querySpec.getString(SOAP_USER_KEY);
			this.soapPass     = querySpec.getString(SOAP_PASSWORD_KEY);
			this.soapAction   = querySpec.getString(SOAP_ACTION_KEY);
			this.soapData     = querySpec.getString(SOAP_DATA_KEY);
			this.soapSource   = conf;
			
			if (querySpec.has(SOAP_DOMAIN_KEY))
			{
				this.soapDomain = querySpec.getString(SOAP_DOMAIN_KEY);
			}
			if (querySpec.has(SOAP_AUTH_KEY))
			{
				this.soapAuth   = querySpec.getString(SOAP_AUTH_KEY);
			}
			if (querySpec.has(SOAP_PATH_KEY))
			{
				this.soapPath   = querySpec.getString(SOAP_PATH_KEY);
			}
		} 
		catch (JSONException e) 
		{
			String msg = "Unable to process stored JSON soap specification.";
			throw new YADAAdaptorException(msg,e);
		}
		return this.soapSource+this.soapPath;
	}
	
	/**
	 * Constructs and executes a SOAP message.  For {@code basic} authentication, YADA uses the 
	 * java soap api, and the {@link SOAPConnection} object stored in the query object.  For 
	 * NTLM, which was never successful using the java api, YADA calls out to {@link #CURL_EXEC}
	 * in {@link #YADA_BIN}. 
	 * @see com.novartis.opensource.yada.adaptor.Adaptor#execute(com.novartis.opensource.yada.YADAQuery)
	 */
	@Override
	public void execute(YADAQuery yq) throws YADAAdaptorExecutionException
	{
		String result = "";
		resetCountParameter(yq);
		SOAPConnection connection = (SOAPConnection)yq.getConnection();
		for(int row=0;row<yq.getData().size();row++)
		{
			yq.setResult();
			YADAQueryResult yqr      = yq.getResult();
			yqr.setApp(yq.getApp());
			String          soapUrl  = yq.getSoap(row);
			
			try
			{
				this.endpoint = new URL(soapUrl);
				MessageFactory  factory  = MessageFactory.newInstance();
				SOAPMessage     message  = factory.createMessage(); 
				
				byte[] authenticationToken = Base64.encodeBase64(
				  (this.soapUser + ":" + this.soapPass).getBytes());
				 
				// Assume a SOAP message was built previously
				MimeHeaders mimeHeaders = message.getMimeHeaders();
				if ("basic".equals(this.soapAuth.toLowerCase()))
				{
					mimeHeaders.addHeader("Authorization", this.soapAuth+" " + new String(authenticationToken));
					mimeHeaders.addHeader("SOAPAction", this.soapAction);
					mimeHeaders.addHeader("Content-Type", "text/xml");
					SOAPHeader     header = message.getSOAPHeader();
					SOAPBody       body = message.getSOAPBody(); 
					header.detachNode(); 
					
					l.debug("query:\n"+this.queryString);
					
					try
					{
						Document xml = DocumentBuilderFactory
					    .newInstance()
					    .newDocumentBuilder()
					    .parse(new ByteArrayInputStream(this.soapData.getBytes()));
						
						//SOAPBodyElement docElement = 
						body.addDocument(xml);
			
						Authenticator.setDefault(new YadaSoapAuthenticator(this.soapUser, this.soapPass));
						
						SOAPMessage response = connection.call(message, this.endpoint); 
						try(ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream())
						{
  						response.writeTo(responseOutputStream);
  						result = responseOutputStream.toString();
						}
					}
					catch(IOException e)
					{
						String msg = "Unable to process input or output stream for SOAP message with Basic Authentication. This is an I/O problem, not an authentication issue.";
						throw new YADAAdaptorExecutionException(msg,e);
					}
					l.debug("SOAP Body:\n"+result);
				}
				else if (AUTH_NTLM.equals(this.soapAuth.toLowerCase()) 
						    || "negotiate".equals(this.soapAuth.toLowerCase()))
				{
					ArrayList<String> args = new ArrayList<>();
					args.add(Finder.getEnv(YADA_BIN)+CURL_EXEC);
					args.add("-X");	
					args.add("-s");
					args.add(this.soapSource+this.soapPath);
					args.add("-u");
					args.add(this.soapDomain+"\\"+this.soapUser);
					args.add("-p");
					args.add(this.soapPass);
					args.add("-a");
					args.add(this.soapAuth);
					args.add("-q");
					args.add(this.soapData);
					args.add("-t");
					args.add(this.soapAction);
					String[] cmds = args.toArray(new String[0]);
					l.debug("Executing soap request via script: "+Arrays.toString(cmds));
					String s = null;
					try
					{
						ProcessBuilder pb = new ProcessBuilder(args);
						l.debug(pb.environment().toString());
						pb.redirectErrorStream(true);
						Process p = pb.start();
						try(BufferedReader si = new BufferedReader(new InputStreamReader(p.getInputStream())))
						{
  						while ((s = si.readLine()) != null)
  						{
  							l.debug(s);
  							if(null == result)
  							{
  								result = "";
  							}
  							result += s;
  						}
						}
					}
					catch(IOException e)
					{
						String msg = "Unable to execute NTLM-authenticated SOAP call using system call to 'curl'.  Make sure the curl executable is still accessible.";
						throw new YADAAdaptorExecutionException(msg,e);
					}
				}
			}
			catch (SOAPException e) 
			{
				String msg = "There was a problem creating or executing the SOAP message, or receiving the response.";
				throw new YADAAdaptorExecutionException(msg,e);
			} 
			catch (SAXException e) 
			{
				String msg = "Unable to parse SOAP message body.";
				throw new YADAAdaptorExecutionException(msg,e);
			} 
			catch (ParserConfigurationException e) 
			{
				String msg = "There was a problem creating the xml document for the SOAP message body.";
				throw new YADAAdaptorExecutionException(msg,e);
			} 
			catch (YADAResourceException e)
			{
				String msg = "Cannot find 'curl' executable at specified JNDI path "+YADA_BIN+CURL_EXEC;
				throw new YADAAdaptorExecutionException(msg,e);
			} 
			catch (MalformedURLException e)
			{
				String msg = "Can't create URL from provided source and path.";
				throw new YADAAdaptorExecutionException(msg,e);
			}
			finally
			{
				try
				{
					ConnectionFactory.releaseResources(connection,yq.getSoap().get(0));
				} 
				catch (YADAConnectionException e)
				{
					l.error(e.getMessage());
				}
			}
			
			yqr.addResult(row, result);
			
		}
		
	}
	
	
	/*
	public String getJSON() throws YADAAdaptorException, SQLException
	{
		String json = "";
		try
		{
			String soapResponse = getSOAPResponse();
			l.debug("Fixing Micro$oft's W3C spec non-compliance issue");
			soapResponse = soapResponse.replace("#RowsetSchema", "http://www.microsoft.com/RowsetSchema");
		
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put("SOAPResponse", new JSONObject());
			JSONObject jsonRs = jsonResponse.getJSONObject("SOAPResponse");
			jsonRs.put("DATA",soapResponse);
			json = jsonResponse.toString();
		}
		catch (JSONException e)
		{
			l.error(e.getMessage());
			e.printStackTrace();
			throw new YADAAdaptorException();
		} 
		catch (YADAFinderException e) 
		{
			l.error(e.getMessage());
			e.printStackTrace();
			throw new YADAAdaptorException();
		}
		return json;
	}
	
	public String getXML() throws YADAAdaptorException, SQLException 
	{
		l.debug("Getting XML in SOAPAdaptor");
		String xml = null;
		try 
		{
			xml = getSOAPResponse();
			l.debug("Fixing Micro$oft's W3C spec non-compliance issue");
			xml = xml.replace("#RowsetSchema", "http://www.microsoft.com/RowsetSchema");
		} 
		catch (YADAFinderException e) 
		{
			l.error(e.getMessage());
			e.printStackTrace();
			throw new YADAAdaptorException();
		}
		
		return xml;
	}*/
}
