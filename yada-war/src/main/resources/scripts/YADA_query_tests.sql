-- List of test queries
-- select 'INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('''||qname||''','''||replace(query,'''','''''')||''',''YADABOT'');' from yada_query where app = 'YADA' and qname like 'YADA test%' and qname not in ('YADA test query','YADA test two','YADA testy testy testy') order by qname;

DELETE from YADA_QUERY where app = 'YADA' and qname like 'YADA test%' and qname not in ('YADA test query','YADA test two','YADA testy testy testy');
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

INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test INSERT','insert into yada_test (col1,col2,col3,col4,col5) VALUES (?v,?i,?n,?d,?t)','YADABOT','YADA');

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
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT JOIN E2','select col2,col3 from YADA_TEST where col2=?v','YADABOT','YADA');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA test SELECT JOIN E3','select col3,col4,col5 from YADA_TEST where col3=?v','YADABOT','YADA');