package bg.uni.sofia.fmi.mjt.finals.server;

import bg.uni.sofia.fmi.mjt.finals.utils.ChatRoom;
import bg.uni.sofia.fmi.mjt.finals.utils.Pair;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class Server {

    private ServerSocket server;
    private final int port;
    private final Vector<ClientThread> clients;
    private final Hashtable<String, ChatRoom> chatRooms;
    private final Set<Pair<String, String>> userData;
    private static final String DOCUMENTATION_FILE_PATH = "./usersData";
    private File dataFile;
    private boolean isDataFileCreated;
    private BufferedWriter bufferedWriter;

    public Server(int port) {
        this.port = port;
        this.clients = new Vector<>();
        this.chatRooms = new Hashtable<>();
        this.userData = new HashSet<>();
    }

    public void startServer() {
        Socket client = null;
        try {
            this.server = new ServerSocket(this.port);
            System.out.println("Port 4444 is now open and waiting for connections.");

            while (true) {
                // accept a new client
                client = this.server.accept();
                System.out.println("A new client has connected: " + client.getInetAddress().getHostAddress());
                //create thread for the client
                ClientThread ch = new ClientThread(this, client);
                this.clients.add(ch);

                //start the thread
                new Thread(ch).start();
            }
        } catch (IOException e) {
            System.err.println("Server connection problem: " + e.getMessage());
        } finally {
            try {
                if (client != null) {
                    client.close();
                }
                if (this.server != null) {
                    this.server.close();
                }
            } catch (IOException e) {
                System.err.println("Problem with closing the socket or server socket " + e.getMessage());
            }
        }
    }

    public void addChatRoom(String roomName, ChatRoom room) {
        this.chatRooms.put(roomName, room);
    }

    public ClientThread getClientByName(String clientName) {
        for (ClientThread client : this.clients) {
            if (client.getName().equals(clientName)) {
                return client;
            }
        }
        throw new IllegalArgumentException("Invalid client");
    }

    public boolean isActiveClient(String clientName) {
        for (ClientThread client : this.clients) {
            if (client.getName().equals(clientName) && client.getIsLoggedIn()) {
                return true;
            }
        }
        return false;
    }

    public boolean containsClient(String clientName) {
        for (ClientThread client : this.clients) {
            if (client.getName().equals(clientName)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastMessage(String msg, String receiver) {
        for (ClientThread client : this.clients) {
            if (client.getName().equals(receiver)) {
                client.getPrintWriter().println(msg);
            }
        }
        //this.getClientByName(receiver).getPrintWriter().println(msg);
    }

    public void listActiveUsers(PrintWriter printWriter) {
        for (ClientThread client : this.clients) {
            if (client.getIsLoggedIn()) {
                printWriter.println(client.getName());
            }
        }
    }

    public boolean isUsedUserData(String name, String password) {
        for (Pair data : this.userData) {
            if (data.getKey().equals(name) || data.getValue().equals(password)) {
                return true;
            }
        }
        return false;
    }

    public void listActiveChatRooms(PrintWriter printWriter) {
        for (ChatRoom room : this.chatRooms.values()) {
            if (room.isActiveChatRoom()) {
                printWriter.println(room.getRoomName());
            }
        }
    }

    public void broadcastMessageToChatRoom(String msg, String roomName) {
        ChatRoom room = this.chatRooms.get(roomName);
        for (ClientThread client : room.getActiveUsersRoom()) {
            if (client.getIsLoggedIn()) {
                client.getPrintWriter().println(roomName + ": " + msg);
            }
        }
        room.writeToFile(msg);
    }

    public void broadcastChatRoomHistoryToUser(String roomName, PrintWriter printWriter) {
        ChatRoom room = this.chatRooms.get(roomName);
        if (room.getRoomFileHistory() != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(room.getRoomFileHistory()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    printWriter.println(line);
                }
            } catch (IOException e) {
                System.err.println("Problem with the file " + e.getMessage());
            }
        }
    }

    public void listActiveUsersInRoom(String roomName, PrintWriter printWriter) {
        for (ClientThread clientThread : this.chatRooms.get(roomName).getActiveUsersRoom()) {
            printWriter.println(clientThread.getName());
        }
    }

    public ChatRoom getChatRoomByName(String roomName) {
        if (!this.chatRooms.containsKey(roomName)) {
            throw new IllegalArgumentException("Invalid room");
        }
        return this.chatRooms.get(roomName);
    }

    public void deleteChatRoomByName(String roomName) {
        this.chatRooms.remove(roomName);
    }

    public boolean containsChatRoom(String roomName) {
        return this.chatRooms.containsKey(roomName);
    }

    private void createDataFile() {
        //dataFile = File.createTempFile(DOCUMENTATION_FILE_PATH, ".txt");
        this.dataFile = new File(DOCUMENTATION_FILE_PATH + ".txt");
        this.dataFile.deleteOnExit();
        this.isDataFileCreated = true;
        try {
            this.bufferedWriter = new BufferedWriter(new FileWriter(this.dataFile));
        } catch (IOException e) {
            System.err.println("Problem with initializing the buffered writer " + e.getMessage());
        }
    }

    public synchronized void addInfoToDocumentation(String username, String password) {
        if (!this.isDataFileCreated) {
            this.createDataFile();
        }
        this.userData.add(new Pair<>(username, password));
        try {
            this.bufferedWriter.write(username + " " + password);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        } catch (IOException e) {
            System.err.println("Problem with writing to the data file " + e.getMessage());
        }
    }

    private void removeUserFromChatRooms(String name) {
        for (ChatRoom room : this.chatRooms.values()) {
            if (room.containsMember(name)) {
                room.removeMember(name);
            }
        }
    }

    public void removeClient(ClientThread client) {
        this.clients.remove(client);
        this.removeUserFromChatRooms(client.getName());
    }

    public synchronized void sendFile(String tmpFilePath, String sendingLocation, String receiverName) {
        String extension = "";
        int index = tmpFilePath.lastIndexOf('.');
        if (index > 0) {
            extension = tmpFilePath.substring(index);
        }

        try {
            Files.copy(Paths.get(tmpFilePath), Paths.get(sendingLocation + extension), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Could not send file: " + e.getMessage());
        }
        this.broadcastMessage("The file has been sent", receiverName);
    }
}