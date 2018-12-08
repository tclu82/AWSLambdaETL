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
import faasinspector.register;
import model.QueryResult;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * uwt.lambda_test::handleRequest
 *
 * @author wlloyd
 */
public class Service3FilteringAndAggregation implements RequestHandler<Request, Response> {

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
        setCurrentDirectory("/tmp");
        
        String filter =  request.getFilter();
        String aggregation =  request.getAggregation();
        
        String bucketname = request.getBucketname();
        String filename = request.getFilename();
        String[] names = filename.split("/");
        String dbname = names[names.length - 1];

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
        //get db file using source bucket and srcKey name and save to /tmp
        File file = new File("/tmp/"+dbname);
        s3Client.getObject(new GetObjectRequest(bucketname, filename), file);
        // StringBuilder sb = new StringBuilder();
        List<QueryResult> results = new LinkedList<>();

        try {
            // Connection string for a file-based SQlite DB
            Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbname); 

            // Detect if the table 'salesrecords' exists in the database
            PreparedStatement ps = con.prepareStatement(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='salesrecords'"
                );
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                // 'salesrecords' does not exist, throw exception
                logger.log("No such table: 'salesrecords'");
                throw new SQLException("No such table: 'salesrecords'");
            }
            rs.close();
            
            // create query from request
            String query = "SELECT " + aggregation + " FROM salesrecords WHERE " + filter;
            ps = con.prepareStatement(query);
            rs = ps.executeQuery();

            // Write query result to output
            String[] aggregations = aggregation.split(",");
            if (rs.next()) {
                for (int i=0; i<aggregations.length; i++) {
                    query = aggregations[i];
                    double value = Double.parseDouble(rs.getString(i+1));
                    QueryResult queryResult = new QueryResult(query, value);
                    results.add(queryResult);
                }
            } else {
                // No result when query with given filter
                logger.log("No result when query");
            }
            rs.close();
            con.close();
        }
        catch (SQLException sqle) {
            logger.log("DB ERROR:" + sqle.toString());
            sqle.printStackTrace();
        }
        // r.setValue(sb.toString());
        // r.setValue(results);
        r.setResults(results);
        return r;
    }

    public static boolean setCurrentDirectory(String directory_name) {
        boolean result = false; // Boolean indicating whether directory was set
        File directory;         // Desired current working directory

        directory = new File(directory_name).getAbsoluteFile();
        if (directory.exists() || directory.mkdirs())
            result = (System.setProperty("user.dir", directory.getAbsolutePath()) != null);

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
        Service3FilteringAndAggregation lt = new Service3FilteringAndAggregation();
        Request req = new Request();
        System.out.println("cmd-line param name=" + req.getBucketname());
        // Run the function
        Response resp = lt.handleRequest(req, c);
        // Print out function result
        System.out.println("function result:" + resp.toString());
    }
}