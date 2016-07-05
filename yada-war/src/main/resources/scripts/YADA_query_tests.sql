
-- List of test queries
-- select 'INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('''||qname||''','''||replace(query,'''','''''')||''',''YADABOT'');' from yada_query where app = 'YADA' and qname like 'YADA test%' and qname not in ('YADA test query','YADA test two','YADA testy testy testy') order by qname;

DELETE from YADA_QUERY where app = 'YADA' and qname like 'YADA test%' and qname not in ('YADA test query','YADA test two','YADA testy testy testy');
DELETE from YADA_PARAMS where target like 'YADA test sec%';
DELETE from YADA_A11N where target like 'YADA test sec%';
DELETE from YADA_QUERY where app = 'YADAFSIN' and qname like 'YADAFSIN test%';
DELETE from YADA_QUERY where qname = 'QGO search';

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test validate preproc','select app APP from yada_query_conf','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test','select qname,app from yada_query','YADABOT','YADA');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test DELETE','delete from YADA_TEST','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test DELETE with STRING','delete from YADA_TEST where COL1=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test DELETE with INTEGER','delete from YADA_TEST where COL2=?i','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test DELETE with NUMBER','delete from YADA_TEST where COL3=?n','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test DELETE with DATE','delete from YADA_TEST where COL4=?d','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test DELETE with TIME','delete from YADA_TEST where COL5=?t','YADABOT','YADA');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test INSERT','insert into yada_test (col1,col2,col3,col4,col5,token) VALUES (?v,?i,?n,?d,?t,?v)','YADABOT','YADA');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test INSERT SELECT with STRING','insert into yada_test (col1,col2,col3,col4,col5) select col1,col2,col3,col4,col5 from yada_test where col1=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test INSERT SELECT with STRING LITERAL','insert into yada_test (col1,col2,col3,col4,col5) select col1,col2,col3,col4,col5 from yada_test where col1=''Z'' or col1=''A''','YADABOT','YADA');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test INSERT with FUNCTION','insert into yada_test (col1,col2,col3,col4,col5) VALUES (?v,?i,?n,to_date(?v,''YYYY-MM-DD''),?t)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test INSERT with LITERAL INTEGER','insert into yada_test (col1,col2,col3,col4,col5) VALUES (?v,1,?n,?d,?t)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test INSERT with LITERAL NUMBER','insert into yada_test (col1,col2,col3,col4,col5) VALUES (?v,?i,1.1,?d,?t)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test INSERT with LITERAL ORACLE DATE','insert into yada_test (col1,col2,col3,col4,col5) VALUES (?v,?i,?n,''04-MAR-13'',?t)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test INSERT with LITERAL STANDARD DATE','insert into yada_test (col1,col2,col3,col4,col5) VALUES (?v,?i,?n,''2013-03-04'',?t)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test INSERT with LITERAL STRING','insert into yada_test (col1,col2,col3,col4,col5) VALUES (''A'',?i,?n,?d,?t)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test INSERT with LITERAL TIME','insert into yada_test (col1,col2,col3,col4,col5) VALUES (?v,?i,?n,?d,''2015-09-05 20:44:33'')','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test INSERT with LITERAL ORACLE TIME','insert into yada_test (col1,col2,col3,col4,col5) VALUES (?v,?i,?n,?d,''05-SEP-15 08:44:33.0 PM'')','YADABOT','YADA');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT','select * from yada_test','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT INTEGER with INS','select * from yada_test where col2 in (?i)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT VARCHAR with INS','select * from yada_test where col1 in (?v)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT NUMBER with INS','select * from yada_test where col3 in (?n)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT DATE with INS','select * from yada_test where col4 in (?d)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT TIME with INS','select * from yada_test where col5 in (?t)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT with multiple INS','select * from yada_test where col1 in (?v) and col2 in (?i)','YADABOT','YADA');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE DATE','update yada_test set col4=?d where col1=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE DATE with INS','update yada_test set col4=?d where col1 in (?v)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE DATE with LITERAL','update yada_test set col4=''2013-04-14'' where col1=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE DATE with LITERAL and INS','update yada_test set col4=''2013-04-14'' where col1 in (?v)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE DATE with ORACLE LITERAL','update yada_test set col4=''04-MAR-13'' where col1=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE DATE with ORACLE LITERAL and INS','update yada_test set col4=''04-MAR-13'' where col1 in (?v)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE DATE with STRING LITERAL and INS in WHERE','update yada_test set col4=?d where (col1=''A'' or col1 = ''Z'') and col3 in (?n)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE DATE with STRING LITERAL in WHERE','update yada_test set col4=?d where (col1=''A'' or col1 = ''Z'')','YADABOT','YADA');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE INTEGER','update yada_test set col2=?i where col1=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE INTEGER and NUMBER','update yada_test set col2=?i, col3=?n where col1=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE INTEGER with INS','update yada_test set col2=?i where col1 in (?v)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE INTEGER with LITERAL','update yada_test set col2=''22'' where col1=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE INTEGER with LITERAL and INS','update yada_test set col2=''22'' where col1 in (?v)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE INTEGER with STRING LITERAL and INS in WHERE','update yada_test set col2=?i where  (col1=''A'' or col1 = ''Z'') and col3 in (?n)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE INTEGER with STRING LITERAL in WHERE','update yada_test set col2=?i where  (col1=''A'' or col1 = ''Z'')','YADABOT','YADA');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE NUMBER','update yada_test set col3=?n where col1=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE NUMBER with INS','update yada_test set col3=?n where col1 in (?v)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE NUMBER with LITERAL','update yada_test set col3=''2.2'' where col1=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE NUMBER with LITERAL and INS','update yada_test set col3=''2.2'' where col1 in (?v)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE NUMBER with STRING LITERAL and INS in WHERE','update yada_test set col3=?n where (col1=''A'' or col1=''Z'') and col2 in (?i)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE NUMBER with STRING LITERAL in WHERE','update yada_test set col3=?n where (col1=''A'' or col1=''Z'')','YADABOT','YADA');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE TIME','update yada_test set col5=?t where col1=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE TIME with INS','update yada_test set col5=?t where col1 in (?v)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE TIME with LITERAL','update yada_test set col5=''2015-09-05 20:44:46.0'' where col1=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE TIME with LITERAL and INS','update yada_test set col5=''2015-09-05 20:44:46.0'' where col1 in (?v)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE TIME with ORACLE LITERAL','update yada_test set col5=''05-SEP-15 08:44:46.0 PM'' where col1=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE TIME with ORACLE LITERAL and INS','update yada_test set col5=''05-SEP-15 08:44:46.0 PM'' where col1 in (?v)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE TIME with STRING LITERAL and INS in WHERE','update yada_test set col5=?t where (col1=''A'' or col1=''Z'') and col2 in (?i)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE TIME with STRING LITERAL in WHERE','update yada_test set col5=?t where (col1=''A'' or col1=''Z'')','YADABOT','YADA');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE STRING','update yada_test set col1=?v where col2=?i','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE STRING with INS','update yada_test set col1=?v where col2 in (?i)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE STRING with INTEGER LITERAL and INS in WHERE','update yada_test set col1=?v where (col2=''1'' or col2=''2'' or col2=''10'') and col3 in (?n)','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE STRING with INTEGER LITERAL in WHERE','update yada_test set col1=?v where (col2=''1'' or col2=''10'')','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE STRING with LITERAL','update yada_test set col1=''Z'' where col2=?i','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test UPDATE STRING with LITERAL and INS','update yada_test set col1=''Z'' where col2 in (?i)','YADABOT','YADA');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test delete test data','delete from YADA_TEST where COL2 > ?i','YADABOT','YADA');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('QGO search','id=?v&format=json','YADABOT','QGO');

