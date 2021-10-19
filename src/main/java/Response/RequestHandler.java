package Response;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RequestHandler {

    private final ServerResponse serverResponse = new ServerResponse();
    private final String request;
    private final String dirPath;
    private final boolean isVerbose;
    private RequestType requestType;
    private String path;
    private ByteBuffer response;
    private File file;

    public RequestHandler(String dirPath, boolean isVerbose, String request) {
        this.isVerbose = isVerbose;
        this.dirPath = dirPath;
        this.request = request;
    }

    public ByteBuffer handleClientRequest() {
        try {
            printRequestToScreen();
            setRequestType();
            setPath();
            setFilePath();
            buildResponse();
            return response;
        } catch (Exception e) {
            printMessagesToScreen(e.toString());
            return response;
        }
    }

    private void printRequestToScreen() {
        printMessagesToScreen(String.format("HTTP request: \n%s", this.request));
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
        file = new File(dirPath + path);
    }

    private void buildResponse() {
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

    private void printFileCreated() {
        printMessagesToScreen(String.format("file created %s", file.toString()));
    }

    private String getGETBody() {
        checkIfFilePathExists();

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

    private void checkIfFilePathExists() {
        if(!file.exists()) {
            this.response = serverResponse.buildResponse(404, Map.of(),  "file path does not exist");
            throw new RuntimeException("File path does not exist");
        }
    }

    private void handlePostRequest() {
        Map<String, String> headers;
        makeFileAndFolders();
        ensurePathIsToFile();
        String body = getBody();
        tryToWriteToFile(body);
        headers = addHeaders();
        printFileCreated();
        this.response = serverResponse.buildResponse(201, headers, body);
    }

    private void makeFileAndFolders() {
        makeFolder();
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("unable to create file");
            }
        }
    }

    private void makeFolder() {
        File file = new File(dirPath);
        file.mkdirs();
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
            Arrays.sort(files, (o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));
            lastModifed =  new Date(files[0].lastModified());
        }
        return Map.of("Last-Modified", lastModifed.toString());
    }

    private void printMessagesToScreen(String message){
        if(this.isVerbose){
            System.out.println("***********debug***********");
            System.out.println(message);
        }
    }

    private enum RequestType {
        GET, POST
    }

}