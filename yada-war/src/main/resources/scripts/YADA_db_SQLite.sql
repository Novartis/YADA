-- to create database on cmdline cd to workspace/containers/sqlite 
--   on windows execute 'C:\path\to\sqlite3.exe YADA.db'
--   on unix    execute /path/to/sqlite3 YADA.db
--   then '.read YADA_db_SQLite.sql'
--   then '.read YADA_query_essentials.sql'
--   then '.read YADA_query_tests.sql'

CREATE TABLE IF NOT EXISTS YADA_QUERY
(	
	QNAME        VARCHAR(255)  NOT NULL,
	QUERY        VARCHAR(4000) NOT NULL, 
	APP          VARCHAR(20), 
	CREATED      TIMESTAMP,
	MODIFIED     TIMESTAMP, 
	CREATED_BY   VARCHAR(20), 
	MODIFIED_BY  VARCHAR(20), 
	LAST_ACCESS  TIMESTAMP, 
	ACCESS_COUNT INT DEFAULT 0,
	COMMENTS     VARCHAR(4000)
);

CREATE TABLE IF NOT EXISTS YADA_QUERY_CONF 
(	
	APP     VARCHAR(20)   NOT NULL, 
	SOURCE  VARCHAR(4000) NOT NULL, 
	VERSION VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS YADA_PARAMS 
(	
	TARGET VARCHAR(20)   NOT NULL, 
	NAME   VARCHAR(20)   NOT NULL, 
	VALUE  VARCHAR(4000) NOT NULL, 
	RULE   VARCHAR(1)    DEFAULT 1
);
   
CREATE TABLE IF NOT EXISTS YADA_TEST 
(	
	COL1 VARCHAR(3), 
	COL2 INT,
	COL3 DOUBLE(2,1), 
	COL4 DATE,
	COL5 TIMESTAMP
);

-- INSERT YADA INDEX QUERY CONF
DELETE from YADA_QUERY_CONF where app = 'YADA' and source = 'java:comp/env/jdbc/yada';
INSERT into YADA_QUERY_CONF (APP,SOURCE,VERSION) values ('YADA','java:comp/env/jdbc/yada',null);
INSERT into YADA_QUERY_CONF (APP,SOURCE,VERSION) values ('QGO','http://www.ebi.ac.uk/QuickGO/GTerm?',null);
INSERT into YADA_QUERY_CONF (APP,SOURCE,VERSION) values ('YADAFSIN','file:///io/in',null);
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA default','select ''YADA is alive''','YADABOT','YADA');
