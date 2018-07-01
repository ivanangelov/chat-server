package bg.uni.sofia.fmi.mjt.finals.main;

import bg.uni.sofia.fmi.mjt.finals.server.Server;

/**
 * The server is started from the MainServer class.
 */
class MainServer {
    public static void main(String[] args) {
        Server chatServer = new Server(4444);
        chatServer.startServer();
    }
}
