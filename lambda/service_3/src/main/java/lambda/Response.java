/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lambda;

import faasinspector.fiResponse;
import model.Value;
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
    private List<Value> values;

    public List<Value>  getValue()
    {
        return values;
    }
    public void setValue(List<Value>  values)
    {
        this.values = values;
    }

    @Override
    public String toString()
    {
        return "value=" + this.getValue() + super.toString(); 
    }
}
