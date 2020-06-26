package edu.integrator.rpagv2;

import java.util.Date;

public class DatosAlerta implements java.io.Serializable {
    public int id;
    public int id_usuario;

    public double latitud;
    public double longitud;

    public int clase_id;
    public Date fecha;

    public DatosAlerta(int id, int id_usuario, double latitud, double longitud, int clase_id, Date fecha) {
        this.id = id;
        this.id_usuario = id_usuario;
        this.latitud = latitud;
        this.longitud = longitud;
        this.clase_id = clase_id;
        this.fecha = fecha;
    }

    public DatosAlerta() {
    }
}
