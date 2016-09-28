<a name="top"></a>
# Secure Your Webapp

<div style="float:right;margin-top:-43px;">
    <img src="../resources/images/blox250.png"/>
</div> 
> If you are completely unfamiliar with the [Security Guide](security.md), it may be helpful to review that first. This document uses some YADA jargon which may be unfamiliar.


If you're using an ad hoc approach to security, or a less elaborate, traditional "users & groups" approach, you might have to put a little more thought into your *Execution Policy* and *Content Policy* implementation.

If, conversely, you're using a token authentication service, all you need to do is override `Gatekeeper.validateToken` to validate your user's cookie value, or the value of the authorization token passed in the url, and then pass the value to `Gatekeeper.setToken()` so it can be interrogated downstream. Then, in your *Execution Policy*, your *protector* queries can allow or block execution based on the value of the token.
 
But you might not have these luxuries. This guide will walk through the default security implementation of the `yada-admin` webapp, and present several examples of YADA security policies. Hopefully this will alleviate some possible confusion and provide enough information and guidance to get you started.

## Requirements

The yada-admin tool security requirements are ostensibly simple. The tool itself supports the following use-cases:

* Create apps
* Edit app configuration
* Create, edit, delete queries and related configuration
* View queries 
* Backup queries to a text file


## Data Model

The yada-admin data model is very basic.  Some columns are omitted for brevity.  

```
+--------------+      +-------------------+     +-----------+
|  YADA_QUERY  |      |  YADA_QUERY_CONF  |     |  YADA_UG  |
+--------------+      +-------------------+     +-----------+
|  APP         |      |  APP              |     |  APP      |
|  QNAME       |      |  CONF             |     |  UID      |
|  QUERY       |      |  ACTIVE           |     |  ROLE     |
|  COMMENTS    |      |  NAME             |     +-----------+
+--------------+      |  DESCR            |
                      +-------------------+
                      
                      
+--------------+         +-------------+        +-------------+
|  YADA_PARAM  |         |  YADA_PROP  |        |  YADA_USER  |
+--------------+         +-------------+        +-------------+
|  ID          |         |  TARGET     |        |  UID        |
|  TARGET      |         |  NAME       |        |  PW         |
|  NAME        |         |  VALUE      |        +-------------+
|  VALUE       |         +-------------+
|  RULE        |
+--------------+      
```

There are no literal foreign key relationships in the implementation, but virtual FKs are used everywhere:

* APP to APP
* APP to TARGET
* QUERY to TARGET
* TARGET+'-'+ID to TARGET
* UID to UID

## Protection Scheme

The protection scheme is so simple as to be predictable:

There are two roles: `ADMIN`, `USER`

* `ADMIN` roles can edit app configurations and have full create, read, update, and delete access to queries.
Any `ADMIN` is permitted to create new apps as well.
* `USER` roles can view apps but cannot see app configurations. They can also see queries, but make no changes.


### Login

First we confirm the user's login credentials (`UID` and `PW,`) by retreiving a "security profile" from the db, specifically the `YADA_UG` table, then if the user passes, we store the profile in a servlet session attribute.  The profile is a list of apps and roles to which the user has access.

The `YADA check credentials` query:

```sql
select a.app "APP", a.uid "UID", a.role "ROLE" 
from yada_ug a join yada_user b on a.uid = b.uid 
where b.uid=?v and b.pw=?v

-- RESULT, e.g. for user: test, pw: testt
/*
 {"RESULTSET": 
   {"ROWS": [
           {"APP": "YADAFSIN", "UID": "test", "ROLE":"ADMIN"},
           {"APP": "QGO", "UID": "test", "ROLE":"USER"},           
          ]
   }  
 }  
*/
```

Nearly all (if not actually all) subsequent queries will depend on this user profile.

### App Manager

#### List of Apps

The app manager does the following:

* presents a list of apps in collapsible panel
* provides app configuration data 
* provides navigation to queries attached to the apps

The list of apps is protected by a *Content Policy*:

```sql
-- Qname: YADA select apps
select 
a.app "APP", 
a.name "NAME", 
a.descr "DESCR", 
CASE WHEN b.role = 'ADMIN' THEN a.conf ELSE 'UNAUTHORIZED' END "CONF", 
a.active "ACTIVE" 
from 
yada_query_conf a 
join yada_ug b on a.app = b.app where a.app != 'YADA'
```
The default parameter plugin configuration for `YADA select apps`:

```c
# Default parameter target: YADA select apps
# Default parameter rule: 1 (non-overrideable)
# Default parameter name: pl
# Default parameter value: 
Gatekeeper,execution.policy=void,content.policy.predicate=uid=getQLoggedUser()
```
This parameter changes the `YADA select apps` query into:

```sql
select 
a.app "APP", 
a.name "NAME", 
a.descr "DESCR", 
CASE WHEN b.role = 'ADMIN' THEN a.conf ELSE 'UNAUTHORIZED' END "CONF", 
a.active "ACTIVE" 
from 
yada_query_conf a 
join yada_ug b on a.app = b.app where a.app != 'YADA'
and uid=getQLoggedUser()  
```
Line 10 was added dynamically, and `getQLoggedUser()` will be replaced with the result of the method execution, so ultimately, in the case where the logged username is `foo` the query to be executed will be:

```sql
select 
a.app "APP", 
a.name "NAME", 
a.descr "DESCR", 
CASE WHEN b.role = 'ADMIN' THEN a.conf ELSE 'UNAUTHORIZED' END "CONF", 
a.active "ACTIVE" 
from 
yada_query_conf a 
join yada_ug b on a.app = b.app where a.app != 'YADA'
and uid='foo'
```

As you can see, for any app for which `foo` user has role = `ADMIN` the value of `conf` will be presented. Otherwise, `foo` will see `UNAUTHORIZED`. Further any apps for which `foo` has no association will be omitted entirely.

#### App creation

Assuming one has the authority to create a new app, when doing so, the following queries get executed:

1. YADA new app
2. YADA new app admin

Both of these queries are protected by the same *Execution Policy*, a *protector* query called `YADA new app protector`.



#### App modification

Assuming one has authority to see the app configuration, when changes are made, the query `


### Query Editor

* Create queries
* Edit/Delete queries
* Edit Security parameters
* Edit Default parameters 

==This document is incomplete. It is a work in progress. More to come...==
