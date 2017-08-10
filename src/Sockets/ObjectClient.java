/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Sockets;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 *
 * @author Kamil Pyska
 */
public class ObjectClient {

    Socket serverConn;
    ObjectInputStream inStream = null;
    ObjectOutputStream outStream = null;

    public ObjectClient(String host, int port)
            throws UnknownHostException, IOException {
        this.serverConn = new Socket(host, port);
        outStream = new ObjectOutputStream(serverConn.getOutputStream());
        inStream = new ObjectInputStream(serverConn.getInputStream());
    }

    public Object sendMessage(int message_id, Object message) throws Exception {
        outStream.writeInt(message_id);
        outStream.writeObject(message);
        outStream.flush();
        Object response = inStream.readObject();
        return response;
    }

    public static void main(String[] args) {
        Scanner scaner = new Scanner(System.in);
        boolean finished = false;
        try {
            ObjectClient client = new ObjectClient("localhost", 3000);
            while (!finished) {
                System.out.println("\n1.Czy plik x znajduje się już na serwerze?");
                System.out.println("2.Czy plik s znajdujący się na serwerze jest taki sam jak u klienta?");
                System.out.println("3.Zapisz plik na serwerze.");
                System.out.println("4.Pobierz plik z serwera.");
                System.out.println("0.Wyloguj.");
                char c = scaner.nextLine().charAt(0);
                Path path;

                switch (c) {
                    case '1':
                        System.out.println("Podaj nazwę pliku do sprawdzenia, np abc.txt:");
                        System.out.println(client.sendMessage(1, scaner.nextLine()));
                        break;
                    case '2':
                        System.out.println("Podaj ścieżkę pliku do sprawdzenia:");
                        path = Paths.get(scaner.nextLine());
                        File clientFile = new File(path.toString());
                        
                        if (clientFile.exists()) {
                            System.out.println(client.sendMessage(2,clientFile.getName()));
                            System.out.println(client.sendMessage(2, Files.readAllBytes(path)));
                        } else {
                            System.out.println("Plik nie istnieje.");
                        }
                        break;
                    case '3':
                        System.out.println("Podaj ścieżkę pliku do zapisania na serwerze:");
                        path = Paths.get(scaner.nextLine());
                        File fileToSend = new File(path.toString());
                        if (fileToSend.exists()) {
                            System.out.println(client.sendMessage(3, fileToSend.getName()));
                            System.out.println(client.sendMessage(3,Files.readAllBytes(path)));
                        } else {
                            System.out.println("Plik nie istnieje.");
                        }
                        break;
                    case '4':
                        System.out.println("Podaj ścieżkę do zapisu pliku:");
                        path = Paths.get(scaner.nextLine());
                        System.out.println("Podaj nazwę pliku do pobrania:");
                        String fileName = scaner.nextLine();

                        Object recivedObject = client.sendMessage(4, fileName);
                        if (recivedObject instanceof String) {
                            System.out.println(recivedObject);
                        } else {
                            FileOutputStream fos = new FileOutputStream(path.toString() + "\\" + fileName);
                            byte[] fileToSave = (byte[]) recivedObject;
                            System.out.println("Plik został zapisany.");
                            fos.write(fileToSave);
                            fos.close();
                        }
                        break;
                    case '0':
                        System.out.println("Serwer odpowiada: " + (String) client.sendMessage(0, null));
                        client.serverConn.close();
                        finished = true;
                        break;

                }
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            e.printStackTrace();
        }
    }

}
