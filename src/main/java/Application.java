import Server.Server;
import ServerInfo.ServerInfo;
import ServerInfo.ServerSettingsParser;

public class Application {
    
    public static void main(String[] args){
        Server server = new Server();
        ServerSettingsParser serverSettingsParser = new ServerSettingsParser();
        
        ServerInfo serverInfo = serverSettingsParser.getServerInfo();
        server.runServer(serverInfo);
    }
}
