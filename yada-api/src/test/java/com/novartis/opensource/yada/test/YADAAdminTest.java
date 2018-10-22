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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * Automated tests for yada-admin webapp
 * 
 * @author David Varon
 * @since 8.3.0
 */
public class YADAAdminTest {
  
  /**
   * Logger impl
   */
  private static Logger l = Logger.getLogger(YADAAdminTest.class);

  /**
   * Selenium browser controller
   */
  public WebDriver driver;
  
  /**
   * http protocol
   */
  private static final String PROTOCOL = "http://";
  
  /**
   * The yada server used for http and soap tests. Defaults to:
   * <code>localhost:8080</code>
   */
  protected String host = "localhost:8080";

  /**
   * The path to the admin tool
   */
  protected String uri = "/yada-admin";

  /**
   * The db driver
   */
  protected String dbDriver;
  
  /**
   * The db username
   */
  protected String dbUser;
  
  /**
   * The db pw
   */
  protected String dbPw;
  
  /**
   * The db url
   */
  protected String dbUrl;
  
  /**
   * The db validation query
   */
  protected String dbValidationQuery;
  
  /**
   * The flag to use authentication
   */
  protected String auth = "false";

  /**
   * The test app
   */
  protected String app;
  /**
   * The Selenium web driver
   * 
   * @return the current driver instance
   */
  public WebDriver getDriver() {
    return this.driver;
  }

  /**
   * Standard mutator
   * 
   * @param driver
   *          the current driver
   */
  public void setDriver(WebDriver driver) {
    this.driver = driver;
    this.driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
  }

  /**
   * Configure test for provided browser
   * 
   * @param browser the currently tested client
   * @param properties the name of the properties file
   * @throws IOException  when properties can't be loaded
   */
  @Parameters({"browser","properties"})
  @BeforeClass
  public void init(String browser, String properties) throws IOException {
    ConnectionFactoryTest.setProps(properties);
    Properties props = ConnectionFactoryTest.getProps();
    this.host        = props.getProperty("YADA.host");
    this.dbDriver    = props.getProperty("YADA.index.driverClassName");
    this.dbUrl       = props.getProperty("YADA.index.url");
    this.dbUser      = props.getProperty("YADA.index.username");
    this.dbPw        = props.getProperty("YADA.index.password");
    this.dbValidationQuery = props.getProperty("YADA.index.validationQuery");
    if (browser.equalsIgnoreCase("firefox")) 
    {
      setDriver(new FirefoxDriver());
    } 
    else if (browser.equalsIgnoreCase("chrome")) 
    {
      setDriver(new ChromeDriver());
    }
    getDriver().get(PROTOCOL + this.host + this.uri);
  }

  /**
   * Cleanup
   * @throws InterruptedException if any thread has interrupted the current thread 
   */
  @AfterClass
  public void cleanup() throws InterruptedException {
    Thread.sleep(3000);
    getDriver().quit();
  }
  
  /**
   * Tests splash screen
   */
  @Test(priority=0)
  public void testLandingPage() {
    WebDriver d = getDriver();
    for (WebElement e : d.findElement(By.tagName("nav"))
        .findElements(By.cssSelector("li a"))) {
      try
      {
        e.click();
      }
      catch(WebDriverException e1)
      {
        l.debug("Navbar unclickable as expected.");
      }
    }
    WebElement e = d.findElement(By.id("app-mgr"));
    Assert.assertFalse(e.isDisplayed());
    e = d.findElement(By.id("query-table"));
    Assert.assertFalse(e.isDisplayed());
    e = d.findElement(By.id("qname-copy"));
    Assert.assertFalse(e.isDisplayed());
    e = d.findElement(By.id("migration-target-selector"));
    Assert.assertFalse(e.isDisplayed());
    e = d.findElement(By.id("query-editor-container"));
    Assert.assertFalse(e.isDisplayed());
    e = d.findElement(By.id("compare"));
    Assert.assertFalse(e.isDisplayed());
    new WebDriverWait(d, 300)
        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#login .modal-dialog")));
    Assert.assertFalse(d.findElement(By.id("login-btn")).isEnabled());
  }

