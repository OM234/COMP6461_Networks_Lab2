import Parser.ServerSettingsParser;
import Server.Server;
import Server.ServerInfo;

public class Application {
    
    public static void main(String[] args){
        Server server = new Server();
        ServerSettingsParser serverSettingsParser = new ServerSettingsParser();
        
        ServerInfo serverInfo = serverSettingsParser.getServerInfo();
        server.runServer(serverInfo);
    }
}
