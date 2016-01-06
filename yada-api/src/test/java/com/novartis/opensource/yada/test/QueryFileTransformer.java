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
/**
 * 
 */
package com.novartis.opensource.yada.test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Properties;

import org.testng.IAnnotationTransformer2;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.ITestAnnotation;

/**
 * @author David Varon
 * @since  0.5.1.0
 */
public class QueryFileTransformer implements IAnnotationTransformer2 {

  /**
   * Properties containing test file lists
   */
  private Properties props = new Properties();
  /**
   * Path to default toggle properties file
   */
  private static final String PROPERTIES = "/conf/TestNG_toggle.properties";
  /**
   * System property to pass toggle file
   */
  private static final String TOGGLE = "test.toggle";
  /**
   * Comma-delimiter for file lists
   */
  private static final String DELIMITER  = ",";        
  /**
   * No-arg constructor, loads properties
   * @throws IOException when {@link #PROPERTIES} can't be loaded
   */
  public QueryFileTransformer() throws IOException {
    String toggleProp = System.getProperty(TOGGLE);
    String toggle     = toggleProp == null || toggleProp.equals("") ? PROPERTIES : toggleProp;
    try(InputStream is    = this.getClass().getResourceAsStream(toggle))
    {
      this.props.load(is);
    }
  }

  /**
   * Grabs test file lists from {@link #PROPERTIES} based on value of {@code method}
   * @see org.testng.IAnnotationTransformer#transform(org.testng.annotations.ITestAnnotation, java.lang.Class, java.lang.reflect.Constructor, java.lang.reflect.Method)
   */
  @SuppressWarnings("rawtypes")
  @Override
  public void transform(ITestAnnotation testAnno, Class claz, Constructor cons, Method method) 
  { 
    String name    = method.getName();
    if(ServiceTest.class.equals(method.getDeclaringClass()))
    {
      if(name.startsWith("test")
          && this.props.getProperty(name) == null)
      {
        testAnno.setEnabled(false);
      }
      else
      {
        String listStr = (String)this.props.get(name);
        if(listStr != null)
        {
          String[] list = listStr.split(DELIMITER);
          if(list != null && list.length > 0)
          {
            Annotation anno = method.getAnnotation(QueryFile.class);
            if (anno != null)
            {
              changeAnnotationValue(anno,"list",list);
            }
          }
        }
      }
    }
  }

  /**
   * Not implemented
   * @see org.testng.IAnnotationTransformer2#transform(org.testng.annotations.IDataProviderAnnotation, java.lang.reflect.Method)
   */
  @Override
  public void transform(IDataProviderAnnotation arg0, Method arg1) { 
    // Nothing to do
  }

  /**
   * Not implemented
   * @see org.testng.IAnnotationTransformer2#transform(org.testng.annotations.IFactoryAnnotation, java.lang.reflect.Method)
   */
  @Override
  public void transform(IFactoryAnnotation arg0, Method arg1) { 
    // Nothing to do
  }

  /**
   * Not implemented
   * @see org.testng.IAnnotationTransformer2#transform(org.testng.annotations.IConfigurationAnnotation, java.lang.Class, java.lang.reflect.Constructor, java.lang.reflect.Method)
   */
  @SuppressWarnings("rawtypes")
  @Override
  public void transform(IConfigurationAnnotation annotation, Class arg1, Constructor arg2, Method arg3) { 
    // Nothing to do
  }

  /**
   * Changes the annotation value for the given key of the given annotation to newValue and returns
   * the previous value. Modified from http://stackoverflow.com/a/28118436
   * @param annotation the {@link QueryFile} annotation applied to the test method
   * @param key a {@link String} equal to {@code "list"}
   * @param newValue a {@link String} containing a comma-separated list of test files 
   * @return the old value of the annotation, which should be an empty {@link String} array
   */
  @SuppressWarnings("unchecked")
  public static Object changeAnnotationValue(Annotation annotation, String key, Object newValue){
      Object handler = Proxy.getInvocationHandler(annotation);
      Field f;
      try {
          f = handler.getClass().getDeclaredField("memberValues");
      } 
      catch (NoSuchFieldException e)
      {
         throw new IllegalStateException(e);
      }
      catch (SecurityException e) 
      {
        throw new IllegalStateException(e);
      }
      f.setAccessible(true);
      Map<String, Object> memberValues;
      try {
          memberValues = (Map<String, Object>) f.get(handler);
      } 
      catch (IllegalArgumentException e) 
      {
        throw new IllegalStateException(e);
      }
      catch (IllegalAccessException e) 
      {
          throw new IllegalStateException(e);
      }
      Object oldValue = memberValues.get(key);
//      if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
//          throw new IllegalArgumentException();
//      }
      memberValues.put(key,newValue);
      return oldValue;
  }
}
