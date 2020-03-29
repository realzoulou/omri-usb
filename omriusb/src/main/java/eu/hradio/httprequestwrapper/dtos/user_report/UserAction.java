package eu.hradio.httprequestwrapper.dtos.user_report;

import java.io.*;

public class UserAction implements Serializable
{
    private static final long serialVersionUID = 372882990003856361L;
    private String actiontime;
    private UserActionEnum action;
    private String actionlabel;
    
    public String getActionTime() {
        return this.actiontime;
    }
    
    public void setActionTime(final String s) {
        this.actiontime = s;
    }
    
    public String getActionLabel() {
        return this.actionlabel;
    }
    
    public void setActionLabel(final String l) {
        this.actionlabel = l;
    }
    
    public UserActionEnum getAction() {
        return this.action;
    }
    
    public void setAction(final UserActionEnum e) {
        this.action = e;
    }
}
