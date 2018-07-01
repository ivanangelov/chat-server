package bg.uni.sofia.fmi.mjt.finals.client;

import bg.uni.sofia.fmi.mjt.finals.server.ClientCommands;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 *Represents a single client. Provides means for connecting to the server
 * and sending messages to it.
 */
public class Client {
    private String host;
    private int port;

    /**
     * Initializes the client with host and port,
     * which are read from the user's standard input.
     */
    public void initializeUser() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter valid host and port to establish connection: ");
            String line = scanner.nextLine();
            String[] args = line.split(" ");

            if (args.length != 3 || !args[0].equals(ClientCommands.CONNECT.toString())) {
                throw new IllegalArgumentException("Not valid arguments ");
            } else if (args[0].equals(ClientCommands.CONNECT.toString())) {
                this.host = args[1];
                this.port = Integer.parseInt(args[2]);
                this.startConnection(scanner);
            }

        }
    }

    /**
     * Establishes connection between the current client and
     * the server. Manages the clients standard input from the console.
     *
     * @param scanner reads messages from the user's standard input
     */
    private void startConnection(Scanner scanner) {
        try (Socket clientSocket = new Socket(this.host, this.port);
             PrintStream outputPrintStream = new PrintStream(clientSocket.getOutputStream())) {

            System.out.println("You have successfully connected to the server!");
            // create a new thread for server responses
            new Thread(new ServerResponse(clientSocket.getInputStream())).start();
            String sendMessage;
            while (scanner.hasNextLine()) {
                sendMessage = scanner.nextLine();
                if (sendMessage.equals(ClientCommands.QUIT.toString())) {
                    outputPrintStream.println(sendMessage);
                    break;
                } else {
                    outputPrintStream.println(sendMessage);
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Problem with the host. Try restarting the app. " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Problem with the output stream. Try restarting the app. " + e.getMessage());
        }
    }

}