#!/bin/bash -x

# set a uuid for the results xml file name in S3
UUID=$(cat /proc/sys/kernel/random/uuid)

if [ $# -ne 2 ]; then
  echo "Usage: $CMDNAME test_id test_name" 1>&2
  exit 1
fi

TEST_ID=${1:-0001}
TEST_NAME=${2:-scenario1}
SCRIPT="scripts/taurus.yaml"

echo "TEST_ID:: ${TEST_ID}"
echo "TEST_NAME:: ${TEST_NAME}"
echo "S3_BUCKET ${S3_BUCKET}"
echo "UUID ${UUID}"

echo "Running test"
stdbuf -i0 -o0 -e0 bzt ${SCRIPT} -${TEST_NAME} | stdbuf -i0 -o0 -e0 tee -a result.tmp | sed -u -e "s|^|$TEST_ID |"
CALCULATED_DURATION=`cat result.tmp | grep -m1 "Test duration" | awk -F ' ' '{ print $5 }' | awk -F ':' '{ print ($1 * 3600) + ($2 * 60) + $3 }'`

echo "Upload results to S3"
# every file goes under $TEST_ID/$PREFIX/$UUID to distinguish the result correctly
ARTIFACTS_DIR=`cat result.tmp | grep -m1 "Artifacts dir" | awk -F ' ' '{ print $5 }'`
REPORT_FILES=`find ${ARTIFACTS_DIR} -name "*.jtl" -or -name "simulation.log"`
for f in "${REPORT_FILES[@]}"; do
  p="s3://$S3_BUCKET/results/$TEST_ID/JMeter_Result/$UUID/`basename $f`"

  echo "Uploading $p"
  # aws s3 cp $f $p
done

if [ -f /tmp/artifacts/results.xml ]; then
  echo "Validating Test Duration"
  TEST_DURATION=`xmlstarlet sel -t -v "/FinalStatus/TestDuration" /tmp/artifacts/results.xml`

  if (( $(echo "$TEST_DURATION > $CALCULATED_DURATION" | bc -l) )); then
    echo "Updating test duration: $CALCULATED_DURATION s"
    xmlstarlet ed -L -u /FinalStatus/TestDuration -v $CALCULATED_DURATION /tmp/artifacts/results.xml
  fi

  echo "Uploading results"
  # aws s3 cp /tmp/artifacts/results.xml s3://$S3_BUCKET/results/${TEST_ID}/${PREFIX}-${UUID}.xml
else
  echo "There might be an error happened while the test."
fi