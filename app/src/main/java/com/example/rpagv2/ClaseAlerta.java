package com.example.rpagv2;

import java.io.Serializable;

public class ClaseAlerta implements Serializable {
    int id;
    String name, icon_name;
    int icon;

    public ClaseAlerta(int id, String name, int icon, String icon_name) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.icon_name = icon_name;
    }
}
