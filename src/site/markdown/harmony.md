# HarmonyMap Specification

## Intro
HarmonyMap is feature which effectively renames columns or keys in the results returned from a YADA Request which contain multiple queries. It is like to an SQL [UNION ALL](http://stackoverflow.com/questions/49925/what-is-the-difference-between-union-and-union-all) statement combined with an SQL [LEFT JOIN]() statement. 

In other words, it will "harmonize" multiple results into a single result set.  

See the [examples](#examples) below the specification.

## Specification



<a name="examples"></a>
## Examples
For example, suppose I want to get data from source two sources in one YADA request:

```javascript

// Unharmonized example

// the YADA JSONParams request parameter:
var j = j=[{qname:YADA%20test%20harmony%201,DATA:[{}]},
           {qname:YADA%20test%20harmony%202,DATA:{}]}];
           
// jquery syntax
$.ajax({
    url:/yada.jsp
    data:{
        pz=2    // for brevity (only 2 rows per query)
        py=true // pretty printing
        j: j    // the JSONParams array defined above
    },
    ...
});
```
This returns the JSON string:

```json
// Unharmonized example returns unharmonized result:
{
  "RESULTSETS": [
    {"RESULTSET": {
      "total": 15,
      "ROWS": [
        {
          "d1": "10",
          "a1": "A"
        },
        {
          "d1": "10",
          "a1": "Z"
        }
      ],
      "qname": "YADA test harmony 1",
      "page": "1",
      "records": 2
    }},
    {"RESULTSET": {
      "total": 15,
      "ROWS": [
        {
          "d2": "10",
          "a2": "A"
        },
        {
          "d2": "10",
          "a2": "Z"
        }
      ],
      "qname": "YADA test harmony 2",
      "page": "1",
      "records": 2
    }}
  ],
  "version": "6.0.0"
}
```
However, if one simply maps the "columns" or "keys" in the expected result to new values using a simple JSON object with "key:value" pairs containing "old key:new key" values, the result will be changed accordingly:

```javascript

// Harmonized example

// the YADA JSONParams request parameter:
var j =[{qname:YADA%20test%20harmony%201,DATA:[{}]},
        {qname:YADA%20test%20harmony%202,DATA:{}]}];
           
var h = h={"a1":"ac","d1":"dc","a2":"ac","d2":"dc"}
           
// jquery syntax
$.ajax({
    url:/yada.jsp
    data:{
        pz=2,    // for brevity (only 2 rows per query)
        py=true, // pretty printing
        j: j,    // the JSONParams array defined above
        h: h     // the HarmonyMap spec defined aboove
    },
    ...
});
```
This returns the JSON string:

```json
// Harmonized example returns single, harmonized result:
{
  "RESULTSET": {
    "total": 30,
    "ROWS": [
      {
        "dc": "10",
        "ac": "A"
      },
      {
        "dc": "10",
        "ac": "Z"
      },
      {
        "dc": "10",
        "ac": "A"
      },
      {
        "dc": "10",
        "ac": "Z"
      }
    ],
    "qname": "YADA test harmony 1",
    "page": "1",
    "records": 4
  },
  "version": "6.0.0"
}```