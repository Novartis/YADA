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
/**
 * 
 */
package com.novartis.opensource.yada.test;

import java.util.List;

import org.testng.IInvokedMethod;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

/**
 * @author David Varon
 *
 */
public class TestNGReporter implements IReporter {

  /**
   * 
   */
  public TestNGReporter() {
    // TODO Auto-generated constructor stub
  }

  /* (non-Javadoc)
   * @see org.testng.IReporter#generateReport(java.util.List, java.util.List, java.lang.String)
   */
  @Override
  public void generateReport(List<XmlSuite> arg0, List<ISuite> arg1, String arg2) {
    ISuite suite = arg1.get(0);
    List<IInvokedMethod> methods = suite.getAllInvokedMethods();
    for(IInvokedMethod m : methods) 
    {
      ITestResult r = m.getTestResult();
      if(r.getStatus() == 2)
      {
        System.out.println("FAILURE: "+m.getTestMethod().getMethodName());
        System.out.println("  PARAMETERS:");
        for(Object p : r.getParameters())
        {
          System.out.println("  "+(String)p);
        }
      }
    }
  }

}