  /**
   * Tests yada-admin login feature. Goods creds = auth
   * @throws InterruptedException if any thread has interrupted the current thread
   */
  @Test(dependsOnMethods={"testLandingPage"})
  public void testLoginSuccess() throws InterruptedException 
  {
    WebDriver d = getDriver();
    this.switchToActiveElement(d,3000);
    d.findElement(By.id("login-user")).sendKeys("test");
    d.findElement(By.id("login-pw")).sendKeys("testt");
    Assert.assertTrue(d.findElement(By.id("login-btn")).isEnabled());
    d.findElement(By.id("login-btn")).click();
    this.switchToActiveElement(d,3000);
    Assert.assertTrue(d.findElement(By.id("app-mgr")).isDisplayed());
  }
  
  /**
   * Test App Manager new app creation
   * @throws InterruptedException if any thread has interrupted the current thread
   */
  @Test(dependsOnMethods={"testLoginSuccess"})
  public void testAppCreation() throws InterruptedException
  {
    String t = new String(Long.valueOf(new Date().getTime()).toString());
    this.app = "FOO"+t.substring(t.length() < 17 ? 0 : t.length()-17);
    WebDriver d = getDriver();
    d.findElement(By.id("app-hdr-new")).click();
    Thread.sleep(100);
    Assert.assertTrue(d.findElement(By.id("app-new")).isDisplayed());
    d.findElement(By.id("app-code-new")).sendKeys(this.app);
    d.findElement(By.id("app-active-new")).click();
    d.findElement(By.id("app-name-new")).sendKeys(this.app);
    d.findElement(By.id("app-desc-new")).sendKeys("This is a test of new app functions using app "+this.app);
    StringBuilder conf = new StringBuilder();
    conf.append("jdbcUrl=");
    conf.append(this.dbUrl);
    conf.append("\nusername=");
    conf.append(this.dbUser);
    conf.append("\npassword=");
    conf.append(this.dbPw);
    conf.append("\ndriverClassName=");
    conf.append(this.dbDriver);
    conf.append("\nautoCommit=false");
    conf.append("\nconnectionTimeout=300000");
    conf.append("\nidleTimeout=600000");
    conf.append("\nmaxLifetime=1800000");
    conf.append("\nminimumIdle=5");
    conf.append("\nmaximumPoolSize=100");
    WebElement e = d.findElement(By.id("app-conf-new"));
    e.clear();
    e.sendKeys(conf.toString());
    d.findElement(By.id("app-submit-new")).click();
    e = new WebDriverWait(d,20)
        .until(ExpectedConditions.presenceOfElementLocated(By.id("app-hdr-"+this.app)));
    Assert.assertTrue(e.isDisplayed());
  }
  
