#!/bin/bash

# JSON object to pass to Lambda Function
json={"\"bucketname\"":"\"tcss562.group.project\"","\"filename\"":"\"$1\u0020Sales\u0020Records.csv\"","\"filter\"":"\"Region='Europe'\u0020AND\u0020Order_Priority='Medium'\"","\"aggregation\"":"\"AVG(Gross_Margin),AVG(Order_Processing_Time)\""}

echo $json
echo "Invoking controller Lambda function using API Gateway"
time output=`curl -s -H "Content-Type: application/json" -X POST -d  $json https://toocqdpoy2.execute-api.us-east-1.amazonaws.com/ETL_DEV`
echo ""
echo "INVOKE RESULT:"
echo $output | jq
echo ""