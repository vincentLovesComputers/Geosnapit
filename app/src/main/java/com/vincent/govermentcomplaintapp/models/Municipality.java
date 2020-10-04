package com.vincent.govermentcomplaintapp.models;


import java.util.ArrayList;

public class Municipality {

    private String name;
    private ArrayList<String> cities;

    Municipality(){

    }

    Municipality(String name, ArrayList<String> cities){
        this.name = name;
        this.cities = cities;

    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getCities() {
        return cities;
    }
}
