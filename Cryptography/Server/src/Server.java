import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int PORT = 8000;
    private static final Vector<User> connectedUsers = new Vector<>();
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
            List<Integer> result = new ArrayList<>();
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(input);
            while (matcher.find()) {
                result.add(Integer.valueOf(matcher.group()));
            }
            return result;
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
                            out.println("Invalid");
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
                        String recipient = parts[1].replace("\n", "").replace("\r", "");

                        // Check if the recipient is the user
                        if (recipient.equals(user.username)) {
                            out.println("Invalid");
                            continue;
                        }

                        User recipientUser = connectedUsers.stream()
                                .filter(u -> u.username.equals(recipient))
                                .findFirst()
                                .orElse(null);
                        if (recipientUser == null) {
                            out.println("Invalid");
                        } else {
                            boolean ready = user.setConnectedUser(recipientUser);
                            out.println("Found");
                            if (ready) {
                                PrintWriter recipientOut = new PrintWriter(recipientUser.socket.getOutputStream(), true);
                                Random random = new Random();
                                int shift = random.nextInt(25) + 1;
                                recipientOut.println("Success " + RSA.encrypt(shift, castToList(recipientUser.publicKey)));
                                out.println("Success " + RSA.encrypt(shift, castToList(user.publicKey)));
                            }
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
                        out.println(userList.append("END"));
                    } else {
                        System.out.println("Unknown command: " + message);
                    }
                }
            } catch (IOException e) {
                logger.error("Client exception", e);
            } finally {
                try {
                    socket.close();
                    connectedUsers.remove(user);
                    PrintWriter out = new PrintWriter(user.connectedUser.socket.getOutputStream(), true);
                    out.println("Close");
                } catch (IOException e) {
                    logger.error("Error closing socket", e);
                }
            }
        }

    }
}
