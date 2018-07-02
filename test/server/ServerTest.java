package bg.uni.sofia.fmi.mjt.finals.server;

import bg.uni.sofia.fmi.mjt.finals.utils.ChatRoom;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.file.Paths;

public class ServerTest {
    private static final String USERS_DATA = "./usersData.txt";
    private static final int PORT = 4444;

    @Test
    public void testIsUsedUserDataWithUsedData() {
        Server server = new Server(PORT);
        server.addInfoToDocumentation("ivan", "1234");
        Assert.assertEquals("info should be used", true, server.isUsedUserData("ivan", "1234"));
    }

    @Test
    public void testIsUsedUserDataWithNotUsedData() {
        Server server = new Server(PORT);
        server.addInfoToDocumentation("ivan", "1234");
        Assert.assertEquals("info should't be used", false, server.isUsedUserData("mitko", "4321"));
    }

    @Test
    public void testGetChatRoomByNameWithValidRoom() {
        Server server = new Server(PORT);
        ChatRoom chatRoom = new ChatRoom("staq", "ivan");
        server.addChatRoom("staq", chatRoom);
        Assert.assertEquals("shout return the chat room", chatRoom, server.getChatRoomByName("staq"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetChatRoomByNameWithNotValidRoom() {
        Server server = new Server(PORT);
        server.getChatRoomByName("staq");
    }

    @Test
    public void testDeleteChatRoomByName() {
        Server server = new Server(PORT);
        ChatRoom chatRoom = new ChatRoom("staq", "ivan");
        server.addChatRoom("staq", chatRoom);
        server.deleteChatRoomByName("staq");
        Assert.assertEquals("the room should't be contained", false, server.containsChatRoom("staq"));
    }

    @Test
    public void testGetClientByNameWithNotMemberClient() {
        Server server = new Server(PORT);
        Assert.assertEquals("the client is not contained", null, server.getClientByName("ivan"));
    }

    @Test
    public void testIsActiveClientWithNotActiveClient() {
        Server server = new Server(PORT);
        Assert.assertEquals("the client should not be active", false, server.isActiveClient("ivan"));
    }

    @Test
    public void testContainsClientWithNotContainedClient() {
        Server server = new Server(PORT);
        Assert.assertEquals("the client should't be contained", false, server.containsClient("ivan"));
    }

    @Test
    public void testContainsChatRoomWithNotContainedOne() {
        Server server = new Server(PORT);
        Assert.assertEquals("the room should't be contained", false, server.containsChatRoom("staq"));
    }

    private String readOneLineFromFile() {
        String lineRead = null;
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_DATA))) {
            lineRead = br.readLine();
        } catch (FileNotFoundException e) {
            System.err.println("The file cannot be found " + e.getMessage());
        } catch (IOException e) {
            System.err.println("A problem occurred when using the file " + e.getMessage());
        }
        return lineRead;
    }

    @Test
    public void testAddInfoToDocumentationWithAddedInfo() {
        Server server = new Server(PORT);
        server.addInfoToDocumentation("petar", "4321");
        Assert.assertEquals("the info should be read from file", "petar 4321", this.readOneLineFromFile());
    }

    @Test
    public void testSendFile() throws IOException {
        Server server = new Server(PORT);
        File file = new File("./testSendFile.txt");
        file.createNewFile();
        file.deleteOnExit();
        String location = "./testReceiveFile";
        server.sendFile(file.toPath().toString(), location, "ivan");
        File newFile = Paths.get(location + ".txt").toFile();
        newFile.deleteOnExit();
        Assert.assertEquals("the new file should exist", true, newFile.isFile());
    }
}
