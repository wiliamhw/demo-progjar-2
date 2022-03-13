package com.serversocket;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileService {
    private String fetchedFilePath;
    private int fileLength;
    private String contentType;
    private String contentDisposition;
    private byte[] fileData;

    public FileService(String path) throws IOException {
        this.fetchedFilePath = path;
        this.setFileLength();
        this.setContentType();
        this.setFileData();
        this.setContentDisposition();
    }

    public static boolean fileExist(String path) {
        return (new File(path)).exists();
    }

    private void setFileLength() throws IOException {
        this.fileLength = (int) Files.size(Path.of(this.fetchedFilePath));
    }

    private void setContentType() throws IOException {
        String type = Files.probeContentType(Path.of(this.fetchedFilePath));

        // Handle Javascript type and set default type to text/plain if mime type isn't found.
        if (type == null || type.equals("")) {
            File file = new File(this.fetchedFilePath);
            String filename = file.getName();

            int idx = filename.lastIndexOf(".");
            type = (filename.substring(idx + 1).equals("js")) ? "application/javascript" : "text/plain";
        }
        this.contentType = type;
    }

    private void setFileData() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(this.fetchedFilePath);
        this.fileData = fileInputStream.readAllBytes();
    }

    public void setContentDisposition() throws IOException {
        if (this.contentType == null || this.contentType == "") {
            this.setContentType();
        }
        this.contentDisposition = (this.contentType.split("/")[0].equals("text")) ? "inline" : "attachment";
    }

    public int getFileLength() {
        return this.fileLength;
    }

    public String getContentType() {
        return this.contentType;
    }

    public byte[] getFileData() {
        return this.fileData;
    }

    public String getContentDisposition() {
        return this.contentDisposition;
    }
}
