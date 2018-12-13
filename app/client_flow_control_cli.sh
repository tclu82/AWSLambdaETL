#!/bin/bash

# JSON object to pass to Service 1 - Data Transformation
json1={"\"bucketname\"":"\"tcss562.group.project\"","\"filename\"":"\"data/$1\u0020Sales\u0020Records.csv\""}

echo $json1
echo "Invoking Service 1 using AWS CLI"
time output1=`aws lambda invoke --invocation-type RequestResponse --function-name service1_data_transformation --region us-east-1 --payload $json1 /dev/stdout | head -n 1 | head -c -2 ; echo`
echo ""
echo "INVOKE RESULT:"
echo $output1 | jq
echo ""

# JSON object to pass to Service 2 - Data Load
json2={"\"bucketname\"":"\"tcss562.group.project\"","\"filename\"":"\"TransformedData/$1\u0020Sales\u0020Records.csv\""}

echo $json2
echo "Invoking Service 2 using AWS CLI"
time output2=`aws lambda invoke --invocation-type RequestResponse --function-name service2_data_load --region us-east-1 --payload $json2 /dev/stdout | head -n 1 | head -c -2 ; echo`
echo ""
echo "INVOKE RESULT:"
echo $output2 | jq
echo ""

# JSON object to pass to Service 3 - Filtering and Aggregation
json3={"\"bucketname\"":"\"tcss562.group.project\"","\"filename\"":"\"SalesRecordsDB/$1_Sales_Records.csv.db\"","\"filter\"":"\"Region='Europe'\u0020AND\u0020Order_Priority='Medium'\"","\"aggregation\"":"\"AVG(Gross_Margin),AVG(Order_Processing_Time)\""}

echo $json3
echo "Invoking Service 3 using AWS CLI"
time output3=`aws lambda invoke --invocation-type RequestResponse --function-name service3_filtering_and_aggregation --region us-east-1 --payload $json3 /dev/stdout | head -n 1 | head -c -2 ; echo`
echo ""
echo "INVOKE RESULT:"
echo $output3 | jq
echo ""