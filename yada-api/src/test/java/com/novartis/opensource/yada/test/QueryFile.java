/**
 * 
 */
package com.novartis.opensource.yada.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A key=value pair mapping.
 * 
 * String array of key-value pairs fed to a dynamic data provider.
 * Should be in the form of key=value, e.g.,
 * args={"foo=bar", "biz=baz"}
 * @author David Varon
 * @since 0.4.1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryFile
{
  /**
   * The list of files to process by the method
   * @return the {@link String} array
   */
	String[] list();
	
}
