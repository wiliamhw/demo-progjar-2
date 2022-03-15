package com.serversocket;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    static final int PORT = 80;

    /**
     * Run server socket
     *
     */
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started: http:://127.0.0.1:" + PORT);

            while (true) {
                ClientServer client = new ClientServer(serverSocket.accept());
                client.serve();
            }


        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }
    }
}
