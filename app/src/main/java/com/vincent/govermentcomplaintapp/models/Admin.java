package com.vincent.govermentcomplaintapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Admin implements Parcelable {

    private String name;
    private String image;
    private String admin_id;
    private String city;
    private String province;
    private String municipality;
    private String postal_code;
    private String role;
    private String user_type;

    Admin(String name, String image, String admin_id, String city, String province, String municipality, String postal_code, String role, String user_type){
        this.name = name;
        this.image = image;
        this.admin_id = admin_id;
        this.city = city;
        this.province = province;
        this.municipality = municipality;
        this.admin_id = admin_id;
        this.postal_code = postal_code;
        this.role = role;
        this.user_type = user_type;

    }



    protected Admin(Parcel in) {
        name = in.readString();
        image = in.readString();
        admin_id = in.readString();
        city = in.readString();
        province = in.readString();
        municipality = in.readString();
        postal_code = in.readString();
        role = in.readString();
        user_type = in.readString();
    }
    Admin(){

    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getImage() {
        return image;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getAdmin_id() {
        return admin_id;
    }

    public void setAdmin_id(String admin_id) {
        this.admin_id = admin_id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public static final Creator<Admin> CREATOR = new Creator<Admin>() {
        @Override
        public Admin createFromParcel(Parcel in) {
            return new Admin(in);
        }

        @Override
        public Admin[] newArray(int size) {
            return new Admin[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(image);
        dest.writeString(admin_id);
        dest.writeString(city);
        dest.writeString(province);
        dest.writeString(municipality);
        dest.writeString(postal_code);
        dest.writeString((role));
        dest.writeString(user_type);
    }
}
