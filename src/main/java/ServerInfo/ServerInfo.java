package ServerInfo;

public class ServerInfo {
    public static final String defaultPath = "./src/workingDir/";
    public static final int defaultPort = 8080;
    private final boolean isVerbose;
    private final int port;
    private final String pathToDir;

    public ServerInfo(boolean isVerbose, int port, String pathToDir) {
        this.isVerbose = isVerbose;
        this.port = port;
        this.pathToDir = pathToDir;
    }

    public boolean isVerbose() {
        return isVerbose;
    }

    public int getPort() {
        return port;
    }

    public String getPathToDir() {
        return pathToDir;
    }
}

