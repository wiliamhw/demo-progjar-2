package com.serversocket;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Objects;

public class ClientServer {
    static final String ROOT = "./src/com/serversocket/root/";
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";

    private final Socket client;

    public ClientServer(Socket client) {
        this.client = client;
    }

    /**
     * Server user request
     */
    public void serve() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            BufferedOutputStream bos = new BufferedOutputStream(client.getOutputStream());

            // Get requested file path
            Header requestHeader = new Header(bufferedReader);
            requestHeader.setRequestStatus();
            String requestedFile = requestHeader.getRequestedFile();
            System.out.format("[%s] %s - Accepted\n", new Date(), requestHeader.getRequestStatus());

            // Get all request header
            requestHeader.getAllRequestHeaders();
            String hostFromRequest = requestHeader.getHeaderWithKey("Host");
            String connectionFromRequest = requestHeader.getHeaderWithKey("Connection");



            // Check whether file exists.
            boolean fileExist = FileService.fileExist(ROOT + requestedFile);

            // Fetch file that handle 404 if the requested file is not found.
            String fetchedFile = (fileExist) ? requestedFile : FILE_NOT_FOUND;
            String responseStatus = (fileExist) ? "200 OK" : "404 File Not Found";

            // Initialize file service class.
            FileService fileService = new FileService(ROOT, fetchedFile, DEFAULT_FILE);

            // Write response header
            bufferedWriter.write("HTTP/1.1 " + responseStatus + "\r\n");
            bufferedWriter.write("Content-Type: " + fileService.getContentType() + "\r\n");
            bufferedWriter.write("Content-Length: " + fileService.getFileLength() + "\r\n");
            bufferedWriter.write("Content-Disposition: " + fileService.getContentDisposition() + "\r\n");
            bufferedWriter.write("\r\n");
            bufferedWriter.flush();

            // Write response body
            bos.write(fileService.getFileData(), 0, fileService.getFileLength());
            bos.flush();

            // Close the connection
            System.out.format("[%s] %s - Closing\n", new Date(), requestHeader.getRequestStatus());
            client.close();

        } catch (IOException e) {
            System.err.println("Server error : " + e);
        }
    }
}
