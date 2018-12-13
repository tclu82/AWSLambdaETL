#!/bin/bash

# JSON object to pass to Lambda Function
json={"\"bucketname\"":"\"tcss562.group.project\"","\"filename\"":"\"$1\u0020Sales\u0020Records.csv\"","\"filter\"":"\"Region='Europe'\u0020AND\u0020Order_Priority='Medium'\"","\"aggregation\"":"\"AVG(Gross_Margin),AVG(Order_Processing_Time)\""}

echo $json
echo "Invoking controller Lambda function using AWS CLI"
time output=`aws lambda invoke --invocation-type RequestResponse --function-name etl_controller_python --region us-east-1 --payload $json /dev/stdout | head -n 1 | head -c -2 ; echo`
echo ""
echo "INVOKE RESULT:"
echo $output | jq
echo ""