package Parser;

import Response.ServerResponse;
import Server.ServerInfo;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RequestHandler {

    private final ServerResponse  serverResponse = new ServerResponse();
    private final String request;
    private RequestType requestType;
    private String path;
    private ByteBuffer response;
    private File file;


    public RequestHandler(String request) {
        this.request = request;
    }

    public ByteBuffer handleClientRequest() {
        try {
            setRequestType();
            setPath();
            setFilePath();
            buildByteBuffer();
            return response;
        } catch (Exception e) {
            return response;
        }
    }

    private void setRequestType() {
        String requestType = this.request.split(" ")[0];
        if(requestType.equalsIgnoreCase("GET")) {
            this.requestType = RequestType.GET;
        } else if(requestType.equalsIgnoreCase("POST")) {
            this.requestType = RequestType.POST;
        } else {
            throw new RuntimeException("Request does not exist");
        }
    }

    private void setPath() {
        this.path = this.request.split(" ")[1];
    }

    private void setFilePath(){
        file = new File(ServerInfo.defaultPath + path);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("unable to create file");
            }
        }
    }

    private void buildByteBuffer() {
        if (requestType.equals(RequestType.GET)) {
            handleGetRequest();
        } else if (requestType.equals(RequestType.POST)) {
            handlePostRequest();
        } else {
            throw new RuntimeException("Request does not exist");
        }
    }

    private void handleGetRequest() {
        try {
            Map<String, String> headers;
            String body = getGETBody();
            headers = addHeaders();
            this.response = serverResponse.buildResponse(200, headers, body);
        } catch (NullPointerException e) {
            throw new RuntimeException("error retrieving file");
        }
    }

    private Map<String, String> addHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.putAll(getLastModified());
        headers.putAll(getCurrentDate());
        headers.put("Content-Type", "text/html");
        return headers;
    }

    private String getGETBody() {
        if(file.isFile()){
            try {
                return Files.readString(Path.of(file.getPath()));
            } catch (IOException e) {
                throw new RuntimeException("Unable to read file");
            }
        } else {
            return Arrays.asList(file.listFiles()).toString();
        }
    }

    private void handlePostRequest() {
        Map<String, String> headers;
        ensurePathIsToFile();
        String body = getBody();
        tryToWriteToFile(body);
        headers = addHeaders();
        this.response = serverResponse.buildResponse(200, headers, body);
    }

    private void ensurePathIsToFile() {
        if(!file.isFile()) {
            throw new RuntimeException("Path is not to file");
        }
    }

    private String getBody() {
        String body = this.request.split("\n\n")[1];
        if(body.isEmpty()){
            throw new RuntimeException("post body is empty");
        }
        return body;
    }

    private void tryToWriteToFile(String body) {
        try {
            Files.write(Path.of(file.getPath()), body.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Unable to write to file");
        }
    }

    private Map<String, String> getCurrentDate() {
        return Map.of("Date", new Date().toString());
    }

    private Map<String, String> getLastModified(){
        Date lastModifed;

        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            lastModifed = new Date(file.lastModified());
        } else {
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File o1, File o2) {
                    return Long.compare(o2.lastModified(), o1.lastModified());
                }
            });
            lastModifed =  new Date(files[0].lastModified());
        }
        return Map.of("Last-Modified", lastModifed.toString());
    }

    private void handleError(String message, int code) {
        this.response = serverResponse.buildResponse(code, Map.of(), null);
    }

    private enum RequestType {
        GET, POST
    }

}
