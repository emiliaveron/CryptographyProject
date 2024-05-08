import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8000;
    private static final RSA rsa = new RSA();

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

            System.out.println("Successfully logged in. Choose the user you want to chat with from the list:");
            out.println("/list");
            List<String> userLines = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null && !line.equals("END")) {
                userLines.add(line);
            }

            for (String userLine : userLines) {
                System.out.println(userLine);
            }
            String recipient = userInput.readLine();

            out.println("/connect " + recipient);
            String serverResponse = in.readLine();
            while (!serverResponse.equals("Found")) {
                System.out.println("User not found. Choose the user you want to chat with:");
                recipient = userInput.readLine();
                out.println("/connect " + recipient);
                serverResponse = in.readLine();
            }
            System.out.println("Waiting for " + recipient + " to connect.");
            serverResponse = in.readLine();
            if (!serverResponse.startsWith("Success")) {
                return;
            }
            System.out.println("Connected to " + recipient + ". Start chatting!");

            int encryptedShift = Integer.parseInt(serverResponse.split(" ")[1]);
            int shift = RSA.decrypt(encryptedShift, rsa.privateKey);

            // Start a thread to handle messages from the server
            Thread messageThread = new Thread(new MessageHandler(socket, shift));
            messageThread.start();

            // Read user input and send messages to the server
            System.out.println("the shift is: " + shift);
            CaesarCipher caesarCipher = new CaesarCipher(shift);
            String message;
            while ((message = userInput.readLine()) != null) {
                String encryptedMessage = caesarCipher.encrypt(message);
                System.out.println("encrypted message: " + encryptedMessage + " message: " + message);
                out.println("/message " + encryptedMessage);
            }
        } catch (IOException e) {
            logger.error("Error in client", e);
        }
    }

    private static class MessageHandler implements Runnable {
        private final Socket socket;
        private final int shift;

        public MessageHandler(Socket socket, int shift) {
            this.socket = socket;
            this.shift = shift;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String message;
                CaesarCipher caesarCipher = new CaesarCipher(shift);
                while ((message = in.readLine()) != null) {
                    String decryptedMessage = caesarCipher.decrypt(message);
                    System.out.println(decryptedMessage);
                }

            } catch (IOException e) {
                logger.error("Error in message handler", e);
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
        out.println("/register " + username + " " + password + " " + rsa.publicKey.toString());
        String serverResponse = in.readLine();
        System.out.println(serverResponse);
        while (serverResponse.equals("Username already exists")) {
            System.out.println("Username already exists. Enter a different username:");
            username = readUsername(userInput);
            out.println("/register " + username + " " + password + " " + rsa.publicKey.toString());
            serverResponse = in.readLine();
        }

        return serverResponse.equals("Success");
    }

    private static boolean handleLogin(BufferedReader userInput, PrintWriter out, BufferedReader in) throws IOException {
        String username = readUsername(userInput);
        String password = readPassword(userInput);
        out.println("/login " + username + " " + password + " " + rsa.publicKey.toString());
        String serverResponse = in.readLine();
        while (serverResponse.equals("Invalid username or password")) {
            System.out.println("Invalid username or password. Enter an username:");
            username = readUsername(userInput);
            System.out.println("Enter a password:");
            password = readPassword(userInput);
            out.println("/login " + username + " " + password + " " + rsa.publicKey.toString());
            serverResponse = in.readLine();
        }

        return serverResponse.equals("Success");
    }
}
