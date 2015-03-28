#!/bin/bash
#
# Usage: ./buildSootAndRunTests.sh -coverage "commons-io" "commons-lang"

#set -xv #verbose

TMP_DIR=`pwd`"/tmp"
COV_DIR=`pwd`"/coverage/libs"

if [ ! -f "./soot-tests.cfg" ]; then
  echo "Please provide configuration file soot-tests.cfg !"
  exit 1
else 
  echo "Reading configuration file soot-tests.cfg!"
fi

source ./soot-tests.cfg

if [ -z $SOOT_PATH ]; then
  echo "No SOOT_PATH given!"
  exit 1;
fi

if [ ! -f $SOOT_PATH ]; then
  echo $SOOT_PATH
  echo "SOOT_PATH is invalid!"
  exit 1
fi

if [ -z $SOOT_SRC_PATH ]; then
  echo "No SOOT_SRC_PATH given!"
  exit 1;
fi

if [ ! -f $SOOT_SRC_PATH ]; then
  echo "SOOT_SRC_PATH is invalid!"
  exit 1
fi

if [ -z $SOOT_BUILD_FILE ]; then
  echo "No SOOT_BUILD_FILE given!"
  exit 1;
fi

if [ ! -f $SOOT_BUILD_FILE ]; then
  echo "SOOT_BUILD_FILE is invalid!"
  exit 1
fi

args=("$@")

coverage=0
if [ ${args[0]} = "-coverage" ]; then
  echo "Measuring coverage!"
  coverage=1
else 
  echo "NOT measuring coverage!"
fi

#SOOT_URL="https://ssebuild.cased.de/nightly/soot/lib/soot-trunk.jar"
#SOOT_PATH="../../ICAProject/lib/soot-trunk.jar"
#SOOT_SRC_PATH="../../ICAProject/lib/sootsources-trunk.jar"
SOOT_JAR=${TMP_DIR}/"`basename ${SOOT_PATH}`"
SOOT_SRC=${TMP_DIR}/"`basename ${SOOT_SRC_PATH}`"
#SOOT_BUILD_FILE="../../ICAProject/build.xml"
JCOV_FILE_SAVER_JAR=${COV_DIR}"/jcov_file_saver.jar"
ANDROID_JARS="/opt/android-platforms/"


# set classpath
if [ $coverage -eq 1 ]; then
  export CLASSPATH=${JCOV_FILE_SAVER_JAR}":"${SOOT_JAR}
else 
  export CLASSPATH=${SOOT_JAR}
fi


# setup
rm -f *.status
rm -rf $TMP_DIR
mkdir $TMP_DIR
ant -file $SOOT_BUILD_FILE fulljar sourcejar
cp $SOOT_PATH $TMP_DIR
cp $SOOT_SRC_PATH $TMP_DIR

if [ $coverage -eq 1 ]; then
  # instrument soot for coverage
  java -jar ${COV_DIR}/jcov.jar Instr -t ${TMP_DIR}/template.xml ${SOOT_JAR}
  cp ${TMP_DIR}/template.xml coverage.xml
fi

# run
i=$coverage
cnt=${#args[@]}
for (( ;i<$cnt;i++)); do
  TARGET_TEST_DIR=${args[${i}]}
  echo "Target: "$TARGET_TEST_DIR
  if [ $coverage -eq 1 ]; then
    cp ${TMP_DIR}/template.xml ${TARGET_TEST_DIR}
  fi
  R=0
  cd ./${TARGET_TEST_DIR}
  ./runSequential.sh  $SOOT_JAR $ANDROID_JARS
  R=$(($? + $R))
  cd ..
  echo $R > ./${TARGET_TEST_DIR}.status

  if [ $coverage -eq 1 ]; then
    # merge and remove the coverage data
    java -jar ${COV_DIR}/jcov.jar Merger -o coverage.xml coverage.xml ${TARGET_TEST_DIR}/result.xml
    rm ${TARGET_TEST_DIR}/template.xml ${TARGET_TEST_DIR}/result.xml
  fi
done
if [ $coverage -eq 1 ]; then
  # create the coverage report
  java -jar ${COV_DIR}/jcov.jar RepGen -src ${SOOT_SRC} coverage.xml
fi

# clean
rm -rf $TMP_DIR

# exit code
exit $R
