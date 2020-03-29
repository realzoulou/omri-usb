package eu.hradio.httprequestwrapper.dtos.service_use;

import java.io.*;

public class Demographics implements Serializable
{
    private static final long serialVersionUID = 1577498806868901863L;
    private Location location;
    private AgeGroup ageGroup;
    private Gender gender;
    
    public Location getLocation() {
        return this.location;
    }
    
    public void setLocation(final Location location) {
        this.location = location;
    }
    
    public AgeGroup getAgeGroup() {
        return this.ageGroup;
    }
    
    public void setAgeGroup(final AgeGroup ageGroup) {
        this.ageGroup = ageGroup;
    }
    
    public Gender getGender() {
        return this.gender;
    }
    
    public void setGender(final Gender gender) {
        this.gender = gender;
    }
}
