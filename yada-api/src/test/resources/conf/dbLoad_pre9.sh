#!/bin/bash
DATE=`date +%Y%m%d%H%M%S`
DATADIR=${project.basedir}/../../data
BAKDIR=${DATADIR}/backup
DB_TMP=/tmp/db.tmp
DB_USER=${YADA.index.username}
DB_NAME=yada
DB_PASS=${YADA.index.password}
DB_HOST=${YADA.index.host}
PGPASSWORD=${DB_PASS}
YADA_SCRIPTDIR="${project.basedir}/../yada-war/src/main/resources/scripts"


EXEC_POSTGRESQL=psql
CMD_POSTGRESQL="${EXEC_POSTGRESQL} -q  -h ${DB_HOST} -U ${DB_USER} -w -d ${DB_NAME} -f ${DB_TMP}"
BAK_POSTGRESQL=pg_dump
BAK_POSTGRESQL_ARCH=${BAKDIR}/${DB_NAME}-${BAK_POSTGRESQL}${DATE}.gz
BAK_CMD_POSTGRESQL="${BAK_POSTGRESQL}  -h ${DB_HOST} -U ${DB_USER} -w ${DB_NAME} | gzip -c > ${BAK_POSTGRESQL_ARCH}"

DB=$1
BU=$2

if [ -z "${DB}" ] # -z = empty string
then
	echo "You must include a db arg, e.g, PostgreSQL, MySQL, SQLite, HSQLdb"
	exit 1
fi

function load() {
	_EXEC=`which $1`
	if [ -n "${_EXEC}" ] # -n non-null local load exec
	then
		if [ -n "$3" ] # -n non-null local backup exec
		then
			_BAK_EXEC=`which $3`
			if [ -n "${_BAK_EXEC}" ]  # -n nun-null local backup exec
			then
				_BAK_CMD=$4
			  echo "${_BAK_CMD}"
			  eval ${_BAK_CMD}
			else
				echo "The $3 executable, for backup, is not in your path"
				exit 2
			fi
		fi
		_CMD=$2
		echo "${_CMD}"
		eval ${_CMD}
	else
		echo "The $1 executable is not in your path"
		exit 1
	fi
}

LOADEXEC=`echo $DB | awk '{print "EXEC_" toupper($0)}'`
LOADCMD=`echo $DB | awk '{print "CMD_" toupper($0)}'`
LOADBAK_EXEC=
LOADBAK_CMD=
if [ ! -z "${BU}" ] && [ "false" != "${BU}" ]
then
	LOADBAK_EXEC=`echo $DB | awk '{print "BAK_" toupper($0)}'`
	LOADBAK_CMD=`echo $DB | awk '{print "BAK_CMD_" toupper($0)}'`
fi

# The '!' character is for parameter redirection.
# See http://stackoverflow.com/questions/1921279/how-to-get-a-variable-value-if-variable-name-is-stored-as-string
# and https://www.gnu.org/software/bash/manual/html_node/Shell-Parameter-Expansion.html

rm ${DB_TMP}
cat ${project.build.testOutputDirectory}/conf/YADA_db_${DB}.sql \
${project.build.testOutputDirectory}/conf/YADA_query_essentials.sql \
${project.build.testOutputDirectory}/conf/YADA_query_tests.sql > ${DB_TMP}
if [ "SQLITE" != "${DB}" ]
then
  echo "COMMIT;" >> ${DB_TMP}
fi
load "${!LOADEXEC}" "${!LOADCMD}" "${!LOADBAK_EXEC}" "${!LOADBAK_CMD}"
