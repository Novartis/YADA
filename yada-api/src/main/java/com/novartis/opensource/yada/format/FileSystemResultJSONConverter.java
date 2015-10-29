
/**
 * 
 */
package com.novartis.opensource.yada.format;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @since 0.4.0.0
 * @author David Varon
 *
 */
public class FileSystemResultJSONConverter extends AbstractConverter
{
	
	/**
	 * Local logger handle
	 */
	@SuppressWarnings("unused")
	private static Logger l = Logger.getLogger(FileSystemResultJSONConverter.class);
	/**
	 * Constant equal to: {@value}
	 */
	private static final String KEY_PATH = "path";
	/**
	 * Constant equal to: {@value}
	 */
	private static final String KEY_CONTENT = "content";
	/**
	 * Process the list of files in {@code result} and reformats it into a json object.
	 * @see com.novartis.opensource.yada.format.AbstractConverter#convert(java.lang.Object)
	 */
	@Override
	public Object convert(Object result) throws YADAConverterException 
	{
		//TODO implement harmony map solution for this
		JSONArray arrayResult = new JSONArray();
		JSONObject o;
		try
		{
			@SuppressWarnings("unchecked")
			List<File> files = (List<File>)result;
			for(File f : files)
			{
				String path = f.getPath();
				o = new JSONObject();
				o.put(KEY_PATH, path);
				arrayResult.put(o);
			}
		} 
		catch (JSONException e)
		{
			String msg = "Unable to parse file list";
			throw new YADAConverterException(msg,e);
		}
		catch (ClassCastException e) 
		{
		  o = new JSONObject();
		  o.put(KEY_CONTENT, result);
		  arrayResult.put(o);
		}
		return arrayResult;
	}
}
