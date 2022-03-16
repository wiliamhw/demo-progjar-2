package com.serversocket;

import javax.naming.ConfigurationException;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;

public class ClientServer {
    public static final String SERVER_ROOT = "./src/com/serversocket/";
    public static final String SERVER_ASSETS_DIR = "server-assets";

    private static final String DEFAULT_FILE = "index.html";
    private static final String FILE_NOT_FOUND = "500.html";

    private final Socket client;
    private final ConfigService configService;

    public ClientServer(Socket client, ConfigService configService) {
        this.client = client;
        this.configService = configService;
    }

    /**
     * Server user request.
     */
    public void serve() {
        try {
            System.out.format("[%s] Accepting client access\n", new Date());

            // create buffer
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            BufferedOutputStream bos = new BufferedOutputStream(client.getOutputStream());
            String connectionFromRequest;

            // Loop if user does not ask to close
            do {

                // Get requested file path
                Header requestHeader = new Header(bufferedReader);
                requestHeader.setRequestStatus();
                String requestedFile = requestHeader.getRequestedFile();

                // Get all request header
                requestHeader.setAllRequestHeaders();
                String hostFromRequest = requestHeader.getHeaderWithKey("Host");
                connectionFromRequest = requestHeader.getHeaderWithKey("Connection");
                System.out.format("[%s] %s - Accepted\n", new Date(), requestHeader.getRequestStatus());

                // Adjust client socket if client request has keep alive connection header.
                if (connectionFromRequest.equals("keep-alive")) {
                    client.setKeepAlive(true);
                    client.setTcpNoDelay(true);
                    client.setSoTimeout(100);
                }

                // Determine document root.
                String documentRoot;
                if (getFirstDirFromPath(requestedFile).equals(SERVER_ASSETS_DIR)) {
                    documentRoot = SERVER_ROOT;
                } else {
                    documentRoot = configService.getSettingsWithKey(hostFromRequest);
                    if (documentRoot == null) {
                        throw new ConfigurationException("Undefined domain.");
                    }

                    documentRoot = (documentRoot.equals(".")) ? "./" : documentRoot;
                    System.out.format("[%s] Access domain %s in folder %s on port %s\n",
                        new Date(), hostFromRequest, documentRoot, configService.getPort()
                    );
                }

                // Check whether file exists.
                boolean fileExist = FileService.fileExist(documentRoot + requestedFile);

                // Fetch file that handle 404 if the requested file is not found.
                String fetchedFile = (fileExist) ? requestedFile : FILE_NOT_FOUND;
                String responseStatus = (fileExist) ? "200 OK" : "500 Internal Server Error";
                documentRoot = (fileExist) ? documentRoot : (SERVER_ROOT + SERVER_ASSETS_DIR + '\\');

                // Initialize file service class.
                FileService fileService = new FileService(
                    hostFromRequest, configService.getPort(), documentRoot, fetchedFile, DEFAULT_FILE
                );

                // Write response header
                bufferedWriter.write("HTTP/1.1 " + responseStatus + "\r\n");
                bufferedWriter.write("Content-Type: " + fileService.getContentType() + "\r\n");
                bufferedWriter.write("Content-Length: " + fileService.getFileLength() + "\r\n");
                bufferedWriter.write("Content-Disposition: " + fileService.getContentDisposition() + "\r\n");
                if (connectionFromRequest.equals("keep-alive")) {
                    bufferedWriter.write("Connection: " + "keep-alive" + "\r\n");
                    bufferedWriter.write("Keep-Alive: " + "timeout=5, max=1000" + "\r\n");
                }
                bufferedWriter.write("Server: WW Server Pro\r\n");
                bufferedWriter.write("\r\n");
                bufferedWriter.flush();

                // Write response body
                bos.write(fileService.getFileData(), 0, fileService.getFileLength());
                bos.flush();
            } while (!connectionFromRequest.equals("close"));

        }
        catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            // Close the connection
            try {
                client.close();
            } catch (IOException e) {
                System.err.println("Server error: " + e.getMessage());
            }
            System.out.format("[%s] Closing client access\n", new Date());
        }
    }

    private String getFirstDirFromPath(String path) {
        if (path.equals("")) {
            return "";
        }
        return path.split("/")[0];
    }
}
