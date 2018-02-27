package bg.uni.sofia.fmi.mjt.finals.client;

import bg.uni.sofia.fmi.mjt.finals.server.ClientCommands;

import java.io.InputStream;
import java.util.Scanner;

class ServerResponse implements Runnable {
    private final InputStream serverInputStream;

    public ServerResponse(InputStream serverInputStream) {

        this.serverInputStream = serverInputStream;
    }

    @Override
    public void run() {
        // receive server messages and print out to screen
        Scanner scanner = new Scanner(this.serverInputStream);
        String sentMessage;
        while (scanner.hasNextLine()) {
            sentMessage = scanner.nextLine();
            if (sentMessage.equals(ClientCommands.QUIT.toString())) {
                break;
            }
            System.out.println(sentMessage);
        }
        scanner.close();
    }
}