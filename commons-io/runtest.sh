#!/bin/bash


TEST_TARGET=$1   # component to test, e.g., "coffi", "asm", ...
SOOT_JAR=$2      # path to Soot jar, e.g, "/d/e/f/soot-trunk.jar"
ANDROID_JARS=$3  # path to Android jars, e.g., "/a/b/c/platforms/"
TARGET_PROGRAM="./target/commons-io-2.2-SNAPSHOT" # path to the target jar / apk Soot will read

####
# function which:
# 1) starts Soot to generate output 
# 2) then runs the test cases on the generated output
####
launch_tests () {

SOOT_INPUT=$1
SOOT_OUTPUT_DIR="./target."${TEST_TARGET}
SOOT_OPTIONS=$2

rm -rf $SOOT_OUTPUT_DIR
mkdir $SOOT_OUTPUT_DIR
java -jar $SOOT_JAR -allow-phantom-refs $SOOT_OPTIONS -process-dir $SOOT_INPUT -d $SOOT_OUTPUT_DIR
R=$?
echo "soot: "$R
rm -rf "test-reports."${TEST_TARGET}
ant -Dcommons-io=$TEST_TARGET test
R=$(($? + $R))
echo "ant: "$R
return $R
}
####

R=0

if [ "$TEST_TARGET" == "coffi" ]; then
  # launch for coffi
  launch_tests "${TARGET_PROGRAM}.jar" "-coffi -f class" 
  R=$(($? + $R))

elif [ "$TEST_TARGET" == "asm" ]; then 
  # launch for asm
  launch_tests "${TARGET_PROGRAM}.jar" " -f class" 
  R=$(($? + $R))

elif [ "$TEST_TARGET" == "dexpler" ]; then
  # launch for dexpler
  launch_tests "${TARGET_PROGRAM}.apk" "-src-prec apk -f class -android-jars $ANDROID_JARS" 
  R=$(($? + $R))

else
  echo "Wrong test target '"${TEST_TARGET}"' !"
  R=-1
fi

exit $R

