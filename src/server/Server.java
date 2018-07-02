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

/**
 * Represents a server.
 * Multiple different clients can connect to the server.
 * Provides communication between tha clients.
 */
public class Server {
    private ServerSocket server;
    private final int port;
    private final Hashtable<String, ChatRoom> chatRooms;
    private final Set<Pair<String, String>> userData;
    private File dataFile;
    private boolean isDataFileCreated;
    private BufferedWriter bufferedWriter;
    private static final String DOCUMENTATION_FILE_PATH = "./usersData";
    private final Hashtable<String, ClientThread> clientsWithData;

    public Server(int port) {
        this.port = port;
        this.chatRooms = new Hashtable<>();
        this.userData = new HashSet<>();
        this.clientsWithData = new Hashtable<>();
    }

    /**
     * Starts the server. After the server is started
     * is waits for new connections on his ServerSocket and
     * establishes them.
     */
    public void startServer() {
        Socket client = null;
        try {
            this.server = new ServerSocket(this.port);
            System.out.println("Port 4444 is now open and waiting for connections.");
            while (true) {
                client = this.server.accept();
                System.out.println("A new client has connected: " + client.getInetAddress().getHostAddress());
                ClientThread ch = new ClientThread(this, client);
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
        return this.clientsWithData.get(clientName);
    }

    public boolean isActiveClient(String clientName) {
        return this.clientsWithData.keySet().contains(clientName) && this.clientsWithData.get(clientName).getIsLoggedIn();
    }

    public boolean containsClient(String clientName) {
        return this.clientsWithData.keySet().contains(clientName);
    }

    public void broadcastMessage(String msg, String receiver) {
        if (this.clientsWithData.containsKey(receiver)) {
            this.clientsWithData.get(receiver).getPrintWriter().println(msg);
        }
    }

    public void listActiveUsers(PrintWriter printWriter) {
        this.clientsWithData.values().stream().filter(ClientThread::getIsLoggedIn).forEach(a -> printWriter.println(a.getName()));
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
        this.chatRooms.values().stream().filter(ChatRoom::isActiveChatRoom).forEach(a -> printWriter.println(a.getRoomName()));
    }

    public void broadcastMessageToChatRoom(String msg, String roomName) {
        ChatRoom room = this.chatRooms.get(roomName);
        room.getActiveUsersInRoom().stream().filter(ClientThread::getIsLoggedIn).forEach(a -> a.getPrintWriter().println(roomName + ": " + msg));
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
        this.chatRooms.get(roomName).getActiveUsersInRoom().forEach(a -> printWriter.println(a.getName()));
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

    private void createDataFile() {
        this.dataFile = new File(DOCUMENTATION_FILE_PATH + ".txt");
        this.dataFile.deleteOnExit();
        this.isDataFileCreated = true;
        try {
            this.bufferedWriter = new BufferedWriter(new FileWriter(this.dataFile));
        } catch (IOException e) {
            System.err.println("Problem with initializing the buffered writer " + e.getMessage());
        }
    }

    public synchronized void addInfoForUser(String username, ClientThread clientThread) {
        this.clientsWithData.put(username, clientThread);
    }

    public void removeClient(ClientThread client) {
        this.clientsWithData.remove(client.getName());
        this.removeUserFromChatRooms(client.getName());
    }

    private void removeUserFromChatRooms(String name) {
        this.chatRooms.values().stream().filter(a -> a.containsMember(name)).forEach(a -> a.removeMember(name));
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