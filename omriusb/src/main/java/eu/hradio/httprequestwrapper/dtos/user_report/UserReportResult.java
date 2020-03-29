package eu.hradio.httprequestwrapper.dtos.user_report;

import java.io.*;
import eu.hradio.httprequestwrapper.dtos.service_use.*;

public class UserReportResult implements Serializable
{
    private static final long serialVersionUID = 17888990227896361L;
    private long id;
    private Context context;
    private String reportId;
    private String description;
    private String[] values;
    private String[] labels;
    private UserData data;
    
    public Context getContext() {
        return this.context;
    }
    
    public void setContext(final Context context) {
        this.context = context;
    }
    
    public String getReportId() {
        return this.reportId;
    }
    
    public void setReportId(final String id) {
        this.reportId = id;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(final String d) {
        this.description = d;
    }
    
    public String[] getValues() {
        return this.values;
    }
    
    public void setValues(final String[] values) {
        this.values = values;
    }
    
    public String[] getLabels() {
        return this.labels;
    }
    
    public void setLabels(final String[] labels) {
        this.labels = labels;
    }
    
    public UserData getUserData() {
        return this.data;
    }
    
    public void setUserData(final UserData ud) {
        this.data = ud;
    }
    
    public long getId() {
        return this.id;
    }
    
    public void setId(final long id) {
        this.id = id;
    }
}
