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
package com.novartis.opensource.yada.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Utility class/custom implementation of abstract {@link ScriptableObject} to enable use of 
 * {@code require.js} to load 3rd party libs into js scope.
 * 
 * @author varonda1
 * @since 6.1.0
 * @see "http://requirejs.org"
 */
public class JsRuntimeSupport extends ScriptableObject {

    private static final long serialVersionUID = 1L;
    private static final boolean silent = false;

    /**
     * Convenience method to display js content in std out.
     * 
     * @param cx the current context
     * @param thisObj the current scope
     * @param args arguments to pass
     * @param funObj 
     */
    public static void print(Context cx, Scriptable thisObj, Object[] args, Function funObj) 
    {
      if (silent)
        return;
    }

    /**
     * @param cx the current context
     * @param thisObj the current scope
     * @param args arguments to pass
     * @param funObj
     * @throws FileNotFoundException when the file can't be located
     * @throws IOException when there is a problem reading the file
     */
    public static void load(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws FileNotFoundException, IOException 
    {
      JsRuntimeSupport shell = (JsRuntimeSupport) getTopLevelScope(thisObj);
      for (int i = 0; i < args.length; i++) 
      {
          shell.processSource(cx, Context.toString(args[i]));
      }
    }

    /**
     * Utility method to do the actual file I/O
     * @param cx current js context
     * @param url location of resource
     * @throws FileNotFoundException 
     * @throws IOException
     */
    private void processSource(Context cx, String url) throws FileNotFoundException, IOException 
    {
      URL u = new URL(url);
      String[] elements = url.split("/");  
      String filename = elements[elements.length - 1];
      cx.evaluateReader(this, new InputStreamReader(u.openStream()), filename, 0, null);
    }

    @Override
    public String getClassName() {
      return "JsRuntimeSupport";
    }

}
