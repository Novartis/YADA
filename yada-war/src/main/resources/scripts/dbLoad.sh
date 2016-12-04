#!/bin/bash

YADA_SCRIPTDIR="${project.basedir}/../yada-war/src/main/resources/scripts"
SQLITE_DB=${YADA_SCRIPTDIR}/../../webapp/YADA.db
HSQLDB_DB=${YADA_SCRIPTDIR}/../../webapp/YADADB

EXEC_POSTGRESQL=psql
CMD_POSTGRESQL="psql -q -U yada -w -d yada -f db.tmp"

EXEC_MYSQL=mysql
CMD_MYSQL="mysql -u yada -pyada yada < db.tmp"

EXEC_SQLITE=sqlite3
CMD_SQLITE="sqlite3 ${SQLITE_DB} < db.tmp"

EXEC_HSQLDB=java
# requires SqlTool to be on classpath (sqltool-2.3.4.jar)
CMD_HSQLDB="java org.hsqldb.cmdline.SqlTool yada db.tmp"

DB=$1
if [ -z "${DB}" ]
then 
	echo "You must include a db arg, e.g, PostgreSQL, MySQL, SQLite, HSQLdb"
	exit 1 
fi

function load() {
	LEXEC=`which $1`
	if [ -n "${LEXEC}" ]
	then
		LCMD=$2
		echo "${LCMD}"
		${LCMD}
	else
		echo "The $1 executable is not in your path"
		exit 1
	fi
}

EXEC=`echo $DB | awk '{print "EXEC_" toupper($0)}'`
CMD=`echo $DB | awk '{print "CMD_" toupper($0)}'`

# The '!' character is for parameter redirection.  
# See http://stackoverflow.com/questions/1921279/how-to-get-a-variable-value-if-variable-name-is-stored-as-string
# and https://www.gnu.org/software/bash/manual/html_node/Shell-Parameter-Expansion.html

cat ${project.build.testOutputDirectory}/YADA_db_${DB}.sql \
${project.build.testOutputDirectory}/YADA_query_essentials.sql \
${project.build.testOutputDirectory}/YADA_query_tests.sql > db.tmp
echo "COMMIT;" >> db.tmp
load "${!EXEC}" "${!CMD}"


