package bg.uni.sofia.fmi.mjt.finals.server;

public enum ClientCommands {
    CONNECT("connect"),
    REGISTER("register"),
    LOGIN("login"),
    DISCONNECT("disconnect"),
    LIST_USERS("list-users"),
    SEND("send"),
    SEND_FILE("send-file"),
    CREATE_ROOM("create-room"),
    DELETE_ROOM("delete-room"),
    JOIN_ROOM("join-room"),
    LEAVE_ROOM("leave-room"),
    LIST_ROOMS("list-rooms"),
    SEND_ALL("send-all"),
    ACCEPT("accept"),
    DECLINE("decline"),
    QUIT("quit");

    private final String command;

    ClientCommands(String command) {
        this.command = command;
    }

    public String toString() {
        return this.command;
    }

    public static boolean containsCommand(String command) {
        for (ClientCommands validCommand : ClientCommands.values()) {
            if (validCommand.toString().equals(command)) {
                return true;
            }
        }
        return false;
    }
}
