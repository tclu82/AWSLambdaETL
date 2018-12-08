/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lambda;

import faasinspector.fiResponse;
import model.QueryResult;
import java.util.List;

/**
 *
 * @author wlloyd
 */
public class Response extends fiResponse {
    
    //
    // User Defined Attributes
    //
    //
    // ADD getters and setters for custom attributes here.
    //

    // Return value
    private List<QueryResult> results;

    public List<QueryResult>  getResults()
    {
        return results;
    }
    public void setResults(List<QueryResult>  results)
    {
        this.results = results;
    }

    @Override
    public String toString()
    {
        return "value=" + this.getResults() + super.toString(); 
    }
}
