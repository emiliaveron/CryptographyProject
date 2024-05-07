import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static final int PORT = 8080;
    private static final Map<String, Socket> users = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                Thread clientThread = new Thread(new ClientHandler(socket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Read user ID from the URL
                String userId = getUserIdFromUrl(socket);
                System.out.println("User connected: " + userId);

                // Add the user to the map
                synchronized (users) {
                    users.put(userId, socket);
                }

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

                    if (message.startsWith("/register")) {
                        // Extract the username and password
                        String[] parts = message.split(" ", 3);
                        String username = parts[1];
                        String password = parts[2];

                        // Register the user in the database
                    } else if (message.startsWith(("/login"))) {
                        // Extract the username and password
                        String[] parts = message.split(" ", 3);
                        String username = parts[1];
                        String password = parts[2];

                        // Check if the user is in the database
                    } else if (message.startsWith("/send")) {
                        // Extract the recipient ID and message
                        String[] parts = message.split(" ", 3);
                        String recipientId = parts[1];
                        String messageToSend = parts[2];

                        // Find the recipient in the map
                        Socket recipientSocket;
                        synchronized (users) {
                            recipientSocket = users.get(recipientId);
                        }

                        // Send the message to the recipient
                        if (recipientSocket != null) {
                            recipientSocket.getOutputStream().write(messageToSend.getBytes());
                        } else {
                            System.out.println("User not found: " + recipientId);
                        }
                    } else {
                        System.out.println("Unknown command: " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Remove user when disconnected
                synchronized (users) {
                    try {
                        users.remove(getUserIdFromUrl(socket));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private String getUserIdFromUrl(Socket socket) throws IOException {
            // Extract user ID from URL
            // Example: http://localhost:8080/user1234
            String url = socket.getRemoteSocketAddress().toString();
            String[] parts = url.split("/");
            return parts[parts.length - 1];
        }
    }
}
