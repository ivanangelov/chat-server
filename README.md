# Chat-server

This is a multithreaded chat server. 
It allows multiple users to use it simultaneously.
Every user has his own unique username and password. User's data is stored in a file.
One user can send a message to every other user that is logged in.
The chat server supports the creation of chat rooms.
A chat room can be created by one user. The room can be deleted only by its creator.
Chat room's history is stored in a file as well.

Commands supported by the server:

- connect <host> <port> - connect to the server
- register <username> <password> - register user
- login <username> <password> - log in to the system
- disconnect – log out of the system
- quit - leave the server
- list-users – list all users, who are currenly active
- send <username> <message> - send DM to a user
- send-file <username> <file_location> - send file to a user
- create-room <room_name> - create a new chat room
- delete-room <room_name> - delete existing chat room
- join-room <room_name> - join existing chat room
- leave-room <room_name> - leave chat room
- list-rooms - list all active chat rooms (with >=1 active users)
- list-users <room> - list all active users in a particular chat room

