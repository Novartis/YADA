#!/bin/bash

DB=$1
if [ -z "${DB}" ]
then 
	echo "You must include a db arg, e.g, PostgreSQL, MySQL, SQLite"
	exit 1 
fi
CMD=""

YADA_SCRIPTDIR="${project.basedir}/../yada-war/src/main/resources/scripts"
cat $YADA_SCRIPTDIR/YADA_db_${DB}.sql ${project.build.testOutputDirectory}/YADA_query_essentials.sql $YADA_SCRIPTDIR/YADA_query_tests.sql > db.tmp
case $DB in
PostgreSQL)
	EXEC=`which psql`
	if [ -n "${EXEC}" ]
	then
		CMD="psql -q -U yada -w -d yada -f db.tmp"
		echo "${CMD}"
		${CMD}
	else
		echo "The psql executable is not in your path"
		exit 1
	fi
	;;
MySQL)
	EXEC=`which mysql`
	if [ -n "${EXEC}" ]
	then
		CMD="mysql -u yada -pyada yada"
		echo "${CMD}"
		${CMD} < db.tmp
	else
		echo "The mysql executable is not in your path"
		exit 1
	fi
	;;	
SQLite)
	EXEC=`which sqlite3`
	if [ -n "${EXEC}" ]
	then
		CMD="sqlite3 ${YADA_SCRIPTDIR}/../../webapp/YADA.db"
		echo "${CMD}"
		${CMD} < db.tmp
	else
		echo "The sqlite3 executable is not in your path"
		exit 1
	fi
	;;
*)
	exit;
	;;
esac



