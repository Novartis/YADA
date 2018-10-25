-- List of Essential Queries

-- Delete possibly pre-existing PARAMs for YADA Queries that we are going to reset
DELETE from YADA_PARAM WHERE target in (
  SELECT distinct qname from YADA_QUERY
    where app = 'YADA'
    and qname not like 'YADA test%'
    and qname not like '%secured%'
    and qname not like '%YSEC%'
    and qname not in ('YADA default','YADA yada','YADA user is authorized','YADA sql tester','YADA select nextval','YADA select multiple nextvals')
  );

-- Delete possibly pre-existing PROPS for YADA Queries that we are going to reset
DELETE from YADA_PROP;

-- Delete possibly pre-existing YADA Queries that we are going to 'reset'
DELETE from YADA_QUERY
  where app = 'YADA'
  and qname not like 'YADA test%'
  and qname not like '%secured%'
  and qname not like '%YSEC%'
  and qname not in ('YADA default','YADA yada','YADA user is authorized','YADA sql tester','YADA select nextval','YADA select multiple nextvals');

-- properties
--INSERT INTO YADA_PROP (target,name,value) VALUES ('system','yada.version','${display.version}');
INSERT INTO YADA_PROP (target,name,value) VALUES ('system','app.home','${app.home}');
INSERT INTO YADA_PROP (target,name,value) VALUES ('system','yada.bin','${app.home}/bin/');
INSERT INTO YADA_PROP (target,name,value) VALUES ('system','yada.util','${app.home}/util/');
INSERT INTO YADA_PROP (target,name,value) VALUES ('system','io/out','${app.home}/files/out');
INSERT INTO YADA_PROP (target,name,value) VALUES ('system','io/in','${app.home}/files/in');
INSERT INTO YADA_PROP (target,name,value) VALUES ('system','adaptor/org.sqlite.JDBC','com.novartis.opensource.yada.adaptor.SQLiteAdaptor');
INSERT INTO YADA_PROP (target,name,value) VALUES ('system','adaptor/oracle.jdbc.OracleDriver','com.novartis.opensource.yada.adaptor.OracleAdaptor');
INSERT INTO YADA_PROP (target,name,value) VALUES ('system','adaptor/com.mysql.jdbc.Driver','com.novartis.opensource.yada.adaptor.MySQLAdaptor');
INSERT INTO YADA_PROP (target,name,value) VALUES ('system','adaptor/org.postgresql.Driver','com.novartis.opensource.yada.adaptor.PostgreSQLAdaptor');
INSERT INTO YADA_PROP (target,name,value) VALUES ('system','adaptor/com.vertica.jdbc.Driver','com.novartis.opensource.yada.adaptor.VerticaAdaptor');
INSERT INTO YADA_PROP (target,name,value) VALUES ('system','adaptor/org.hsqldb.jdbc.JDBCDriver','com.novartis.opensource.yada.adaptor.HSQLdbAdaptor');
INSERT INTO YADA_PROP (target,name,value) VALUES ('system','adaptor/SOAP','com.novartis.opensource.yada.adaptor.SoapAdaptor');
INSERT INTO YADA_PROP (target,name,value) VALUES ('system','adaptor/com.microsoft.sqlserver.jdbc.SQLServerDriver','com.novartis.opensource.yada.adaptor.SQLServerAdaptor');
INSERT INTO YADA_PROP (target,name,value) VALUES ('system','login','default');

