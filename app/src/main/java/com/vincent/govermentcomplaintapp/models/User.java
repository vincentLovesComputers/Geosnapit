package com.vincent.govermentcomplaintapp.models;


import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String name;
    private String image;
    private String user_id;
    private String city;
    private String province;
    private String municipality;
    private String postal_code;
    private String user_type;



    public User(String name, String profile_image, String user_id, String city, String province, String municipality, String postal_code, String user_type){
        this.name= name;
        this.image = profile_image;
        this.user_id = user_id;
        this.city =city;
        this.province = province;
        this.municipality = municipality;
        this.postal_code=  postal_code;
        this.user_type = user_type;

    }

    public User() {

    }

    protected User(Parcel in) {
        name = in.readString();
        image = in.readString();
        user_id = in.readString();
        city = in.readString();
        municipality = in.readString();
        province = in.readString();
        postal_code = in.readString();
        user_type = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
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

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", user_id='" + user_id + '\'' +
                ", profile_image='" + image + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(name);
        dest.writeString(user_id);
        dest.writeString(city);
        dest.writeString(province);
        dest.writeString(municipality);
        dest.writeString(postal_code);
        dest.writeString(user_type);
    }
}
