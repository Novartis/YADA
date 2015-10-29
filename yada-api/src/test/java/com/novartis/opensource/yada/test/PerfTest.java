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
