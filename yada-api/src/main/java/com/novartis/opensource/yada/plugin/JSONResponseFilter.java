/**
 * 
 */
package com.novartis.opensource.yada.plugin;

import com.novartis.opensource.yada.YADARequest;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.burt.jmespath.JmesPath;
import io.burt.jmespath.Expression;
import io.burt.jmespath.jackson.JacksonRuntime;

/**
 * @author dvaron
 * @since 9.0.0
 */
public class JSONResponseFilter extends AbstractPostprocessor {

  /**
   * local logger
   */
  private final static Logger l = Logger.getLogger(JSONResponseFilter.class);
  /**
   * 
   */
  public JSONResponseFilter() {
    // TODO Auto-generated constructor stub
  }
  
  @Override
  public String engage(YADARequest yadaReq, String result) throws YADAPluginException {
    // The first thing you need is a runtime. These objects can compile expressions
    // and they are specific to the kind of structure you want to search in.
    // For most purposes you want the Jackson runtime, it can search in JsonNode
    // structures created by Jackson.
    JmesPath<JsonNode> jmespath = new JacksonRuntime();
    // Expressions need to be compiled before you can search. Compiled expressions
    // are reusable and thread safe. Compile your expressions once, just like database
    // prepared statements.
    Expression<JsonNode> expression = jmespath.compile(yadaReq.getArgs().get(0));
    // This you have to fill in yourself, you're probably using Jackson's ObjectMapper
    // to load JSON data, and that should fit right in here.
    ObjectMapper mapper = new ObjectMapper();
    JsonNode input;
    try
    {
      input = mapper.readTree(result);
      JsonNode filtered = expression.search(input);    
      return mapper.writeValueAsString(filtered);
    }
    catch (IOException e)
    {
      String msg = "Unable to process the filter string. Returning original result.";
      l.error(msg);
      e.printStackTrace();
      return result;
    }
    // Finally this is how you search a structure. There's really not much more to it.    
  }

}
