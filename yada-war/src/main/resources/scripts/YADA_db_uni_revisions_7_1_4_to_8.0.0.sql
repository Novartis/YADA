/* sqlite only
CREATE TABLE YADA_QUERY_CONF_TMP AS SELECT APP, SOURCE FROM YADA_QUERY_CONF;
DROP TABLE YADA_QUERY_CONF;
CREATE TABLE IF NOT EXISTS YADA_QUERY_CONF 
( 
  APP     VARCHAR(20)   NOT NULL, 
  NAME    VARCHAR(20),
  DESCR   VARCHAR(400),
  SOURCE  VARCHAR(4000), 
  CONF    VARCHAR(4000)
);
INSERT INTO YADA_QUERY_CONF SELECT APP, SOURCE FROM YADA_QUERY_CONF_TMP;
DROP TABLE YADA_QUERY_CONF_TMP;
*/

/* non sqlite
ALTER TABLE YADA_QUERY_CONF DROP COLUMN VERSION;
ALTER TABLE YADA_QUERY_CONF ADD NAME VARCHAR(255);
ALTER TABLE YADA_QUERY_CONF ADD DESCR VARCHAR(400);
ALTER TABLE YADA_QUERY_CONF ADD CONF VARCHAR(4000);
ALTER TABLE YADA_QUERY_CONF ADD ACTIVE INT DEFAULT 1;
ALTER TABLE YADA_QUERY_CONF MODIFY SOURCE NULL;

*/

/* any
CREATE TABLE YADA_UG
(
  APP VARCHAR(20) NOT NULL;
  UID VARCHAR(255) NOT NULL;
  ROLE VARCHAR(20) NOT NULL;
);
UPDATE YADA_QUERY_CONF SET CONF = SOURCE WHERE SOURCE LIKE 'http%' or SOURCE LIKE 'soap%' OR SOURCE LIKE 'file%';
*/

-- corrections
UPDATE YADA_QUERY. SET QUERY = 'select a.qname as "QNAME", 
a.query as "QUERY", 
a.app as "APP", 
a.created as  "CREATED",
a.modified as "MODIFIED", 
a.created_by as "CREATED_BY", 
a.modified_by as  "MODIFIED_BY", 
a.last_access as  "LAST_ACCESS", 
a.access_count as "ACCESS_COUNT",
count(b.target) as "DEFAULT_PARAMS",
a.comments as "COMMENTS"
from yada_query a left
join YADA_PARAM b on a.qname = b.target
where app = ?v
group by
a.app,
a.qname,
a.query,
a.last_access,
a.access_count,
a.modified,
a.modified_by,
a.created,
a.created_by,a.comments' WHERE QNAME = 'YADA queries';

-- app mgr
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA new app','insert into yada_query_conf (app,name,descr,conf,active) values (?v,?v,?v,?v,?i)', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA update app','update yada_query_conf set name=?v, descr=?v, conf=?v, active=?i where app=?v', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select apps','select app "APP", name "NAME", descr "DESCR", conf "CONF", active "ACTIVE" from yada_query_conf where app != ''YADA''', 'YADABOT', 'YADA');

-- configs
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA update query','pl',1,'CachedQueryUpdater');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA select apps','pz',1,'-1');
