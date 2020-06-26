package edu.integrator.rpagv2;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActualizarAlertas extends Thread {
    InputStream in = null;
    OutputStream out = null;
    //DataInputStream dataInputStream;
    //DataOutputStream dataOutputStream;
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    BufferedReader br;

    MainActivity mainActivity;

    String host;
    int port;

    public ActualizarAlertas(String host, int port, MainActivity mainActivity) {
        this.host = host;
        this.port = port;
        this.mainActivity = mainActivity;

        start();
    }

    public void run() {
        Socket socket = null;
        try {

            Log.v( "RPAG-Log","Actualizando Alertas");

            socket = new Socket(host, port);
            //System.out.println("Conexion establecida");
            Log.v( "RPAG-Log","Conexion establecida");
            in = socket.getInputStream();
            out = socket.getOutputStream();



            //OutputStream outputStream = socket.getOutputStream();

            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            printWriter.println("Actualizar");
            Log.v( "RPAG-Log","Mensaje Enviado");

            objectOutputStream = new ObjectOutputStream(out);
            //System.out.println("objectOutputStream establecido");
            Log.v( "RPAG-Log","objectOutputStream establecido");
            objectInputStream = new ObjectInputStream(in);
            //System.out.println("objectInputStream establecido");
            Log.v( "RPAG-Log","objectInputStream establecido");
            //System.out.println("Esperando Respuesta...");

            Log.v( "RPAG-Log","Esperando Respuesta...");

            PackDatos packDatos = (PackDatos) objectInputStream.readObject();
            Log.v( "RPAG-Log","Objeto Recibido...");
            ArrayList<DatosAlerta> listaAlertas = packDatos.listaDatosAlertas;
            ArrayList<Confirmacion> listConfirmaciones = packDatos.listaConfirmaciones;
            ArrayList<Reporte> listReportes = packDatos.listaReportes;
            ArrayList<Comentario> listComentarios = packDatos.listaComentarios;
            ArrayList<Imagen> listImagenes = packDatos.listaImagenes;

            Log.v( "RPAG-Log","Alertas: " + listaAlertas.size());
            Log.v( "RPAG-Log","Confirmaciones: " + listConfirmaciones.size());
            Log.v( "RPAG-Log","Reportes: " + listReportes.size());
            Log.v( "RPAG-Log","Comentarios: " + listComentarios.size());
            Log.v( "RPAG-Log","Imagenes: " + listImagenes.size());

            /*
            String recibido = "Recibido: ";
            for (int i = 0; i < listaAlertas.size(); i++) {
                recibido += mainActivity.getClase(listaAlertas.get(i).clase_id).name + ", ";
            }
            Log.v( "RPAG-Log",recibido);
             */
            //System.out.println("Recibido: " + listaAlertas);

            mainActivity.listDatosAlertas = listaAlertas;
            mainActivity.listConfirmaciones = listConfirmaciones;
            mainActivity.listReportes = listReportes;
            mainActivity.listComentarios = listComentarios;
            mainActivity.listImagenes = listImagenes;

            printWriter.close();
            socket.close();

            //mainActivity.metodosAlertas.mostrarAlertas();

        } catch (UnknownHostException e) {
            Log.v( "RPAG-Log","Unknown host: " + host);
            //System.exit(1);
        } catch (ConnectException ce) {
            Log.v("RPAG-Log","ConnectException: " + ce.getMessage());
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ActualizarAlertas.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
                out.close();
                objectInputStream.close();
                objectOutputStream.close();
                if (socket != null) {
                    socket.close();
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Exception e) {
                Log.v("RPAG-Log","Exception: " + e.getMessage());
            }
        }
    }
}
