package eu.hradio.httprequestwrapper.dtos;

import java.io.*;

public class Sort implements Serializable
{
    private static final long serialVersionUID = 5195630784747835852L;
    private boolean sort;
    private boolean unsort;
    
    public boolean getUnsort() {
        return this.unsort;
    }
    
    public void setUnsort(final boolean unsort) {
        this.unsort = unsort;
    }
    
    public boolean getSort() {
        return this.sort;
    }
    
    public void setSort(final boolean sort) {
        this.sort = sort;
    }
}
