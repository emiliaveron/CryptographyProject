import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8000;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Connected to server");

            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Show register or login prompt
            System.out.println("Enter 1 to register or 2 to login:");
            String choice = userInput.readLine();
            while (!choice.equals("1") && !choice.equals("2")) {
                System.out.println("Invalid choice. Enter 1 to register or 2 to login:");
                choice = userInput.readLine();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            boolean success;
            if (choice.equals("1")) {
                success = handleRegistration(userInput, out, in);
            } else {
                success = handleLogin(userInput, out, in);
            }

            if (!success) {
                System.out.println("Failed to register or login. Exiting...");
                return;
            }

            System.out.println("Successfully logged in. Choose the user you want to chat with:");
            String recipient = userInput.readLine();

            out.println("/connect " + recipient);
            String serverResponse = in.readLine();
            while (!serverResponse.equals("Success")) {
                System.out.println("User not found. Choose the user you want to chat with:");
                recipient = userInput.readLine();
                out.println("/connect " + recipient);
                serverResponse = in.readLine();
            }

            System.out.println("Connected to " + recipient + ". Start chatting!");

            // Start a thread to handle messages from the server
            Thread messageThread = new Thread(new MessageHandler(socket));
            messageThread.start();

            // Read user input and send messages to the server
            String message;
            while ((message = userInput.readLine()) != null) {
                out.println(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class MessageHandler implements Runnable {
        private Socket socket;

        public MessageHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String readUsername(BufferedReader userInput) throws IOException {
        System.out.println("Enter a username:");
        String username = userInput.readLine();
        while (username.isEmpty()) {
            System.out.println("Username cannot be empty. Enter a username:");
            username = userInput.readLine();
        }
        return username;
    }

    private static String readPassword(BufferedReader userInput) throws IOException {
        System.out.println("Enter a password:");
        String password = userInput.readLine();
        while (password.isEmpty()) {
            System.out.println("Password cannot be empty. Enter a password:");
            password = userInput.readLine();
        }
        return password;
    }

    private static boolean handleRegistration(BufferedReader userInput, PrintWriter out, BufferedReader in) throws IOException {
        String username = readUsername(userInput);
        String password = readPassword(userInput);
        out.println("/register " + username + " " + password);
        String serverResponse = in.readLine();
        System.out.println(serverResponse);
        while (serverResponse.equals("Username already exists")) {
            System.out.println("Username already exists. Enter a different username:");
            username = readUsername(userInput);
            out.println("/register " + username + " " + password);
            serverResponse = in.readLine();
        }

        return serverResponse.equals("Success");
    }

    private static boolean handleLogin(BufferedReader userInput, PrintWriter out, BufferedReader in) throws IOException {
        String username = readUsername(userInput);
        String password = readPassword(userInput);
        out.println("/login " + username + " " + password);
        String serverResponse = in.readLine();
        while (serverResponse.equals("Invalid username or password")) {
            System.out.println("Invalid username or password. Enter an username:");
            username = readUsername(userInput);
            System.out.println("Enter a password:");
            password = readPassword(userInput);
            out.println("/login " + username + " " + password);
            serverResponse = in.readLine();
        }

        return serverResponse.equals("Success");
    }
}
