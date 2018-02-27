package bg.uni.sofia.fmi.mjt.finals.main;

import bg.uni.sofia.fmi.mjt.finals.server.Server;

public class MainServer {
    public static void main(String[] args) {
        Server chatServer = new Server(4444);
        chatServer.startServer();
    }
}
