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
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import faasinspector.register;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;

/**
 * uwt.lambda_test::handleRequest
 *
 * @author wlloyd
 */
public class ProcessCSV implements RequestHandler<Request, Response> {

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

        // *********************************************************************
        // Implement Lambda Function Here
        // *********************************************************************
        int row = request.getRow();
        int col = request.getCol();
        String bucketname = request.getBucketname();
        String filename = request.getFilename();

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
        //get object file using source bucket and srcKey name
        S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketname, filename));
        //get content of the file
        InputStream objectData = s3Object.getObjectContent();
        //scanning data line by line
        String textToUpload = "";
        Scanner scanner = new Scanner(objectData);
        while (scanner.hasNext()) {
            textToUpload += scanner.nextLine();
        }
        scanner.close();
        
        long total = 0;
        double avg = 0.0;
        
        logger.log("ProcessCSV bucketname:" + bucketname 
                + " filename:" + filename 
                + " avg­element:" + avg 
                + " total:" + total);
        
        r.setValue("Bucket: " + bucketname + " filename: " + filename + " processed.");
        return r;
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
        ProcessCSV lt = new ProcessCSV();

        // Create a request object
//        String bucketname = args[0];
//        String filename = args[1];
//        int row = Integer.parseInt(args[2]);
//        int col = Integer.parseInt(args[3]);
//        Request req = new Request(bucketname, filename, row, col);
        Request req = new Request();

        // Grab the name from the cmdline from arg 0
//        String name = (args.length > 0 ? args[0] : "");
        // Load the name into the request object
        // req.setName(name);
        // Report name to stdout
        System.out.println("cmd-line param name=" + req.getBucketname());

        // Run the function
        Response resp = lt.handleRequest(req, c);

        // Print out function result
        System.out.println("function result:" + resp.toString());
    }
}
