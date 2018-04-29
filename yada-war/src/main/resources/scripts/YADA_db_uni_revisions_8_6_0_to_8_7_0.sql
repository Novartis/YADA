INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA select app config','select a.app "APP", a.name "NAME", a.descr "DESCR", CASE WHEN b.role = ''ADMIN'' THEN a.conf ELSE ''UNAUTHORIZED'' END "CONF", a.active "ACTIVE" from yada_query_conf a join yada_ug b on a.app = b.app where a.app != ''YADA'' and a.app = ?v','YADABOT', 'YADA');
INSERT into YADA_PARAM (id,target,name,rule,value) VALUES ('1','YADA select app config','pl',1,'Gatekeeper,execution.policy=void,content.policy.predicate=userid=getQLoggedUser()');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA select app config','YADA view protector','E','whitelist');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA select app config-1','protected','true');
INSERT into YADA_PROP (target,name,value) VALUES ('YADA select app config','protected','true');
