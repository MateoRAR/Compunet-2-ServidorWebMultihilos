package org.example;

import java.io.*;
import java.net.*;
import java.util.*;
class ServidorWeb {
    public static void main(String[] argv) throws Exception {


        String nombreArchivo = "";
        ServerSocket enterSocket = new ServerSocket(8080);
        System.out.println("Waiting for connection...");
        while (true) {
            Socket particularSocket = enterSocket.accept();
            System.out.println("Connection established");

            SolicitudHttp solicitud = new SolicitudHttp(particularSocket);

            Thread hilo = new Thread(solicitud);
            hilo.start();

        }
    }
}