-- will create the file 'test.txt' in the 'io/in' directory, with the content in ?v.
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADAFSIN test write','/test.txt<?v','YADABOT','YADAFSIN');
-- will append the value of ?v to the file 'text.txt' in dir 'io/in'
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADAFSIN test append','/test.txt<<?v','YADABOT','YADAFSIN');
-- will list the content of 'io/in/?v'
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADAFSIN test read dir','/?v','YADABOT','YADAFSIN');
-- will return the content of 'io/in/?v'
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADAFSIN test read content','/?v','YADABOT','YADAFSIN');

-- Harmony Map tests
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test harmony map 1','select col1,col2,col3 from YADA_TEST','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test harmony map 2','select col1,col4,col5 from YADA_TEST','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test harmony map 3','select col1,col5 from YADA_TEST','YADABOT','YADA');

-- Join tests
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT JOIN A','select col1,col2,col3 from YADA_TEST','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT JOIN B','select col1,col4,col5 from YADA_TEST','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT JOIN C','select col1,col2,col3,col4,col5 from YADA_TEST where col1=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT JOIN D1','select col1,col2 from YADA_TEST','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT JOIN D2','select col2,col3 from YADA_TEST','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT JOIN D3','select col3,col4,col5 from YADA_TEST','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT JOIN E1','select col1,col2 from YADA_TEST where col1=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT JOIN E2','select col2,col3 from YADA_TEST where col2=?i','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT JOIN E3','select col3,col4,col5 from YADA_TEST where col3=?n','YADABOT','YADA');

