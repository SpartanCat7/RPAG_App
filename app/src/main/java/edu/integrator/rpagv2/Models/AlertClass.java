package edu.integrator.rpagv2.Models;

import java.io.Serializable;

public class AlertClass implements Serializable {
    public int id;
    public String icon_name;
    public int icon, name_string_ID;

    public AlertClass(int id, int icon, String icon_name, int name_string_ID) {
        this.id = id;
        this.icon = icon;
        this.icon_name = icon_name;
        this.name_string_ID = name_string_ID;
    }
}
