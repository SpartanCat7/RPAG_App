package com.example.rpagv2;

import com.mapbox.mapboxsdk.plugins.annotation.Symbol;

public class Alerta{
    int id;
    double lat, len;
    Symbol symbol;
    ClaseAlerta claseAlerta;

    public Alerta(int id, double lat, double len, Symbol symbol, ClaseAlerta claseAlerta) {
        this.id = id;
        this.lat = lat;
        this.len = len;
        this.symbol = symbol;
        this.claseAlerta = claseAlerta;
    }

    public Alerta(int id, double lat, double len, ClaseAlerta claseAlerta) {
        this.id = id;
        this.lat = lat;
        this.len = len;
        this.claseAlerta = claseAlerta;
    }
}
