

UPDATE YADA_PARAM
SET VALUE = 'Gatekeeper,content.policy=void,execution.policy.columns=app:getValue(TARGET) userid:getLoggedUser()'
WHERE VALUE = 'Gatekeeper,content.policy=void,execution.policy.columns=app:getValue(TARGET) uid:getLoggedUser()';


UPDATE YADA_PARAM
SET VALUE = 'Gatekeeper,content.policy=void,execution.policy.columns=userid:getLoggedUser()'
WHERE VALUE='Gatekeeper,content.policy=void,execution.policy.columns=uid:getLoggedUser()';


UPDATE YADA_PARAM
SET VALUE ='Gatekeeper,content.policy=void,execution.policy.columns=qname:getValue(TARGET) userid:getLoggedUser()'
WHERE VALUE = 'Gatekeeper,content.policy=void,execution.policy.columns=qname:getValue(TARGET) uid:getLoggedUser()';


-- perhaps:
-- DROP TABLE IF EXIST YADA_USER_X;
-- ALTER TABLE YADA_USER RENAME TO YADA_USER_X;
-- CREATE TABLE YADA_USER (USERID VARCHAR(255) NOT NULL,PW  VARCHAR(20) NOT NULL);
-- INSERT into YADA_USER(USERID,PW) SELECT UID,PW FROM YADA_USER_X;
-- DELETE from YADA_USER_X where UID in (SELECT USERID FROM YADA_USER);

-- DROP TABLE IF EXIST YADA_UG_X;
-- ALTER TABLE YADA_UG RENAME TO YADA_UG_X;
-- CREATE TABLE YADA_UG (APP VARCHAR(20) NOT NULL, USERID VARCHAR(255) NOT NULL, ROLE VARCHAR(20) NOT NULL);
-- INSERT into YADA_UG(APP,USERID,ROLE) SELECT APP,UID,ROLE from YADA_UG;
-- DELETE FROM YADA_UG_X WHERE UID IN (SELECT distinct USERID from YADA_UG);

-- We delete instead of drop because we've not tested all this...
-- but if you're feeling lucky:
-- DROP TABLE YADA_UG_X;
-- DROP TABLE YADA_USER_X;

