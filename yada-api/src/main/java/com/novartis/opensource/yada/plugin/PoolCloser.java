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

import org.json.JSONObject;

import com.novartis.opensource.yada.ConnectionFactory;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADARequest;

/**
 * Retrieves the connection pool and shuts it down.
 * @author David Varon
 * @since 8.4.0
 */
public class PoolCloser extends AbstractPostprocessor implements Preprocess{

  @Override
  public String engage(YADARequest yadaReq, String result)
      throws YADAPluginException {
    JSONObject jo   = new JSONObject(result);
    String     app  = jo.getJSONObject("RESULTSET").getJSONArray("ROWS").getJSONObject(0).getString("APP");
    String     pool = ConnectionFactory.getConnectionFactory().closePool(app);
    return "Connection pool ["+pool+"] closed successfully.";
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
    yadaReq.setPlugin(new String[] {"PoolCloser"});
  }
}