-- Security
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec zero params zero polcols','select * from yada_test','YADABOT','YADA');           -- no params
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec zero params zero polcols protector','select * from yada_test','YADABOT','YADA'); -- no polcols
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec zero params zero polcols','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec zero params zero polcols','a',1,'content.policy=void');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec zero params zero polcols','YADA test sec zero params zero polcols protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param zero polcols','select * from yada_test where col1=?v','YADABOT','YADA'); -- one param
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param zero polcols protector','select * from yada_test','YADABOT','YADA');     -- no polcols
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param zero polcols','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param zero polcols','a',1,'content.policy=void');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec one param zero polcols','YADA test sec one param zero polcols protector','E','whitelist');


INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param one derived polcol','select * from yada_test where col1=?v','YADABOT','YADA');           -- one param
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param one derived polcol protector','select * from yada_test where col1=?v','YADABOT','YADA'); -- one polcol
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param one derived polcol','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param one derived polcol','a',1,'content.policy=void,execution.policy.indices=0');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec one param one derived polcol','YADA test sec one param one derived polcol protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param one derived polcol indexes','select * from yada_test where col1=?v','YADABOT','YADA');           -- one param
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param one derived polcol indexes protector','select * from yada_test where col1=?v','YADABOT','YADA'); -- one polcol
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param one derived polcol indexes','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param one derived polcol indexes','a',1,'content.policy=void,execution.policy.indexes=0');          -- indexes
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec one param one derived polcol indexes','YADA test sec one param one derived polcol indexes protector','E','whitelist');


INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param one polcol use token','select * from yada_test where col1=?v','YADABOT','YADA');                   -- one param
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param one polcol use token protector','select * from yada_test where token=?v','YADABOT','YADA'); -- token
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param one polcol use token','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param one polcol use token','a',1,'content.policy=void,execution.policy.indices=1');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec one param one polcol use token','YADA test sec one param one polcol use token protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param one polcol use token indexes','select * from yada_test where col1=?v','YADABOT','YADA');                   -- one param
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param one polcol use token indexes protector','select * from yada_test where token=?v','YADABOT','YADA'); -- token
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param one polcol use token indexes','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param one polcol use token indexes','a',1,'content.policy=void,execution.policy.indexes=1');                  -- indexes
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec one param one polcol use token indexes','YADA test sec one param one polcol use token indexes protector','E','whitelist');


INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec zero params one polcol use token','select * from yada_test','YADABOT','YADA');                                 -- no params
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec zero params one polcol use token protector','select * from yada_test where token=?v','YADABOT','YADA'); -- token
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec zero params one polcol use token','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec zero params one polcol use token','a',1,'content.policy=void,execution.policy.indices=0');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec zero params one polcol use token','YADA test sec zero params one polcol use token protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec zero params one polcol use token indexes','select * from yada_test','YADABOT','YADA');                                 -- no params
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec zero params one polcol use token indexes protector','select * from yada_test where token=?v','YADABOT','YADA'); -- token
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec zero params one polcol use token indexes','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec zero params one polcol use token indexes','a',1,'content.policy=void,execution.policy.indexes=0');                  -- indexes
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec zero params one polcol use token indexes','YADA test sec zero params one polcol use token indexes protector','E','whitelist');


INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params no polcols','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA'); -- multi params
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params no polcols protector','select * from yada_test','YADABOT','YADA');                 -- no polcols
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params no polcols','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params no polcols','a',1,'content.policy=void');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params no polcols','YADA test sec multi params no polcols protector','E','whitelist');


INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params one polcol','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA'); -- multiparams
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params one polcol protector','select * from yada_test where col1=?v','YADABOT','YADA');   -- one polcol
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params one polcol','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params one polcol','a',1,'content.policy=void,execution.policy.indices=0');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params one polcol','YADA test sec multi params one polcol protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params one polcol indexes','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');   -- multiparams
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params one polcol indexes protector','select * from yada_test where col1=?v','YADABOT','YADA');     -- one polcol
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params one polcol indexes','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params one polcol indexes','a',1,'content.policy=void,execution.policy.indexes=0');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params one polcol indexes','YADA test sec multi params one polcol indexes protector','E','whitelist'); -- indexes


INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params one polcol use token','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');       -- multiparams
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params one polcol use token protector','select * from yada_test where token=?v','YADABOT','YADA'); -- token
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params one polcol use token','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params one polcol use token','a',1,'content.policy=void,execution.policy.indices=2');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params one polcol use token','YADA test sec multi params one polcol use token protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params one polcol use token indexes','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');       -- multiparams
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params one polcol use token indexes protector','select * from yada_test where token=?v','YADABOT','YADA'); -- token
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params one polcol use token indexes','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params one polcol use token indexes','a',1,'content.policy=void,execution.policy.indexes=2');                  -- indexes
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params one polcol use token indexes','YADA test sec multi params one polcol use token indexes protector','E','whitelist');


INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params multi polcol','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');            -- multiparams
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params multi polcol protector','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');  -- multi polcols
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params multi polcol','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params multi polcol','a',1,'content.policy=void,execution.policy.indices=0 1');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params multi polcol','YADA test sec multi params multi polcol protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params multi polcol indexes','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');            -- multiparams
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params multi polcol indexes protector','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');  -- multi polcols
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params multi polcol indexes','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params multi polcol indexes','a',1,'content.policy=void,execution.policy.indexes=0 1');                     -- indexes
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params multi polcol indexes','YADA test sec multi params multi polcol indexes protector','E','whitelist');


INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params multi polcol use token','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');                   -- multiparams 
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params multi polcol use token protector','select * from yada_test where col1=?v and token=?v','YADABOT','YADA'); -- one polcol + token
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params multi polcol use token','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params multi polcol use token','a',1,'content.policy=void,execution.policy.indices=0 2');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params multi polcol use token','YADA test sec multi params multi polcol use token protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params multi polcol use token indexes','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');                   -- multiparams 
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params multi polcol use token indexes protector','select * from yada_test where col1=?v and token=?v','YADABOT','YADA'); -- one polcol + token
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params multi polcol use token indexes','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params multi polcol use token indexes','a',1,'content.policy=void,execution.policy.indexes=0 2');                            -- indexes
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params multi polcol use token indexes','YADA test sec multi params multi polcol use token indexes protector','E','whitelist');

