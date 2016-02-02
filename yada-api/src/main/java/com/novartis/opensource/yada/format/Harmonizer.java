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
package com.novartis.opensource.yada.format;


import com.novartis.opensource.yada.YADAResourceException;
import com.novartis.opensource.yada.io.YADAIOException;
import com.novartis.opensource.yada.util.JsRuntimeSupport;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
/**
 * Class containing utility methods for re-mapping JSON keys, result set columns, 
 * flattening and pruning results, etc. The class hands off processing of these 
 * transformations to Javascript via Rhino.
 * @author Dave Varon
 * @since 6.1.0
 * @see "https://developer.mozilla.org/en-US/docs/Mozilla/Projects/Rhino"
 */
public class Harmonizer 
{
  private ScriptEngineManager factory;
  @SuppressWarnings("unused")
  private ScriptEngine engine;
  private ScriptableObject global;
  
  /**
   * A constant with value: {@value}
   */
  public static final String PRUNE = "prune";
  
  /**
   * Default constructor which initializes javascript context and scope
   * @throws YADAResourceException when the path to {@code r.js} or {@code harmony.js} is corrupt
   * @throws YADAIOException when the path to {@code r.js} or {@code harmony.js} can't be read
   * @throws YADAConverterException when the Rhino javascript engine throws an error
   * @see "https://developer.mozilla.org/en-US/docs/Mozilla/Projects/Rhino/Scopes_and_Contexts"
   */
  public Harmonizer() throws YADAResourceException, YADAIOException, YADAConverterException 
  {
    this.factory       = new ScriptEngineManager();
    this.engine        = this.factory.getEngineByName("JavaScript");
    Context    ctx     = Context.enter();
    this.global        = ctx.initStandardObjects(new JsRuntimeSupport(), true);
    String[]   names   = { "print", "load" };
    this.global.defineFunctionProperties(names, this.global.getClass(), ScriptableObject.DONTENUM);
    Scriptable argsObj = ctx.newArray(this.global, new Object[] { });
    this.global.defineProperty("arguments", argsObj, ScriptableObject.DONTENUM);
    URL requireURL = null;
    try 
    {
      requireURL = new URL("http://requirejs.org/docs/release/2.1.9/r.js");
    
      try(Reader inHmap  = new InputStreamReader(getClass().getResourceAsStream("/utils/harmony.js"));
          Reader inR   = new InputStreamReader(requireURL.openStream()))
      {
        ctx.evaluateReader(this.global, inR , "require", 0, null);
        ctx.evaluateReader(this.global, inHmap, "harmony", 0, null);
      } 
      catch (MalformedURLException e) 
      {
        String msg = "One of the required resources could not be loaded from the provided path.";
        throw new YADAResourceException(msg, e);
      } 
      catch (IOException e) 
      {
        String msg = "One of the required resources could not be read.";
        throw new YADAIOException(msg, e);
      }  
      catch (EvaluatorException e)
      {
        String msg = "There was a problem with the Rhino Javascript engine.";
        throw new YADAConverterException(msg, e);
      }
      finally
      {
        Context.exit();
      }
    } 
    catch (MalformedURLException e1) 
    {
      e1.printStackTrace();
    }
  }
    
  /**
   * Accepts a {@link String} name of the javascript function to execute, 
   * which must be {@code harmonize} or {@code flatten}, and an array of 
   * Objects (Strings) to pass to these functions.  For {@code harmonize}, 
   * {@code o} must contain both the source string to transform, and the 
   * harmony map. Both are expected to be JSON strings. The source string 
   * can be a JSON array.  For {@code flatten} only the source is expected. 
   * @param func the name of the javascript function to execute. Must be {@code harmonize} or {@code flatten}. 
   * @param o the array of arguments to hand off to the js function
   * @return the string result of the transformation.
   * @throws YADAConverterException when the Rhino javascript engine throws an error
   */
  public String call(String func, Object[] o) throws YADAConverterException
  {
    Function f      = (Function)this.global.get(func,this.global);
    String   result = "";
    Context  ctx    = Context.enter();
    try
    {
      result = f.call(ctx, this.global, this.global, o).toString();
    } 
    catch (EvaluatorException e)
    {
      String msg = "There was a problem with the Rhino Javascript engine.";
      throw new YADAConverterException(msg, e);
    }
    finally
    {
      Context.exit();
    }
    
    return result;
  }
}
