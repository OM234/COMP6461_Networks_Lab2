package Response;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;

public class ServerResponse {

    public final static Map<Integer, String> codes = Map.of(
            200, "OK",
            201, "Created",
            400, "Bad Request",
            401, "Unauthorized",
            404, "Not Found"
    );

    private int responseCode;
    private String message = "";
    private Date current;
    private Date lastModified;
    private String contentType;
    private int code;
    private Map<String, String> headers;
    private String body;

    public ByteBuffer buildResponse(Map<String, String> headers) {
        return null;
    }

    public ByteBuffer buildResponse(int code, Map<String, String> headers, String body) {
        this.code = code;
        this.headers = headers;
        this.body = body;

        buildResponseMessage();
        return ByteBuffer.wrap(message.getBytes());
    }

    private void buildResponseMessage() {
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
