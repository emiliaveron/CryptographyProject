import java.net.Socket;

public class User {
    public String username;
    public Socket socket;
    public User connectedUser;
    public String publicKey;
    public boolean isConnected = false;

    public User(String username, Socket socket, String publicKey) {
        this.username = username;
        this.socket = socket;
        this.publicKey = publicKey;
    }

    public boolean setConnectedUser(User connectedUser) {
        this.connectedUser = connectedUser;
        this.isConnected = true;
        return connectedUser.isConnected;
    }
}
