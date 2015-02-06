#!/bin/bash
#
# Usage: ./runtests.sh "commons-io" "commons-lang"

TMP_DIR=`pwd`"/tmp/"
#SOOT_URL="https://ssebuild.cased.de/nightly/soot/lib/soot-trunk.jar"
SOOT_PATH="../ICAProject/lib/soot-trunk.jar"
SOOT_JAR=${TMP_DIR}/"`basename $SOOT_PATH`"
SOOT_BUILD_FILE="../ICAPROJECT/build.xml"
ANDROID_JARS="/opt/android-platforms/"


# setup
rm -f *.status
rm -rf $TMP_DIR
mkdir $TMP_DIR
ant -file $SOOT_BUILD_FILE fulljar
cp $SOOT_PATH $TMP_DIR


# run
for i in "$@"; do
  TARGET_TEST_DIR=$i
  R=0
  cd ./${TARGET_TEST_DIR}
  ./runSequential.sh  $SOOT_JAR $ANDROID_JARS
  R=$(($? + $R))
  cd ..
  echo $R > ./${TARGET_TEST_DIR}.status
done

# clean
rm -rf $TMP_DIR

# exit code
exit $R
