package Server;

import Response.RequestHandler;
import ServerInfo.ServerInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ForkJoinPool;

public class Server {

    private final String host = "localhost";
    private int port;
    private String filePath;
    private boolean isVerbose;

    public void runServer(ServerInfo serverInfo) {
        setServerInfo(serverInfo);
        runServerOnSpecifiedPort();
    }

    private void setServerInfo(ServerInfo serverInfo) {
        this.port = serverInfo.getPort();
        this.filePath = serverInfo.getPathToDir();
        this.isVerbose = serverInfo.isVerbose();
    }

    private void runServerOnSpecifiedPort() {
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(port));
            printServerInfo();
            while (true) {
                acceptAndSubmit(server);
            }
        } catch (IOException e) {
            printMessagesToScreen(e.getMessage());
        }
    }

    private void printServerInfo() {
        printMessagesToScreen(String.format(
                "Server running at port: %d, verbose: %s, working directory: %s", port, isVerbose, filePath));
    }

    private void acceptAndSubmit(ServerSocketChannel server) throws IOException {
        SocketChannel client = server.accept();
        printMessagesToScreen(String.format("Connected to client at: %s", client.getRemoteAddress()));
        ForkJoinPool.commonPool().submit(() -> readAndWriteToClient(client));
    }

    private void readAndWriteToClient(SocketChannel socket) {
        try (SocketChannel client = socket) {
            ByteBuffer input = readFromClient(client);
            RequestHandler requestHandler = new RequestHandler(filePath, isVerbose, new String(input.array()));
            ByteBuffer response = requestHandler.handleClientRequest();
            client.write(response);
            printMessagesToScreen(String.format("Disconnected from client at: %s", client.getRemoteAddress()));
        } catch (Exception e) {
            printMessagesToScreen(e.getMessage());
        }
    }

    private ByteBuffer readFromClient(SocketChannel client) throws IOException {
        ByteBuffer input = ByteBuffer.allocate(10000);
        client.read(input);
        input.flip();
        return input;
    }

    private void printMessagesToScreen(String message){
        if(this.isVerbose){
            System.out.println("***********debug***********");
            System.out.println(message);
        }
    }
}
