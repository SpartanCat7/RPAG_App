package com.example.rpagv2;

import android.graphics.Bitmap;

import java.util.Date;

public class Imagen implements java.io.Serializable {
    public int id;
    public String nombre;
    public int id_alerta;
    public int id_usuario;
    public Date fecha;
    public byte[] bitmap;

    public DatosAlerta alerta;
}
