package com.vincent.govermentcomplaintapp.models;

public class Address {
    private String street_name;
    private String city;
    private String province;

    Address(){

    }

    Address(String street_name, String city, String province){
        this.city = city;
        this.street_name = street_name;
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getStreet_name() {
        return street_name;
    }
}
