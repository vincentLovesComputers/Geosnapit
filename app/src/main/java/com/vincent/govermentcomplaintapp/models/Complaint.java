package com.vincent.govermentcomplaintapp.models;

import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.Date;

public class Complaint {

    private String title;
    private String description;
    private String user;
    private ArrayList<String> images;
    private Date timeStamp;
    private String municipality;
    private String post_code;
    private String city;
    private String province;
    private String  street_name;
    private String issue_update;


    Complaint(){

    }

    Complaint(String title, String description, ArrayList<String> images, Date timeStamp, String municipality, String post_code, String city, String street_name, String user, String province, String issue_update){
        this.title = title;
        this.description = description;
        this.images =  images;
        this.user = user;
        this.city = city;
        this.post_code = post_code;
        this.province=  province;
        this.street_name =street_name;
        this.municipality = municipality;
        this.timeStamp = timeStamp;
        this.issue_update = issue_update;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public String getStreet_name() {
        return street_name;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public String getMunicipality() {
        return municipality;
    }

    public String getPost_code() {
        return post_code;
    }

    public String getUser() {
        return user;
    }

    public String getIssue_update() {
        return issue_update;
    }


}
