package com.example.rpagv2;

import com.mapbox.mapboxsdk.plugins.annotation.Symbol;

import java.util.Date;

public class Alerta{
    int id;
    double lat, len;
    public Date fecha;
    Symbol symbol;
    ClaseAlerta claseAlerta;

    public Alerta(int id, double lat, double len, Date fecha, Symbol symbol, ClaseAlerta claseAlerta) {
        this.id = id;
        this.lat = lat;
        this.len = len;
        this.symbol = symbol;
        this.claseAlerta = claseAlerta;
    }

    public Alerta(int id, double lat, double len, Date fecha, ClaseAlerta claseAlerta) {
        this.id = id;
        this.lat = lat;
        this.len = len;
        this.claseAlerta = claseAlerta;
    }
}
