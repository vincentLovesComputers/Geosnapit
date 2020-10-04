package com.vincent.govermentcomplaintapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Users {

    private String name;
    private String profile_image;
    private String user_id;

    Users(){

    }

    Users(String name, String profile_image, String user_id){
        this.name= name;
        this.profile_image = profile_image;
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getProfile_image() {
        return profile_image;
    }
}

