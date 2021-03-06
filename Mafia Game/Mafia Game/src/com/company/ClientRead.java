package com.company;

import java.io.*;
import java.net.*;
import java.util.Date;

/**
 * This thread is responsible for reading server's input and printing it
 * to the console.
 * It runs in an infinite loop until the client disconnects from the server.
 *
 * @author www.codejava.net
 */
public class ClientRead extends Thread {
    private BufferedReader reader;
    private Socket socket;
    private Client client;

    public ClientRead(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;

        try {
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
//                System.out.println("Aaaap");
                String response = reader.readLine();;
//                System.out.println("aa");
                if (response.equals("End")){
                    synchronized (client.getThread()) {
                        client.getThread().notify();
                    }
                    break;
                }
                System.out.println(response);

                 //prints the username after displaying the server's message
//                if (client.getUserName() != null) {
//                    System.out.print("[" + client.getUserName() + "]: ");
//                }

            } catch (IOException ex) {
                System.out.println("Error reading from server: " + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }
    }

}

