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
import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.SourceDataLine;

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
        setCurrentDirectory("/tmp");

        String bucketname = request.getBucketname();
        String filename = request.getFilename();
        String[] names = filename.split("/");
        String dbname = names[names.length - 1] + ".db";
        dbname = dbname.replace(' ', '_');

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
        //get object file using source bucket and srcKey name
        S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketname, filename));
        //get content of the file
        InputStream objectData = s3Object.getObjectContent();
        //scanning data line by line
        Scanner scanner = new Scanner(objectData);
        String line = scanner.nextLine();

        try {
            // Connection string for a file-based SQlite DB
            Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbname); 

            // Detect if the table 'salesrecords' exists in the database
            PreparedStatement ps = con.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name='salesrecords'");
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                // 'salesrecords' does not exist, and should be created
                logger.log("trying to create table 'salesrecords'");
                ps = con.prepareStatement(
                    "CREATE TABLE salesrecords (" +
                        "Region text," +
                        "Country text," +
                        "Item_Type text," +
                        "Sales_Channel text," +
                        "Order_Priority text," +
                        "Order_Date date," +
                        "Order_ID integer PRIMARY KEY," +
                        "Ship_Date date," +
                        "Units_Sold integer," +
                        "Unit_Price float," +
                        "Unit_Cost float," +
                        "Total_Revenue float," +
                        "Total_Cost float," +
                        "Total_Profit float," +
                        "Order_Processing_Time integer," +
                        "Gross_Margin float" +
                    ");"
                );
                ps.execute();
            }
            rs.close();
            
            // Insert row into salesrecords
            while (scanner.hasNext()) {
                line = scanner.nextLine();
                line = line.replace("\'","\'\'");
                String[] token = line.split(",");

                for (int i = 0; i < 5; i++) token[i] = "'" + token[i] + "'";

                String[] date = token[5].split("/");
                token[5] = "'" + date[2] + "-" + date[0] + "-" + date[1] + "'";
                date = token[7].split("/");
                token[7] = "'" + date[2] + "-" + date[0] + "-" + date[1] + "'";

                line = String.join(",", token);
                ps = con.prepareStatement("INSERT INTO salesrecords values(" + line + ");");
                ps.execute();
            }
            con.close();
            
        }
        catch (SQLException sqle) {
            logger.log("DB ERROR:" + sqle.toString());
            sqle.printStackTrace();
        }

        scanner.close();
        File file = new File("/tmp/"+dbname);
        s3Client.putObject("tcss562.group.project", "SalesRecordsDB/" + dbname, file);
        file.delete();

        r.setValue("Bucket: " + bucketname + " filename: " + filename + " loaded. DBname: " + dbname);
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
        Service2DataLoad lt = new Service2DataLoad();
        Request req = new Request();
        System.out.println("cmd-line param name=" + req.getBucketname());
        // Run the function
        Response resp = lt.handleRequest(req, c);
        // Print out function result
        System.out.println("function result:" + resp.toString());
    }
}
