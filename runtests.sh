#!/bin/bash

TMP_DIR=`pwd`"/tmp/"
SOOT_URL="https://ssebuild.cased.de/nightly/soot/lib/soot-trunk.jar"
SOOT_JAR=${TMP_DIR}/"`basename $SOOT_URL`"
ANOROID_JARS="/opt/android-platforms/"

COMMONS_IO="commons-io"

# setup 
rm -f *.status
rm -rf $TMP_DIR
wget -P $TMP_DIR http://vandyk.st.informatik.tu-darmstadt.de/abc/sootclasses.jar


# run
R=0
cd ./${COMMONS_IO}
./runSequential.sh  $SOOT_JAR $ANDROID_JARS
R=$(($? + $R))
cd ..
echo $R > ./${COMMONS_IO}.status

# clean
rm -rf $TMP_DIR

# exit code
exit $R
