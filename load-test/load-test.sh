#!/bin/bash
set -o pipefail

if [ $# -lt 2 ]; then
  echo "Usage: $CMDNAME test_id test_name [options ...]" 1>&2
  exit 1
fi

# set a uuid for the results xml file name in S3
UUID=$(cat /proc/sys/kernel/random/uuid)

TEST_ID=${1}
TEST_NAME=${2}
shift 2
SCRIPT="scripts/taurus.yaml"
S3_DIR="s3://$S3_BUCKET/results/$TEST_ID"
OPTIONS=$@
echo "TEST_ID:: ${TEST_ID}"
echo "TEST_NAME:: ${TEST_NAME}"
echo "UUID ${UUID}"

echo "Running test"
stdbuf -i0 -o0 -e0 bzt ${SCRIPT} -${TEST_NAME} ${OPTIONS} | stdbuf -i0 -o0 -e0 tee -a result.tmp | sed -u -e "s|^|$TEST_ID |"
BZT_EXIT_CODE=$?
if [ $BZT_EXIT_CODE -ne 0 ]; then
  echo "Stopping test with exit code: ${BZT_EXIT_CODE}"
  exit 1
fi

# calculated test duration from result.tmp
CALCULATED_DURATION=`cat result.tmp | grep -m1 "Test duration" | awk -F ' ' '{ print $5 }' | awk -F ':' '{ print ($1 * 3600) + ($2 * 60) + $3 }'`

echo "Upload results to S3"
LOGS_DIR=`cat result.tmp | grep -m1 "Artifacts dir" | awk -F ' ' '{ print $5 }'`
aws s3 cp $LOGS_DIR $S3_DIR/logs/$UUID/ --exclude "*" --include "*.jtl" --include "simulation.log" --recursive

if [ ! -f /tmp/artifacts/results.xml ]; then
  echo "There might be an error happened while the test."
  exit 1
fi

echo "Validating Test Duration"
TEST_DURATION=`xmlstarlet sel -t -v "/FinalStatus/TestDuration" /tmp/artifacts/results.xml`

if (( $(echo "$TEST_DURATION > $CALCULATED_DURATION" | bc -l) )); then
  echo "Updating test duration: $CALCULATED_DURATION s"
  xmlstarlet ed -L -u /FinalStatus/TestDuration -v $CALCULATED_DURATION /tmp/artifacts/results.xml
fi

echo "Uploading results"
aws s3 cp /tmp/artifacts/results.xml $S3_DIR/artifacts/$UUID.xml
