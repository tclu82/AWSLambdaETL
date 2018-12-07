/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lambda;

import faasinspector.fiResponse;
import java.lang.annotation.Native;
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
    private String value;
    private List<String> names;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    // For names output
    public List<String> getName() {
        return names;
    }

    public void setName(List<String> names) {
        this.names = names;
    }

    @Override
    public String toString() {
        return "value=" + this.getValue() + super.toString(); 
    }

}
