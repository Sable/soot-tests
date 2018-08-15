#!/bin/bash

TARGET="commons-lang"
SOOT_JAR=$1
ANDROID_JARS=$2

# setup

rm -f "${TARGET}.asm.out"
rm -f "${TARGET}.dexpler.out"

# run
R=0
./runtest.sh "asm" "$SOOT_JAR" "$ANDROID_JARS" &> "${TARGET}.asm.out"
R=$(($? + $R))
./runtest.sh "dexpler" "$SOOT_JAR" "$ANDROID_JARS" &> "${TARGET}.dexpler.out"
R=$(($? + $R))

exit $R
