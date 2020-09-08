#!/bin/bash

usage() { echo "Usage: $0 [-T surefire|failsafe] [-x surefire|failsafe] [-p test|test_pre9] [-Xtd]" 1>&2; exit 1; }

SUSPEND=n
MAVEN_DEBUG=
TOGGLE_TESTS=
LOG_LEVEL="-Dlog.level=info"
SUREFIRE_X=
FAILSAFE_X=
DEBUG=
PROFILE=test
YADA_PROPS=
SKIP_SUREFIRE=
SKIP_FAILSAFE=

while getopts "x:Xtdp:T:" opt; do
  case ${opt} in
    T )
      if [ "surefire" == "$OPTARG" ]
      then
        SKIP_SUREFIRE=
        SKIP_FAILSAFE="-Dskip.tests=true -Dskip.deploy.war=true"
      elif [ "failsafe" == "$OPTARG" ]
      then
        SKIP_SUREFIRE=-Dsurefire.skip=true
        SKIP_FAILSAFE=
      elif [ -z "$OPTARG" ]
      then
        SKIP_SUREFIRE=
        SKIP_FAILSAFE=
      fi
      ;;
    x )
      SUSPEND=y
      if [ "surefire" == "$OPTARG" ]
      then
        SUREFIRE_X=true
      elif [ "failsafe" == "$OPTARG" ]
      then
        FAILSAFE_X=true
      elif [ "all" = "$OPTARG" ] || [ -z "$OPTARG"]
      then
        SUREFIRE_X=true
        FAILSAFE_X=true
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
      if [ "test_pre9" == "$PROFILE" ]
      then
        YADA_PROPS=-DYADA.properties.path=/YADA_pre9.properties
      fi
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
$SKIP_FAILSAFE \
$SKIP_LICENSE \
$SKIP_DB_LOAD \
$SKIP_JAVADOC \
$SKIP_SOURCE \
$TOGGLE_TESTS \
$LOG_LEVEL \
$YADA_PROPS"

rm $LOG
cd $YADA_SRCDIR


if [ "y" = "$SUSPEND" ]
then
  # Remember: If the debugger fails with an ExceptionInitializer error or it can't
  # find the Finder class, it's probably because there is an open build artifact
  # in one of the tools, e.g., YADA.properties
  SUREFIRE_DEBUG="-Dmaven.surefire.debug"
  FAILSAFE_DEBUG="-Dmaven.failsafe.debug"
  if [ ! -z "${SUREFIRE_X}" ]
  then
      DEBUG="${DEBUG} ${SUREFIRE_DEBUG}"
  elif [ ! -z "${FAILSAFE_X}" ]
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