-- sanity check
-- INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA default','select ''YADA is alive''','YADABOT','YADA');
-- parameter dml
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select default params','SELECT id as "ID", target as "TARGET", name as "NAME", value as "VALUE", rule as "RULE" FROM YADA_PARAM where target in (?v) and target not in (''YADA apps'',''YADA queries'',''YADA new query'',''YADA delete query'',''YADA insert usage log'',''YADA update query'')','YADABOT','YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA select default params','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=app:getValue(TARGET) userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA select default params','YADA view protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA select default params-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA select default params','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select default params for app','SELECT id as "ID", target as "TARGET", name as "NAME", value as "VALUE", rule as "RULE" FROM YADA_PARAM where target in (select qname from yada_query where app = ?v and qname not in (''YADA apps'',''YADA queries'',''YADA new query'',''YADA delete query'',''YADA insert usage log'',''YADA update query''))','YADABOT','YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA select default params for app','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA select default params for app','YADA view protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA select default params for app-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA select default params for app','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA insert default param','insert into YADA_PARAM (id, target, name, value, rule) values (?v,?v,?v,?v,?i)','YADABOT','YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA insert default param','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=qname:getValue(TARGET) userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA insert default param','YADA crud by qname protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA insert default param-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA insert default param','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA update default param','update YADA_PARAM set value = ?v, rule = ?i where target = ?v and name = ?v','YADABOT','YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA update default param','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=qname:getValue(TARGET) userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA update default param','YADA crud by qname protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA update default param-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA update default param','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA delete default param','delete from YADA_PARAM where target = ?v and name = ?v and value = ?v and rule = ?i','YADABOT','YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA delete default param','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA delete default param','YADA crud protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA delete default param-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA delete default param','protected','true');

-- property dml
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select props for target','select target as "TARGET", name as "NAME", value as "VALUE" from yada_prop where target = ?v', 'YADABOT', 'YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA select props for target','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=app:getValue(TARGET) userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA select props for target','YADA view protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA select props for target-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA select props for target','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select props for targets','select target as "TARGET", name as "NAME", value as "VALUE" from yada_prop where target in (?v)', 'YADABOT', 'YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA select props for targets','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA select props for targets','YADA view protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA select props for targets-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA select props for targets','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select props like target','select target as "TARGET", name as "NAME", value as "VALUE" from yada_prop where target like ?v||''%''', 'YADABOT', 'YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA select props like target','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=app:getValue(TARGET) userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA select props like target','YADA view protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA select props like target-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA select props like target','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA insert prop','insert into yada_prop (target, name, value) values (?v,?v,?v)','YADABOT','YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA insert prop','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA insert prop','YADA crud protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA insert prop-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA insert prop','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA update prop','update yada_prop set value = ?v where target = ?v and name = ?v', 'YADABOT', 'YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA update prop','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA update prop','YADA crud protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA update prop-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA update prop','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA delete prop','delete from yada_prop where target = ?v and name = ?v and value = ?v', 'YADABOT', 'YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA delete prop','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA delete prop','YADA crud protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA delete prop-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA delete prop','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA delete prop for target','delete from yada_prop where target = ?v', 'YADABOT', 'YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA delete prop for target','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA delete prop for target','YADA crud protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA delete prop for target-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA delete prop for target','protected','true');



