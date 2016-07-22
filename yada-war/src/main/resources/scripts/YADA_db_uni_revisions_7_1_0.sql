CREATE TABLE IF NOT EXISTS YADA_PROP
( 
  TARGET VARCHAR(500), 
  NAME VARCHAR(500),
  VALUE VARCHAR(4000)
);
ALTER TABLE YADA_PARAMS RENAME TO YADA_PARAM;
ALTER TABLE YADA_PARAM ADD ID VARCHAR(255);

UPDATE YADA_QUERY SET QUERY = 'select a.*,count(b.target) default_params 
from yada_query a left 
join yada_param b on a.qname = b.target 
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
a.created_by,
a.comments' WHERE QNAME = 'YADA queries';
UPDATE YADA_QUERY SET QUERY = 'update yada_param set value = ?v, rule = ?i where target = ?v and name = ?v' WHERE QNAME = 'YADA update default param';
UPDATE YADA_QUERY SET QUERY = 'SELECT * FROM YADA_PARAMS 
where target in (?v) 
  and target 
  not in (''YADA apps'',
          ''YADA queries'',
          ''YADA new query'',
          ''YADA delete query'',
          ''YADA insert usage log'',
          ''YADA update query'')' WHERE QNAME = 'YADA select default params';
UPDATE YADA_QUERY SET QUERY = 'SELECT
id as "ID", 
target as "TARGET",
name as "NAME",
value as "VALUE",
rule as "RULE"
FROM YADA_PARAM where target in (select qname from yada_query where app = ?v and qname not in (''YADA apps'',''YADA queries'',''YADA new query'',''YADA delete query'',''YADA insert usage log'',''YADA update query''))'
WHERE QNAME = 'YADA select default params for app';
UPDATE YADA_QUERY SET QUERY = 'insert into YADA_PARAM (id, target, name, value, rule) values (?v,?v,?v,?v,?i)' WHERE QNAME = 'YADA insert default param';


INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA insert prop','insert into yada_prop (target, name, value) values (?v,?v,?v)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA update prop','update yada_prop set value = ?v where target = ?v and name = ?v', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA delete prop','delete from yada_prop where target = ?v and name = ?v and value = ?v', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select props for target','select target as "TARGET", name as "NAME", value as "VALUE" from yada_prop where target = ?v', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select props for targets','select target as "TARGET", name as "NAME", value as "VALUE" from yada_prop where target in (?v)', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select props like target','select target as "TARGET", name as "NAME", value as "VALUE" from yada_prop where target like ?v||''%''', 'YADABOT', 'YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA update protector for target','update yada_a11n set qname = ?v, type = ?v where target = ?v', 'YADABOT', 'YADA');