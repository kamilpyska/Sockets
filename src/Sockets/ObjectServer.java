/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Sockets;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Kamil Pyska
 */
public class ObjectServer {

    ServerSocket clientConn;

    public ObjectServer(int port) {
        System.out.println("Serwer łączy z portem " + port);
        try {
            clientConn = new ServerSocket(port);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        int port = 3000;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                port = 3000;
            }
        }
        ObjectServer server = new ObjectServer(port);
        System.out.println("Serwer nasłuchuje na porcie " + port);
        server.listen();
    }

    public void listen() {
        try {
            System.out.println("Czekanie na żądanie...");
            while (true) {
                Socket clientReq = clientConn.accept();
                System.out.println("Połączenie z "
                        + clientReq.getInetAddress().getHostName());
                serviceClient(clientReq);
            }
        } catch (IOException e) {
            System.out.println("Exception: " + e);
        }
    }

    public void serviceClient(Socket s) {
        ObjectOutputStream outStream;
        ObjectInputStream inStream;
        boolean messageFlag = true;
        String fileName = "";
        try {
            outStream = new ObjectOutputStream(s.getOutputStream());
            inStream = new ObjectInputStream(s.getInputStream());
            int message_id;
            Object message = null;
            boolean finished = false;
            while (!finished) {
                message_id = inStream.readInt();
                System.out.println("Odebrana wiadomość o id: " + message_id);
                message = inStream.readObject();
                String serverDirectory = "C:\\Users\\Kamson8\\Desktop\\Serwer\\";
                Path path = null;
                switch (message_id) {
                    case 1:
                        File file = new File(serverDirectory + message.toString());
                        if (file.exists()) {
                            outStream.writeObject("Plik " + message + " znajduje się na serwerze.");
                        } else {
                            outStream.writeObject("Plik " + message + " nie znajduje się na serwerze.");
                        }
                        outStream.flush();
                        break;
                    case 2:
                        if (messageFlag) {
                            fileName = message.toString();
                            outStream.writeObject("");
                            messageFlag = false;
                        } else {
                            byte[] clientFileByteArray = (byte[]) message;
                            path = Paths.get(serverDirectory + fileName);
                            byte[] serwerFileByteArray = Files.readAllBytes(path);

                            if (Arrays.equals(clientFileByteArray, serwerFileByteArray)) {
                                outStream.writeObject("Pliki są identyczne.");
                            } else {
                                outStream.writeObject("Pliki są różne.");
                            }
                            messageFlag = true;
                        }
                        outStream.flush();
                        break;
                    case 3:
                        if (messageFlag) {
                            fileName = message.toString();
                            outStream.writeObject("");
                            messageFlag = false;
                        } else {
                            FileOutputStream fos = new FileOutputStream(serverDirectory + fileName);
                            fos.write((byte[]) message);
                            fos.close();
                            outStream.writeObject("Plik został zapisany na serwerze.");
                            messageFlag = true;
                        }
                        outStream.flush();
                        break;
                    case 4:
                        path = Paths.get(serverDirectory + message.toString());
                        File serwerFile = new File(path.toString());

                        if (serwerFile.exists()) {
                            byte[] array = Files.readAllBytes(path);
                            outStream.writeObject(array);
                        } else {
                            outStream.writeObject("Nie ma takiego pliku na serwerze.");
                        }
                        outStream.flush();
                        break;
                    case 0:
                        outStream.writeObject("Wylogowano.");
                        finished = true;
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Wyłączono.");
    }
}
