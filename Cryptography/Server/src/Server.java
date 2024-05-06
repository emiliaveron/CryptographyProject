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
                    // Receive message
                    // Process the message or forward it to the other user
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
