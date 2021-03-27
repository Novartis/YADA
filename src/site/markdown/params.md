#YADA Parameters  

<div style="float:right;margin-top:-43px;">
    <img src="../resources/images/blox250.png"/>
</div>


## Table of Contents

* [Alphabetical Parameter List](#params)
* [Categorical Parameter List](#params_by_cat)
* [Reference](#ref)

<a name="params"></a>  
## Alphabetical Parameter List  

- [colHead] *(provisional)*
- [commitQuery]  
- [compact] *(provisional)*
- [converter]  
- [cookies]
- [count]  
- [countOnly]  
- [delimiter]  
- [export]  
- [exportlimit]  
- [filters]  
- [format]  
- [harmonyMap]  
- [httpHeaders]
- [join]
- [JSONParams]  
- [leftJoin]
- [labels] *(provisional)*
- [mail]  
- [method]
- [pagesize]  
- [pagestart]  
- [params]  
- [paramset] *(provisional)*
- [path] *(internal use only)*
- [plugin]  
- [pretty]  
- [protocol] *(internal use only)*
- [proxy]  
- [qname]  
- [response]  
- [rowDelimiter]  
- [sortkey]  
- [updateStats]
- [uploadItems] *(internal use only)*
- [user]  
- [viewlimit]  

<a name="params_by_cat"></a>
## Categorical Parameter List


### Query Configuration Parameters

- [compact] *(provisional)*
- [cookies]
- [count]
- [countOnly]
- [method]
- [httpHeaders]
- [JSONParams]
- [params]
- [paramset] *(provisional)*
- [path] *(internal use only)*
- [proxy]
- [qname]
- [user]

### Query Processing Parameters

- [commitQuery]
- [protocol] *(internal use only)*
- [updateStats]
- [uploadItems] *(internal use only)*

### Result Limiting/Extending Parameters

- [exportlimit]
- [filters]
- [harmonyMap]
- [join]
- [leftJoin]
- [pagesize]
- [pagestart]
- [viewlimit]

### Result Formatting Parameters

- [colHead] *(provisional)*
- [converter]
- [delimiter]
- [export]
- [format]
- [labels] *(internal use only)*
- [pretty]
- [response]
- [rowDelimiter]
- [sortkey]

### Plugin Parameters

- [mail]
- [plugin]

### Deprecated Parameters

- [args]
- [bypassargs]
- [overargs]
- [parallel]
- [plugintype]
- [postargs]
- [preargs]
- [sortorder]

<a name="ref"></a>
## Reference

<a name="args"></a>  


**Name _(deprecated)_**: `args`  
**Alias _(deprecated)_**: `a`  
**Required _(deprecated)_**: No  
**Default _(deprecated)_**: empty list  
**Accepted Values _(deprecated)_**: comma-delimited list of strings  
**Description _(deprecated)_**: **Deprecated**. When accompanied by a single `plugin` or `pl` parameter, used as the default argument list, per below.  
A comma-separated list of values to be passed to the plugin designated in the `plugin` or `pl` parameter value. The plugin should be expecting these values as positional arguments, i.e.,`arg1,arg2,...argN`. When using script plugins, the first argument must contain the path to the registered script (relative to the `yada.bin` directory,) i.e., `myscript.pl,arg1,arg2,...argN`. See the [Plugin Guide] for more info.  

<a name="bypassargs"></a>

----  

**Name _(deprecated)_**: `bypassargs`  
**Alias _(deprecated)_**: `b`  
**Required _(deprecated)_**: No  
**Default _(deprecated)_**: empty list  
**Accepted Values _(deprecated)_**: comma-delimited list of strings  
**Description _(deprecated)_**: **Deprecated**.A comma-separated list of values to be passed to the plugin designated in the `plugin` or `pl` parameter value. The plugin should be expecting these values as positional arguments, i.e., `arg1,arg2,...argN`. When using script plugins, the first argument must contain the path to the registered script (relative to the `yada.bin` directory,) i.e., `myscript.pl,arg1,arg2,...argN`. See the [Plugin Guide] for more info.  

<a name="colHead"></a>  

----

**Name**: `colHead`  
**Alias**:   
**Required**: No  
**Default**: `false`  
**Accepted Values**: `true`, `false`  
**Description**: _**PROVISIONAL**_  

<a name="commitQuery"></a>  

----

**Name**: `commitQuery`  
**Alias**: `cq`  
**Required**: No  
**Default**: `false`  
**Accepted Values**: `true`, `false`  
**Description**: By default, YADA will execute a single commit per connection per request. In other words, if multiple queries for a single data source are passed in a single http request to the YADA server, the `QueryManager` will execute a single commit for each of these query's connections after all queries are processed. If queries for multiple data sources are included, a single commit for each source will be executed after all queries for all sources are executed. Setting this parameter to `true` will cause YADA to issue a commit after each individual query.  

<a name="compact"></a>  

----

**Name**: `compact`  
**Alias**:  
**Required**: No  
**Default**: `false`  
**Accepted Values**: `true`, `false`    
**Description**: _**PROVISIONAL**_  

<a name="converter"></a>  

----

**Name**: `converter`  
**Alias**: `cv`  
**Required**: No  
**Default**: computed  
**Accepted Values**: Name or FQCN of converter class  
**Description**:   

<a name="cookies"></a>  

----

**Name**: `cookies`  
**Alias**: `ck`  
**Required**: No  
**Default**: null  
**Accepted Values**: Comma-delimited list of Strings  
**Description**:  **FOR REST REQUESTS ONLY** A list of cookie names present in the client that should be included in the request sent to the REST source.
<a name="count"></a>  

----

**Name**: `count`  
**Alias**: `c`  
**Required**: No   
**Default**: true    
**Accepted Values**: `true`, `false`  
**Description**: When `true`, YADA executes the query twice, the first time to retrieve the first page of records, and the second, to get the total record count, returning this value in the result JSON `records` key or XML attribute. This is useful for tables which, for example, can display `records 1-10 of 50` in the footer. When `false`, the second query is not executed. This enables better performance for drop down list population, or data analysts who simply want the data as fast as possible.  

<a name="countOnly"></a>  

----

**Name**: `countOnly`  
**Alias**: `co`  
**Required**: No  
**Default**: false  
**Accepted Values**: `true`, `false`  
**Description**: When `true`, skips the data retrieval query and only returns the record count.  

<a name="delimiter"></a>  

----

**Name**: `delimiter`
**Alias**: `d`  
**Required**: No  
**Default**: `,` (comma)  
**Accepted Values**: any string  
**Description**: The character or sequence of characters used to separate data fields in a delimited text file format, as a comma in CSV, or a tab in TSV. Note that comma, tab, and pipe-delimiters are supported natively using the format parameter. Use of this parameter will cause format to be set automatically to delimited. See [format].  

<a name="export"></a>  

----

**Name**: `export`  
**Alias**: `e`  
**Required**: No  
**Default**: `false`  
**Accepted Values**: `true`, `false`  
**Decription**: When `true`, outputs the result as a file in `/files/out/<user>/<YYYYHHMMhhmmss>.<ext>` and returns the URL in the response with an `HTTP 201` response code and the URL in the `Location` header. This was created to be used in javascript applications with ajax calls, having success handlers that proceed to retrieve exported files using `window.location.href`.  There are likely other uses as well.  

<a name="exportlimit"></a>  

----

**Name**: `exportlimit`  
**Alias**: `el`  
**Required**: No  
**Default**: `-1`  
**Accepted Values**: any positive integer  
**Description**:   

<a name="filters"></a>  

----

**Name**: `filters`  
**Alias**: `fi`  
**Required**: No  
**Default**: null  
**Accepted Values**: JSON string conforming to [Filter Specification]  
**Description**:   

<a name="format"></a>  

----

**Name**: `format`  
**Alias**: `f`  
**Required**: No  
**Default**: `json`  
**Accepted Values**: `json`, `csv`, `tsv` or `tab`, `psv` or `pipe`, `xml`, `html`, `delimited`  
**Description**: Retrieve results in JSON, XML, rudimentary HTML, or tab-, csv-, or pipe-delimited files.  
Custom delimiters are now supported, which automatically set the format to `delimited`. See [delimiter]  

<a name="harmonyMap"></a>  

----

**Name**: `harmonyMap`  
**Alias**: `h`  
**Required**: No  
**Default**: null  
**Accepted Values**: JSON string conforming to [Harmonizer Specification]    
**Description**: A JSON string pairing source-result field names or paths to response field names or paths. **NOTE** See [Harmonizer Specification]  

<a name="httpHeaders"></a>  

----

**Name**: `httpHeaders`  
**Alias**: `H`   
**Required**: No  
**Default**: null  
**Accepted Values**: JSON string or comma-separated list of values  
**Description**: `httpHeaders` can be included in three formats, each of which results in the setting of designated HTTP headers in requests constructed and executed by the `com.novartis.opensource.yada.adaptor.RESTAdaptor`.  The three options are:
1. A comma-separated list of header names:  
  This will result in the inclusion of designated headers and their values from the original YADA request.
2. A JSON string representing a single JSON object in which keys are header names and values are header values:
  This will result in the inclusion of designated headers and values in the subsequent REST request.
3. A JSON string representing a single JSON object in which keys are header names and values are boolean `true`:  
  This will result in the inclusion of designated headers and corresponding values from the original YADA request in the subsequent REST request.  
  **NOTE:** Methods 2 and 3 above may be combined. For example an JSON string could contain both a custom header key:value pair as well as a boolean key:true pass-thru. E.g.,  

   ```H={"Content-Type":"application/json","X-Authorized-User":"true"}```

<a name="join"></a>  

----

**Name**: `join`  
**Alias**: `ij`  
**Required**: No  
**Default**: null  
**Accepted Values**: `true` or comma-separated list of values  
**Description**: YADA uses an in-memory database to create tables containing the results of a multi-query request on-the-fly, then constructs and executes a `SELECT...JOIN` query and returns the results in the desired format. When set to `true`, the `Joiner` class will evaluate all the converted headers and base the in-memory db configuration and join query on matching column names in the results. When set to a comma-delimited string, YADA will construct the query only using the designated columns.

**Note**: Currently, the original queries must be JDBC `ResultSet` objects, as the `Converter` classes `ResultSetResultJSONConverter` and `ResultSetResultDelimitedConverter` are the only ones storing converted header and data values utiziled by the `Joiner` class.


<a name="JSONParams"></a>  

----

**Name**: `JSONParams`  
**Alias**: `j`  
**Required**: Yes, in the absence of `qname` or `q`, otherwise no  
**Default**: null  
**Accepted Values**: JSON string conforming to [JSONParams Specification]  
**Description**: See [JSONParams Specification]  

<a name="labels"></a>  

----

**Name**: `labels`  
**Alias**:  
**Required**: No   
**Default**: `true`  
**Accepted Values**: `true`, `false`  
**Description**:  _**PROVISIONAL**_

<a name="leftJoin"></a>  

----

**Name**: `leftJoin`  
**Alias**: `lj`  
**Required**: No  
**Default**: null  
**Accepted Values**: `true` or comma-separated list of values  
**Description**: YADA uses an in-memory database to create tables containing the results of a multi-query request on-the-fly, then constructs and executes a `SELECT...LEFT JOIN` query and returns the results in the desired format. When set to `true`, the `Joiner` class will evaluate all the converted headers and base the in-memory db configuration and join query on matching column names in the results. When set to a comma-delimited string, YADA will construct the query only using the designated columns.

**Note**: Currently, the original queries must be JDBC `ResultSet` objects, as the `Converter` classes `ResultSetResultJSONConverter` and `ResultSetResultDelimitedConverter` are the only ones storing converted header and data values utiziled by the `Joiner` class.


----

**Name**: `mail`  
**Alias**:  
**Required**:  
**Default**: null  
**Accepted Values**: JSON string conforming to Mail Specification  
**Description**: See [Mail Specification]  

<a name="method"></a>  

----

**Name**: `method`  
**Alias_**: `m`  
**Required**: No  
**Default**: `GET`	  
**Accepted Values**: `GET`,`POST`,`PUT`,`PATH`,`DELETE`,`OPTIONS`,`HEAD`,`update`,`upload`  
**Description**: `method` is used by the `com.novartis.opensource.yada.adaptor.RESTAdaptor` to execute the designated HTTP method at the endpoint.  

For example, if a REST API is accessed through YADA, YADA is effectively a proxy. If the REST endpoint's API requires a `POST` to create a resource, then setting `method=POST`, `m=POST`,  is what enables YADA to forward the request to the endpoint with the proper method. Similary if the endpoint requires a `PUT` to modify a resource, use `method=PUT` or `m=PUT`.

Note that `POST`, `PUT`, and `PATCH` each expect or require HTTP request body content. To include this content in the YADA request one is required to use [JSONParams] with a `YADA_PAYLOAD` key in each object in the `DATA` array of each query object.  See the [JSONParams Specification] for more information.

Often, use of a REST endpoint requires custom headers as well, for example, one must freqeuntly include `Content-Type: application/json`.  For more information about this aspect of REST processing, see [httpHeaders].

Note: `method` was originally created to distinguish between requests to retrieve data from requests to insert, update, or delete it, as well as from multipart form file uploads. Then it was deprecated for a few versions. The original documentation follows in italics. This still applies to non-REST requests.  

_Identifies for the `com.novartis.opensource.yada.Service` object which JDBC method to execute. `get`, the default, performs a `SELECT`. `update` performs any SQL data change action (i.e., `update`, `delete`, `insert`, transactionally or not, using JDBC). `upload` is explicit to file uploads from a ui to the server. `REST` and `SOAP` play by different rules._

<a name="overargs"></a>  

----

**Name _(deprecated)_**: `overargs`  
**Alias _(deprecated)_**: `o`  
**Required _(deprecated)_**: No  
**Default _(deprecated)_**: empty list  
**Accepted Values _(deprecated)_**: comma-delimited list of strings  
**Description _(deprecated)_**: **Deprecated**. See [bypassargs]  

<a name="pagesize"></a>  

----

**Name**: `pagesize`  
**Alias**: `pz`  
**Required**: No  
**Default**: 20  
**Accepted Values**: any positive integer  
**Description**: The number of records to return when implementing pagination.  Set `pagesize=-1` to retrieve all results (up to 1 billion rows).  

<a name="pagestart"></a>  

----

**Name**: `pagestart`  
**Alias**: `page, pg`  
**Required**: No  
**Default**: 1  
**Accepted Values**: any positive integer  
**Description**: The current page of data returned, e.g., if `pagesize=20`, `page=2` would return records 21-40.  

<a name="parallel"></a>  

----

**Name _(deprecated)_**: `parallel`  
**Alias _(deprecated)_**:   
**Required _(deprecated)_**: No  
**Default _(deprecated)_**: `false`  
**Accepted Values _(deprecated)_**: `true`, `false`  
**Description _(deprecated)_**: **Deprecated**.  

<a name="params"></a>  

----

**Name**: `params`  
**Alias**: `p`  
**Required**: No  
**Default**: null  
**Accepted Values**: comma-delimited list of strings  
**Description**: The values corresponding to positional parameters in the YADA query, in a comma separated list.  

<a name="paramset"></a>  

----

**Name**: `paramset`  
**Alias**: `ps`  
**Required**: No  
**Default**: null  
**Accepted Values**: comma-delimited list of strings   
**Description**: **PROVISIONAL** a named set of url parameters and values stored in the YADA Index, just like queries   

<a name="path"></a>  

----

**Name**: `path`  
**Alias**:  
**Required**: No  
**Default**: null  
**Accepted Values**:  
**Description**:	 _**For internal use only**_ Currently used by `Service` object when processing uploads.

<a name="plugin"></a>  

----

**Name**: `plugin`  
**Alias**: `pl`  
**Required**: No  
**Default**: empty string  
**Accepted Values**: Name or FQCN of plugin class  
**Description**: For java plugins that are deployed in the YADA server, in the `com.novartis.opensource.yada.plugin` package, the plugin value is the either the classname, or the fully qualified class name (FQCN) e.g, `XSLPostprocessor` or `com.novartis.opensource.yada.XSLPostprocesor`.  For script plugins, one can use the `preargs`, `postargs`, and `bypassargs` url parameters to pass the script path. YADA Service will automatically engage the correct script plugin helper, e.g., `com.novartis.opensource.yada.plugin.ScriptPreProcessor`. See `preargs` for more info. See the [Plugin Guide] for more information.  

<a name="plugintype"></a>  

----

**Name _(deprecated)_**: `plugintype`  
**Alias  _(deprecated)_**: `pt`  
**Required  _(deprecated)_**: No  
**Default  _(deprecated)_**: `PreProcess`  
**Accepted Values  _(deprecated)_**: `Preprocess`, `preprocess`, `Postprocess`, `postprocess`, `Override`, `override`   
**Description  _(deprecated)_**: **Deprecated**. Plugin Type is autodetected via implemented interface.  

<a name="postargs"></a>  

----

**Name _(Deprecated)_**: `postargs`  
**Alias _(Deprecated)_**: `pa`  
**Required _(Deprecated)_**: No  
**Default _(Deprecated)_**: empty list	  
**Accepted Values _(Deprecated)_**: comma-delimited list of strings  
**Description _(Deprecated)_**: **Deprecated**. A comma-separated list of values to be passed to the plugin designated in the `plugin` or `pl` parameter value. The plugin should be expecting these values as positional arguments, i.e., `arg1,arg2,...argN`. When using script plugins, the first argument must contain the path to the registered script (relative to the `yada.bin` directory,) i.e., `myscript.pl,arg1,arg2,...argN`. See the [Plugin Guide] for more info.  

<a name="preargs"></a>  

----

**Name _(Deprecated)_**: `preargs`  
**Alias _(Deprecated)_**: `pr`  
**Required _(Deprecated)_**: No  
**Default _(Deprecated)_**: empty list	  
**Accepted Values _(Deprecated)_**: comma-delimited list of strings  
**Description _(Deprecated)_**: A comma-separated list of values to be passed to the plugin designated in the `plugin` or `pl` parameter value. The plugin should be expecting these values as positional arguments, i.e., `arg1,arg2,...argN`. When using script plugins, the first argument must contain the path to the registered script (relative to the `yada.bin` directory,) i.e., `myscript.pl,arg1,arg2,...argN`. See the [Plugin Guide] for more info.  

<a name="pretty"></a>  

----

**Name**: `pretty`  
**Alias**: `py`  
**Required**: No  
**Default**: `false`  
**Accepted Values**: `true`, `false`  
**Description**: "Pretty prints" JSON output, i.e., formats JSON output with 2-character indents and line feeds  

<a name="protocol"></a>  

----

**Name**: `protocol`  
**Alias**: `pc `  
**Required**: No  
**Default**: `Parser.JDBC`  
**Accepted Values**: `Parser.JDBC`, `Parser.REST`, `Parser.SOAP`, `Parser.FILE`   
**Description**: **_For internal use only_**.  This parameter is set internally to facilitate processing. It is only mentioned here as it is conveyed using the same `YADARequest` object in which other parameter values are stored, and may be noticed in the Java® API.  

<a name="proxy"></a>  

----

**Name**: `proxy`  
**Alias**: `px`  
**Required**: No  
**Default**: null  
**Accepted Values**: host:port  
**Description**: A string containing the HTTP proxy and port in the format host:port. This is used only for REST queries which are hosted at otherwise inaccessible sites. For this to work, you may need to set the `Java®_OPTS`p for proxies, `-Dhttp.proxyHost=...` and `-Dhttp.proxyPort=...` in `$TOMCAT_HOME/bin/setenv.sh`

<a name="qname"></a>  

----

**Name**: `qname`  
**Alias**: `q`  
**Required**: Yes, in the absence of `JSONParams` or `j`, otherwise, no  
**Default**: `YADA Dummy`  
**Accepted Values**: any registered query  
**Description**:   

<a name="response"></a>  

----

**Name**: `response`  
**Alias**: `r`  
**Required**: No  
**Default**: computed  
**Accepted Values**: Name or FQCN of response class   
**Description**:   

<a name="rowDelimiter"></a>  

----

**Name**: `rowDelimiter`  
**Alias**: `rd`  
**Required**: No  
**Default**: `\n`  
**Accepted Values**: any string  
**Description**: The character or sequence of characters used to separate rows of results, or records, in a delimited text file format, as a comma in CSV, or a tab in TSV. Note that comma, tab, and pipe-delimiters are supported natively using the format parameter.  

<a name="sortkey"></a>  

----

**Name**: `sortkey`  
**Alias**: `s`  
**Required**: No  
**Default**: null  
**Accepted Values**: any string  
**Description**: A list of columns or indices, including sort direction if not ascending, indicating order of results  

<a name="sortorder"></a>  

----

**Name _(deprecated)_**: `sortorder`  
**Alias _(deprecated)_**: `so`  
**Required _(deprecated)_**: No  
**Default _(deprecated)_**: `asc`  
**Accepted Values _(deprecated)_**: **Deprecated**. `asc`, `desc`  
**Description**:   

<a name="updateStats"></a>  

----

**Name**: `updateStats`  
**Alias**: `us`  
**Required**: No  
**Default**: `true`  
**Accepted Values**: `true`, `false`  
**Description**: When `true`, `Finder` will execute an additional JDBC query, `updateQueryStatistics`, in a separate thread, on each request, to increment the access count for the requested query, and record the current timestamp in as the LAST_ACCESS value. This additional database transaction can cause problems for SQLite®, and is suppressable using this parameter.

<a name="uploadItems"></a>  

----

**Name**: `uploadItems`  
**Alias**:   
**Required**: No  
**Default**: null  
**Accepted Values**:  
**Description**: **_For internal use only_** This parameter is set internally when a request is detected to contain multipart content.  

<a name="user"></a>  

----

**Name**: `user`  
**Alias**: `u`  
**Required**: No  
**Default**: `YADABOT`  
**Accepted Values**: any String  
**Description**:   



----

**Name**: `viewlimit`  
**Alias**: `vl`  
**Required**: No  
**Default**: `-1`  
**Accepted Values**: any positive integer  
**Description**: For use in user interfaces when a "View All" selection (vs paginated) would still return too many rows.

[JSONParams Specification]: jsonparams.md
[Harmonizer Specification]: harmony.md
[Plugin Guide]: pluginguide.md
[Filter Specification]: filters.md
[Mail Specification]: mail.md

[args]: #args
[bypassargs]: #bypassargs
[colHead]: #colHead
[commitQuery]: #commitQuery
[compact]: #compact
[converter]: #converter
[cookies]: #cookies
[count]: #count
[countOnly]: #countOnly
[delimiter]: #delimiter
[export]: #export
[exportlimit]: #exportlimit
[filters]: #filters
[format]: #format
[harmonyMap]: #harmonyMap
[httpHeaders]: #httpHeaders
[join]: #join
[JSONParams]: #JSONParams
[labels]: #labels
[leftJoin]: #leftJoin
[mail]: #mail
[method]: #method
[pagesize]: #pagesize
[pagestart]: #pagestart
[parallel]: #parallel
[params]: #params
[paramset]: #paramset
[path]: #path
[plugin]: #plugin
[postargs]: #postargs
[preargs]: #preargs
[pretty]: #pretty
[protocol]: #protocol
[proxy]: #proxy
[qname]: #qname
[response]: #response
[rowDelimiter]: #rowDelimiter
[sortkey]: #sortkey
[sortorder]: #sortorder
[updateStats]: #updateStats
[uploadItems]: #uploadItems
[user]: #user
[viewlimit]: #viewlimit  
