#!/bin/bash

# JSON object to pass to Lambda Function
json={"\"row\"":50,"\"col\"":10,"\"bucketname\"":\"test.bucket.562.zaclu\"","\"filename\"":\"test.csv\""}

echo $json
echo "Invoking Lambda function using API Gateway"
time output=`curl -s -H "Content-Type: application/json" -X POST -d  $json https://7kczqayj0d.execute-api.us-east-1.amazonaws.com/processCSV_dev`

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
