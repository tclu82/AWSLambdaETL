/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lambda;

/**
 *
 * @author wlloyd
 */
public class Request {

    private String bucketname;
    private String filename;
    private int row;
    private int col;

    public Request() {
    }

    public Request(String bucketname, String fileName, int row, int col) {
        this();
        this.bucketname = bucketname;
        this.filename = fileName;
        this.row = row;
        this.col = col;
    }

    public String getBucketname() {
        return bucketname;
    }
    
    public void setBucketname(String bucketname) {
        this.bucketname = bucketname;
    }

    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getRow() {
        return row;
    }
    
    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }
    
    public void setCol(int col) {
        this.col = col;
    }
}
