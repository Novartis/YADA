#Testing Notes

## Framework

[TestNG](http://testng.org) is used for all automated testing of the API and web services.  [Selenium](http://docs.seleniumhq.org/) will be used for testing the yada-admin tool, eventually.

## Types

### Unit Testing

Definition: Validation of execution of specific methods in the framework, independent of any specific data source. 

> There are currently no unit tests written or running.

### Integration Testing

Definition: Validation of execution of features in a variety of contexts including

* Use of YADA Index on multiple database vendor and technology platforms
    * MySQL
    * Oracle
    * PostgreSQL
    * SQLite
* Use of YADA JAVA and Web API
* Create, Read, Update, Delete (CRUD) queries for all supported data types: 
    * Character `?v`
    * Floating Point `?n`
    * Integer `?i`
    * Date `?d`
    * Time `?t`
* All [YADA Parameters](params.html)
* Combinations of parameters to success and failure

Tests are also segmented to take advantage of context-specific scenarios such as database-vendor-specific features, or path-style URIs.