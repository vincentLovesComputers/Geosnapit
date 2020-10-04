package com.vincent.govermentcomplaintapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ReviewRoom implements Parcelable {

    private String review_room_id;
    private String municipailty;

    public ReviewRoom(String review_room_id, String municipailty){
        this.review_room_id = review_room_id;
        this.municipailty = municipailty;

    }

    public ReviewRoom(){

    }

    protected ReviewRoom(Parcel in){
        review_room_id = in.readString();
        municipailty = in.readString();

    }

    public static final Creator<ReviewRoom> CREATOR = new Creator<ReviewRoom>() {
        @Override
        public ReviewRoom createFromParcel(Parcel in) {
            return new ReviewRoom(in);
        }

        @Override
        public ReviewRoom[] newArray(int size) {
            return new ReviewRoom[size];
        }
    };

    public void setReview_room_id(String review_room_id) {
        this.review_room_id = review_room_id;
    }

    public void setMunicipailty(String municipailty) {
        this.municipailty = municipailty;
    }

    public String getReview_room_id() {
        return review_room_id;
    }

    public String getMunicipailty() {
        return municipailty;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(review_room_id);
        dest.writeString(municipailty);
    }
}
