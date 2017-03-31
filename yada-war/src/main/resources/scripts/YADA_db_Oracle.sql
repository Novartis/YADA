`CREATE USER YADA identified by 'yada';		
GRANT connect to YADA;
GRANT ALL PRIVILEGES to YADA;

CREATE TABLE YADA.YADA_QUERY
(	
	QNAME        VARCHAR2(255)  NOT NULL, 
	QUERY        VARCHAR2(4000) NOT NULL, 
	APP          VARCHAR2(20), 
	CREATED      TIMESTAMP,
	MODIFIED     TIMESTAMP, 
	CREATED_BY   VARCHAR2(20), 
	MODIFIED_BY  VARCHAR2(20), 
	LAST_ACCESS  TIMESTAMP, 
	ACCESS_COUNT INT DEFAULT 0,
	COMMENTS     VARCHAR(4000)
);

CREATE TABLE YADA.YADA_QUERY_CONF 
(	
	APP     VARCHAR2(20)   NOT NULL, 
	NAME    VARCHAR2(20),
	DESCR   VARCHAR2(400),
	SOURCE  VARCHAR2(4000), 
	CONF    VARCHAR2(4000),
	ACTIVE  INT DEFAULT 1
);

CREATE TABLE YADA.YADA_PARAM 
(	
	ID     VARCHAR2(255),
	TARGET VARCHAR2(255)   NOT NULL, 
	NAME   VARCHAR2(20)   NOT NULL, 
	VALUE  VARCHAR2(4000) NOT NULL, 
	RULE   VARCHAR2(1)    DEFAULT 1
);

CREATE TABLE YADA.YADA_A11N 
( 
	TARGET VARCHAR2(500), 
	POLICY VARCHAR2(1), 
	QNAME  VARCHAR2(2000), 
	TYPE   VARCHAR2(9)
);
   
CREATE TABLE YADA.YADA_TEST 
(	
	COL1 VARCHAR(3), 
	COL2 INT,
	COL3 NUMBER(10,5), 
	COL4 DATE,
	COL5 TIMESTAMP,
	TOKEN VARCHAR(8) DEFAULT 'YADA'
);

CREATE TABLE YADA.YADA_PROP
( 
  TARGET VARCHAR2(500), 
  NAME VARCHAR2(500),
  VALUE VARCHAR2(4000)
);

-- YADA_UG is a whitelist table for the yada-admin webapp
-- no such thing as: DROP TABLE IF EXISTS YADA_USER; in Oracle, perhaps use exec immediate with exception handling?
CREATE TABLE YADA_USER
(
  USERID VARCHAR2(255) NOT NULL,
  PW  VARCHAR2(20) NOT NULL
);

-- YADA_UG is a whitelist table for the yada-admin webapp
-- no such thing in Oracle: DROP TABLE IF EXISTS YADA_UG;
CREATE TABLE YADA_UG
(
  APP VARCHAR2(20) NOT NULL,
  USERID VARCHAR2(255) NOT NULL,
  ROLE VARCHAR2(20) NOT NULL
);



-- INSERT YADA INDEX QUERY CONF
DELETE from YADA_QUERY_CONF where app = 'YADA' and source = 'java:comp/env/jdbc/yada';
INSERT into YADA_QUERY_CONF (APP,SOURCE,CONF) values ('YADA','java:comp/env/jdbc/yada',null);
INSERT into YADA_QUERY_CONF (APP,SOURCE,CONF) values ('YADATEST','java:comp/env/jdbc/yada','jdbcUrl=jdbc:oracle:thin//localhost/yada
username=yada
password=yada
autoCommit=false
connectionTimeout=300000
idleTimeout=600000
maxLifetime=1800000
minimumIdle=5
maximumPoolSize=100
driverClassName=oracle.jdbc.OracleDriver');
INSERT into YADA_QUERY_CONF (APP,SOURCE,CONF) values ('QGO',null,'http://www.ebi.ac.uk/QuickGO/GTerm?');
INSERT into YADA_QUERY_CONF (APP,SOURCE,CONF) values ('YADAFSIN',null,'file:///io/in');
INSERT into YADA_QUERY (qname,query,created_by,app) VALUES ('YADA default','select ''YADA is alive'' from dual','YADABOT','YADA');
@YADA_query_essentials.sql;
@YADA_query_tests.sql;
