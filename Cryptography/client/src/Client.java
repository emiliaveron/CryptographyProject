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

            System.out.println("Enter a username:");
            String username = userInput.readLine();
            while (username.isEmpty()) {
                System.out.println("Username cannot be empty. Enter a username:");
                username = userInput.readLine();
            }
            System.out.println("Enter a password:");
            String password = userInput.readLine();
            while (password.isEmpty()) {
                System.out.println("Password cannot be empty. Enter a password:");
                password = userInput.readLine();
            }

            // Send choice to the server
            if (choice.equals("1")) {
                out.println("/register " + username + " " + password);
            } else {
                out.println("/login " + username + " " + password);
            }

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
}
