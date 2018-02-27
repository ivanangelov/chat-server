package bg.uni.sofia.fmi.mjt.finals.server;

import bg.uni.sofia.fmi.mjt.finals.utils.ChatRoom;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class ClientThread implements Runnable {
    private String name;
    private String password;
    private final Server server;
    private final Socket socket;
    private InputStream clientInputStream;
    private PrintWriter printWriter;
    private boolean isLoggedIn;
    private String sendingFilePath;
    private static final int SEND_SUBSTRING = 6;
    private static final int SEND_ALL_SUBSTRING = 10;

    public ClientThread(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        this.initializeStreams(this.socket);
    }

    private void initializeStreams(Socket socket) {
        try {
            this.clientInputStream = socket.getInputStream();
            this.printWriter = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Problem initializing the streams: " + e.getMessage());
        }
    }

    public String getName() {
        return this.name;
    }

    public PrintWriter getPrintWriter() {
        return this.printWriter;
    }

    private boolean isMemberOfGivenChatRoom(String roomName) {
        return this.server.getChatRoomByName(roomName).containsMember(this.name);
    }

    private boolean isValidLocation(String path) {
        if (!path.contains("/")) {
            return false;
        }
        String newFilePath = "";
        int index = path.lastIndexOf("/");
        if (index > 0) {
            newFilePath = path.substring(0, index);
        }

        return Paths.get(newFilePath).toFile().isDirectory();
    }

    public boolean getIsLoggedIn() {
        return this.isLoggedIn;
    }

    private void setSendingFilePath(String path) {
        this.sendingFilePath = path;
    }

    private void registerUser(String name, String password) {
        if (this.name != null) {
            this.printWriter.println("You have already registered into the system");
        } else if (this.server.isUsedUserData(name, password)) {
            this.printWriter.println("Choose another username or password");
        } else {
            this.name = name;
            this.password = password;
            this.server.addInfoToDocumentation(this.name, this.password);
            this.printWriter.println("You have registered into the system");
        }
    }

    private void login(String name, String password) {
        if (this.name.equals(name) && this.password.equals(password)) {
            if (!this.isLoggedIn) {
                this.printWriter.println("You have logged into the system");
                this.isLoggedIn = true;
            } else {
                this.printWriter.println("You have already logged into the system");
            }
        } else {
            this.printWriter.println("Wrong username or password");
        }
    }

    private void deleteRoom(String roomName) {
        if (this.server.containsChatRoom(roomName)) {
            if (server.getChatRoomByName(roomName).getCreatorUsername().equals(this.name)) {
                this.server.deleteChatRoomByName(roomName);
                this.printWriter.println("The room " + roomName + " is deleted.");
            } else {
                this.printWriter.println("You do not have permission to delete");
            }
        } else {
            this.printWriter.println("Inactive room");
        }
    }


    private void createRoom(String roomName) {
        if (!this.server.containsChatRoom(roomName)) {
            this.server.addChatRoom(roomName, new ChatRoom(roomName, this.name));
            this.printWriter.println("The room " + roomName + " is created.");
        } else {
            this.printWriter.println("The room " + roomName + " has already been created.");
        }
    }

    private void joinRoom(String[] args) {
        if (this.server.containsChatRoom(args[1])) {
            if (!this.isMemberOfGivenChatRoom(args[1])) {
                this.server.getChatRoomByName(args[1]).addMember(this.name, this);
                this.printWriter.println("You have joined the room " + args[1]);
                this.printWriter.println("Room history:");
                this.server.broadcastChatRoomHistoryToUser(args[1], this.printWriter);
            } else
                this.printWriter.println("You are already member of the room " + args[1]);
        } else {
            this.printWriter.println("Inactive room");
        }
    }

    private void leaveRoom(String[] args) {
        if (!this.server.containsChatRoom(args[1])) {
            this.printWriter.println("Inactive room");
        } else if (!this.isMemberOfGivenChatRoom(args[1])) {
            this.printWriter.println("You are not member of the room.");
        } else if (this.server.containsChatRoom(args[1])) {
            this.server.getChatRoomByName(args[1]).removeMember(this.name);
            this.printWriter.println("You have left the room " + args[1]);
        }
        /*else {
            this.printWriter.println("Inactive room");
        }*/
    }


    private void registrationHandle(String[] args) {
        if (args.length == 3 && args[0].equals(ClientCommands.REGISTER.toString())) {
            this.registerUser(args[1], args[2]);
        } else if (args.length == 3 && args[0].equals(ClientCommands.LOGIN.toString()) && args[1].equals(this.name)) {
            this.login(args[1], args[2]);
        } else if (args.length == 1 && args[0].equals(ClientCommands.DISCONNECT.toString())) {
            if (this.isLoggedIn) {
                this.printWriter.println("You have disconnected from the system");
                this.isLoggedIn = false;
            } else {
                this.printWriter.println("You have already disconnected from the system");
            }
        } else {
            this.printWriter.println("Invalid, try again");
        }
    }

    private void roomHandle(String[] args) {
        if (args.length == 2) {
            if (args[0].equals(ClientCommands.CREATE_ROOM.toString())) {
                this.createRoom(args[1]);
            } else if (args[0].equals(ClientCommands.DELETE_ROOM.toString())) {
                this.deleteRoom(args[1]);
            } else if (args[0].equals(ClientCommands.JOIN_ROOM.toString())) {
                this.joinRoom(args);
            } else if (args[0].equals(ClientCommands.LEAVE_ROOM.toString())) {
                this.leaveRoom(args);
            } else {
                this.printWriter.println("Something is wrong");
            }
        } else if (args.length == 1 && args[0].equals(ClientCommands.LIST_ROOMS.toString())) {
            this.server.listActiveChatRooms(this.getPrintWriter());
        } else {
            this.printWriter.println("Something is wrong");
        }
    }

    private void fileHandle(String[] args) {
        if (!this.isLoggedIn) {
            this.printWriter.println("You are not logged in");
        } else if (args.length == 1 && args[0].equals(ClientCommands.DECLINE.toString())) {
            if (this.sendingFilePath != null) {
                this.printWriter.println("File transfer declined");
                this.sendingFilePath = null;
            }
        } else if (args.length == 2 && args[0].equals(ClientCommands.ACCEPT.toString())) {
            if (!this.isValidLocation(args[1])) {
                this.printWriter.println("Invalid location, please try again");
            } else if (this.sendingFilePath != null) {
                this.printWriter.println("File transfer accepted");
                this.server.sendFile(this.sendingFilePath, args[1], this.name);
                this.sendingFilePath = null;
            }
        } else {
            this.printWriter.println("Something is wrong");
        }
    }

    private void sendHandle(String[] args, String message) {
        if (!this.isLoggedIn) {
            this.printWriter.println("You are not logged in");
        } else if (args.length < 3) {
            this.printWriter.println("Wrong number of arguments");
        } else if (args[0].equals(ClientCommands.SEND_ALL.toString())) {
            if (this.server.containsChatRoom(args[1]) && this.isMemberOfGivenChatRoom(args[1])) {
                this.server.broadcastMessageToChatRoom(this.name + ": " + message.substring(SEND_ALL_SUBSTRING + args[1].length()), args[1]);
            } else {
                this.printWriter.println("Inactive room");
            }
        } else if (!this.server.containsClient(args[1])) {
            this.printWriter.println("Invalid receiver");
        } else if (!this.server.getClientByName(args[1]).getIsLoggedIn()) {
            this.printWriter.println("The receiver is not logged in");
        } else {
            if (args[0].equals(ClientCommands.SEND.toString())) {
                this.server.broadcastMessage(this.name + ": " + message.substring(SEND_SUBSTRING + args[1].length()), args[1]);
            } else if (args.length == 3 && args[0].equals(ClientCommands.SEND_FILE.toString())) {
                this.server.getClientByName(args[1]).setSendingFilePath(args[2]);
                if (Files.exists(Paths.get(args[2]))) {
                    this.server.broadcastMessage("Do you accept pending file", args[1]);
                } else {
                    this.printWriter.println("File not found or is invalid");
                }
            }
        }
    }

    private void listUsers(String[] args) {
        if (this.isLoggedIn) {
            if (args.length == 1) {
                this.server.listActiveUsers(this.printWriter);
            } else if (args.length == 2) {
                if (this.server.containsChatRoom(args[1])) {
                    this.server.listActiveUsersInRoom(args[1], this.getPrintWriter());
                } else {
                    this.printWriter.println("Inactive room");
                }
            }
        } else {
            this.printWriter.println("You are not logged in ");
        }
    }

    private void quitHandle() {
        this.isLoggedIn = false;
        this.printWriter.println("Goodbye");
        this.printWriter.println(ClientCommands.QUIT.toString());
        this.server.removeClient(this);
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(this.clientInputStream);
        String message;

        while (scanner.hasNextLine()) {
            message = scanner.nextLine();
            //args is a String array containing all parts of the message separated by ' ';
            String[] args = message.split(" ");
            if (message.equals("")) {
                this.printWriter.println("No message found, please try again");
            } else if (!ClientCommands.containsCommand(args[0])) {
                this.printWriter.println("This is not a valid command please try again");
            } else if (message.equals(ClientCommands.QUIT.toString())) {
                this.quitHandle();
                break;
            } else if (args[0].contains("room")) {
                this.roomHandle(args);
            } else if (args[0].contains(ClientCommands.SEND.toString())) {
                this.sendHandle(args, message);
            } else if (args[0].equals(ClientCommands.ACCEPT.toString()) || args[0].equals(ClientCommands.DECLINE.toString())) {
                this.fileHandle(args);
            } else if (args[0].equals(ClientCommands.LIST_USERS.toString())) {
                this.listUsers(args);
            } else {
                this.registrationHandle(args);
            }
        }
    }
}