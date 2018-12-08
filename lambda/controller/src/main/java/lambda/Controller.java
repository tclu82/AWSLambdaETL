/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lambda;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import faasinspector.register;
import java.nio.charset.Charset;

/**
 * uwt.lambda_test::handleRequest
 *
 * @author wlloyd
 */
public class Controller implements RequestHandler<Request, Response> {

    static String CONTAINER_ID = "/tmp/container-id";
    static Charset CHARSET = Charset.forName("US-ASCII");

    // Lambda Function Handler
    public Response handleRequest(Request request, Context context) {
        // Create logger
        LambdaLogger logger = context.getLogger();

        // Register function
        register reg = new register(logger);

        // stamp container with uuid
        Response r = reg.StampContainer();

        String bucketname = request.getBucketname();
        String filename = request.getFilename();

        // Define the AWS Region
        Regions region = Regions.fromName("us-east-1");
        // Instantiate AWSLambdaClientBuilder to build the Lambda client
        AWSLambdaClientBuilder builder = AWSLambdaClientBuilder.standard().withRegion(region);
        // Build the client, which will ultimately invoke the function"bucketname": "tcss562.group.project",
        AWSLambda client = builder.build();
        
        String payload = "{\"filename\":\"" + getBucketname + "\",\"filename\":\"data/" + filename + "\"}"; 
        logger.log("Input JSON: " + payload);

        InvokeResult result = InvokeLambda(client, "service1_data_transformation", payload);
        
        r.setValue(results.getPayload());
        return r;
    }

    private InvokeResult InvokeLambda(AWSLambda client, String functionName, String payload) {
        // Create an InvokeRequest with required parameters
        InvokeRequest req = new InvokeRequest().withFunctionName(functionName).withPayload(payload);
        // Invoke the function and capture response
        InvokeResult result = client.invoke(req);
        return result;
    }

    // int main enables testing function from cmd line
    public static void main(String[] args) {
        Context c = new Context() {
            @Override
            public String getAwsRequestId() {
                return "";
            }

            @Override
            public String getLogGroupName() {
                return "";
            }

            @Override
            public String getLogStreamName() {
                return "";
            }

            @Override
            public String getFunctionName() {
                return "";
            }

            @Override
            public String getFunctionVersion() {
                return "";
            }

            @Override
            public String getInvokedFunctionArn() {
                return "";
            }

            @Override
            public CognitoIdentity getIdentity() {
                return null;
            }

            @Override
            public ClientContext getClientContext() {
                return null;
            }

            @Override
            public int getRemainingTimeInMillis() {
                return 0;
            }

            @Override
            public int getMemoryLimitInMB() {
                return 0;
            }

            @Override
            public LambdaLogger getLogger() {
                return new LambdaLogger() {
                    @Override
                    public void log(String string) {
                        System.out.println("LOG:" + string);
                    }
                };
            }
        };

        // Create an instance of the class
        Controller lt = new Controller();
        Request req = new Request();
        System.out.println("cmd-line param name=" + req.getBucketname());
        // Run the function
        Response resp = lt.handleRequest(req, c);
        // Print out function result
        System.out.println("function result:" + resp.toString());
    }
}