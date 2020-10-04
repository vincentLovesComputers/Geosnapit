package com.vincent.govermentcomplaintapp.models;

public class Services {
    private String service_type;
    private String icon;
    Services(){

    }

    Services(String service_type, String icon){
        this.service_type = service_type;
        this.icon = icon;
    }

    public String getService_type() {
        return service_type;
    }

    public String getIcon() {
        return icon;
    }
}
