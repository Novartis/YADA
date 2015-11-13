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
package com.novartis.opensource.yada.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.Finder;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADAResourceException;
import com.novartis.opensource.yada.io.YADAIOException;

/**
 * A file system utility.
 * @author David Varon
 *
 */
public class FileUtils {
	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(FileUtils.class);
	
	/**
	 * Check if a directory existss
	 * @param dir the directory to check
	 * @return {@code true} if the {@code dir} exists on the file system
	 */
	public static boolean exists(String dir)
	{
		l.debug("Checking existence of directory ["+dir+"]");
		File f = new File(dir);
		if (f.exists()) { l.debug("["+dir+"] exists."); return true; }
		l.debug("["+dir+"] does not exist.");
		return false;
	}
	
	/**
	 * A wrapper for {@link java.io.File#renameTo(File)}
	 * @param oldf the original filename
	 * @param newf the new filename
	 * @return {@code true} if the move was successful
	 */
	public static boolean move(File oldf, File newf)
	{
		return oldf.renameTo(newf);
	}
	
	/**
	 * A wrapper for {@link java.io.File#renameTo(File)} which creates new File handles for both args.
	 * @param oldf the original filename
	 * @param newf the new filename
	 * @return {@code true} if the move was successful
	 */
	public static boolean move(String oldf, String newf)
	{
		return new File(oldf).renameTo(new File(newf));
	}
	
	/**
	 * Creates {@code dir} if it doesn't yet exist.  A wrapper for {@link java.io.File#mkdir()}
	 * @param dir the directory to create
	 * @return {@code true} if the operation was successful
	 */
	public static boolean mkdir(String dir)
	{
		boolean success = exists(dir) ? true : new File(dir).mkdir();
		return success;
	}
	
	/**
	 * Creates all {@code dir}s in the path as needed.  A wrapper for {@link java.io.File#mkdirs()}
	 * @param dir the path to create
	 * @return {@code true} if the operation was successful
	 */
	public static boolean mkdirs(String dir)
	{
		boolean success = exists(dir) ? true : new File(dir).mkdirs();
		return success;
	}
	
	/**
	 * Calls {@link #mkUserDir(String)} with the current {@code user} value
	 * @param yadaReq YADA request configuration
	 * @return the new directory path
	 * @throws YADAResourceException when the YADA output directory cannot be found
	 */
	public static String mkUserDir(YADARequest yadaReq) throws YADAResourceException
	{		
		String user = yadaReq.getUser();
		String dir = mkUserDir(user);
		return dir;
	}
	
	/**
	 * Creates a directory name {@code user} in {@code io/out} 
	 * @param user the user id to use for the name of the new directory
	 * @return the new directory path
	 * @throws YADAResourceException when the YADA output directory cannot be found 
	 */
	public static String mkUserDir(String user) throws YADAResourceException
	{
		String filesep = System.getProperty("file.separator");
		//TODO add a new argument to enable these directories in 'in' or 'out'
		String baseDir = Finder.getEnv("io/out");
		String userdir = "";
		if (null != user && !"".equals(user))
		{
			userdir = FileUtils.mkdir(baseDir+filesep+user) ? baseDir+filesep+user : "";
		}
		return userdir;
	}
	
	/**
	 * Returns the path relative to {@code app_home}
	 * @param path the {@code app_home} subdirectory path
	 * @return the path from {@code app_home}
	 * @throws YADAResourceException when the path to {@code app_home} cannot be found
	 */
	public static String getRelativePath(String path) throws YADAResourceException
	{
		String appHome  = Finder.getEnv("app_home");
		return path.substring(appHome.length());
	}
	
	/**
	 * Returns the current timestamp in format {@code yyyyMMddHHmmssSSS}
	 * @return the current timestamp
	 * @see java.text.SimpleDateFormat
	 */
	public static String getTimeStamp()
	{
		return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
	}
	
	/**
	 * Get a list of directory contents
	 * @param d the directory to list
	 * @return a list of directory contents
	 */
	public static List<File> getFileList(File d)
	{
		return getFileList(d,-1);
	}

	
	/**
	 * Gets a recursive list of directory contents
	 * @param d the directory to interrogate
	 * @param depth the number of level to recurse
	 * @return a list of directory contents
	 */
	public static List<File> getFileList(File d, int depth) 
	{
		List<File> fList = new ArrayList<>();
		l.debug("checking ["+d.getName()+"]");
		if (d.canRead() && d.isDirectory())
		{
			File[] list = d.listFiles();
			for (int i=0;i<list.length;i++)
			{
				File f = list[i];
				if (f.isFile()) 
				{ 
					fList.add(f); 
					l.debug("Adding ["+f.getName()+"]");
				}
				else 
				{ 
					if(depth > 0)
					{
						l.debug("Descending...");
						fList.addAll(getFileList(f,depth-1));
					}
					else if(depth == -1)
					{
						fList.addAll(getFileList(f,-1));
					}
				}
			}
		}
		return fList;
	}
	
	/**
	 * Retrieves the requested file from the filesystem and returns it's content as a {@link String}
	 * @param f the file from which content is desired
	 * @throws YADAResourceException when the requested file can't be found
	 * @throws YADAIOException when the requested content can't be read
	 * @return {@link String} containing the contents of the file {@code f}
	 * @since 5.0.1
	 */
	public static String getText(File f) throws YADAResourceException, YADAIOException 
	{
	  String result = "";
	  try(FileInputStream fis = new FileInputStream(f))
	  {
	     
	     byte[] content = new byte[(int) f.length()];
	     fis.read(content);
	     result = new String(content);
	  }
	  catch(FileNotFoundException e)
	  {
	    throw new YADAResourceException(e);
	  } 
	  catch (IOException e) 
	  {
	    throw new YADAIOException(e);
    }
	  return result;
	}
}