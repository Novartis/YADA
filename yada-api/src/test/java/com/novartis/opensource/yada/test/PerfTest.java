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
package com.novartis.opensource.yada.test;

import org.testng.annotations.Test;

import com.novartis.opensource.yada.Service;
import com.novartis.opensource.yada.YADARequest;

/**
 * Contains methods for testing api performance
 * @author David Varon
 * @since 0.4.1.0
 */
public class PerfTest {
	
  /**
   * Tests {@code YADA default} query repetitively to generate performance stats for API
   */
  @Test (threadPoolSize = 1, invocationCount = 1000, groups = {"perf"})
  public void runQuery()   
  {
	  	YADARequest yadaReq = new YADARequest();
	  	yadaReq.setQname(new String[] {"YADA default"});
	  	yadaReq.setCount(new String[] {"false"});
	  	Service    svc    = new Service(yadaReq);
	    String result = svc.execute();
	    assert result.indexOf("Exception") == -1 : "Result is "+result;
  }
}
