# Harmonizer Guide & Specification

<div style="float:right;margin-top:-43px;">
    <img src="../resources/images/blox250.png"/>
</div> 


Harmonizer enables on-the-fly renaming of columns or keys in results returned from a YADA Request. 

Some uses of harmonizer:

* Aggregate results of multiple queries in a single request into a single result set with uniform column names or keys
* Reflow or transform JSON results
* Flatten JSON into delimited formats
* Prune or omit unwanted keys or columns

For SQL-nerds: think of it like an SQL [UNION ALL](http://stackoverflow.com/questions/49925/what-is-the-difference-between-union-and-union-all) statement combined with an SQL [LEFT JOIN]() statement that works on JSON as well as delimited formats.

In sum, Harmonizer will "harmonize" multiple results into a single result set.  

Due to the use of square brackets `[` and `]` in JSON notation, parentheses `(` and `)` are used to denote optional content.

See the [examples] below the specification.


The harmonyMap is a JSON object containing a set of key:value pairs.  Again, parentheses `(` and `)` are used to denote optional content.

## Usage 

### Embedded

The harmonyMap spec can be included in one or more of the elements of a JSONParams array value. When embedded, YADA will account for omitted harmonyMap specs in other included JSONParams elements, as well as formulate a global harmonyMap comprised of non-redundant key:value pairs from all JSONParams elements. See the [examples] for more information.

### Global

The harmonyMap spec can be includud as a top-level url parameter.

## Specification

### Base 

```javascript
{
  key:value (…, key:value) (, prune)
}
```

### key:value

```javascript
  key : value 
```
### key

|Variant|Accepted Values|Description/Usage|  
|---|---|-----|----------------------|
|String|`String`|**Description**: A single contiguous character sequence representing a column or key name in the original result<br/>**Use**: to reference single column in a tabular result; to reference a simple key or path element in a JSON result|
|Path|`String.String(.String…)`|**Description**: A sequence of two or more Strings separated by `.` (dot) characters, indicating descent down a path of nested JSON objects<br/>**Use**: to map a nested value from a JSON object to a new key or column|  
|Array|`String|Path.[*](.…)`|**Description**: A sequence beginning with a String or Path, followed by a `.` (dot) character, followed by opening and closing square brackets `[` and `]` containing an asterisk `*`.<br/>**Use**: to map all elements of a JSON array to a new key or column|
|Slice|`String|Path.[lo-hi](.…)`|**Description**: A sequence beginning with a String or Path, followed by a `.` (dot) character, followed by opening and closing square brackets `[` and `]` containing a number representing the lower bound (lo) and a number representing an upper bound (hi) separated by a hyphen `-`. *Note:*  As in javascript's `slice` function, the upper bound in exclusive. Unlike `slice` the upper bound is required and must be a positive integer.<br/>**Use**: to map a slice of a nested JSON array to a new key or column|
|Select|`String|Path.[n(,n…)](.…)`|**Description**: A sequence beginning with a String or Path, followed by a `.` (dot) character, followed by opening and closing square brackets `[` and `]` containing a comma-delimited list of numbers representing the desired indices of JSON array.<br/>**Use**: to map individual elements of a nested JSON array to a new key or column|

### value
|Variant|Accepted Values|Description/Usage|  
|---|---|-----|----------------------|
|String|`String`|**Description**: A single contiguous character sequence representing a column or key name in the transformed result<br/>**Use**: to map a source value to a single column simple top-level JSON key|
|Path|`String.String(.String…)`|**Description**: A sequence of two or more Strings separated by `.` (dot) characters, indicating descent down a path of nested JSON objects<br/>**Use**: to map a source value to a a single column or nested JSON object|  

### prune
```javascript
  "prune" : true
```

Include this key:value pair in the harmonyMap JSON string to omit undesired output. The pair may appear anywhere in the harmonyMap, i.e., it may be appear first, last, or in the midst of other pairs in the harmonyMap JSON string. The value associated to `prune` **must be** the `boolean` `true`

<a name="examples"></a>
## Examples

### db query with defaults
```javascript
// base query
http://localhost/yada.jsp?q=YADA test harmony map 1

// default JSON result
{"RESULTSET":
  {"total":8,
    "ROWS":[
    {"col3":"7.5","col2":"10","col1":"Z"},
    {"col3":"7.5","col2":"10","col1":"Z,Z"},
    {"col3":"7.5","col2":"10","col1":"ZZ"},
    {"col3":"7.5","col2":"10","col1":"A"},
    {"col3":"8.5","col2":"10","col1":"Z"},
    {"col3":"8.5","col2":"10","col1":"A"},
    {"col3":"9.5","col2":"10","col1":"Z"},
    {"col3":"9.5","col2":"10","col1":"A"}
    ],
    "qname":"YADA test harmony map 1",
    "page":"1",
    "records":8},
  "version":"6.0.0"
}
```

### db query with harmonyMap parameter
```javascript
// query
http://localhost/yada.jsp?q=YADA test harmony map 1&h={"col1":"STRING","col2":"INT","col3":"FLOAT"}

// JSON result - note the base query was unchanged, 
// but with the harmonyMap (h) parameter, 
// the column names have been changed in the result
{"RESULTSET":
  {"total":8,
    "ROWS":[
    {"FLOAT":"7.5","INT":"10","STRING":"Z"},
    {"FLOAT":"7.5","INT":"10","STRING":"Z,Z"},
    {"FLOAT":"7.5","INT":"10","STRING":"ZZ"},
    {"FLOAT":"7.5","INT":"10","STRING":"A"},
    {"FLOAT":"8.5","INT":"10","STRING":"Z"},
    {"FLOAT":"8.5","INT":"10","STRING":"A"},
    {"FLOAT":"9.5","INT":"10","STRING":"Z"},
    {"FLOAT":"9.5","INT":"10","STRING":"A"}
    ],
    "qname":"YADA test harmony map 1",
    "page":"1",
    "records":8},
  "version":"6.0.0"
}
```


### db query with csv results
```javascript
// base query
http://localhost/yada.jsp?q=YADA test harmony map 1&f=csv

// csv result
"col1","col2","col3"
"Z","10","7.5"
"Z,Z","10","7.5"
"ZZ","10","7.5"
"A","10","7.5"
"Z","10","8.5"
"A","10","8.5"
"Z","10","9.5"
"A","10","9.5"
```

### db query with csv results and harmonyMap param
```javascript
// base query
http://localhost/yada.jsp?q=YADA test harmony map 1&f=csv&h={"col1":"STRING","col2":"INT","col3":"FLOAT"}

// csv result with mapped column names
"STRING","INT","FLOAT"
"Z","10","7.5"
"Z,Z","10","7.5"
"ZZ","10","7.5"
"A","10","7.5"
"Z","10","8.5"
"A","10","8.5"
"Z","10","9.5"
"A","10","9.5"

```

### public web service (REST) query
```javascript
// query to QuickGO: https://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0005515&format=json
http://localhost/yada.jsp?q=QGO%20search&p=GO:0005515

// result is enormous, here are first ~30 lines
{
  "termInfo": {
    "info": {
      "code": 4303,
      "id": "GO:0005515",
      "name": "protein binding",
      "ontology": "Function",
      "obsolete": false,
      "usage": {
        "code": "U",
        "text": "Unrestricted",
        "description": "This term may be used for any kind of annotation."
      }
    },
  "code": 5515,
  "parents": [
    {
      "child": {
        "id": "GO:0005515",
        "aspect": "Function",
        "definition": "Interacting selectively and non-covalently with any protein or protein complex (a complex of two or more proteins that may include other nonprotein molecules).",
        "obsolete": false,
        "name": "protein binding",
        "ancestors": [
          {
            "id": "GO:0005488",
            "name": "binding",
            "usage": "E"
          },
          …
          // result continues much further
```

### public web service (REST) query with harmonyMap
```javascript
// query to QuickGO: https://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0005515&format=json
http://localhost/yada.jsp?q=QGO%20search&p=GO:0005515&h={"prune":true,"termInfo.info.name":"name","termInfo.info.code":"code"}

// pruned, and harmonized result
// note the "keys" in the harmony map refer to nested objects at a 
// dot-delimited path.
{
  "RESULTSET": {
    "ROWS": [
      {
        "name": "protein binding",
        "code": 4303
      }
    ],
    "qname": "QGO search",
    "records": 1
  },
  "version": "6.1.0-SNAPSHOT"
}
```
### combined, harmonized & pruned db and web query in single request

This request and the result are obviously contrived, but they illustrate utility of dynamic "on-the-fly" query result aggregration and harmonization in a single http request.

```javascript
// request, broken onto multiple lines for clarity, but this is all one url:
http://localhost/yada.jsp?j=[{"qname":"YADA test harmony map 1",DATA:[{}],harmonyMap:
{"col1":"STRING","col2":"INT","col3":"FLOAT"}},{"qname":"QGO search",DATA:[{YADA_1:"GO:0005515"}],
harmonyMap:{"termInfo.info.name":"STRING","termInfo.info.code":"INT","prune":true}}]&f=csv
```

jquery ajax version  

```javascript
// here's a jquery ajax version of the same request:
var q1 = {}, q2 = {}; // query objects

// query 1 attributes
q1["qname"] = "YADA test harmony map 1";  
q1["DATA"]  = [{}];
q1["harmonyMap" = {"col1":"STRING","col2":"INT","col3":"FLOAT"};

// query 2 attributes
q2["qname"] = "QGO search";
q2["DATA"]= [{YADA_1:"GO:0005515"}];
q2["harmonyMap"] = {"termInfo.info.name":"STRING","termInfo.info.code":"INT","prune":true};

// JSONParams array
var jsonParams = [q1,q2];

// ajax call
$.ajax({
    url:'/yada.jsp',
    data:{
        j:jsonParams,
        pz:-1,
        f:'csv'
    },
    …
});
```
and the result  

```javascript
// "flattened" harmonized csv result
"STRING","INT","FLOAT"
"Z","10","7.5"
"Z,Z","10","7.5"
"ZZ","10","7.5"
"A","10","7.5"
"Z","10","8.5"
"A","10","8.5"
"Z","10","9.5"
"A","10","9.5"
"protein binding","4303",

```

[examples]: #examples