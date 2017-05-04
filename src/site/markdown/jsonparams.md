# JSONParams Specification

<div style="float:right;margin-top:-43px;">
    <img src="../resources/images/blox250.png"/>
</div>


The JSONParams object is a JSON object adherent to the following specific format. Due to the use of square brackets `[` and `]` in JSON notation, parentheses `(` and `)` are used to denote optional content.

## Requirements

### JSONParams array
The parameter value itself, a JSON array containing at least one query object

```javascript
[ query (, query) ]
```

### query
A JSON object containing `qname` and `DATA` keys and values.

The value associated to `qname` must be a YADA query name

The value associated to `DATA` must be an array of at least one data object

```javascript
{
  "qname" : "string",
  "DATA"  : [ data (, data) ]
}
```

### data

An object containing name-value pairs corresponding to the parameter names referenced by your query.

Parameter names are often JDBC column names, though they could be sequential dynamic names referenced by REST queries.

```javascript
{ "column_1_name" : "value_1" (, "column_n_name" : "value_n") }
```

### Special Case for RESTAdaptor queries
When issuing requests for REST endpoints using `com.novartis.opensource.yada.adaptor.RESTAdaptor` with HTTP `POST`,`PUT`, or `PATCH` methods, content must be included in the request body. To pass this content, use the `YADA_PAYLOAD` key in your data object:

```javascript
{ "YADA_PAYLOAD": "body_content" }
```

## Examples
Here is an javascript/jQuery® example of using a JSONParams object used to pass data to a JDBC statement:

```javascript
// execute query 'MYAPP insertIntoColumn' with single "row" of data containing
// one column named 'COLUMN'
var jp = [
           {
             "qname" : "MYAPP insertIntoColumn",
             "DATA"  : [ { "COLUMN" : "value" } ]
           }
         ];

$.ajax({
         url: YADA_SERVER,
         type:'POST',
         data: {
                 JSONParams : JSON.stringify(jp)
               },
         success: function(data) { ... }
         ...
       }
);

// execute the following REST GET request with one query parameter
//   http://example.com/rest/api/get/?v


var jp = [
           {
             "qname" : "REST getrequest",
             "DATA"  : [ { "YADA_1" : "value" } ]
           }
         ];

$.ajax({
         url: YADA_SERVER,
         type:'POST',
         data: {
                 JSONParams : JSON.stringify(jp)
               },
         success: function(data) { ... }
         ...
       }
);

// execute the following REST POST request with one query parameter and body
// content
//   http://example.com/rest/api/get/?v

// note the type:POST is for the ajax call to the YADA server, which is not
// passed to the REST endpoint.  That http method call is in the 'data' object

var jp = [
           {
             "qname" : "REST postrequest",
             "DATA"  : [ { "YADA_1" : "value", "YADA_PAYLOAD":"field2=value2&field3=value3"} ]
           }
         ];

$.ajax({
         url: YADA_SERVER,
         type:'POST',
         data: {
                 method : "POST",
                 JSONParams : JSON.stringify(jp)
               },
         success: function(data) { ... }
         ...
       }
);

// execute the following REST PUT request with one query parameter and body
// content
//   http://example.com/rest/api/get/?v

// note the type:POST is for the ajax call to the YADA server, which is not
// passed to the REST endpoint.  That http method call is in the 'data' object
// also note the inclusion of custom http headers in the data object, to enable
// passing of JSON to the endpoint in the body content

var jp = [
           {
             "qname" : "REST putrequest",
             "DATA"  : [ { "YADA_1" : "value",
                           "YADA_PAYLOAD":'{"field2":"value2","field3":"value3"}'} ]
           }
         ];

$.ajax({
         url: YADA_SERVER,
         type:'POST',
         data: {
                 method : "PUT",
                 httpHeaders: JSON.stringify({"Content-Type":"application/json"}),
                 JSONParams : JSON.stringify(jp)
               },
         success: function(data) { ... }
         ...
       }
);
```


## Future expansion

### Optional keys

It should be possible to include additional query-level parameters in each query object. For example, a query-specific plugin, a converter class, or a filter:


```javascript
{
  "qname"   : "string",
  "DATA"    : [ data (, data) ]
  ("qparams" : { qparam (, qparam) })
}
```
It should maybe also be possible to include additional request-level parameters:

```javascript
[ query (, query...) (, reqparam) ]

// e.g., [ { "qname": "string", "DATA": [ data (, data) ] }, (param (,param...)) ]
```

This functionality is likely easy to implement (and may already work,) but is totally untested.
