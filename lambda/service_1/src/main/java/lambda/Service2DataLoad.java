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
import com.amazonaws.services.s3.model.S3Object;

import faasinspector.register;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.Properties;

/**
 * uwt.lambda_test::handleRequest
 * 
 * @author wlloyd
 */
public class Service2DataLoad implements RequestHandler<Request, Response> {
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

        // Load csv from S3
        Scanner scanner = readCSVfromS3(bucketname, filename);

        // Write to local DB
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        writeToLocalDB(con, ps, rs, scanner, logger);

        // Write to Aurora
        LinkedList<String> ll = writeToAurora(con, ps, rs, logger);
        r.setName(ll);
        // rs.close();
        // con.close();
        // r.setValue(request.getName());
        r.setValue(request.toString());
    

        // String hello = "Hello " + request.getName();
        String hello = request.toString();

        // Print log information to the Lambda log as needed
        // logger.log("log message...");

        // Set return result in Response class, class is marshalled into JSON
        r.setValue(hello);

        return r;
    }

    private Scanner readCSVfromS3(String bucketname, String filename) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
        // get object file using source bucket and srcKey name
        S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketname, filename));
        // get content of the file
        InputStream objectData = s3Object.getObjectContent();
        Scanner scanner = new Scanner(objectData);
        return scanner;
    }

    private void writeToLocalDB(Connection con, PreparedStatement ps, ResultSet rs, Scanner scanner, LambdaLogger logger) {
        // Connection string for a file-based SQlite DB
        try {
            // Connection string for a file-based SQlite DB
            con = DriverManager.getConnection("jdbc:sqlite:mytest.db");
            // Detect if the table 'mytable' exists in the database
            ps = con.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name='mytable'");
            rs = ps.executeQuery();
            if (!rs.next()) {
                // 'mytable' does not exist, and should be created
                logger.log("trying to create table 'mytable'");

                // Build the schema
                // Consume first line
                String line = scanner.nextLine();

                String statement = String.format(
                    "CREATE TABLE mytable (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);",
                    "Order ID INTEGER PRIMARY KEY",
                    "Region TEXT",
                    "Country TEXT",
                    "Item Type TEXT",
                    "Sales Channel TEXT",
                    "Order Date TEXT",
                    "Ship Date TEXT",
                    "Units Sold INTEGER",
                    "Units Price REAL",
                    "Unit Cost REAL",
                    "Total Revenue REAL",
                    "Total Cost REAL",
                    "Total Profit REAL",
                    "Order Processing Days INTEGER",
                    "Gross Margin REAL"
                    );
                ps = con.prepareStatement(statement);
                ps.execute();
            }
            
            // Write to local db



            rs.close();
            con.close();
        } catch (SQLException e) {
            logger.log("DB ERROR:" + e.toString());
            e.printStackTrace();
        }
    }

    private LinkedList<String> writeToAurora(Connection con, PreparedStatement ps, ResultSet rs, LambdaLogger logger) {
        LinkedList<String> ll = new LinkedList<String>();
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("db.properties"));
            String url = properties.getProperty("url");
            String username = properties.getProperty("username");
            String password = properties.getProperty("password");
            con = DriverManager.getConnection(url, username, password);

            // PreparedStatement ps = con.prepareStatement("insert into mytable values('" +
            // request.getName() + "','b','c');");
            ps = con.prepareStatement("insert into mytable values('a','b','c');");
            ps.execute();
            ps = con.prepareStatement("select * from mytable;");
            rs = ps.executeQuery();

            while (rs.next()) {
                logger.log("name=" + rs.getString("name"));
                ll.add(rs.getString("name"));
                logger.log("col2=" + rs.getString("col2"));
                logger.log("col3=" + rs.getString("col3"));
            }
            rs.close();
            con.close();
        } catch (Exception e) {
            logger.log("Got an exception working with MySQL! ");
            logger.log(e.getMessage());
        }
        return ll;
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
        Service2DataLoad lt = new Service2DataLoad();

        // Create a request object
        Request req = new Request();

        // Grab the name from the cmdline from arg 0
        String name = (args.length > 0 ? args[0] : "");

        // Load the name into the request object
        // req.setName(name);

        // Report name to stdout
        // System.out.println("cmd-line param name=" + req.getName());

        // Test properties file creation
        Properties properties = new Properties();
        properties.setProperty("driver", "com.mysql.cj.jdbc.Driver");
        properties.setProperty("url", "");
        properties.setProperty("username", "");
        properties.setProperty("password", "");
        try {
            properties.store(new FileOutputStream("test.properties"), "");
        } catch (IOException ioe) {
            System.out.println("error creating properties file.");
        }

        // Run the function
        // Response resp = lt.handleRequest(req, c);
        System.out.println(
                "The MySQL Serverless can't be called directly without running on the same VPC as the RDS cluster.");
        Response resp = new Response();

        // Print out function result
        System.out.println("function result:" + resp.toString());
    }
}
