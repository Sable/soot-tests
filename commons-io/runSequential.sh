#!/bin/bash

TARGET="commons-io"
SOOT_JAR=$1
ANDROID_JARS=$2

# setup

rm "${TARGET}.coffi.out"
rm "${TARGET}.asm.out"
rm "${TARGET}.dexpler.out"

# run
R=0
./runtest.sh "coffi" "$SOOT_JAR" "$ANDROID_JARS" &> "${TARGET}.coffi.out"
R=$(($? + $R))
./runtest.sh "asm" "$SOOT_JAR" "$ANDROID_JARS" &> "${TARGET}.asm.out"
R=$(($? + $R))
./runtest.sh "dexpler" "$SOOT_JAR" "$ANDROID_JARS" &> "${TARGET}.dexpler.out"
R=$(($? + $R))

exit $R
