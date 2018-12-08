package model;

public class Value {
    private String query;
    private double result;

    public Value() {
    }

    public Value(String query) {
        this();
        this.query = query;
    }

    public Value(String query, double result) {
        this(query);
        this.result = result;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public double getLongValue() {
        return result;
    }

    public void setLongValue(double longValue) {
        this.result = longValue;
    }
}