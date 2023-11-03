import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class ConnectionHandler implements Runnable {

    public static final String CLASS_NAME = ConnectionHandler.class.getSimpleName();
    public static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private String username;

    private UserManager users;
    private Socket clientSocket = null;

    private BufferedReader input;
    private PrintWriter output;


    public ConnectionHandler(UserManager u, Socket s) {
        users = u;
        clientSocket = s;

        try {
            input = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        String buffer = null ;
        while (true) {
            try {
                buffer = input.readLine();
            } catch (IOException e) {
                LOGGER.severe(e.getMessage());
                e.printStackTrace();
            }
            String command = buffer.trim();
            // CONNECT Juan
            if( command.startsWith("CONNECT") ) {
                String userName = command.substring(command.indexOf(' ') + 1).trim();

                if (users.connect(userName, clientSocket)) {
                    username = userName;
                    output.println("OK");
                } else {
                    output.println("FAIL");
                }
            }

            // SEND #<mensaje>@<usuario>
             else if (command.startsWith("SEND")) {
                String messagePart = command.substring(command.indexOf('#') + 1, command.indexOf('@')).trim();
                String recipient = command.substring(command.indexOf('@') + 1).trim();

                if (!messagePart.isEmpty()) {
                    if (messagePart.length() > 140) {
                        messagePart = messagePart.substring(0, 140);
                    }
                    String sender = this.username;
                    String message = "MENSAJE DE " + sender + ": " + messagePart;

                    if (recipient.equals(sender)) {
                        output.println("NO PUEDES ENVIARTE MENSAJES A TI MISMO");
                    } else {

                        users.send(sender, recipient, message);
                        output.println("EL MENSAJE HA SIDO ENVIADO A " + recipient);
                    }
                } else {
                    output.println("EL MENSAJE ESTA VACIO");
                }
            }


            else if (command.startsWith("DISCONNECT")) {
                String userName = command.substring(command.indexOf(' ') + 1).trim();
                users.disconnect(userName);

                LOGGER.info("User " + userName + " SE HA DESCONECTADO");

                output.println("TE HAS DESCONECTADO");

            }

            else if(command.startsWith("LIST")){
                String userList = users.getUserList();
                output.println("USERS: " + userList);
            }

            else {
                output.println("COMANDO INVALIDO: " + command);

            }

        }


    }
}