-- app dml
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select apps','select a.app "APP", a.name "NAME", a.descr "DESCR", CASE WHEN b.role = ''ADMIN'' THEN a.conf ELSE ''UNAUTHORIZED'' END "CONF", a.active "ACTIVE" from yada_query_conf a join yada_ug b on a.app = b.app where a.app != ''YADA''','YADABOT', 'YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA select apps','pl',1,'Gatekeeper,execution.policy=void,content.policy.predicate=userid=getQLoggedUser()');
-- INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA select apps','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA select apps','YADA view protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA select apps-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA select apps','protected','true');

-- INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA apps','select app as "LABEL" from yada_query_conf where lower(app) like lower(''%''||?v||''%'')','YADABOT','YADA');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA new app','insert into yada_query_conf (app,name,descr,conf,active) values (?v,?v,?v,?v,?i)', 'YADABOT', 'YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA new app','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA new app','YADA new app protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA new app-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA new app','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA new app admin','insert into yada_ug (app,userid,role) values (?v,?v,''ADMIN'')', 'YADABOT', 'YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA new app admin','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA new app admin','YADA new app protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA new app admin-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA new app admin','protected','true');


INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA update app','update yada_query_conf set name=?v, descr=?v, conf=?v, active=?i where app=?v', 'YADABOT', 'YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA update app','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA update app','YADA crud protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA update app-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA update app','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA close pool','select app "APP" from yada_query_conf where app=?v', 'YADABOT', 'YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA close pool','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA close pool','YADA view protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA close pool-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA close pool','protected','true');

-- It is not currently possible to delete an app except through direct db access.  Apps can be disabled in the ui which makes their queries unretrievable

-- query dml
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA queries','select a.qname as "QNAME", a.query as "QUERY", a.app as "APP", a.created as "CREATED", a.modified as "MODIFIED", a.created_by as "CREATED_BY", a.modified_by as "MODIFIED_BY", a.last_access as "LAST_ACCESS", a.access_count as "ACCESS_COUNT", count(b.target) as "DEFAULT_PARAMS", a.comments as "COMMENTS" from yada_query a left join YADA_PARAM b on a.qname = b.target where app = ?v group by a.app, a.qname, a.query, a.last_access, a.access_count, a.modified, a.modified_by, a.created, a.created_by,a.comments','YADABOT','YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA queries','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA queries','YADA view protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA queries-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA queries','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA check uniqueness','select count(qname) as "count" from yada_query where qname = ?v','YADABOT','YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA check uniqueness','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA check uniqueness','YADA view protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA check uniqueness-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA check uniqueness','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA new query','insert into yada_query (app,qname,query,created_by,modified_by,created,modified,access_count,comments) values (?v,?v,?v,?v,?v,?t,?t,?i,?v)','YADABOT','YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA new query','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA new query','YADA crud protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA new query-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA new query','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA insert usage log','insert into yada_usage_log (userid, href, action, note, id) values (?v, ?v, ?v, ?v, yada_usage_log_seq.nextval)','YADABOT','YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA insert usage log','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA insert usage log','YADA crud protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA insert usage log-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA insert usage log','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA update query','update yada_query set qname=?v, query=?v, modified_by=?v, modified=?t, comments=?v where qname=?v and app=?v','YADABOT','YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA update query','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA update query','YADA crud protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA update query-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA update query','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA delete query','delete from yada_query where app=?v and qname=?v','YADABOT','YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA delete query','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA delete query','YADA crud protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA delete query-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA delete query','protected','true');

-- utility
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA dummy','select qname as "QNAME" from yada_query where qname = ''YADA dummy'' ','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select tests','select qname as "QNAME", query as "QUERY" from yada_query where qname like ''YADA test%'' and  (qname like ''%INSERT%'' or qname like ''%UPDATE%'' or qname like ''%SELECT%'')','YADABOT','YADA');

-- security
-- protector dml
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select protectors for target','select qname as "QNAME", type as "TYPE" from yada_a11n where target = ?v', 'YADABOT', 'YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA select protectors for target','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=qname:getValue(TARGET) userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA select protectors for target','YADA view by qname protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA select protectors for target-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA select protectors for target','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA insert protector for target','insert into yada_a11n (target,policy,qname,type) values (?v,''E'',?v,?v)', 'YADABOT', 'YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA insert protector for target','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=qname:getValue(TARGET) userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA insert protector for target','YADA crud by qname protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA insert protector for target-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA insert protector for target','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA update protector for target','update yada_a11n set qname = ?v, type = ?v where target = ?v', 'YADABOT', 'YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA update protector for target','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=qname:getValue(TARGET) userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA update protector for target','YADA crud by qname protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA update protector for target-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA update protector for target','protected','true');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA delete protector for target','delete from yada_a11n where target = ?v and qname = ?v', 'YADABOT', 'YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA delete protector for target','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=qname:getValue(TARGET) userid:getLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA delete protector for target','YADA crud by qname protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA delete protector for target-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA delete protector for target','protected','true');


-- protector queries
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA new app protector','select 1 from yada_ug where upper(role)=''ADMIN'' and userid=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA crud protector','select 1 from yada_ug where upper(role)=''ADMIN'' and app=?v and userid=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA crud by qname protector','select 1 from yada_ug where upper(role) = ''ADMIN'' and app = (select app from yada_query where qname = ?v) and userid=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA view protector','select 1 from yada_ug where upper(role) in (''ADMIN'',''USER'') and app=?v and userid=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA view by qname protector','select 1 from yada_ug where upper(role) in (''ADMIN'',''USER'') and app = (select app from yada_query where qname = ?v) and userid=?v','YADABOT','YADA');

-- default login property

-- check login property (unprotected -- is this a vulnerability?)
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select prop value','select value "VALUE" from YADA_PROP where target=?v and name=?v','YADABOT','YADA');
-- login query and defaut plugin
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA check credentials','select a.app "APP", a.userid "USERID", a.role "ROLE" from yada_ug a join yada_user b on a.userid = b.userid where b.userid=?v and b.pw=?v','YADABOT','YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA check credentials','pl',1,'Login');
-- 'YADA select apps' requires content policy to filter for userid in YADA_UG - user must be mapped to app in yada_ug table
-- INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA select apps','pl',1,'Gatekeeper,execution.policy=void,content.policy.predicate=userid=getLoggedUser()');
-- INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA select apps','pl',1,'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()');

-- baseline sec
-- ADMIN role has full crud for APP
-- USER role can has read-only for APP (eventually USER should have full crud for his/her authored queries)
-- YADA userid has full crud for all APPs
INSERT INTO YADA_USER (USERID,PW) VALUES ('YADA','yada');
INSERT INTO YADA_USER (USERID,PW) VALUES ('YADAUSER','yada');
INSERT INTO YADA_USER (USERID,PW) VALUES ('test','testt');

INSERT into YADA_UG (app,userid,role) VALUES ('YADA','YADA','ADMIN');
INSERT into YADA_UG (app,userid,role) VALUES ('YADA','YADAUSER','USER');
INSERT into YADA_UG (app,userid,role) VALUES ('YADAFSIN','test','ADMIN');
INSERT into YADA_UG (app,userid,role) VALUES ('QGO','test','USER');
INSERT into YADA_UG (app,userid,role) VALUES ('YADATEST','test','ADMIN');
INSERT into YADA_UG (app,userid,role) VALUES ('RESTTEST','test','ADMIN');


-- configuration
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('2','YADA update query','pl',1,'CachedQueryUpdater');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('3','YADA update query','cv',1,'RESTResultJSONConverter');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('4','YADA update query','cq',1,'true');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('5','YADA update query','c',1,'false');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('2','YADA close pool','pl',1,'PoolCloser');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('2','YADA insert prop','pl',1,'CachedQueryUpdater');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('3','YADA insert prop','cv',1,'RESTResultJSONConverter');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('2','YADA delete prop','pl',1,'CachedQueryUpdater');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('3','YADA delete prop','cv',1,'RESTResultJSONConverter');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('4','YADA delete prop','cq',1,'true');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('5','YADA delete prop','c',1,'false');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('2','YADA select apps','pz',1,'-1');

-- COMMIT;
