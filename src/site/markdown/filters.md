# Filter Specification

<div style="float:right;margin-top:-43px;">
    <img src="../resources/images/blox250.png"/>
</div> 



Filters are deep and versatile.  All manner of boolean grouping and operations are supported, with only a few exceptions. 

Due to the use of square brackets `[` and `]` in JSON notation, parentheses `(` and `)` are used to denote optional content.

### filter

The filter object is a JSON object containing two or three keys. 

```javascript
{ 
  "groupOp": groupOperator,
  "rules"  : rules
  ("groups" : groups)
}
```

### groupOperator

|Key|Accepted Values|Description|Notes|  
|:--|:--------------|:----------|:----|  
|`groupOp`|`AND`<br/>`OR`|applies to all rules at the current level and the nested groups as a whole|	 |  

### rules

A json array defining your criteria in rule objects. Again, parentheses, `(` and `)` denote optional content.

```javascript
[ rule (, rule) ]
```

### groups

An optional json array containing nested filter objects, theoretically enabling infinite nesting of filters. Again, parentheses, `(` and `)` denote optional content.

```javascript
[ filter (,filter) ]
```

### rule

A json object defining a single criterion for filtering. Again, parentheses, `(` and `)` denote optional content.

```javascript
{ 
  "field" : field,
  "op"    : fieldOperator,
  "data"  : data
  (, "type"  : type)
}
```

### field

|Key|Accepted Values|Description|Notes|  
|:--|:--------------|:----------|:----|  
|`field`|a string|the value must correspond to the fields in query referenced by qname	 |  

### fieldOperator

The comparison operator defining how to evaluate the field/value relationship

|Key|Accepted Values|Description|Notes|  
|:--|:--------------|:----------|:----|  
|`op`|`eq`|equals| |  
| |`ne`|not equals| |  
| |`lt`|less than| |  
| |`le`|less than or equal to| |  
| |`gt`|greater than| |  
| |`ge`|greater than or equal to| |  
| |`in`|in| |  
| |`ni`|not in| |  
| |`nu`|null| |  
| |`nn`|not null| |  
| |`bw`|begins with| |  
| |`bn`|does not begin with| |  
| |`ew`|ends with| |  
| |`en`|does not end with| |  
| |`cn`|contains| |  
| |`nc`|does not contain| |  

### data

The value to use in the comparison operation. Effectively any string, integer, decimal, or date.

### type

An optional classifier to clarify what type of operator can be used when building the filtering code on the back end. 

|Key|Accepted Values|Description|Notes|  
|:--|:--------------|:----------|:----|  
|`type`|`number`|for any integer or decimal|optional|  
| |`text`|for case-insensitive text, will apply the `LOWER()` function|optional|  
| |`etxt`|for case-sensitive text|optional|  


## Examples

The following example shows a filter which should return values where: `f1 = 'v1' AND ( (f2 < 6 OR f3 >= 100) AND (f4 > 0.5 OR f5 is not null) )`

```json
{"groupOp":"AND",
 "rules":[{"field":"f1","op":"eq","data":"v1","type":"text"}],
 "groups":[{"groupOp":"OR",
              "rules":[{"field":"f2","op":"lt","data":"6","type": "number"},
                       {"field":"f3","op":"ge","data":"100","type": "number"}],
             "groups":[]},
           {"groupOp":"OR",
              "rules":[{"field":"f4","op":"gt","data":"0.5", "type": "number"},
                       {"field":"f5","op":"nn","data":""}],
             "groups":[]}]}
```
It may be easier to think it terms of [Polish or Prefix Notation](http://en.wikipedia.org/wiki/Polish_notation):  `AND  f1='v1' (OR (< f 26) (>= f3 100)) (OR (>f4 0.5) not(is null(f5)))`
...or not 

## Javascript Example

```javascript
var filter={
             "groupOp":"AND",
             "rules": [
                       { 
                         "field": "GENE_SYMBOL", 
                         "op"   : "in", 
                         "data" : "ABL1,EGFR" 
                       },
                       { 
                         "field": "SAMPLE_TYPE", 
                         "op"   : "eq", 
                         "data" : "Cell Line", 
                         "type" : "etxt"
                       }
                      ]
           };
$.ajax({  
  "url":...,  
  "data":{  
    "qname":...,  
    "filters":JSON.stringify(filter)
  }
});
```

## Behind the scenes

`JDBCAdaptor.getQueryFilters()` used constants, but here is a version of the method using string literals to illustrate the mechanism:

```java
   // nulls, not nulls
        if(op.matches("nu|nn"))
        {
            sql.append(SQL_CORE_ALIAS+"." + field + " ");
            sql.append("IS ");
            if ("nn".equals(op))  {  sql.append("NOT "); }
            sql.append("NULL");
        }
        // numbers
        else
        {
            // strings:  eq, ne, cn, nc, bw, bn, ew, en, in, ni
            // numbers:  eq, ne, lt, le, gt, ge, in, ni
            if ("number".equals(type))
            {
                sql.append(SQL_CORE_ALIAS+"." + field + " ");
                if("ne".equals(op))            {  sql.append("<>");  }
                else if(op.matches("lt|le"))   {  sql.append("<");   }
                else if(op.matches("gt|ge"))   {  sql.append(">");   }
                if(op.matches("eq|le|ge"))     {  sql.append("=");   }
                if("ni".matches(op))           {  sql.append("NOT"); }
                sql.append(" ");
                if(op.matches("in|ni"))        {  sql.append("IN ("+value+") "); }
                else                           {  sql.append(value);  }
            }
 
            // varchars
            else if ("text".equals(type) | | ("etxt".equals(type)))
            {
                // case sensitive
                if ("etxt".equals(type))
                {
                    sql.append(SQL_CORE_ALIAS+"." + field + " ");
                }
 
                // case insensitive
                else
                {
                    sql.append("LOWER("+SQL_CORE_ALIAS+"." + field + ") ");
                }
 
                if("eq".equals(op))            {  sql.append(" = "); }
                else if("ne".equals(op))       {  sql.append(" <> ");}
 
                // not contains, not begins with, not ends with
                if(op.matches("nc|bn|en|ni"))  {  sql.append("NOT ");}
                if (op.matches("cn|bw|ew"))  {  sql.append("LIKE "); }
 
                // equals, not equals, contains,
                if (op.matches("eq|ne|cn|nc|bw|bn|ew|en")) {  sql.append("'");     }
 
                // contains, ends with, not ends with
                if (op.matches("cn|ew|en"))    {  sql.append("%");   }
                if (op.matches("eq|ne|cn|nc|bw|bn|ew|en")) { sql.append(value);  } // value already lower case
                 
                // in, ni
                else
                {
                    String[] split = value.split(",");
                    sql.append("IN (");
                    for (int j=0;j<split.length;j++)
                    {
                        sql.append("'"+split[j]+"'");
                        if (j<split.length-1)
                        {
                            sql.append(",");
                        }
                    }
                    sql.append(")");
                }
                 
                // contains, begins with, not begins with
                if (op.matches("cn|bw|bn"))                {  sql.append("%"); }
                if (op.matches("eq|ne|cn|nc|bw|bn|ew|en")) {  sql.append("'"); }
             
            } // end "text"
        } // end "non null ops
    } // end rules iteration
} // end is rules null  
```  