-- security json

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec zero params zero polcols jp','select * from yada_test','YADABOT','YADA');           -- no params
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec zero params zero polcols jp protector','select * from yada_test','YADABOT','YADA'); -- no polcols
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec zero params zero polcols jp','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec zero params zero polcols jp','a',1,'content.policy=void');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec zero params zero polcols jp','YADA test sec zero params zero polcols jp protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param zero polcols jp','select * from yada_test where col1=?v','YADABOT','YADA'); -- one param
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param zero polcols jp protector','select * from yada_test','YADABOT','YADA');     -- no polcols
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param zero polcols jp','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param zero polcols jp','a',1,'content.policy=void');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec one param zero polcols jp','YADA test sec one param zero polcols jp protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param one derived polcol jp','select * from yada_test where col1=?v','YADABOT','YADA');           -- one param
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param one derived polcol jp protector','select * from yada_test where col1=?v','YADABOT','YADA'); -- one polcol
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param one derived polcol jp','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param one derived polcol jp','a',1,'content.policy=void,execution.policy.columns=COL1');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec one param one derived polcol jp','YADA test sec one param one derived polcol protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param one polcol use token jp','select * from yada_test where col1=?v','YADABOT','YADA');                   -- one param
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param one polcol use token jp protector','select * from yada_test where token=?v','YADABOT','YADA'); -- token
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param one polcol use token jp','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param one polcol use token jp','a',1,'content.policy=void,execution.policy.columns=TOKEN');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec one param one polcol use token jp','YADA test sec one param one polcol use token jp protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec zero params one polcol use token jp','select * from yada_test','YADABOT','YADA');                                 -- no params
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec zero params one polcol use token jp protector','select * from yada_test where token=?v','YADABOT','YADA'); -- token
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec zero params one polcol use token jp','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec zero params one polcol use token jp','a',1,'content.policy=void,execution.policy.columns=TOKEN');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec zero params one polcol use token jp','YADA test sec zero params one polcol use token jp protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params no polcols jp','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA'); -- multi params
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params no polcols jp protector','select * from yada_test','YADABOT','YADA');                 -- no polcols
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params no polcols jp','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params no polcols jp','a',1,'content.policy=void');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params no polcols jp','YADA test sec multi params no polcols protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params one polcol jp','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA'); -- multiparams
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params one polcol jp protector','select * from yada_test where col1=?v','YADABOT','YADA');   -- one polcol
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params one polcol jp','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params one polcol jp','a',1,'content.policy=void,execution.policy.columns=COL1');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params one polcol jp','YADA test sec multi params one polcol jp protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params one polcol use token jp','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');       -- multiparams
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params one polcol use token jp protector','select * from yada_test where token=?v','YADABOT','YADA'); -- token
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params one polcol use token jp','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params one polcol use token jp','a',1,'content.policy=void,execution.policy.columns=TOKEN');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params one polcol use token jp','YADA test sec multi params one polcol use token jp protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params multi polcol jp','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');            -- multiparams
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params multi polcol jp protector','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');  -- multi polcols
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params multi polcol jp','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params multi polcol jp','a',1,'content.policy=void,execution.policy.columns=COL1 COL2');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params multi polcol jp','YADA test sec multi params multi polcol jp protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params multi polcol use token jp','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');                   -- multiparams 
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params multi polcol use token jp protector','select * from yada_test where col1=?v and token=?v','YADABOT','YADA'); -- one polcol + token
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params multi polcol use token jp','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params multi polcol use token jp','a',1,'content.policy=void,execution.policy.columns=COL1 TOKEN');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params multi polcol use token jp','YADA test sec multi params multi polcol use token jp protector','E','whitelist');

