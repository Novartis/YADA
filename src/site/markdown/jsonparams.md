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
A JSON object containg qname and DATA keys and values. 

The value associated to "qname" must be a YADA query name

The value assicated to "DATA" must be an array of at least one data object

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

## Examples
Here is an javascript/jQuery example of using a JSONParams object used to pass data to a JDBC statement:

```javascript
// prepare the data for the Query to verify CLNumbers
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

