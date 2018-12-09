import json
import boto3
import uuid

def lambda_handler(event, context):
    
    lambdaClient = boto3.client("lambda", region_name="us-east-1")
    
    # Invoke service 1
    payload1 = {
        'bucketname': event['bucketname'],
        'filename': 'data/' + event['filename']
    }
    resp1 = lambdaClient.invoke(
        FunctionName = "service1_data_transformation", 
        InvocationType = "RequestResponse", 
        Payload = json.dumps(payload1)
    )
    data1 = resp1['Payload'].read()
    
    # Invoke service 2
    payload2 = {
        'bucketname': event['bucketname'],
        'filename': 'TransformedData/' + event['filename']
    }
    resp2 = lambdaClient.invoke(
        FunctionName = "service2_data_load", 
        InvocationType = "RequestResponse", 
        Payload = json.dumps(payload2)
    )
    data2 = resp2['Payload'].read()
    
    # Invoke service 3
    payload3 = {
        'bucketname': event['bucketname'],
        'filename': 'SalesRecordsDB/' + event['filename'].replace(" ", "_") + ".db",
        'filter': event['filter'],
        'aggregation': event['aggregation']
    }
    resp3 = lambdaClient.invoke(
        FunctionName = "service3_filtering_and_aggregation", 
        InvocationType = "RequestResponse", 
        Payload = json.dumps(payload3)
    )
    data3 = resp3['Payload'].read()

    return {
        'uuid': str(uuid.uuid4()),
        'error': '',
        'response1': json.loads(data1),
        'response2': json.loads(data2),
        'response3': json.loads(data3)
    }
    