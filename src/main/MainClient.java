package bg.uni.sofia.fmi.mjt.finals.main;

import bg.uni.sofia.fmi.mjt.finals.client.Client;

public class MainClient {
    public static void main(String[] args) {
        Client chatClient = new Client();
        chatClient.startConnection();
    }
}
