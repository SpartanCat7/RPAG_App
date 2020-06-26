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

public class EnviarConfirmacion extends Thread {

    Confirmacion paquete;
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

    public EnviarConfirmacion(Confirmacion paquete, MainActivity main, String IP, int PORT) {
        this.paquete = paquete;
        this.main = main;
        this.IP = IP;
        this.PORT = PORT;

        start();
    }

    public void run() {

        Socket socket = null;
        try{

            socket = new Socket(main.IP_SERVIDOR, main.PORT_SERVIDOR);


            in = socket.getInputStream();
            out = socket.getOutputStream();


            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            printWriter.println("Confirmacion");

            objectOutputStream = new ObjectOutputStream(out);
            //System.out.println("objectOutputStream establecido");
            Log.v( "RPAG-Log","objectOutputStream establecido");
            objectInputStream = new ObjectInputStream(in);
            //System.out.println("objectInputStream establecido");
            Log.v( "RPAG-Log","objectInputStream establecido");

            objectOutputStream.writeObject(paquete);
            objectOutputStream.flush();
            Log.v( "RPAG-Log","Paquete enviado");

            printWriter.close();
            socket.close();

        } catch (UnknownHostException e) {
            Log.v( "RPAG-Log","Unknown host: " + main.IP_SERVIDOR);
            //System.exit(1);
        } catch (ConnectException ce) {
            Log.v("RPAG-Log","ConnectException: " + ce.getMessage());
        } catch  (IOException e) {
            Log.v( "RPAG-Log","No I/O");
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
