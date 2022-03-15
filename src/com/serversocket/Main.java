package com.serversocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class Main {
    static final int PORT = 80;

    /**
     * Run server socket
     *
     */
    public static void main(String[] args) {
        try {
            ConfigService configService = new ConfigService();
            InetAddress address = InetAddress.getByName(configService.getIP());
            ServerSocket serverSocket = new ServerSocket(configService.getPort(), 50, address);
            System.out.println("Server started: http:://" + configService.getIP() + ":" + PORT);

            while (true) {
                ClientServer client = new ClientServer(serverSocket.accept(), configService);
                client.serve();
            }


        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Config Service error : " + e.getMessage());
        }
    }
}
