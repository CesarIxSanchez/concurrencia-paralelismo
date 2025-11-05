package Handlers;

import concurrentChat.Cliente;

import java.net.Socket;
import java.util.Scanner;

import static Handlers.Handler.*;

public class WriteHandler extends Handler implements Runnable {

    public WriteHandler(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        try {
            while (out != null && !out.checkError()) {
                String line = sc.nextLine();

                if (line.toLowerCase().startsWith("/connect ")) {
                    String[] parts = line.trim().split("\\s+");

                    if (parts.length == 3) {
                        try {
                            String ip = parts[1];
                            int port = Integer.parseInt(parts[2]);

                            Cliente.initiateConnectionChange(ip, port);

                            break;

                        } catch (NumberFormatException e) {
                            System.err.println(fmtServer(RED + "Puerto inválido. Debe ser numérico." + RESET));
                        }
                    } else {
                        System.err.println(fmtServer(YELLOW + "Uso: /connect <ip> <port>" + RESET));
                    }
                } else {
                    out.println(line);

                    if (line.equalsIgnoreCase("/exit") || line.equalsIgnoreCase("exit")) {
                        break;
                    }
                }
            }
        } finally {
            dismiss();
        }
    }
}
