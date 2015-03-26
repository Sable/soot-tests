#!/bin/bash
#
# Usage: ./buildSootAndRunTests.sh "commons-io" "commons-lang"

set -xv #verbose

TMP_DIR=`pwd`"/tmp"
COV_DIR=`pwd`"/coverage/libs"

#SOOT_URL="https://ssebuild.cased.de/nightly/soot/lib/soot-trunk.jar"
SOOT_PATH="../ICAProject/lib/soot-trunk.jar"
SOOT_SRC_PATH="../ICAProject/lib/sootsources-trunk.jar"
SOOT_JAR=${TMP_DIR}/"`basename ${SOOT_PATH}`"
SOOT_SRC=${TMP_DIR}/"`basename ${SOOT_SRC_PATH}`"
SOOT_BUILD_FILE="../ICAPROJECT/build.xml"
JCOV_FILE_SAVER_JAR=${COV_DIR}"/jcov_file_saver.jar"
ANDROID_JARS="/opt/android-platforms/"

export CLASSPATH=${JCOV_FILE_SAVER_JAR}":"${SOOT_JAR}

# setup
rm -f *.status
rm -rf $TMP_DIR
mkdir $TMP_DIR
ant -file $SOOT_BUILD_FILE fulljar sourcejar
cp $SOOT_PATH $TMP_DIR
cp $SOOT_SRC_PATH $TMP_DIR

# instrument soot for coverage
java -jar ${COV_DIR}/jcov.jar Instr -t ${TMP_DIR}/template.xml ${SOOT_JAR}
cp ${TMP_DIR}/template.xml coverage.xml

# run
for i in "$@"; do
  TARGET_TEST_DIR=$i
  cp ${TMP_DIR}/template.xml ${TARGET_TEST_DIR}
  R=0
  cd ./${TARGET_TEST_DIR}
  ./runSequential.sh  $SOOT_JAR $ANDROID_JARS
  R=$(($? + $R))
  cd ..
  echo $R > ./${TARGET_TEST_DIR}.status

  # merge and remove the coverage data
  java -jar ${COV_DIR}/jcov.jar Merger -o coverage.xml coverage.xml ${TARGET_TEST_DIR}/result.xml
  rm ${TARGET_TEST_DIR}/template.xml ${TARGET_TEST_DIR}/result.xml
done

# create the coverage report
java -jar ${COV_DIR}/jcov.jar RepGen -src ${SOOT_SRC} coverage.xml

# clean
rm -rf $TMP_DIR

# exit code
exit $R
