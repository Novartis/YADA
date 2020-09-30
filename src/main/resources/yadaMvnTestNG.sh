#!/bin/bash

usage() {
printf "Usage: $0 [-T [surefire|failsafe]] [-x surefire|failsafe] [-p test|test_pre9] [-Xtdsi] \n\n \
  -T  Execute either surefire (api) or failsafe (http) testing. Omit argument to suppress both. \n \
      Failsafe suppression will also suppress cargo deployment. Default (option -T omitted altogether) \n \
      is to execute both. \n \
  -x  debug surefire or failsafe execution. Leave argument empty to debug both. \n \
  -p  choose the profile. 'test' is the currently preferred test profile.  \n \
      If 'test_pre9' is selected, the YADA.properties will be modified accordingly. \n \
  -X  show maven debug output. \n \
  -t  use the 'tmp_toggle' file to cherry pick tests. Default is all tests. \n \
  -d  show java debug log output. Default log level is 'info'. \n \
  -s  deloy snapshot to maven central.  Implies -T \n \
  -i  test the webapp interactively. Combine with  '-x failsafe' to debug as well \n \
  -?  show this help \n\n \
" 1>&2; exit 1; }

SUSPEND=n
MAVEN_DEBUG=
TOGGLE_TESTS=
LOG_LEVEL="-Dlog.level=info"
SUREFIRE_X=
FAILSAFE_X=
DEBUG=
PROFILE=
YADA_PROPS=
SKIP_SUREFIRE=
SKIP_FAILSAFE=
DEPLOY_SNAPSHOT=0
INTERACTIVE=0

OPTERR=0
while getopts "x:Xtdp:T:s" opt; do
  case ${opt} in
    i )
      INTERACTIVE=1
      ;;
    s )
      DEPLOY_SNAPSHOT=1
      ;;
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
        SKIP_SUREFIRE=-Dsurefire.skip=true
        SKIP_FAILSAFE="-Dskip.tests=true -Dskip.deploy.war=true"
      fi
      ;;
    x )
      SUSPEND=y
      if [ "surefire" == "$OPTARG" ]
      then
        SUREFIRE_X=1
      elif [ "failsafe" == "$OPTARG" ]
      then
        FAILSAFE_X=1
      elif [ "all" = "$OPTARG" ] || [ -z "$OPTARG"]
      then
        SUREFIRE_X=1
        FAILSAFE_X=1
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
    ? ) usage
      ;;
  esac
done


CMD=
MAVEN=mvn
WORKSPACE=/Users/dvaron/Documents/work
YADA_SRCDIR=$WORKSPACE/YADA
CONTAINER=tomcat
TOMCAT_VERSION=8x
YADA_LOCAL_TOMCAT_HOME=-DYADA.local.tomcat.home=$WORKSPACE/containers/$CONTAINER$TOMCAT_VERSION/
MVN_DEPLOYMENT_GOAL=-Ddeployment.goal=start
LOG=$YADA_SRCDIR/src/main/resources/testng.log
LOG_STDOUT=-Dlog.stdout=true
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
  SUREFIRE_DEBUG="-Dmaven.surefire.debug"
  FAILSAFE_DEBUG="-Dmaven.failsafe.debug"
  if [ 1 -eq "${SUREFIRE_X}" ]
  then
      DEBUG="${DEBUG} ${SUREFIRE_DEBUG}"
  elif [ 1 -eq "${FAILSAFE_X}" ]
  then
      DEBUG="${DEBUG} ${FAILSAFE_DEBUG}"
  fi
  DEBUG="${DEBUG} -DYADA_LIB=${YADA_LIB}"
fi

if [ 1 -eq "$DEPLOY_SNAPSHOT" ]
then
  CMD="$MAVEN ${SKIP_FAILSAFE} ${SKIP_SUREFIRE} ${SKIP_LICENSE} -DskipTests=true clean deploy"
elif [ 1 -eq "$INTERACTIVE" ]
then
  if [ 1 -eq "$FAILSAFE_X" ]
  then
    DEBUG='-Dcargo.start.jvmargs="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 -Xnoagent -Djava.compiler=NONE'
  fi
  GOAL=org.codehaus.cargo:cargo-maven2-plugin:run
  CONTAINER_ID=-Dcargo.maven.containerId=tomcat8x
  CONTAINER_URL=-Dcargo.maven.containerUrl=https://repo.maven.apache.org/maven2/org/apache/tomcat/tomcat/8.5.49/tomcat-8.5.49.zip
  CMD="${MAVEN} ${GOAL} ${CONTAINER_ID} ${CONTAINER_URL} ${DEBUG}"
else
  CMD="$MAVEN $MAVEN_DEBUG clean verify -P${PROFILE},deploy-war $DEBUG -Dsuspend.debugger=$SUSPEND $COMMON_VARS"
fi
echo $CMD
exec $CMD > >(tee -i $LOG)
echo "[$$] ${CMD}"

perl -e 'while (<>) {chomp;if (/^Tests run.+Time/) {print $_."\n";}}' < $LOG
