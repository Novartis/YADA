package com.novartis.opensource.yada.plugin;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.QueryManager;
import com.novartis.opensource.yada.Service;
import com.novartis.opensource.yada.YADAConnectionException;
import com.novartis.opensource.yada.YADAFinderException;
import com.novartis.opensource.yada.YADAParserException;
import com.novartis.opensource.yada.YADAProperty;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADAQueryConfigurationException;
import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADARequestException;
import com.novartis.opensource.yada.YADAResourceException;
import com.novartis.opensource.yada.YADASecurityException;
import com.novartis.opensource.yada.YADAUnsupportedAdaptorException;
import com.novartis.opensource.yada.adaptor.YADAAdaptorException;
import com.novartis.opensource.yada.adaptor.YADAAdaptorExecutionException;

/**
 * @since 9.3.6
 */
public class ScinamicPlugin implements Preprocess, Postprocess {
  /**
   * Local logger handle
   */
  private static final Logger LOG = Logger.getLogger(ScinamicPlugin.class);
	
	/**
	 * {@link QueryManager} object to enable inline API calls
	 */
	private QueryManager qmgr = null;
	
	/**
	 * {@link YADARequest} object to facilitate inline API calls
	 */
	private YADARequest yr = new YADARequest();

	/**
	 * constant equal to {@value}
	 */
	private static final String SCINAMIC_TOKEN = "scinamic.token";
	
	/**
   * constant equal to {@value}
   */
  private static final String TOKEN_PLACEHOLDER = "tokenval";
  
  /**
   * constant equal to {@value}
   */
  private static final String ENTITY_PLACEHOLDER = "entityval";
  
  /**
   * constant equal to {@value}
   */
  private static final String DATA_KEY = "data";
  
  /**
   * constant equal to {@value}
   */
  private static final String SCINAMIC_LOGIN = "SCINAMIC/login";
  
  /**
   * constant equal to {@value}
   */
  private static final String SCINAMIC_LOGOUT = "SCINAMIC/logout";
  
  /**
   * constant equal to {@value}
   */
  private static final String SCINAMIC_SEARCH = "SCINAMIC/search";
	
	// preprocessor req level
	@Override	
	public YADARequest engage(YADARequest yadaReq) throws YADAPluginException
	{
		return yadaReq;
	}


	// preprocessor query level
	@Override
	public void engage(YADARequest yadaReq, YADAQuery yq) throws YADAPluginException
	{	
	  // get auth token
		yr.setQname(new String[] { SCINAMIC_LOGIN });
		Service     svc    = new Service(yr);
		String      result = svc.execute();
		
		// get PKs
		JSONObject  reso;
		String      payload;
		String      entity = null; 
		String      ids    = "";   
		try 
		{
		  reso = new JSONObject(result);
		  if(reso.has(DATA_KEY))
		  {
		    // get pks for FP numbers
		    // TODO remind myself how to use Session properties instead of System
		    yq.addProperty(new YADAProperty(yq.getQname(),SCINAMIC_TOKEN,reso.getString(DATA_KEY)));
		    yr = new YADARequest();
		    yr.setQname(new String[] { SCINAMIC_SEARCH });  
		    
        try
        {
          qmgr = new QueryManager(yr);
        }
        catch (YADAQueryConfigurationException | YADAResourceException | YADAConnectionException | YADAFinderException
            | YADAUnsupportedAdaptorException | YADARequestException | YADAAdaptorException | YADAParserException e1)
        {
          String msg = "There was a problem configuring the primary key search query." +
          "This could be the result of bad parameters.  The proper syntax is:\n\n" +
          "entity_type,FP1,FP2...,FPn\n\n" +
          "where entity_type can be: compound, compoundbatch, etc";
          throw new YADAPluginException(msg, e1);          
        }	      
	      YADAQuery yqSearch = qmgr.getQuery(0);
	      
        for (String[] param : yadaReq.getParams())
        {
          if(entity == null)
          {
            entity = param[0];
          }
          else
          {
            if(ids.length() > 0)
              ids += ",";
            ids += param[0];            
          }
        }                
        payload = transformPayload(yqSearch, yq.getProperty(SCINAMIC_TOKEN, yq.getQname()), entity, ids);
        yqSearch.addParam(YADARequest.YADA_PAYLOAD, payload);
        try
        {
          yqSearch.getAdaptor().execute(yqSearch);
        }
        catch (YADASecurityException | YADAAdaptorExecutionException e)
        {
          handleException(yqSearch, entity,ids);
        }
	      result = (String) yqSearch.getResult().getResult(0);	  
	      	      
	      // prep get_records (requested query)
		    reso = new JSONObject(result);
		    try
		    {
  		    JSONArray pks = reso.getJSONArray(DATA_KEY);		    
  		    if(pks != null && pks.length() > 0)
    		  {		        		      		      		    
    		    payload = transformPayload(yq, yq.getProperty(SCINAMIC_TOKEN, yq.getQname()), entity, pks.join(","));
    		    yq.getYADAQueryParamsForKey(YADARequest.YADA_PAYLOAD).get(0).setValue(payload);
  		    }
  		    else
  		    {
  		      handleException(yq, entity,ids);
  		    }
		    }
		    catch(JSONException e)
		    {
		      handleException(yq, entity,ids);
		    }
		  }
		  
		}
		catch (JSONException e)
		{
		  String msg = "Could neither obtain nor parse login data response";
		  throw new YADAPluginException(msg, e);
		}
    
	}
	
