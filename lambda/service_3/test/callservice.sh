#!/bin/bash

# JSON object to pass to Lambda Function
json={"\"bucketname\"":"\"tcss562.group.project\"","\"filename\"":"\"SalesRecordsDB/$1_Sales_Records.csv.db\"","\"filter\"":"\"Region='Europe'\u0020AND\u0020Order_Priority='Medium'\"","\"aggregation\"":"\"AVG(Gross_Margin),AVG(Order_Processing_Time)\""}

echo $json
# echo "Invoking Lambda function using API Gateway"
# time output=`curl -s -H "Content-Type: application/json" -X POST -d  $json https://toocqdpoy2.execute-api.us-east-1.amazonaws.com/ETL_DEV/service3-filtering-and-aggregation`

# echo ""
# echo "CURL RESULT:"
# echo $output
# echo ""
# echo ""

echo "Invoking Lambda function using AWS CLI"
time output=`aws lambda invoke --invocation-type RequestResponse --function-name service3_filtering_and_aggregation --region us-east-1 --payload $json /dev/stdout | head -n 1 | head -c -2 ; echo`
echo ""
echo "AWS CLI RESULT:"
echo $output | jq
echo ""