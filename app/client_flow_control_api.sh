#!/bin/bash

# JSON object to pass to Service 1 - Data Transformation
json1={"\"bucketname\"":"\"tcss562.group.project\"","\"filename\"":"\"data/$1\u0020Sales\u0020Records.csv\""}

echo $json1
echo "Invoking Service 1 using API Gateway"
time output1=`curl -s -H "Content-Type: application/json" -X POST -d  $json1 https://toocqdpoy2.execute-api.us-east-1.amazonaws.com/ETL_DEV/service1-data-transformation`
echo ""
echo "INVOKE RESULT:"
echo $output1 | jq
echo ""

# JSON object to pass to Service 2 - Data Load
json2={"\"bucketname\"":"\"tcss562.group.project\"","\"filename\"":"\"TransformedData/$1\u0020Sales\u0020Records.csv\""}

echo $json2
echo "Invoking Service 2 using API Gateway"
time output2=`curl -s -H "Content-Type: application/json" -X POST -d  $json2 https://toocqdpoy2.execute-api.us-east-1.amazonaws.com/ETL_DEV/service2-data-load`
echo ""
echo "INVOKE RESULT:"
echo $output2 | jq
echo ""

# JSON object to pass to Service 3 - Filtering and Aggregation
json3={"\"bucketname\"":"\"tcss562.group.project\"","\"filename\"":"\"SalesRecordsDB/$1_Sales_Records.csv.db\"","\"filter\"":"\"Region='Europe'\u0020AND\u0020Order_Priority='Medium'\"","\"aggregation\"":"\"AVG(Gross_Margin),AVG(Order_Processing_Time)\""}

echo $json3
echo "Invoking Service 3 using API Gateway"
time output3=`curl -s -H "Content-Type: application/json" -X POST -d  $json3 https://toocqdpoy2.execute-api.us-east-1.amazonaws.com/ETL_DEV/service3-filtering-and-aggregation`
echo ""
echo "INVOKE RESULT:"
echo $output3 | jq
echo ""