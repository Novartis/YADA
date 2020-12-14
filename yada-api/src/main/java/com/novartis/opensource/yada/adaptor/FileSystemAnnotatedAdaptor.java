/**
 * 
 */
package com.novartis.opensource.yada.adaptor;

import java.io.File;

import org.json.JSONArray;

import com.novartis.opensource.yada.YADAResourceException;
import com.novartis.opensource.yada.io.YADAIOException;
import com.novartis.opensource.yada.util.FileUtils;

/**
 * @author dvaron
 *
 */
public class FileSystemAnnotatedAdaptor extends FileSystemAdaptor {
  
  @Override
  protected Object read(File f) throws YADAAdaptorExecutionException {
    Object result;
    if(f.isDirectory())
    {
      try
      {
        result = FileUtils.getAnnotatedFileList(new JSONArray(), f,-1);
      }
      catch (YADAIOException e)
      {
        String msg = "Unable to obtain directory listing.";
        throw new YADAAdaptorExecutionException(msg, e);        
      }
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
