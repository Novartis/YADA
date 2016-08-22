-- List of Essential Queries
-- select 'INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('''||qname||''','''||replace(query,'''','''''')||''',''YADABOT'');' from YADA_QUERY where app = 'YADA' and qname not like 'YADA test%' and qname not like '%secured%' and qname not like '%YSEC%' and qname not in ('YADA yada','YADA user is authorized','YADA sql tester','YADA select nextval','YADA select multiple nextvals') order by qname;

DELETE from YADA_QUERY where app = 'YADA' and qname not like 'YADA test%' and qname not like '%secured%' and qname not like '%YSEC%' and qname not in ('YADA yada','YADA user is authorized','YADA sql tester','YADA select nextval','YADA select multiple nextvals');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA apps','select app as "LABEL" from yada_query_conf where lower(app) like lower(''%''||?v||''%'')','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA delete default param','delete from YADA_PARAM where target = ?v and name = ?v and value = ?v and rule = ?i','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA delete queries for app','delete from yada_query where app in (?v)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA delete query','delete from yada_query where app=?v and qname=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA dummy','select qname as "QNAME" from yada_query where qname = ''YADA dummy'' ','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA insert default param','insert into YADA_PARAM (id, target, name, value, rule) values (?v,?v,?v,?v,?i)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA insert usage log','insert into yada_usage_log (userid, href, action, note, id) values (?v, ?v, ?v, ?v, yada_usage_log_seq.nextval)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA new query','insert into yada_query (app,qname,query,created_by,modified_by,created,modified,access_count,comments) values (?v,?v,?v,?v,?v,?t,?t,?i,?v)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA queries','select a.qname as "QNAME", 
a.query as "QUERY", 
a.app as "APP", 
a.created as	"CREATED",
a.modified as "MODIFIED", 
a.created_by as "CREATED_BY", 
a.modified_by as	"MODIFIED_BY", 
a.last_access as	"LAST_ACCESS", 
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
a.created_by,a.comments','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA queries for apps','select
a.query as "QUERY", 
a.app as "APP", 
a.created as	"CREATED",
a.modified as "MODIFIED", 
a.created_by as "CREATED_BY", 
a.modified_by as	"MODIFIED_BY", 
a.last_access as	"LAST_ACCESS", 
a.access_count as "ACCESS_COUNT", 
from yada_query a where a.app in (?v) ','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select default params','SELECT 
id as "ID",
target as "TARGET",
name as "NAME",
value as "VALUE",
rule as "RULE"
FROM YADA_PARAM
where target in (?v)
  and target
  not in (''YADA apps'',
          ''YADA queries'',
          ''YADA new query'',
          ''YADA delete query'',
          ''YADA insert usage log'',
          ''YADA update query'')','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select default params for app','SELECT
id as "ID", 
target as "TARGET",
name as "NAME",
value as "VALUE",
rule as "RULE"
FROM YADA_PARAM where target in (select qname from yada_query where app = ?v and qname not in (''YADA apps'',''YADA queries'',''YADA new query'',''YADA delete query'',''YADA insert usage log'',''YADA update query''))','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select tests','select qname as "QNAME", query as "QUERY" from yada_query
where qname like ''YADA test%''
  and  (qname like ''%INSERT%'' or qname like ''%UPDATE%'' or qname like ''%SELECT%'')','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA update default param','update YADA_PARAM set value = ?v, rule = ?i where target = ?v and name = ?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA update query','update yada_query set qname=?v, query=?v, modified_by=?v, modified=?t, comments=?v where qname=?v and app=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA check uniqueness','select count(qname) as count from yada_query where qname = ?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA insert prop','insert into yada_prop (target, name, value) values (?v,?v,?v)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA update prop','update yada_prop set value = ?v where target = ?v and name = ?v', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA delete prop','delete from yada_prop where target = ?v and name = ?v and value = ?v', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA delete prop for target','delete from yada_prop where target = ?v', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select props for target','select target as "TARGET", name as "NAME", value as "VALUE" from yada_prop where target = ?v', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select props for targets','select target as "TARGET", name as "NAME", value as "VALUE" from yada_prop where target in (?v)', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select props like target','select target as "TARGET", name as "NAME", value as "VALUE" from yada_prop where target like ?v||''%''', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select protectors for target','select qname as "QNAME", type as "TYPE" from yada_a11n where target = ?v', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA insert protector for target','insert into yada_a11n (target,policy,qname,type) values (?v,''E'',?v,?v)', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA delete protector for target','delete from yada_a11n where target = ?v and qname = ?v', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA update protector for target','update yada_a11n set qname = ?v, type = ?v where target = ?v', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA new app','insert into yada_query_conf (app,name,descr,conf,active) values (?v,?v,?v,?v,?i)', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA update app','update yada_query_conf set name=?v, descr=?v, conf=?v, active=?i where app=?v', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select apps','select app "APP", name "NAME", descr "DESCR", conf "CONF", active "ACTIVE" from yada_query_conf where app != ''YADA''', 'YADABOT', 'YADA');

-- configs
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA update query','pl',1,'CachedQueryUpdater');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA select apps','pz',1,'-1');

-- baseline sec
-- ADMIN role has full crud for APP
-- USER role can has read-only for APP (eventually USER should have full crud for his/her authored queries)
-- YADA uid has full crud for all APPs
INSERT into YADA_UG (app,uid,role) VALUES ('YADA','YADA','ADMIN');
INSERT into YADA_UG (app,uid,role) VALUES ('YADA','YADAUSER','USER');

-- 'YADA select apps' requires content policy to filter for uid in YADA_UG

-- 'YADA insert...'   requires execution policy by whitelist for APP admins
-- 'YADA update...'   same
-- 'YADA delete...'   same
-- 'YADA select...'   "internal" queries for properties, params, uniqueness, protectors, all require admin role

-- 'YADA select...' 