	/**
	 * @param yq The currently executing query
	 * @param entity the scinamic entity type requested
	 * @param ids the list of ids attempted to process
	 */
	private void handleException(YADAQuery yq, String entity, String ids) {
	  String msg = String.format("Primary keys or record data were not returned for the requested entity: %s, and ids: %s", entity, ids);
    msg += "\\n\\nThis may be the result of a problem, or simply because these compounds are not in the system, or due to " +
        "misconfigured parameters. The proper syntax is:\\n\\n  entity_type,FP1,FP2...,FPn\\n\\n" + 
        "where entity_type can be: compound, compoundbatch, etc\\n";
    yq.setResult(new YADAQueryResult());
    yq.getResult().getResults().add(String.format("{\"data\":null,\"message\":\"%s\"}",msg));
    yq.setQname("YADA/dummy");
    yq.setApp("YADA");
	}
	
	/**
	 * 
	 * @param yq The {@link YADAQuery} object to which the YADA_PAYLOAD parameter is attached
	 * @param token The scinamic auth token
	 * @param entity The scinamic entity type
	 * @param amendment The data to append to the YADA_PLAYLOAD
	 * @return the transformed YADA_PAYLOAD {@link String}
	 */
	private String transformPayload(YADAQuery yq, String token, String entity, String amendment) {
	  String payload = yq.getYADAQueryParamValue(YADARequest.YADA_PAYLOAD)[0]
        .replace(TOKEN_PLACEHOLDER, token)
        .replace(ENTITY_PLACEHOLDER, entity);
    payload += amendment;           
    return payload;
	}

	// Postprocessor req level
  @Override
  public String engage(YADARequest yadaReq, String result) throws YADAPluginException {
    // TODO Auto-generated method stub 
    return null;
  }


  // Postprocessor query level
  @Override
  public void engage(YADAQuery yq) throws YADAPluginException {
    yr = new YADARequest();
    yr.setQname(new String[] { SCINAMIC_LOGOUT });
    try
    {
      qmgr = new QueryManager(yr);
    }
    catch (YADAQueryConfigurationException | YADAResourceException | YADAConnectionException | YADAFinderException
        | YADAUnsupportedAdaptorException | YADARequestException | YADAAdaptorException | YADAParserException e1)
    {
      String msg = "Could not configure YADA to execute logout request";
      throw new YADAPluginException(msg, e1);
      
    }       
    YADAQuery  yqlogout = qmgr.getQuery(0);    
    String     payload  = yqlogout.getYADAQueryParamValue(YADARequest.YADA_PAYLOAD)[0]
                          .replace("tokenval", yq.getProperty(SCINAMIC_TOKEN,yq.getQname()));
        
    yqlogout.getYADAQueryParamsForKey(YADARequest.YADA_PAYLOAD).get(0).setValue(payload);
    try
    {
      yqlogout.getAdaptor().execute(yqlogout);
    }
    catch (YADASecurityException | YADAAdaptorExecutionException e)
    {
        String msg = "There was a problem logging out of Scinamic.";        
        throw new YADAPluginException(msg,e);
    }
    String result = (String) yqlogout.getResult().getResult(0);
    LOG.debug(result);
  }	
}