-- security multi syntax

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param one derived polcol uni','select * from yada_test where col1=?v','YADABOT','YADA');           -- one param
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param one derived polcol uni protector','select * from yada_test where col1=?v','YADABOT','YADA'); -- one polcol
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param one derived polcol uni','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param one derived polcol uni','a',1,'content.policy=void,execution.policy.columns=COL1,execution.policy.indices=0');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec one param one derived polcol uni','YADA test sec one param one derived polcol protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param one polcol use token uni','select * from yada_test where col1=?v','YADABOT','YADA');                   -- one param
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec one param one polcol use token uni protector','select * from yada_test where token=?v','YADABOT','YADA'); -- token
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param one polcol use token uni','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec one param one polcol use token uni','a',1,'content.policy=void,execution.policy.columns=TOKEN,execution.policy.indices=1');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec one param one polcol use token uni','YADA test sec one param one polcol use token uni protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec zero params one polcol use token uni','select * from yada_test','YADABOT','YADA');                                 -- no params
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec zero params one polcol use token uni protector','select * from yada_test where token=?v','YADABOT','YADA'); -- token
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec zero params one polcol use token uni','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec zero params one polcol use token uni','a',1,'content.policy=void,execution.policy.columns=TOKEN,execution.policy.indices=0');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec zero params one polcol use token uni','YADA test sec zero params one polcol use token uni protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params one polcol uni','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA'); -- multiparams
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params one polcol uni protector','select * from yada_test where col1=?v','YADABOT','YADA');   -- one polcol
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params one polcol uni','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params one polcol uni','a',1,'content.policy=void,execution.policy.columns=COL1,execution.policy.indices=0');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params one polcol uni','YADA test sec multi params one polcol uni protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params one polcol use token uni','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');       -- multiparams
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params one polcol use token uni protector','select * from yada_test where token=?v','YADABOT','YADA'); -- token
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params one polcol use token uni','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params one polcol use token uni','a',1,'content.policy=void,execution.policy.columns=TOKEN,execution.policy.indices=2');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params one polcol use token uni','YADA test sec multi params one polcol use token uni protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params multi polcol uni','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');            -- multiparams
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params multi polcol uni protector','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');  -- multi polcols
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params multi polcol uni','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params multi polcol uni','a',1,'content.policy=void,execution.policy.columns=COL1 COL2,execution.policy.indices=0 1');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params multi polcol uni','YADA test sec multi params multi polcol uni protector','E','whitelist');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params multi polcol use token uni','select * from yada_test where col1=?v and col2=?i','YADABOT','YADA');                   -- multiparams 
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec multi params multi polcol use token uni protector','select * from yada_test where col1=?v and token=?v','YADABOT','YADA'); -- one polcol + token
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params multi polcol use token uni','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec multi params multi polcol use token uni','a',1,'content.policy=void,execution.policy.columns=COL1 TOKEN,execution.policy.indices=0 2');
INSERT into YADA_A11N (target,qname,policy,type) VALUES ('YADA test sec multi params multi polcol use token uni','YADA test sec multi params multi polcol use token uni protector','E','whitelist');

-- security content policy

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec get token','select * from yada_test','YADABOT','YADA');   
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec get token','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec get token','a',1,'execution.policy=void,content.policy.predicate=token=getQToken()');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec get token twice','select * from yada_test','YADABOT','YADA');   
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec get token twice','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec get token twice','a',1,'execution.policy=void,content.policy.predicate=token=getQToken() and token=getQToken()');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec get token twice with spaces','select * from yada_test','YADABOT','YADA');   
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec get token twice with spaces','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec get token twice with spaces','a',1,'execution.policy=void,content.policy.predicate=token = getQToken() and token = getQToken()');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec get token plus params','select * from yada_test where col1=?v','YADABOT','YADA');   
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec get token plus params','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec get token plus params','a',1,'execution.policy=void,content.policy.predicate=token=getQToken()');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec get token twice plus params','select * from yada_test where col1=?v','YADABOT','YADA');   
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec get token twice plus params','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec get token twice plus params','a',1,'execution.policy=void,content.policy.predicate=token=getQToken() and token=getQToken()');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec get token twice with spaces plus params','select * from yada_test where col1=?v','YADABOT','YADA');   
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec get token twice with spaces plus params','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec get token twice with spaces plus params','a',1,'execution.policy=void,content.policy.predicate=token = getQToken() and token = getQToken()');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec get token plus params with group by','select * from yada_test where col1=?v group by col1, col2, col3, col4, col5, token','YADABOT','YADA');   
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec get token plus params with group by','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec get token plus params with group by','a',1,'execution.policy=void,content.policy.predicate=token=getQToken()');

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test sec get token plus params with group by order by','select * from yada_test where col1=?v group by col1, col2, col3, col4, col5, token order by col1, col2','YADABOT','YADA');   
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec get token plus params with group by order by','pl',1,'Gatekeeper');
INSERT into YADA_PARAMS (target,name,rule,value) VALUES ('YADA test sec get token plus params with group by order by','a',1,'execution.policy=void,content.policy.predicate=token=getQToken()');
