/**
 * 
 */
package com.novartis.opensource.yada.adaptor;

import java.io.File;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADAResourceException;
import com.novartis.opensource.yada.io.YADAIOException;
import com.novartis.opensource.yada.util.FileUtils;

/**
 * @author dvaron
 * @since 9.2.0
 *
 */
public class FileSystemAnnotatedAdaptor extends FileSystemAdaptor {
  
  /**
   * Local instance of logger
   */
  static Logger l = Logger.getLogger(FileSystemAnnotatedAdaptor.class);
  
  /**
   * Default constructor.
   */
  public FileSystemAnnotatedAdaptor()
  {
    super();
    l.debug("Initializing FileSystemAnnotatedAdaptor");
  }

  /**
   * Preferred "YADARequest" constructor.
   * @param yadaReq YADA request configuration
   */
  public FileSystemAnnotatedAdaptor(YADARequest yadaReq)
  {
    super(yadaReq);
  }
  
  @Override
  protected Object read(File f) throws YADAAdaptorExecutionException {
    Object result;
    if(f.isDirectory())
    {
       result = FileUtils.getAnnotatedFileList(new JSONArray(), f,-1);
    }
    else
    {
      try 
      {
        result = FileUtils.getText(f);
      } 
      catch (YADAIOException e) 
      {
        throw new YADAAdaptorExecutionException(e);
      }
      catch (YADAResourceException e) 
      {
        throw new YADAAdaptorExecutionException(e);
      }
    }
    return result;
  }

}
