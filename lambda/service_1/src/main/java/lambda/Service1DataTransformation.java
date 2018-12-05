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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * uwt.lambda_test::handleRequest
 *
 * @author wlloyd
 */
public class Service1DataTransformation implements RequestHandler<Request, Response> {

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
        StringWriter sw = new StringWriter();
        String line = scanner.nextLine();
        // Add column [Order Processing Time] at the end of first row
        line += ",Order Processing Time,Gross Margin\n";
        sw.append(line);
        // Check duplication order ID
        Set<Long> orderIdSet = new HashSet<>();
        // Build a map
        String[] p1 = {"L", "M", "H", "C"};
        String[] p2 = {"Low", "Medium", "High", "Critical"};
        Map<String, String> priorityMap = new HashMap<>();
        for (int i=0; i<p1.length; i++) {
            priorityMap.put(p1[i], p2[i]);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        while (scanner.hasNext()) {
            line = scanner.nextLine();
            String[] token = line.split(",");
            // Calculate processing days
            String orderDateString = token[5];
            String shipDateString = token[7];
            int processingDays = 0;
            long orderId = Long.parseLong(token[6]);
            // Duplication detect, ignore current record
            if (orderIdSet.contains(orderId)) {
                System.out.println("Duplication detect: " + orderId);
                continue;
            }
            orderIdSet.add(orderId);
            try {
                Date orderDate = dateFormat.parse(orderDateString);
                Date shipDate = dateFormat.parse(shipDateString);
                long difference = shipDate.getTime() - orderDate.getTime();
                processingDays = (int) (difference / 1000 / 60/ 60 / 24);
            } catch (ParseException ex) {
                Logger.getLogger(Service1DataTransformation.class.getName()).log(Level.SEVERE, null, ex);
            }
            // Calculate Gross Margin
            double profit = Double.parseDouble(token[token.length -1]);
            double revenue = Double.parseDouble(token[token.length -3]);
            double grossMargin = profit / revenue;
            // Map "L" to "Low", "M" to "Medium", "H" to "High", and "C" to "Critical"
            String priority = token[4];
            token[4] = priorityMap.get(priority);
            // Convert to String and write back to StringWriter.
            line = String.join(",", token);
            String lastToken = String.format(",%d,%.2f\n", processingDays, grossMargin);
            line += lastToken;
            sw.append(line);
        }
        scanner.close();
        
        byte[] bytes = sw.toString().getBytes(StandardCharsets.UTF_8);
        InputStream is = new ByteArrayInputStream(bytes);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(bytes.length);
        meta.setContentType("text/plain");
        String[] names = filename.split("/");
        String filename2 = "TransformedData/" + names[names.length -1];
        s3Client.putObject(bucketname, filename2, is, meta);
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
        Service1DataTransformation lt = new Service1DataTransformation();
        Request req = new Request();
        System.out.println("cmd-line param name=" + req.getBucketname());
        // Run the function
        Response resp = lt.handleRequest(req, c);
        // Print out function result
        System.out.println("function result:" + resp.toString());
    }
}
