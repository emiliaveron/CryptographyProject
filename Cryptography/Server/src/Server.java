import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int PORT = 8000;
    private static final Vector<User> connectedUsers = new Vector<User>();
    private static final DatabaseHandler db = new DatabaseHandler("users.db");

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress().getHostAddress());
                Thread clientThread = new Thread(new ClientHandler(socket));
                clientThread.start();
            }
        } catch (IOException e) {
            logger.error("Server exception", e);
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private User user;
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public static List<Integer> castToList(String input) {
            String[] elements = input.substring(1, input.length() - 1).split(", ");
            List<Integer> result = new ArrayList<>(elements.length);
            for (String item : elements) {
                result.add(Integer.valueOf(item));
            }
            return result;
        }

        private Integer generateRandomEncryptedNumber(List<Integer> publicKey){
            Random r = new Random();
            return RSA.encrypt(r.nextInt(26), publicKey);
        }

        @Override
        public void run() {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                // Handle messages from this user
                while (true) {
                    // Read message from the user
                    byte[] buffer = new byte[1024];
                    int bytesRead = socket.getInputStream().read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }

                    // Convert the message to a string
                    String message = new String(buffer, 0, bytesRead);
                    System.out.println("Received message: " + message);

                    if (message.startsWith("/register")) {
                        // Extract the username and password
                        String[] parts = message.split(" ", 4);
                        String username = parts[1];
                        String password = parts[2];
                        String publicKey = parts[3];

                         if (db.userExists(username)) {
                            out.println("User already exists.");
                            continue;
                        }

                         db.insertUser(username, password);

                        // Add the user to the connected users map
                        User user = new User(username, socket, publicKey);
                        connectedUsers.add(user);
                        this.user = user;
                        out.println("Success");
                        System.out.println("User registered: " + username);
                    } else if (message.startsWith(("/login"))) {
                        // Extract the username and password
                        String[] parts = message.split(" ", 4);
                        String username = parts[1];
                        String password = parts[2];
                        String publicKey = parts[3];

                        if (!db.verifyPassword(username, password)) {
                            out.println("Invalid");
                            continue;
                        }

                        if (connectedUsers.stream().anyMatch(u -> u.username.equals(username))) {
                            out.println("User already logged in.");
                            continue;
                        }

                        // Add the user to the connected users map
                        User user = new User(username, socket, publicKey);
                        connectedUsers.add(user);
                        this.user = user;
                        out.println("Success");
                        System.out.println("User logged in: " + username);
                    } else if (message.startsWith("/connect")) {
                        // Extract the recipient
                        String[] parts = message.split(" ", 2);
                        String recipient = parts[1];

                        // Check if the recipient is the user
                        if (recipient.equals(user.username)) {
                            out.println("User not found.");
                            System.out.println("Same user");
                            continue;
                        }

                        // list all users
                        StringBuilder userList = new StringBuilder();
                        for (User user : connectedUsers) {
                            if (Objects.equals(user.username, this.user.username)) {
                                continue;
                            }
                            userList.append(user.username).append("\n");
                        }
                        System.out.println(userList.toString());

                        User recipientUser = connectedUsers.stream()
                                .filter(u -> u.username.equals(recipient))
                                .findFirst()
                                .orElse(null);
                        if (recipientUser == null) {
                            System.out.println("User not found.");
                            out.println("User not found.");
                        } else {
                            user.setConnectedUser(recipientUser);
                            out.println("Found");
                            System.out.println(user.username + " connected to " + recipient);
                        }
                    } else if (message.startsWith(("/message"))) {
                        // Extract the message
                        String[] parts = message.split(" ", 2);
                        String messageContent = parts[1];

                        // Send the message to the recipient
                        PrintWriter recipientOut = new PrintWriter(user.connectedUser.socket.getOutputStream(), true);
                        recipientOut.println(messageContent);
                    } else if (message.startsWith("/list")) {
                        // Send the list of connected users to the client
                        StringBuilder userList = new StringBuilder();
                        for (User user : connectedUsers) {
                            if (Objects.equals(user.username, this.user.username)) {
                                continue;
                            }
                            userList.append(user.username).append("\n");
                        }
                        out.println(userList.toString());
                    } else {
                        System.out.println("Unknown command: " + message);
                    }
                }
            } catch (IOException e) {
                logger.error("Client exception", e);
            }
        }

    }
}
