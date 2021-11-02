package Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static Response.RequestHandler.APPLICATION_JSON;

public class ServerResponse {

    public final static Map<Integer, String> codes = Map.of(
            200, "OK",
            201, "Created",
            400, "Bad Request",
            401, "Unauthorized",
            404, "Not Found"
    );

    private String message = "";
    private int code;
    private Map<String, String> headers;
    private String body;
    private File file;
    private ByteBuffer response;
    private final Gson gson = new Gson();

    public ByteBuffer buildResponse(int code, Map<String, String> headers, File file) {
        this.code = code;
        this.headers = headers;
        this.file = file;
        this.body = null;
        try {
            addBody();
            addContentDispositionHeader();
            buildResponseMessageWithBody();
            return ByteBuffer.wrap(message.getBytes());
        } catch (Exception e){
            return response;
        }
    }

    public ByteBuffer buildResponse(int code, Map<String, String> headers, String body) {
        this.code = code;
        this.headers = headers;
        this.body = body;

        addContentDispositionHeader();
        buildResponseMessageWithBody();
        return ByteBuffer.wrap(message.getBytes());
    }

    private void addBody() {
        checkIfFilePathExists();
        if(file.isFile()){
            try {
                setBodyFromFile();
            } catch (Exception e) {
                throw new RuntimeException("Unable to read file");
            }
        } else {
            File[] files = file.listFiles();
            if(contentTypeIsJSON()) {
                setBodyFromDirectory(files);
            } else {
                body = Arrays.asList(file.listFiles()).toString();
            }
        }
    }

    private void setBodyFromDirectory(File[] files) {
        Map<String, Object> body = new HashMap<>();
        int index = 0;
        for(File file : files){
            body.put(Integer.toString(index++), file.toString());
        }
        this.body = gson.toJson(body).replace("\\\\", "\\");
    }

    private void setBodyFromFile() throws IOException {
        String contents = Files.readString(Path.of(file.getPath()));
        if(contentTypeIsJSON()){
            Map<String, String> map = Map.of("file-contents", contents);
            this.body = gson.toJson(map);
        } else {
            this.body = contents;
        }
    }

    private boolean contentTypeIsJSON() {
        return this.headers.get("Content-Type").equals(APPLICATION_JSON);
    }

    private void checkIfFilePathExists() {
        if(!file.exists()) {
            Map<String, String> headers = new HashMap<>(Map.of("Content-Type", APPLICATION_JSON));
            this.response = buildResponse(404, headers, "file path does not exist");
            throw new RuntimeException("File path does not exist");
        }
    }

    private void addContentDispositionHeader() {
        if(contentTypeIsJSON()){
            headers.put("Content-Disposition", "inline");
        } else {
            headers.put("Content-Disposition", "attachment; filename=\"file.txt\"");
        }
    }

    private void buildResponseMessageWithBody() {
        this.message += "HTTP/1.1 " + code + " " + codes.get(code) + "\n";
        for(Map.Entry<String, String> header : headers.entrySet()) {
            this.message += header.getKey() + ": " + header.getValue() + "\n";
        }
        this.message += "\n";
        if(body != null){
            this.message += body;
        }
    }
}
