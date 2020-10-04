package com.vincent.govermentcomplaintapp.models;

import java.util.Date;

public class ReviewMessage {

    private User user;
    private Admin admin;
    private String message;
    private String message_id;
    private String user_id;
    Date timeStamp;
    private String user_type;

    ReviewMessage(User user, String message, String message_id, String user_id, Date timeStamp, Admin admin, String user_type){
        this.user = user;
        this.message = message;
        this.message_id = message_id;
        this.timeStamp = timeStamp;
        this.user_id = user_id;
        this.admin = admin;
        this.user_type = user_type;
    }

    public ReviewMessage(){

    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }



    @Override
    public String toString() {
        return "ChatMessage{" +
                "user=" + user +
                ", message='" + message + '\'' +
                ", message_id='" + message_id + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
