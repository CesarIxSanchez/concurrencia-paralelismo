package concurrentChat;

import Handlers.WriteHandler;
import Handlers.ReadHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    private static final int PORT = 8080;

    public static void main(String[] args) {

        try {
            ServerSocket server = new ServerSocket(PORT);
            System.err.println("Server activo en localhost:" + PORT);

            Socket socket = server.accept();
            System.out.println("IP del nuevo cliente:" + socket.getInetAddress());

            WriteHandler writer = new WriteHandler(socket);
            ReadHandler reader = new ReadHandler(socket);

            Thread writeThread = new Thread(writer);
            Thread readThread = new Thread(reader);

            writeThread.start();
            readThread.start();

        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

}
