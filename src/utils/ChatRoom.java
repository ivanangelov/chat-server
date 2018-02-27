package bg.uni.sofia.fmi.mjt.finals.utils;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

import bg.uni.sofia.fmi.mjt.finals.server.ClientThread;

public class ChatRoom {
    private final String roomName;
    private final String creatorUsername;
    private final Hashtable<String, ClientThread> members;
    private File historyFile;
    private BufferedWriter bufferedWriter;

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
            this.bufferedWriter = new BufferedWriter(new FileWriter(this.historyFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void writeToFile(String message) {
        try {
            this.bufferedWriter.write(message);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
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

    public Vector<ClientThread> getActiveUsersRoom() {
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
