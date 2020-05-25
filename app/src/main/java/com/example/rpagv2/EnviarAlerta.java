package com.example.rpagv2;

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

public class EnviarAlerta extends Thread { //DatosAlerta

    DatosAlerta paquete;
    MainActivity main;
    String IP;
    int PORT;

    InputStream in = null;
    OutputStream out = null;
    //DataInputStream dataInputStream;
    //DataOutputStream dataOutputStream;
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    BufferedReader br;

    public EnviarAlerta(DatosAlerta paquete, MainActivity main, String IP, int PORT) {
        this.paquete = paquete;
        this.main = main;
        this.IP = IP;
        this.PORT = PORT;

        start();
    }

    public void run() {

        Socket socket = null;
        try{
            Log.i( "RPAG-Log","Intentando Conectar con: " + IP);
            socket = new Socket(IP, PORT);
            Log.i( "RPAG-Log","Conexion Establecida con " + socket.getRemoteSocketAddress());

            in = socket.getInputStream();
            out = socket.getOutputStream();


            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            printWriter.println("Alerta");

            objectOutputStream = new ObjectOutputStream(out);
            //System.out.println("objectOutputStream establecido");
            Log.i( "RPAG-Log","objectOutputStream establecido");
            objectInputStream = new ObjectInputStream(in);
            //System.out.println("objectInputStream establecido");
            Log.i( "RPAG-Log","objectInputStream establecido");

            objectOutputStream.writeObject(paquete);
            objectOutputStream.flush();
            Log.i( "RPAG-Log","Paquete enviado");

            printWriter.close();
            socket.close();

        } catch (UnknownHostException e) {
            Log.e( "RPAG-Log","Unknown host: " + MainActivity.IP_SERVIDOR);
            //System.exit(1);
        } catch (ConnectException ce) {
            Log.e("RPAG-Log","ConnectException: " + ce.getMessage());
        } catch (IOException e) {
            Log.e( "RPAG-Log","Error: " + e.getMessage());
            //System.exit(1);
        } finally {
            try {
                if (socket != null){
                    if (!socket.isClosed()) {
                        objectInputStream.close();
                        objectOutputStream.close();
                        in.close();
                        out.close();
                        socket.close();
                        Log.i( "RPAG-Log","Conexion Cerrada");
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
