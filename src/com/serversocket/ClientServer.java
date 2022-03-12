package com.serversocket;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;

public class ClientServer {
    static final String ROOT = "./src/com/serversocket/root/";
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";

    private Socket client;

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
            String requestStatus = "";
            do {
                requestStatus = bufferedReader.readLine();
            } while (requestStatus == null || Objects.equals(requestStatus, ""));
            String[] parsed = requestStatus.split(" ");
            String requestedFile = (parsed[1].equals("/")) ? DEFAULT_FILE : parsed[1].substring(1);
            System.out.format("[%s] %s - Accepted\n", new Date(), requestStatus);

            // Check whether file exists.
            boolean fileExist = fileExist(requestedFile);

            // Fetch file that handle 404 if the requested file is not found.
            String fetchedFile = (fileExist) ? requestedFile : FILE_NOT_FOUND;

            // Get response status line and meta data of fetched file
            int fileLength = getFileLength(fetchedFile);
            String fileContentType = getContentType(fetchedFile);
            byte[] fileData = getFileData(fetchedFile);
            String responseStatus = (fileExist) ? "200 OK" : "404 File Not Found";

            // Determine whether the fetched file should be downloaded or displayed in browser
            String contentDisposition = (fileContentType.split("/")[0].equals("text")) ? "inline" : "attachment";

            // Write response header
            bufferedWriter.write("HTTP/1.1 " + responseStatus + "\r\n");
            bufferedWriter.write("Content-Type: " + fileContentType + "\r\n");
            bufferedWriter.write("Content-Length: " + fileLength + "\r\n");
            bufferedWriter.write("Content-Disposition: " + contentDisposition + "\r\n");
            bufferedWriter.write("\r\n");
            bufferedWriter.flush();

            // Write response body
            bos.write(fileData, 0, fileLength);
            bos.flush();

            // Close the connection
            System.out.format("[%s] %s - Closing\n", new Date(), requestStatus);
            client.close();

        } catch (IOException e) {
            System.err.println("Server error : " + e);
        }
    }

    private boolean fileExist(String path) {
        return (new File(ROOT + path)).exists();
    }

    private int getFileLength(String path) throws IOException {
        return (int) Files.size(Path.of(ROOT + path));
    }

    private String getContentType(String path) throws IOException {
        String $type = Files.probeContentType(Path.of(ROOT + path));

        // Handle Javascript type and set default type to text/plain if mime type isn't found.
        if ($type == null || $type.equals("")) {
            File file = new File(ROOT + path);
            String filename = file.getName();

            int idx = filename.lastIndexOf(".");
            $type = (filename.substring(idx + 1).equals("js")) ? "application/javascript" : "text/plain";
        }
        return $type;
    }

    private byte[] getFileData(String path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(ROOT + path);
        return fileInputStream.readAllBytes();
    }
}