  /**
   * Test App Manager new query for new app then cancel
   * @throws InterruptedException if any thread has interrupted the current thread
   */
  @Test(dependsOnMethods={"testAppCreation"})
  public void testNewQueryForNewAppCancel() throws InterruptedException
  {
    WebDriver d = getDriver();
    this.createQueryForNewApp("Test Cancel", "cancel");
    
    new WebDriverWait(d, 20)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id("query-editor-container")));
    Assert.assertEquals(d.findElements(By.cssSelector("#query-table tbody td.dataTables_empty")).size(), 1);
  }
  
  /**
   * Test App Manager new query for new app (prep only)
   * @throws InterruptedException if any thread has interrupted the current thread
   */
  @Test(dependsOnMethods={"testNewQueryForNewAppCancel"})
  public void testNewQueryForNewAppSave() throws InterruptedException
  {
    String qname = "Test Save";
    this.createQueryForNewApp(qname,"save");
    WebDriver d = getDriver();
    
    // validate ui
    WebElement e = new WebDriverWait(d,20)
        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
    String expected = "Hooray! Query "+this.app + " " + qname + " was saved successfully.";
    Assert.assertEquals(e.getText(), expected);
    WebElement save = d.findElement(By.id("button-save"));
    Assert.assertFalse(save.isDisplayed());
    
    // close dialog
    d.findElement(By.id("button-cancel")).click();
    Thread.sleep(3000);
    
    // validate query table has rows
    Assert.assertEquals(d.findElements(By.cssSelector("#query-table tbody td.dataTables_empty")).size(), 0);  
    List<WebElement> rows = d.findElements(By.cssSelector("#query-table tbody tr[role=\"row\"]"));
    Assert.assertNotEquals(Integer.valueOf(rows.size()), Integer.valueOf(0));
    
    // validate content of query table
    List<WebElement> cells = d.findElements(By.cssSelector("#query-table tbody tr[role=\"row\"] td"));
    String expectedQname = this.app + " " + qname;
    String expectedComments = "Testing comments".substring(0,12) + "…";
    boolean qnameMatches = false;
    boolean commentsMatches = false;
    for(WebElement cell : cells)
    {
      
      if(cell.getText().equals(expectedQname))
      {
        qnameMatches = true;
        break;
      }
    }
    for(WebElement cell : cells)
    {
      if(cell.getText().equals(expectedComments))
      {
        commentsMatches = true;
        break;
      }
    }
    Assert.assertTrue(qnameMatches && commentsMatches);
  }
  
  /**
   * Creates a new query for the current app using {@code SELECT 1 AS COL1} 
   * @param qname the query to insert
   * @param action the imperative to executed
   * @throws InterruptedException if the any thread has interrupted the current thread
   */
  private void createQueryForNewApp(String qname, String action) throws InterruptedException
  {
    String query = "SELECT 1 as COL1";
    this.createQueryForNewApp(qname, query, action);
  }
  
  /**
   * Asserts that menu bar has either 3 disabled buttons, when app has 0 queries, or 0 disabled buttons when
   * it has at least 1 query
   * @throws InterruptedException if any thread has interrupted the current thread 
   */
  private void validateMenues() throws InterruptedException {
    WebDriver d = getDriver();
//    new WebDriverWait(d,20).until(ExpectedConditions.presenceOfElementLocated(By.id("app-qname-"+this.app)));
    
    List<WebElement> emptyWarning = d.findElements(By.className("dataTables_empty"));
    Thread.sleep(3000);
    String xpath = "//nav[contains(@class,\"main-menu\")]//li[contains(@class,\"disabled\")]/a[not(@href)]";
    List<WebElement> menuButtons = d.findElements(By.xpath(xpath));
    Thread.sleep(3000); //sometimes the next step takes time to appear, but WebDriverWait will throw an exception in the default case
    if(emptyWarning.size() > 0)
      Assert.assertEquals(menuButtons.size(),3);
    else
      Assert.assertEquals(menuButtons.size(),0);
  }
  
  /**
   * Clicks the "New Query" button and waits for dialog to open
   * @throws InterruptedException if any thread has interrupted the current thread
   */
  private void openQueryEditor() throws InterruptedException {
 // initiate query creation
    WebDriver d = getDriver();
    d.findElement(By.id("new-query")).click();
    this.switchToActiveElement(d,3000);
    new WebDriverWait(d,20).until(ExpectedConditions.presenceOfElementLocated(By.id("query-editor-container")));
  }
  
  /**
   * Makes assertions about default state of query editor modal
   */
  private void validateQueryEditor()
  {
    WebDriver d = getDriver();
    WebElement e = d.findElement(By.id("query-editor-container"));
    Assert.assertTrue(e.isDisplayed());
    List<WebElement> list = d.findElements(By.xpath("//div[contains(@class,\"panel \")]"));
    Assert.assertEquals(list.size(), 3);
    list = d.findElements(By.xpath("//div[contains(@class,\"panel \") and @style=\"display: none;\"]"));
    Assert.assertEquals(list.size(), 2);
    new WebDriverWait(d, 20)
    .until(ExpectedConditions.presenceOfElementLocated(By.id("collapseOne")));
    Assert.assertTrue(d.findElement(By.id("collapseOne")).isDisplayed()); // comments
    Assert.assertFalse(d.findElement(By.id("collapseTwo")).isDisplayed()); // security
    Assert.assertFalse(d.findElement(By.id("collapseThree")).isDisplayed()); // params

    // validate buttons in footer
    list = d.findElements(By.xpath("//div[@id=\"query-editor-container\"]//div[contains(@class,\"modal-footer\")]/button"));
    Assert.assertEquals(list.size(), 5);
    list = d.findElements(By.xpath("//div[@id=\"query-editor-container\"]//div[contains(@class,\"modal-footer\")]/button[@style=\"display: none;\"]"));
    Assert.assertEquals(list.size(), 3);
    Assert.assertTrue(d.findElement(By.id("button-save")).isDisplayed());
    Assert.assertTrue(d.findElement(By.id("button-cancel")).isDisplayed());
    Assert.assertFalse(d.findElement(By.id("button-rename")).isDisplayed());
    Assert.assertFalse(d.findElement(By.id("button-copy")).isDisplayed());
    Assert.assertFalse(d.findElement(By.id("button-delete")).isDisplayed());
  }
  
  /**
   * Test App Manager new query for new app (prep only)
   * @param qname the name of query to test
   * @param query the sql to insert
   * @param action {@code save} or {@code cancel}. Case matters as this value corresponds to a button id
   * @throws InterruptedException if any thread has interrupted the current thread
   */
  private void createQueryForNewApp(String qname, String query, String action) throws InterruptedException
  {
    WebDriver d = getDriver();
    
    // restore app-mgr
    d.findElement(By.id("app")).click();
    new WebDriverWait(d,20).until(ExpectedConditions.presenceOfElementLocated(By.id("app-mgr")));
    // click app's 'queries' button
    d.findElement(By.id("app-qname-"+this.app)).click();
    
    validateMenues();
    openQueryEditor();
    validateQueryEditor();
    
    // populate fields
    d.findElement(By.id("query-name")).sendKeys(qname);
    d.findElement(By.id("query-comments")).sendKeys("Testing comments");
    // codemirror
    WebElement e = d.findElement(By.cssSelector("div.CodeMirror"));
    JavascriptExecutor js = (JavascriptExecutor) d;
    js.executeScript("arguments[0].CodeMirror.setValue(\""+query+"\");", e);
    
    d.findElement(By.id("button-"+action)).click();
  }
  
  
  
  
  /**
   * Convenience method to wrap try/catch and {@link Thread#sleep(long)}
   * @param d the active {@link WebDriver}
   * @param sleep the duration for which the {@link Thread} should sleep before continuing
   * @throws InterruptedException if any thread has interrupted the current thread
   */
  private void switchToActiveElement(WebDriver d, int sleep) throws InterruptedException
  {
    try
    {
      d.switchTo().activeElement();
    }
    catch(UnsupportedCommandException e)
    {
      // Chrome driver requires above step, but Firefox driver doesn't currently support it.
      // This test succeeds now with the try block
    }
    Thread.sleep(sleep);
  }
  /**
   * Negative test of yada-admin login feature. Bad creds = Unauth
   */
  @Test(enabled=false)
  public void loginWithUnknownCredentials() {
    //
  }

  /**
   * <ol>
   * <li>Opens the query editor</li>
   * <li>Validates all the buttons are present</li>
   * <li>Opens the security panel</li>
   * <li>Sets URL validation policy</li>
   * <li>Sets TokenValidation policy</li>
   * <li>Sets ExecutionPolicy</li>
   * <li>Sets ContentPolicy</li>
   * </ol>
   * @throws InterruptedException if any thread has interrupted the current thread
   * @throws IOException when the test URL can't be accessed 
   */
  @Test(dependsOnMethods={"testNewQueryForNewAppSave"})
  public void testSecurityConfigForQuery() throws InterruptedException, IOException 
  {
    WebDriver d = getDriver();
    String protector = this.app+" Test Protector";
    this.createQueryForNewApp(" Test Protector", "SELECT 1 AS COL1", "save");
    WebElement save = d.findElement(By.id("button-save")); 
    
    // validate ui
    WebElement e = new WebDriverWait(d,20)
        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
    String expected = "Hooray! Query "+ protector + " was saved successfully.";
    Assert.assertEquals(e.getText(), expected);
    Assert.assertFalse(save.isDisplayed());
    
    // close dialog
    d.findElement(By.id("button-cancel")).click();
    Thread.sleep(3000);
    
    // restore app-mgr
    d.findElement(By.id("app")).click();
    new WebDriverWait(d, 20)
        .until(ExpectedConditions.presenceOfElementLocated(By.id("app-mgr")));
    Thread.sleep(3000);
    
    // nav to queries
    d.findElement(By.id("app-qname-"+this.app)).click();
    Thread.sleep(3000);
    
    // open query for edit
    String qname = this.app+" Test Save";
    d.findElement(By.xpath("//*[text()[contains(.,\""+qname+"\")]]")).click();
    this.switchToActiveElement(d,3000);
    new WebDriverWait(d, 20)
        .until(ExpectedConditions.presenceOfElementLocated(By.id("query-editor-container")));
    
    // validate buttons
    List<WebElement> list = d.findElements(By.cssSelector("#query-editor-container .modal-footer button"));
    Assert.assertEquals(list.size(), 5);
    for(WebElement b : list)
    {
      Assert.assertTrue(b.isDisplayed() && b.isEnabled());
    }
    
    // open security panel
    d.findElement(By.id("headingTwo")).click();
    Thread.sleep(3000);
    
    // Confirm query result
    String result = getUrl(qname);
    l.debug("EXPECT NORMAL RESULT:\n" + result);
    Assert.assertTrue(result.contains(qname) && result.toUpperCase().contains("\"ROWS\":[{\"COL1\":\"1\"}]"));
    
    // "Mark this query as secure" and confirm "unauth"
    d.findElement(By.id("secure-query-ckbx")).click();
    d.findElement(By.id("button-save")).click();
    Thread.sleep(3000);
    result = getUrl(qname);
    l.debug("EXPECT UNAUTHORIZED RESULT:\n" + result);
    //   should be rejected at this point due to checkbox + lack of plugin
    Assert.assertFalse(result.contains(qname) && result.toUpperCase().contains("\"ROWS\":[{\"COL1\":\"1\"}]"));
    Assert.assertTrue(result.contains("Server returned HTTP response code: 403"));
    d.findElement(By.id("button-cancel")).click(); // back to app-mgr
    Thread.sleep(3000);
    
    // open query for edit
    e = new WebDriverWait(d,20)
      .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()[contains(.,\""+qname+"\")]]")));
    e.click();
    this.switchToActiveElement(d,3000);
    new WebDriverWait(d, 20)
        .until(ExpectedConditions.presenceOfElementLocated(By.id("query-editor-container")));

    if(!d.findElement(By.id("secure-query-ckbx")).isDisplayed())
    {
      d.findElement(By.id("headingTwo")).click();
      Thread.sleep(3000);
    }

    // confirm "Mark this query as not secure"
    d.findElement(By.id("secure-query-ckbx")).click();
    d.findElement(By.id("button-save")).click();
    Thread.sleep(3000);
    result = getUrl(qname);
    l.debug("EXPECT NORMAL RESULT:\n" + result);
    //   should be accepted, as box is unchecked
    Assert.assertTrue(result.contains(qname) && result.toUpperCase().contains("\"ROWS\":[{\"COL1\":\"1\"}]"));
    d.findElement(By.id("button-cancel")).click();
    Thread.sleep(3000);
    
    
    // open query for edit
    e = new WebDriverWait(d,20)
      .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()[contains(.,\""+qname+"\")]]")));
    e.click();
    this.switchToActiveElement(d,3000);
    new WebDriverWait(d, 20)
        .until(ExpectedConditions.presenceOfElementLocated(By.id("query-editor-container")));

    if(!d.findElement(By.id("secure-query-ckbx")).isDisplayed())
    {
      d.findElement(By.id("headingTwo")).click();
    }
