package com.novartis.opensource.yada.adaptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.renjin.eval.EvalException;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.SEXP;

import com.novartis.opensource.yada.Service;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;

/**
 * @author dvaron
 * @since 9.1.0
 */
public class Rds extends FileSystemAdaptor {
  
  private static Logger l = Logger.getLogger(Rds.class);
  
  /**
   * Delimeter separating file name and references to objects in file
   */
  protected final static String INDEX_DELIMITER_RX = "(?:#(.+))?";
  
  /**
   * Regex matches strings like any non-path-delimiter char followed by optional path delimiter chars.
   * Path pattern to facilitate extraction of path and arguments into separate groups   * 
   */
  protected final static String PATH_RX = "^([^#]+)?"+INDEX_DELIMITER_RX+"$";
  
  /**
   * Constant equal to: {@code (\\?[idvn])}
   */
  protected final static String  PARAM_SYMBOL_RX = "\\?[idvn]";

  /**
   * No arg constructor (unused)
   */
  public Rds() {
  }

  /**
   * @param yadaReq the request containing the query the adaptor will execute
   */
  public Rds(YADARequest yadaReq) {
    super(yadaReq);
  }
  
  @Override
  public void execute(YADAQuery yq) throws YADAAdaptorExecutionException
  {
    Object result = null;
    resetCountParameter(yq);
    for(int row=0;row<yq.getData().size();row++)
    {
      yq.setResult();
      YADAQueryResult yqr    = yq.getResult();
      yqr.setApp(yq.getApp());
      
      String rawPath = yq.getUrl(row);
      String path = "";
      String args = "";
      
      // What are all the different use cases?
      // 1. source is directory, 1st arg is file, no 2nd arg:  /?v
      // 2. source is directory, 1st arg is file, 2nd arg is var: /?v#?v
      // 3. source is file, 1st arg is var: #?v
      
      // queries be like:
      // '/path/to/file.RDS\[[1]]$df
            
      Matcher m = Pattern.compile(PATH_RX).matcher(rawPath);
      int i=0;
      if(m.matches())
      {
         if(null != m.group(1))
         {
           path = m.group(1);
           while(path.matches(".*"+PARAM_SYMBOL_RX))
           {
             path = path.replaceFirst(PARAM_SYMBOL_RX, yq.getVals(row).get(i++));
           }
         }
         
         if(null != m.group(2))
         {
           args = m.group(2);           
           while(args.matches(".*"+PARAM_SYMBOL_RX))
           {
             String val = yq.getVals(row).get(i++).replace("$","\\$");
             args = args.replaceFirst(PARAM_SYMBOL_RX, val);
           }
         }
      }
      
      path = path.replace(PROTOCOL, "");
            
      RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
      ScriptEngine engine = factory.getScriptEngine();
      try
      {
        SEXP jobj = null;
        // read command
        String rcmd = String.format("robj <- readRDS('%s')%s",path,args);
        engine.eval(rcmd);
        
        // prefer jsonlite library
        String pkg = "jsonlite";
        String fn  = "toJSON";
        String lcmd = String.format("library('org.renjin.cran:%s')", pkg);
        engine.eval(lcmd);
        
        // convert to json
        String jcmd = String.format("%s(robj)",fn);
        try 
        {          
          jobj = (SEXP)engine.eval(jcmd);
        }
        catch(EvalException e)
        {
          l.warn("RDS file was not parsable with current arguments by jsonlite. Trying rjson.");
          // jsonlite failed.  use 'rjson'
          pkg  = "rjson";
          lcmd = String.format("library('org.renjin.cran:%s')", pkg);
          engine.eval(lcmd);
          jobj = (SEXP)engine.eval(jcmd);
        }
        

        result = jobj.toString();
      }
      
      catch (ScriptException e)
      {
        e.printStackTrace();
      }
      
// Working forked process code -- keep this here for a stretch 2020-09-27 
//      try
//      {
//        String s            = null;
//        result = "";
//        String[] cmds = {"/usr/local/bin/Rscript","-e","jsonlite::toJSON(readRDS('"+path+"')[[1]])"};
//        ProcessBuilder builder = new ProcessBuilder(cmds);
//        builder.redirectErrorStream(true);
//        Process process = builder.start(); // send a security exception when the permission denied
//        try(BufferedReader si = new BufferedReader(new InputStreamReader(process.getInputStream())))
//        {
//          while ((s = si.readLine()) != null)
//          {
////            l.debug("  LINE: "+s);
//            result += s;
//          }
//        }
//        process.waitFor();
//      }
//      catch(IOException e)
//      {
//        String msg = "Failed to get input from InputStream.";
//        throw new YADAAdaptorExecutionException(msg,e);
//      }
//      catch(InterruptedException e)
//      {
//        String msg = "The external process executing the script was interrupted.";
//        throw new YADAAdaptorExecutionException(msg,e);
//      }
      yqr.addResult(row, result);
    }    
  }

}
