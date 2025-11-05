package concurrentChat;

import Handlers.WriteHandler;
import Handlers.ReadHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import static Handlers.Handler.*;

public class Cliente {

    public static Socket conection = null;

    private static boolean needsReconnect = false;
    private static String nextIp = null;
    private static int nextPort = 0;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println(BOLD + "Bienvenido al chat" + RESET);
        System.out.println("Uso: " + BOLD + "start-connection <IP> <PORT>" + RESET);
        System.out.println("Para cambiar de servidor, usa: " + BOLD + "/connect <IP> <PORT>" + RESET);
        System.out.println("Ejemplo: start-connection localhost 8080");

        while(true) {
            needsReconnect = false;

            if (conection == null) {
                while (true) {
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
            }

            if (conection != null) {
                WriteHandler writer = new WriteHandler(conection);
                ReadHandler reader = new ReadHandler(conection);

                Thread writeThread = new Thread(writer, "client-writer");
                Thread readThread = new Thread(reader, "client-reader");

                readThread.start();
                writeThread.start();

                try {
                    // El hilo 'main' se bloquea aquí, esperando que los hilos mueran
                    // (ya sea por /exit o por /connect)
                    readThread.join();
                    writeThread.join();
                } catch (InterruptedException e) {
                    System.err.println("Hilos de chat interrumpidos.");
                }
            }

            if (needsReconnect) {
                System.out.println(fmtServer(YELLOW + "Cambiando de conexión a " + nextIp + ":" + nextPort + "..." + RESET));
                if (startConnection(nextIp, nextPort)) {
                    System.out.println(fmtServer(GREEN + "Conexión exitosa a " + nextIp + ":" + nextPort + RESET));
                    // El 'conection' está actualizado. El 'while(true)' principal
                    // volverá a empezar, verá que 'conection' no es null,
                    // e iniciará nuevos hilos (Bucle 2).
                } else {
                    System.err.println(fmtServer(RED + "No se pudo conectar a " + nextIp + ":" + nextPort + ". Volviendo al inicio." + RESET));
                    conection = null; // Forzar a que pida 'start-connection' de nuevo
                }
            } else {
                // Si los hilos murieron y no fue por reconexión, fue por /exit
                System.out.println(fmtServer("Desconectado. Gracias por usar el chat."));
                break; // Salir del 'while(true)' principal y terminar el programa
            }
        }

        scanner.close();
    }

    public static boolean startConnection(String address, int port) {
        try {
            // Asegurarse de cerrar la conexión anterior si existe
            if (conection != null && !conection.isClosed()) {
                conection.close();
            }
            conection = new Socket(address, port);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void initiateConnectionChange(String ip, int port) {
        needsReconnect = true;
        nextIp = ip;
        nextPort = port;
    }
}
