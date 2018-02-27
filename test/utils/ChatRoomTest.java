package bg.uni.sofia.fmi.mjt.finals.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ChatRoomTest {
    private static final String ROOM_NAME = "staq";
    private static final String ROOM_CREATOR = "ivan";

    @Test
    public void testIsActiveChatRoomWithNotActive() {
        ChatRoom chatRoom = new ChatRoom(ROOM_NAME, ROOM_CREATOR);
        Assert.assertEquals("not active room", false, chatRoom.isActiveChatRoom());
    }

    @Test
    public void testGetActiveUsersRoomWithNoUsers() {
        ChatRoom chatRoom = new ChatRoom(ROOM_NAME, ROOM_CREATOR);
        Assert.assertEquals("room with no added users has 0 users", 0, chatRoom.getActiveUsersRoom().size());
    }

    @Test
    public void testContainsMemberWithInvalidMember() {
        ChatRoom chatRoom = new ChatRoom(ROOM_NAME, ROOM_CREATOR);
        Assert.assertEquals("not contained member, should return false", false, chatRoom.containsMember("petio"));
    }

    private String readOneLineFromFile(ChatRoom chatRoom) {
        String lineRead = null;
        try (BufferedReader br = new BufferedReader(new FileReader(chatRoom.getRoomFileHistory()))) {
            lineRead = br.readLine();
        } catch (FileNotFoundException e) {
            System.err.println("The file cannot be found " + e.getMessage());
        } catch (IOException e) {
            System.err.println("A problem occurred when using the file " + e.getMessage());
        }
        return lineRead;
    }

    @Test
    public void testWriteToFileWithNothingWritten() {
        ChatRoom chatRoom = new ChatRoom(ROOM_NAME, ROOM_CREATOR);
        Assert.assertEquals("nothing should be read from the file", null, this.readOneLineFromFile(chatRoom));
    }

    @Test
    public void testWriteToFileWithSomethingWritten() {
        ChatRoom chatRoom = new ChatRoom(ROOM_NAME, ROOM_CREATOR);
        chatRoom.writeToFile("hello");
        Assert.assertEquals("one line should be read", "hello", this.readOneLineFromFile(chatRoom));
    }
}
