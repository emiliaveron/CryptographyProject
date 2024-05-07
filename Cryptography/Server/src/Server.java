import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static final int PORT = 8000;

    private static Map<String, String> connectedUsers = new HashMap<>();

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
                        String[] parts = message.split(" ", 3);
                        String username = parts[1];
                        String password = parts[2];

                        // Register the user in the database

                        // after success
                        // Add the user to the connected users map
                        connectedUsers.put(username, socket.getInetAddress().getHostAddress());
                        out.println("Success");
                        System.out.println("User registered: " + username);
                    } else if (message.startsWith(("/login"))) {
                        // Extract the username and password
                        String[] parts = message.split(" ", 3);
                        String username = parts[1];
                        String password = parts[2];

                        // Check if the user is in the database

                        // after success
                        // Add the user to the connected users map
                        connectedUsers.put(username, socket.getInetAddress().getHostAddress());
                        socket.getOutputStream().write("Success".getBytes());
                        System.out.println("User logged in: " + username);
                    } else if (message.startsWith("/connect")) {
                        // Extract the recipient
                        String[] parts = message.split(" ", 2);
                        String recipient = parts[1];

                        // Connect the user to the chosen recipient
                    } else if (message.startsWith(("/message"))) {

                    } else {
                        System.out.println("Unknown command: " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