//    l.debug("Sleeping for 2 minutes to enable manual investigation");
    Thread.sleep(3000);
    // validate all components
    //   what is there before any data entry?
    
    WebElement plugin = d.findElement(By.className("policy-plugin"));
    WebElement policyArg = d.findElement(By.className("policy-arg"));
    WebElement argString = d.findElement(By.className("arg-string"));
    
    Assert.assertTrue(d.findElement(By.className("policy-group")).isDisplayed());
    Assert.assertTrue(plugin.isDisplayed());
    Assert.assertTrue(d.findElement(By.className("policy-action")).isDisplayed());
    Assert.assertTrue(argString.isDisplayed());
    Assert.assertTrue(d.findElement(By.className("policy-type")).isDisplayed());
    Assert.assertTrue(policyArg.isDisplayed());
    Assert.assertTrue(d.findElement(By.className("remove-policy")).isDisplayed());
    
    // enter policies
    
    //   what is there after each policy entry  
    plugin.sendKeys("Gatekeeper");
    Select policyType = new Select(d.findElement(By.className("policy-type")));
    policyType.selectByVisibleText("URL Pattern Matching");
    policyArg.clear();
    policyArg.sendKeys("auth.path.rx=^(https?://)?localhost:8080/.+$");
    Assert.assertEquals(argString.getText(), "auth.path.rx=^(https?://)?localhost:8080/.+$");
    
    Select policyAction = new Select(d.findElement(By.className("policy-action")));
    policyAction.selectByValue("save");
    Thread.sleep(2000);
    policyAction.selectByValue("add-same");
    //Thread.sleep(3000);
    
    String xpath = "//div[contains(@class,\"security-options\")][2]";
    e = new WebDriverWait(d, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
    
    List<WebElement> policies = d.findElements(By.className("security-options"));
    List<WebElement> policyTypes = d.findElements(By.className("policy-type"));
    List<WebElement> policyArgs = d.findElements(By.className("policy-arg"));
    
    
    policyType = new Select(policyTypes.get(1));
    policyType.selectByVisibleText("Execution Policy (Indices)");
    policyArgs.get(1).clear();
    policyArgs.get(1).sendKeys("execution.policy.indices=0:getToken()");
    Assert.assertTrue(d.findElement(By.className("policy-protector")).isDisplayed());
    d.findElement(By.className("policy-protector")).sendKeys(protector);
    
    policyAction.selectByValue("save");
    Thread.sleep(3000);
    policyAction.selectByValue("add-same");
    //Thread.sleep(3000);
    
    xpath = "//div[contains(@class,\"security-options\")][3]";
    e = new WebDriverWait(d, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
    
    policies = d.findElements(By.className("security-options"));
    policyTypes = d.findElements(By.className("policy-type"));
    policyArgs = d.findElements(By.className("policy-arg"));
    List<WebElement> policyProtectors = d.findElements(By.className("policy-protector"));
    
    policyType = new Select(policyTypes.get(2));
    policyType.selectByVisibleText("Execution Policy (Columns)");
    policyArgs.get(2).clear();
    policyArgs.get(2).sendKeys("execution.policy.columns=0:getToken()");
    
    
    Assert.assertEquals(policyProtectors.size(), 2);
    policyProtectors.get(1).sendKeys(protector);
    
    policyAction.selectByValue("save");
    Thread.sleep(2000);
    policyAction.selectByValue("add-same");
    
    xpath = "(//input[contains(@class,\"security-options\")])[3]";
    e = new WebDriverWait(d, 20).until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
    
    policies = d.findElements(By.className("security-options"));
    policyTypes = d.findElements(By.className("policy-type"));
    policyArgs = d.findElements(By.className("policy-arg"));
    policyProtectors = d.findElements(By.className("policy-protector"));

    policyType = new Select(policyTypes.get(3));
    policyType.selectByVisibleText("Content Policy");
    policyArgs.get(3).clear();
    policyArgs.get(3).sendKeys("content.policy.predicate=x=y");
    
    expected = "auth.path.rx=^(https?://)?localhost:8080.+$,execution.policy.indices=0:getToken(),execution.policy.columns=token:getToken(),content.policy.predicate=x=y";
    Assert.assertEquals(argString.getText(), expected);
    
    // save 
    //   multiple ways to save? (not yet)
    policyAction.selectByValue("save");

    // TODO validate data is in db
    // TODO validate security works? or is this just for a standard integration test?
    // TODO alert user when security setting is invalid or unsavable for any reason
    // TODO alert user to save security settings (this may enable safety net after all)
    // TODO parse security param into UI when appropriate (it currently doesn't) 
    
    
    d.findElement(By.id("button-save")).click();
    Thread.sleep(3000);
    
    d.findElement(By.id("app")).click();
    new WebDriverWait(d, 20)
        .until(ExpectedConditions.presenceOfElementLocated(By.id("app-mgr")));
    Thread.sleep(3000);
    
    // nav to queries
    d.findElement(By.id("app-qname-"+this.app)).click();
    Thread.sleep(3000);
    
    // open query for edit
    d.findElement(By.xpath("//*[text()[contains(.,\""+qname+"\")]]")).click();
    this.switchToActiveElement(d,3000);
    new WebDriverWait(d, 20)
        .until(ExpectedConditions.presenceOfElementLocated(By.id("query-editor-container")));
    
    d.findElement(By.id("headingThree")).click();
    Thread.sleep(3000);
    String actual = d.findElement(By.cssSelector("$('#default-params tbody tr:eq(0) td input:eq(1)')")).getAttribute("value");
    Assert.assertEquals(actual,expected);
    
    
  }
  
  /**
   * Executes a yada query via HTTP and returns the result
   * @param q the qname to process
   * @return the result of the query
   */
  private String getUrl(String q) 
  {
    URL url;
    String result = "";
    StringBuilder sb = new StringBuilder();
    try {
      url = new URL(PROTOCOL+this.host+"/yada/q/"+q.replace(" ","+"));
      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
      String line = ""; 
      while((line = br.readLine()) != null)
      {
        sb.append(line);
      }
      result = sb.toString();
    } 
    catch (IOException e) 
    {
      sb = new StringBuilder();
      sb.append(e.toString() + "\n");                     
      for (StackTraceElement e1 : e.getStackTrace()) 
      {
          sb.append("\t at " + e1.toString() + "\n");
      }
      return sb.toString();
    }
    return result;
  }
}
