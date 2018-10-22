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
package com.novartis.opensource.yada.plugin;

import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADASecurityException;

/**
 * This plugin is both a {@link Preprocess} and {@link Postprocess} plugin.
 * When set as a default on the {@code YADA check credentials} query, the preprocessor will set itself
 * as a postprocessor on the {@link YADARequest}.
 *
 * <p>When the {@link #engage(YADAQuery)} method is executed, it will examine the payload and if valid,
 * store it in the {@link HttpSession}.</p>
 *
 * @author David Varon
 * @since 8.1.0
 */
public class Login extends AbstractPostprocessor implements Preprocess{

  /**
   * Examies the payload.  If it contains &gt;0 rows, it will store the payload in the session.
   * @throws YADASecurityException if the passed credentials are invalid
   */
  @Override
  public String engage(YADARequest yadaReq, String result) throws YADASecurityException {
    String uid = "";
    if(result != null && !"".equals(result))
    {
      JSONObject j = new JSONObject(result);
      JSONObject r = j.getJSONObject("RESULTSET");

      if(r.getInt("records") > 0)
      {
        JSONArray a = r.getJSONArray("ROWS");
        uid = a.getJSONObject(0).getString("USERID");
        yadaReq.getRequest().getSession().setAttribute("YADA.user.privs", a);
      }
      else
      {
        String msg = "User is not authorized";
        throw new YADASecurityException(msg);
      }
    }
    else
    {
      String msg = "User is not authorized";
      throw new YADASecurityException(msg);
    }
    return "{\"RESULTSET\":{\"ROWS\":[{\"USER\":\""+uid+"\",\"AUTH\":true}],\"records\":1}}";
  }

  @Override
  public YADARequest engage(YADARequest yadaReq) throws YADAPluginException {
    // nothing to do
    return null;
  }

  /**
   * Sets this object as a request post processor.
   */
  @Override
  public void engage(YADARequest yadaReq, YADAQuery yq)
      throws YADAPluginException {
    yadaReq.setPlugin(new String[] {"Login"});
  }
}
