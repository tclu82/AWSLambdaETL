#!/bin/bash

# JSON object to pass to Lambda Function
json={""\"bucketname\"":"\"tcss562.group.project\"","\"filename\"":"\"data/100\u0020Sales\u0020Records.csv\""}

echo $json
echo "Invoking Lambda function using API Gateway"
time output=`curl -s -H "Content-Type: application/json" -X POST -d  $json https://2iued6l2ak.execute-api.us-east-1.amazonaws.com/ETL_DEV/service1-data-transformation`

echo ""
echo "CURL RESULT:"
echo $output
echo ""
echo ""

: '
echo "Invoking Lambda function using AWS CLI"
time output=`aws lambda invoke --invocation-type RequestResponse --function-name createCSV --region us-east-1 --payload $json /dev/stdout | head -n 1 | head -c -2 ; echo`
echo ""
echo "AWS CLI RESULT:"
echo $output
echo ""
'