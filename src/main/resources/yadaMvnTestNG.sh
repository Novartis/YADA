#!/bin/bash

usage() { echo "Usage: $0 [-x surefire|failsafe|all] [-p test|test-pre9] [-Xtd]" 1>&2; exit 1; }

SUSPEND=n
MAVEN_DEBUG=
TOGGLE_TESTS=
LOG_LEVEL="-Dlog.level=info"
SUREFIRE=
FAILSAFE=
DEBUG=
PROFILE=test

while getopts "x:Xtdp:" opt; do
  case ${opt} in
    x )
      SUSPEND=y
      if [ "surefire" = "$OPTARG" ]
      then
        SUREFIRE=true
      elif [ "failsafe" = "$OPTARG" ]
      then
        FAILSAFE=true
      elif [ "all" = "$OPTARG" ]
      then
        SUREFIRE=true
        FAILSAFE=true
      fi
      ;;
    X )
      MAVEN_DEBUG=-X
      ;;
    t )
      TOGGLE_TESTS=-Dtest.toggle=/conf/tmp_TestNG_toggle.properties
      ;;
    d )
      LOG_LEVEL="-Dlog.level=debug"
      ;;
    p )
      PROFILE="$OPTARG"
      ;;
    * ) usage
      ;;
  esac
done



MAVEN=mvn
WORKSPACE=/Users/dvaron/Documents/work
YADA_SRCDIR=$WORKSPACE/YADA
CONTAINER=tomcat
TOMCAT_VERSION=8x
YADA_LOCAL_TOMCAT_HOME=-DYADA.local.tomcat.home=$WORKSPACE/containers/$CONTAINER$TOMCAT_VERSION/
MVN_DEPLOYMENT_GOAL=-Ddeployment.goal=start
LOG=$YADA_SRCDIR/src/main/resources/testng.log
LOG_STDOUT=-Dlog.stdout=true
#SKIP_SUREFIRE=-Dsurefire.skip=true
SKIP_LICENSE=-Dlicense.skip=true
# SKIP_DB_LOAD=-Dskip.db.load=true
SKIP_JAVADOC=-Dmaven.javadoc.skip=true
SKIP_SOURCE=-Dmaven.source.skip=true
#TOGGLE_TESTS=-Dtest.toggle=/conf/tmp_TestNG_toggle.properties

COMMON_VARS="\
$LOG_STDOUT \
$MVN_DEPLOYMENT_GOAL \
$SKIP_SUREFIRE \
$SKIP_LICENSE \
$SKIP_DB_LOAD \
$SKIP_JAVADOC \
$SKIP_SOURCE \
$TOGGLE_TESTS \
$LOG_LEVEL"

rm $LOG
cd $YADA_SRCDIR


if [ "y" = "$SUSPEND" ]
then
  # Remember: If the debugger fails with an ExceptionInitializer error or it can't
  # find the Finder class, it's probably because there is an open build artifact
  # in one of the tools, e.g., YADA.properties
  SUREFIRE_DEBUG="-Dmaven.surefire.debug"
  FAILSAFE_DEBUG="-Dmaven.failsafe.debug"
  if [ "${SURFIRE}" ]
  then
      DEBUG="${SUREFIRE_DEBUG}"
  elif [ "${FAILSAFE}" ]
  then
      DEBUG="${DEBUG} ${FAILSAFE_DEBUG}"
  fi
  DEBUG="${DEBUG} -DYADA_LIB=${YADA_LIB}"
fi

CMD="$MAVEN $MAVEN_DEBUG clean verify -P${PROFILE},deploy-war $DEBUG -Dsuspend.debugger=$SUSPEND $COMMON_VARS"
echo $CMD
exec $CMD > >(tee -i $LOG)
echo "[$$] ${CMD}"

perl -e 'while (<>) {chomp;if (/^Tests run.+Time/) {print $_."\n";}}' < $LOG
