-- List of Essential Queries
-- select 'INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('''||qname||''','''||replace(query,'''','''''')||''',''YADABOT'');' from YADA_QUERY where app = 'YADA' and qname not like 'YADA test%' and qname not like '%secured%' and qname not like '%YSEC%' and qname not in ('YADA yada','YADA user is authorized','YADA sql tester','YADA select nextval','YADA select multiple nextvals') order by qname;

DELETE from YADA_QUERY where app = 'YADA' and qname not like 'YADA test%' and qname not like '%secured%' and qname not like '%YSEC%' and qname not in ('YADA yada','YADA user is authorized','YADA sql tester','YADA select nextval','YADA select multiple nextvals');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA apps','select app as "LABEL" from yada_query_conf where lower(app) like lower(''%''||?v||''%'')','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA delete default param','delete from YADA_PARAMS where target = ?v and name = ?v and value = ?v and rule = ?i','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA delete queries for app','delete from yada_query where app in (?v)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA delete query','delete from yada_query where app=?v and qname=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA dummy','select qname as "QNAME" from yada_query where qname = ''YADA dummy'' ','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA insert default param','insert into YADA_PARAMS (target, name, value, rule) values (?v,?v,?v,?i)','YADABOT','YADA');
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
count(b.target) as "DEFAULT_PARAMS"
from yada_query a left
join yada_params b on a.qname = b.target
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
a.created_by','YADABOT','YADA');
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
--no longer in use
--INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select apps for migration','select a.app label, upper(to_char(max(b.modified),''dd-mon-yyyy'')) max_date
--from yada_query_conf a
--left join yada_query b on a.app = b.app
--group by a.app','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select default params','SELECT 
target as "TARGET",
name as "NAME",
value as "VALUE",
rule as "RULE"
FROM YADA_PARAMS
where target in (?v)
  and target
  not in (''YADA apps'',
          ''YADA queries'',
          ''YADA new query'',
          ''YADA delete query'',
          ''YADA insert usage log'',
          ''YADA update query'')','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select default params for app','SELECT 
target as "TARGET",
name as "NAME",
value as "VALUE",
rule as "RULE"
FROM YADA_PARAMS where target in (select qname from yada_query where app = ?v and qname not in (''YADA apps'',''YADA queries'',''YADA new query'',''YADA delete query'',''YADA insert usage log'',''YADA update query''))','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select tests','select qname as "QNAME", query as "QUERY" from yada_query
where qname like ''YADA test%''
  and  (qname like ''%INSERT%'' or qname like ''%UPDATE%'' or qname like ''%SELECT%'')','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA update default param','update yada_params set value = ?v, rule = ?i where target = ?v and name = ?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA update query','update yada_query set qname=?v, query=?v, modified_by=?v, modified=?t, comments=?v where qname=?v and app=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA check uniqueness','select count(qname) as count from yada_query where qname = ?v','YADABOT','YADA');
