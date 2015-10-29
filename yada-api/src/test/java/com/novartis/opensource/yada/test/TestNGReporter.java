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
 * @author varonda1
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
