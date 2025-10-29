package concurrentChat;

import Handlers.WriteHandler;
import Handlers.ReadHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import static Handlers.Handler.*;

public class Cliente {

    public static Socket conection = null;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println(BOLD + "Bienvenido al chat" + RESET);
        System.out.println("Uso: " + BOLD + "start-connection <IP> <PORT>" + RESET);
        System.out.println("Ejemplo: start-connection localhost 8080");

        // Loop hasta que la conexión sea exitosa
        while(true) {
            String command = scanner.nextLine();
            String[] parts = command.trim().split("\\s+");

            // Requisito: método start-conection
            if (parts.length == 3 && parts[0].equalsIgnoreCase("start-connection")) {
                String ip = parts[1];
                try {
                    int port = Integer.parseInt(parts[2]);
                    if(startConnection(ip, port)) {
                        System.out.println(fmtServer("Conexión exitosa a " + ip + ":" + port));
                        break; // Salir del loop
                    } else {
                        System.err.println(fmtServer(RED + "No se pudo conectar. Verifica IP/puerto y servidor." + RESET));
                    }
                } catch (NumberFormatException e) {
                    System.err.println(fmtServer(RED + "Puerto inválido. Debe ser numérico (ej. 8080)." + RESET));
                }
            } else {
                System.err.println(fmtServer(YELLOW + "Comando desconocido." + RESET + " Uso: start-connection <IP> <PORT>"));
            }
        }

        WriteHandler writer = new WriteHandler(conection);
        ReadHandler reader = new ReadHandler(conection);

        Thread writeThread = new Thread(writer, "client-writer");
        Thread readThread = new Thread(reader, "client-reader");

        readThread.start();
        writeThread.start();
    }

    public static boolean startConnection(String address, int port) {
        try {
            conection = new Socket(address, port);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
