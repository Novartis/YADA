<a name="top"></a>
# What's here?


If you know your way around a computer, like a developer or data expert, this is a good place to be. If you need a bit more help, like a spreadsheet jockey or designer, use the [Beginners' Guide/FAQ]

1. [Using YADA] (i.e., making YADA Requests)
2. YADA Markup
3. Defintions
4. The YADA "Service"
5. Query Authoring 
6. The YADAQuery object
7. Client-side Development
8. Tips and Tricks

<a name="using"></a>
# Using YADA


Every request must contain either both `qname` and `params` request parameters, or a `JSONParams` parameter.  Note that short parameter names are also available, so the previous sentence could read: Every query must contain either both `q` and `p` request parameters, or a `j` parameter

## YADA Markup

For authoring queries, use the following markup for parameter symbols:

|Markup|Definition             |Description                  |
|:----:|:----------------------|:----------------------------|
|?v    |VARCHAR, VARCHAR2, CHAR|Any character-based data type|
|?i    |INTEGER, INT           |Any integer data type        |
|?n    |NUMBER, DECIMAL        |Any floating point data type |
|?d    |DATE                   |Any date data type           |


## Definitions

Some terms that are used repeatedly throughout the documentation:

|Term              |Definition
|:-----------------|:---------
|standard parameter|a YADA request parameter appended to the url like 'qname' in the following example: `http://example.com/yada.jsp?qname=YADA+default`
|JSON parameter    |a YADA request parameter expressed using the [JSONParams Specification]
|default parameter |a YADA request parameter that is stored with a query in the YADA Index and attached automatically to each request that references the query
|default value     |a parameter for which the default value is set by YADA, e.g., `format=json`
|protocols         |a reference to one of the supported protocols, i.e., JDBC, SOAP, REST, or FILE
|YADA markup       |The modification of the query parameter symbol to indicate the datatype of the parameter in the query itself, e.g., `?v, ?i, ?d, ?n`

# YADA Services


Most users coming to this page just want data, and probably want it from an existing YADA app.  Getting data from these sources is simple, and in fact, is precisely the way data is delivered to the single-page JavaScript application using YADA under the hood.

## Curl Example


From the linux command-line, or any system call, a user can request any data simply using a known query, with standard parameters.  For example, the following query will return the Oracle SYSDATE of the YADA server in a standard YADA JSON string.

```bash
curl "http://example.com/yada.jsp?qname=YADA+default"
 
# returns
# {"RESULTSET":{"total": 1,"ROWS": [{"RNUM": "1","SYSDATE": "2014-10-27 17:34:43.0"}],"qname": "YADA default","page": "1","records": 1}}
 
# short params names are available to:
# curl "http://example.com/yada.jsp?q=YADA+default"
```

The JSON params version of the above query is

```bash
curl "http://example.com/yada.jsp?JSONParams=[{qname:YADA default,DATA:[{}]}]"
 
# returns
# {"RESULTSET": {"total": 1,"ROWS": [{"RNUM": "1","SYSDATE": "2014-10-27 17:34:43.0"}],"qname": "YADA default","page": "1","records": 1}}
 
# using short param names:
# curl "http://example.com/yada.jsp?j=[{qname:YADA default,DATA:[{}]}]"
```

# Query Authoring


Some users are authoring their own queries to access configured data sources. These queries, especially for JDBC, are bascially SQL queries with some minimal added markup.  Basic knowledge of the source database schemas, REST or SOAP API, or Filesystem, is required—just like in any other query-authoring scenario.

## YADA Markup

All YADA queries with value parameters require YADA markup, whether they are processed as positional parameters, or transmitted as name/value pairs in JSONParams strings. At a minimum, for example, for REST queries which always take Strings as values, probably only require `?v` for parameter placeholders. See [JDBC queries with datatypes] below, for more details.

## Parameterized Queries

### Positional Parameters

YADA supports positional parameters for all supported protocols: JDBC, REST, SOAP, and FILE.  Given that you're reading this, you probably understand what positional parameters are and how to use them, so we won't go into too much detail.  
Say you want to get a users id number from your database using first and last name as search criteria.  You could write the following query: 

```sql
SELECT ID FROM MY_TABLE WHERE FIRST_NAME = 'Joe' AND LAST_NAME = 'Cool';
```

This will return Joe Cool's id number.  But what if you want to enable users of a webapp to search using their own names?  You have to write the query so the values 'Joe' and 'Cool' can be substituted with a user's own criteria.  The way to do this is to parametirize the query.  The standard syntax for a query parameter is a question mark: ? .  See YADA Markup at the bottom of this subsection for how to markup this query with a datatype indicator, to make it YADA compatible.

```sql
SELECT ID FROM MY_TABLE WHERE FIRST_NAME = ? AND LAST_NAME = ?;
```

Internally, when a parameterized query is processed, it is typically embedded in some kind of object such as a [java.sql.PreparedStatement](http://docs.oracle.com/javase/7/docs/api/java/sql/PreparedStatement.html), which in addition to housing the statement, also offers methods for setting parameter values, such as [setString(int index, String value)](http://docs.oracle.com/javase/7/docs/api/java/sql/PreparedStatement.html#setString(int,%20java.lang.String)). Java, .NET, Perl, Python, and other platforms all offer similar methods.  YADA uses JAVA internally to apply values to the appropriate parameters.  

When YADA [standard parameters] are in use, positional parameters correspond to the order in which they are conveyed in the request url, for example:

```
# parameter 1: Joe
# parameter 2: Cool
# Request:
http://example.com/yada.jsp?qname=my+named+query&params=Joe,Cool

# The following statement is not equivalent to the former:
http://example.com/yada.jsp?qname=my+name+query&params=Cool,Joe
```

When JSON parameters are in use, the values to plug in to the query are associated to column names, so order is not significant:

```
# The following statements are equivalent
http://example.com/yada.jsp?JSONparams=[{qname:my named query,DATA[{FIRST_NAME:Joe,LAST_NAME:Cool}]}]
http://example.com/yada.jsp?JSONparams=[{qname:my named query,DATA[{LAST_NAME:Cool,FIRST_NAME:Joe}]}]
```

### YADA Markup

As noted above, all YADA queries require parameter symbols (?) to be accompanied by YADA markup datatype indicators.  Thus the example query above should be written as follows:
```sql
SELECT ID FROM MY_TABLE WHERE FIRST_NAME = ?v AND LAST_NAME = ?v;
```

See [JDBC queries with datatypes] below, for more details.

### JDBC Queries with Datatypes

Parameterized JDBC queries require data typing, because values correspond to database columns which themselves are typed. YADA currently supports four datatypes for parameterized JDBC queries:  *character*, *integer*, *number* (floating point), and *date*.  As noted above, when writing an SQL query to be executed via JDBC, the question mark character ( ? ) is used as a positional parameter marker, and values and data types are "plugged in" to these positions programmatically.  With YADA, there must be a shortcut system to convey not only the position of parameters, but also their datatypes, simultaneously.  This is achieved for all protocols using the designated markup:

|Markup|Definition           |Description                  |
|:----:|:--------------------|:----------------------------|
|?v    |VARCHAR,VARCHAR2,CHAR|Any character-based data type|
|?i    |INTEGER,INT          |Any integer data type        |
|?n    |NUMBER,DECIMAL       |Any floating point data type |
|?d    |DATE                 |Any date data type           |


## SQL (JDBC)

Virtually any SQL statement is supported by YADA.  Again, if the statement is not ANSI-compliant, or otherwise un-parseable, the YADA adaptor will fall back to the use of positional parameters.

An example:

```sql
select * mytable where col1 = ?v where col2 = ?i
```

Calling the above query from a curl call would look like the following,assuming the query is stored with name `EXMPL example`, and with the string, `abc`, in the first params position, and an integer, `123`, in the second

```
curl "http://example.com/yada.jsp?qname=EXMPL+example&params=abc,123"
```

## REST Example

REST queries behave like proxied queries to third party services.  In fact, if you're writing a javascript application, YADA is a great single-origin-policy workaround. Even if this isn't an issue, YADA enables the aggregation of multiple queries in a single http request, even from multiple sources, and the stacking or harmonization of results. REST queries look just like any other YADA query when being called.  Here's an example of a EBI QuickGO query:

```
http://example.com/yada.jsp?q=QGO%20search&params=0005515
```

Here's an example from TODO example:

```
http://example.com/yada.jsp?example
```

These are the results of the example query, below.  Note, by default, results are wrapped in the standard YADA JSON object structure:

```
{"RESULTSET": {"ROWS": [
```

It is also possible to use one of the built in Response classes to simply pass through the REST result as is.  The new query, with the 'r=RESTPassThruResponse' parameter:

```
http://example.com/yada.jsp?q=N&p=&r=RESTPassThruResponse
```

And the result:

```
{
```

# The `YADAQuery` class
 

# Client-side Development

For UI developers, most commonly, YADA will be called from jQuery plugins or similar ilk.

## AJAX Example


# Tips & Tricks

## Multiple Oracle Sequence Values

[Beginners' Guide/FAQ]: faq.html
[Getting into the YADA Mindset]: readme.html#mindset
[JSONParams Specification]: jsonparams.html
[Filter Specification]: filters.html
[HarmonyMap Specification]: harmonymap.html
[Mail Specification]: mail.html
[Plugin Guide]: plugins.html

[standard parameters]: params.html#params
[format]: params.html#format
[delimiter]: params.html#delimiter
[bypassargs]: params.html#bypassargs

[Go to top]: #top
[Using YADA]: #using
[YADA Requests]: #requests
[JDBC queries with datatypes]:




[Javadoc]: 
[TestNG Results]: