package Server;

import Parser.RequestHandler;

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
            System.out.println("Server running at port: " + port);
            while (true) {
                acceptAndSubmit(server);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptAndSubmit(ServerSocketChannel server) throws IOException {
        SocketChannel client = server.accept();
        ForkJoinPool.commonPool().submit(() -> readAndWriteToClient(client));
    }

    private void readAndWriteToClient(SocketChannel socket) {
        try (SocketChannel client = socket) {
            ByteBuffer input = readFromClient(client);
            RequestHandler requestHandler = new RequestHandler(new String(input.array()));
            ByteBuffer response = requestHandler.handleClientRequest();
            client.write(response);
        } catch (Exception e) {
            handleExceptions(e);
        }
    }

    private ByteBuffer readFromClient(SocketChannel client) throws IOException {
        ByteBuffer input = ByteBuffer.allocate(10000);
        client.read(input);
        input.flip();
        return input;
    }

    private void handleExceptions(Exception e) {
        if(this.isVerbose) {
            e.printStackTrace();
        }
    }
}
