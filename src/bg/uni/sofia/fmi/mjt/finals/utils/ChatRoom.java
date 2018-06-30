package bg.uni.sofia.fmi.mjt.finals.utils;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

import bg.uni.sofia.fmi.mjt.finals.server.ClientThread;

/**
 * Represents a chat room. The chat room can contain
 * multiple different users.
 */
public class ChatRoom {
    private final String roomName;
    private final String creatorUsername;
    private final Hashtable<String, ClientThread> members;
    private File historyFile;
    private BufferedWriter chatHistoryBufferedWriter;

    public ChatRoom(String roomName, String creatorUsername) {
        this.roomName = roomName;
        this.creatorUsername = creatorUsername;
        this.members = new Hashtable<>();
        this.initializeFile();
    }

    private void initializeFile() {
        this.historyFile = new File("./" + this.roomName + ".txt");
        try {
            this.historyFile.createNewFile();
            this.historyFile.deleteOnExit();
            this.chatHistoryBufferedWriter = new BufferedWriter(new FileWriter(this.historyFile));
        } catch (IOException e) {
            System.err.println("Failed to initialize the file.  " + e.getMessage());
        }
    }

    public synchronized void writeToFile(String message) {
        try {
            this.chatHistoryBufferedWriter.write(message);
            this.chatHistoryBufferedWriter.newLine();
            this.chatHistoryBufferedWriter.flush();
        } catch (IOException e) {
            System.err.println("Failed to write to the file. " + e.getMessage());
        }

    }

    public boolean isActiveChatRoom() {
        for (ClientThread member : this.members.values()) {
            if (member.getIsLoggedIn()) {
                return true;
            }
        }
        return false;
    }

    public Vector<ClientThread> getActiveUsersInRoom() {
        Vector<ClientThread> activeUsers = new Vector<>();
        for (ClientThread member : this.members.values()) {
            if (member.getIsLoggedIn()) {
                activeUsers.add(member);
            }
        }
        return activeUsers;
    }

    public void addMember(String memberName, ClientThread newMember) {
        this.members.put(memberName, newMember);

    }

    public void removeMember(String memberName) {
        this.members.remove(memberName);
    }

    public boolean containsMember(String memberName) {
        return this.members.containsKey(memberName);
    }

    public File getRoomFileHistory() {
        return this.historyFile;
    }

    public String getRoomName() {
        return this.roomName;
    }

    public String getCreatorUsername() {
        return this.creatorUsername;
    }
}
