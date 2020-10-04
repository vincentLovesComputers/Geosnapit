package com.vincent.govermentcomplaintapp.models;

import java.util.Date;

public class UserNotification {

    private String title;
    private Date date_posted;
    private Date date_resolved;
    private String address;
    private String notification_id;

    UserNotification(){

    }

    UserNotification(String title, Date date_posted, Date date_resolved, String address, String notification_id){
        this.title = title;
        this.date_posted =date_posted;
        this.date_resolved = date_resolved;
        this.address = address;
        this.notification_id = notification_id;

    }



    public String getTitle() {
        return title;
    }

    public Date getDate_posted() {
        return date_posted;
    }

    public String getAddress() {
        return address;
    }

    public Date getDate_resolved() {
        return date_resolved;
    }

    public String getNotification_id() {
        return notification_id;
    }
}
