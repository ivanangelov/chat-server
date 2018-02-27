package bg.uni.sofia.fmi.mjt.finals.client;

import bg.uni.sofia.fmi.mjt.finals.server.ClientCommands;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private String host;
    private int port;

    public void startConnection() {
        //connect client to server
        Socket clientSocket = null;
        PrintStream outputPrintStream = null;
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("Enter valid host and port to establish connection: ");
            String line = sc.nextLine();
            String[] args = line.split(" ");

            if (args.length != 3 || !args[0].equals(ClientCommands.CONNECT.toString())) {
                throw new IllegalArgumentException("Not valid arguments ");
            } else if (args[0].equals(ClientCommands.CONNECT.toString())) {
                this.host = args[1];
                this.port = Integer.parseInt(args[2]);
            }

            clientSocket = new Socket(this.host, this.port);
            System.out.println("You have successfully connected to a server!");
            // create a new thread for server responses
            new Thread(new ServerResponse(clientSocket.getInputStream())).start();

            //read the messages from the keyboard and send them to the server
            outputPrintStream = new PrintStream(clientSocket.getOutputStream());
            String sendMessage;
            while (sc.hasNextLine()) {
                sendMessage = sc.nextLine();
                if (sendMessage.equals(ClientCommands.QUIT.toString())) {
                    outputPrintStream.println(sendMessage);
                    break;
                } else {
                    outputPrintStream.println(sendMessage);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to get session. Try restarting the app " + e.getMessage());
        } finally {
            if (outputPrintStream != null) {
                outputPrintStream.close();
            }
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Problem with closing the socket " + e.getMessage());
            }
        }
    }
}