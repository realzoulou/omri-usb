package eu.hradio.httprequestwrapper.dtos.recommendation;

import java.io.*;
import android.os.*;

public class Recommender implements Parcelable, Serializable
{
    private static final long serialVersionUID = -1647027757429297252L;
    private String recommenderName;
    private String numberOfServices;
    public static final Parcelable.Creator CREATOR;
    
    public Recommender(final String recommenderName, final String numberOfServices) {
        this.recommenderName = recommenderName;
        this.numberOfServices = numberOfServices;
    }
    
    public Recommender() {
    }
    
    public String getRecommenderName() {
        return this.recommenderName;
    }
    
    public void setRecommenderName(final String recommenderName) {
        this.recommenderName = recommenderName;
    }
    
    public String getNumberOfServices() {
        return this.numberOfServices;
    }
    
    public void setNumberOfServices(final String numberOfServices) {
        this.numberOfServices = numberOfServices;
    }
    
    @Override
    public String toString() {
        return this.recommenderName;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof Recommender && ((Recommender)o).numberOfServices.equals(this.numberOfServices) && ((Recommender)o).recommenderName.equals(this.recommenderName);
    }
    
    public Recommender(final Parcel in) {
        this.recommenderName = in.readString();
        this.numberOfServices = in.readString();
    }
    
    public int describeContents() {
        return 0;
    }
    
    public void writeToParcel(final Parcel parcel, final int i) {
        parcel.writeString(this.recommenderName);
        parcel.writeString(this.numberOfServices);
    }
    
    static {
        CREATOR = (Parcelable.Creator)new Parcelable.Creator() {
            public Recommender createFromParcel(final Parcel in) {
                return new Recommender(in);
            }
            
            public Recommender[] newArray(final int size) {
                return new Recommender[size];
            }
        };
    }
}
