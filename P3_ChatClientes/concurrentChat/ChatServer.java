package concurrentChat;

import Handlers.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static Handlers.Handler.*;

public class ChatServer {
    private static final int PORT = 8080;

    private static final Set<ClientHandler> CLIENTES = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket server = new ServerSocket(PORT);){
            System.err.println(fmtServer(GREEN + "Servidor activo en localhost:" + PORT + RESET));

            while (true) {
                Socket clientSocket = server.accept();
                System.out.println(fmtServer("Nueva conexión desde " + clientSocket.getInetAddress().getHostAddress()));

                ClientHandler client = new ClientHandler(clientSocket, CLIENTES);
                CLIENTES.add(client);
                pool.execute(client);
            }
        } catch (IOException e) {
            System.err.println(fmtServer("Error en servidor: " + e.getMessage()));
        }

    }
}
