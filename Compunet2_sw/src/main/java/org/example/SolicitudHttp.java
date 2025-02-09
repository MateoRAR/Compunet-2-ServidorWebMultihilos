package org.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

final class SolicitudHttp implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;

    // Constructor
    public SolicitudHttp(Socket socket) throws Exception {
        this.socket = socket;
    }

    // Implementa el metodo run() de la interface Runnable.
    public void run() {
        try {
            proceseSolicitud();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void proceseSolicitud() throws Exception {

        // Referencia al stream de salida del socket.
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

        String lineaDeLaSolicitudHttp="";

        BufferedReader in =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // GET nombre_archivo HTTP/1.0
        lineaDeLaSolicitudHttp = in.readLine();
        System.out.println("soli: " + lineaDeLaSolicitudHttp);

        StringTokenizer st = new StringTokenizer(lineaDeLaSolicitudHttp);
        String method = st.nextToken();
        String resource = "";
        String linea = "";
        while ((linea = in.readLine()) != null && !linea.isEmpty()) {
            System.out.println(linea);
            if (method.equals("GET")){
                resource = "." + st.nextToken();
                System.out.println(resource);
                break;
            } else {
                resource = "./404.html";
                System.out.println("Bad Request Message");
                break;
            }
        }

        InputStream inputStream = null;
        long filesize = 0;
        try {
            inputStream = ClassLoader.getSystemResourceAsStream(resource);
            File file = new File(ClassLoader.getSystemResource(resource).toURI());
            filesize = file.length();
        }  catch (Exception e) {
            System.out.println(e);
        }


        buildResponse(inputStream, out, resource, filesize);
        in.close();
        out.close();
        socket.close();
    }

    private static void enviarString(String line, OutputStream os) throws Exception {
        os.write(line.getBytes(StandardCharsets.UTF_8));
    }

    private static void enviarBytes(InputStream fis, OutputStream os) throws Exception {
        // Construye un buffer de 1KB para guardar los bytes cuando van hacia el socket.
        byte[] buffer = new byte[1024];
        int bytes = 0;

        // Copia el archivo solicitado hacia el output stream del socket.
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private static void buildResponse (InputStream in, OutputStream os, String nombreArchivo, long size) throws Exception {
        String lineaDeEstado = null;
        String lineaHeader = null;
        String cuerpoMensaje = null;
        String contentLength = null;

        if (in != null) {
            lineaDeEstado = "HTTP/1.0 200 OK";
            lineaHeader = "Content-type: " + contentType( nombreArchivo ) + CRLF;
            contentLength = "Content-Length: " + size + CRLF;
            //Enviar linea de estado
            enviarString(lineaDeEstado, os);
            //Enviar linea de header
            enviarString(lineaHeader, os);
            enviarString(contentLength, os);
            enviarString(CRLF, os);
            //Enviar el archivo
            enviarBytes(in,os);

        } else {
            lineaDeEstado = "HTTP/1.0 404 Not Found\r\n";
            lineaHeader = "Content-type: " + contentType( "404.html" ) + CRLF;
            //Enviar linea de estado
            enviarString(lineaDeEstado, os);
            //Enviar linea de header
            enviarString(lineaHeader, os);

            InputStream is404 = ClassLoader.getSystemResourceAsStream("404.html");
            File file = new File(ClassLoader.getSystemResource("404.html").toURI());
            long filesize = file.length();

            contentLength = "Content-Length: " + filesize + CRLF;
            enviarString(contentLength, os);

            enviarString(CRLF, os);
            //Enviar archivo 404.html
            enviarBytes(is404,os);
        }
        os.flush();

    }

    private static String contentType(String nombreArchivo) {
        if(nombreArchivo.endsWith(".htm") || nombreArchivo.endsWith(".html")) {
            return "text/html";
        }
        if(nombreArchivo.endsWith(".jpg") || nombreArchivo.endsWith(".jpeg") || nombreArchivo.endsWith(".png")) {
            return "image/jpeg";
        }
        if(nombreArchivo.endsWith(".gif")) {
            return "image/gif";
        }
        if(nombreArchivo.endsWith(".ico")) {
            return "icon/ico";
        }
        return "application/octet-stream";
    }

}

