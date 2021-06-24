package edu.integrator.rpagv2.Models;

import com.mapbox.mapboxsdk.plugins.annotation.Symbol;

import java.util.Date;

public class UIAlert {
    public String id;
    public double lat, len;
    public Date date;
    public Symbol symbol;
    public AlertClass alertClass;

    public UIAlert(String id, double lat, double len, Date date, Symbol symbol, AlertClass alertClass) {
        this.id = id;
        this.lat = lat;
        this.len = len;
        this.symbol = symbol;
        this.alertClass = alertClass;
    }

    public UIAlert(String id, double lat, double len, Date date, AlertClass alertClass) {
        this.id = id;
        this.lat = lat;
        this.len = len;
        this.alertClass = alertClass;
    }
}
