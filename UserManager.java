import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

public class UserManager {

    public static final String CLASS_NAME = UserManager.class.getSimpleName();
    public static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private HashMap<String, Socket> connections;

    public UserManager() {
        super();
        connections = new HashMap<String, Socket>();
    }

    public synchronized boolean connect(String user, Socket socket) {
        if (!connections.containsKey(user)) {
            connections.put(user, socket);
            return true;
        }
        return false;
    }

    public synchronized void disconnect(String user) {
        connections.remove(user);
    }

    public synchronized void send(String sender, String recipient, String message) {
        Socket userSocket = connections.get(recipient);
        if (userSocket != null) {
            try {
                PrintWriter recipientOutput = new PrintWriter(userSocket.getOutputStream(), true);
                recipientOutput.println(message);
            } catch (IOException e) {
                LOGGER.severe(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public boolean userExists(String userName) {
        return connections.containsKey(userName);
    }


    public synchronized String getUserList() {
        return String.join(", ", connections.keySet());
    }

}
