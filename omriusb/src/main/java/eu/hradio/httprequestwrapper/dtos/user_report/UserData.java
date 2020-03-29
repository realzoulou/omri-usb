package eu.hradio.httprequestwrapper.dtos.user_report;

import java.io.*;

public class UserData implements Serializable
{
    private static final long serialVersionUID = 8890206902256361L;
    private int id;
    private UserAction[] actions;
    
    public int getId() {
        return this.id;
    }
    
    public void setId(final int id) {
        this.id = id;
    }
    
    public UserAction[] getActions() {
        return this.actions;
    }
    
    public void setActions(final UserAction[] ua) {
        this.actions = ua;
    }
}
