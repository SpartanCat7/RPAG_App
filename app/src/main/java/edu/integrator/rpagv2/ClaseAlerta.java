package edu.integrator.rpagv2;

import java.io.Serializable;

public class ClaseAlerta implements Serializable {
    int id;
    String icon_name;
    int icon, name_string_ID;

    public ClaseAlerta(int id, int icon, String icon_name, int name_string_ID) {
        this.id = id;
        this.icon = icon;
        this.icon_name = icon_name;
        this.name_string_ID = name_string_ID;
    }
}
