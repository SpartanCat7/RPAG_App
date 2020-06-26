package edu.integrator.rpagv2;

import java.util.ArrayList;

public class PackDatos implements java.io.Serializable {
    public double latitude, longitude;

    public ArrayList<DatosAlerta> listaDatosAlertas;
    public ArrayList<Confirmacion> listaConfirmaciones;
    public ArrayList<Reporte> listaReportes;
    public ArrayList<Comentario> listaComentarios;
    public ArrayList<Imagen> listaImagenes;
}
