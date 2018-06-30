package bg.uni.sofia.fmi.mjt.finals.main;

import bg.uni.sofia.fmi.mjt.finals.client.Client;

/**
 * Each client is started from the MainClient class.
 */
class MainClient {
    public static void main(String[] args) {
        Client chatClient = new Client();
        chatClient.initializeUser();
    }
}
