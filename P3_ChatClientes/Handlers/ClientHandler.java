package Handlers;

import java.io.*;
import java.net.Socket;
import java.util.Set;

import static Handlers.Handler.*;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Set<ClientHandler> allClients;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ClientHandler(Socket clientSocket, Set<ClientHandler> allClients) {
        this.socket = clientSocket;
        this.allClients = allClients;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            out.println(fmtServer("Conexión establecida. Escribe tu nombre de usuario:"));
            this.username = safeUsername(in.readLine());

            System.out.println(username + " se ha unido al chat.");
            broadcast(fmtServer(GREEN + username + RESET + " se ha unido al chat."), false);

            // imprimir ayuda de comandos
            printHelp();

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // se normaliza para comparar sin importar mayúsculas/minúsculas
                String lc = line.toLowerCase();

                // salir
                if (lc.equals("/exit") || lc.equals("exit")) break;

                // ayuda/help
                if (lc.equals("/help") || lc.equals("help") || lc.equals("/commands") || lc.equals("commands") || lc.equals("menu")) {
                    printHelp();
                    continue;
                }

                // change-username | change-userName | /nick | nick
                if (lc.startsWith("change-username ") || lc.startsWith("change-username\t")
                        || lc.startsWith("change-username\r")
                        || lc.startsWith("change-username")) {
                    String newName = line.substring(line.indexOf(' ') + 1).trim();
                    processChangeUsername(newName);
                    continue;
                }
                if (lc.startsWith("change-username")) { // por si solo escriben el comando sin espacio
                    out.println(fmtServer(RED + "Uso: change-username <nuevo>" + RESET));
                    continue;
                }
                if (lc.startsWith("change-username") || lc.startsWith("change-username ")) {
                    String newName = line.substring(line.indexOf(' ') + 1).trim();
                    processChangeUsername(newName);
                    continue;
                }
                if (lc.startsWith("change-username ") || lc.startsWith("/nick ") || lc.startsWith("nick ")) {
                    String newName = line.substring(line.indexOf(' ') + 1).trim();
                    processChangeUsername(newName);
                    continue;
                }
                if (lc.startsWith("change-username") || lc.equals("/nick") || lc.equals("nick")) {
                    out.println(fmtServer(RED + "Uso: change-username <nuevo>  (alias: /nick <nuevo>)" + RESET));
                    continue;
                }

                // send-msg | /w
                if (lc.startsWith("send-msg ") || lc.startsWith("/w ")) {
                    String rest = line.substring(line.indexOf(' ') + 1).trim();
                    processPrivate(rest);
                    continue;
                }
                if (lc.equals("send-msg") || lc.equals("/w")) {
                    out.println(fmtServer(RED + "Uso: send-msg <usuario> <mensaje>  (alias: /w <usuario> <mensaje>)" + RESET));
                    continue;
                }

                // global-msg o mensaje plano
                if (lc.startsWith("global-msg ")) {
                    String msg = line.substring("global-msg ".length()).trim();
                    processGlobal(msg);
                } else {
                    processGlobal(line);
                }
            }

        } catch (IOException e) {
            System.err.println("Cliente " + (username != null ? username : "") + " desconectado: " + e.getMessage());
        } finally {
            if (this.username != null) {
                System.out.println(this.username + " ha abandonado el chat.");
                broadcast(fmtServer(YELLOW + this.username + RESET + " ha abandonado el chat."), false);
            }
            allClients.remove(this); // quitarse a sí mismo de la lista
            try {
                if (socket != null) socket.close();
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String safeUsername(String raw) {
        String name = (raw == null ? "" : raw.trim());
        if (name.isEmpty()) name = "user-" + (int)(Math.random() * 1000);
        return name.replaceAll("\\s+", "_");
    }

    private void printHelp() {
        out.println(fmtServer("¡Bienvenido " + BOLD + username + RESET + "! Comandos:"));
        out.println(GRAY + "   " + RESET + BOLD + "change-username <nuevo>" + RESET + "   (alias: /nick <nuevo>)");
        out.println(GRAY + "   " + RESET + BOLD + "send-msg <usuario> <mensaje>" + RESET + "   (alias: /w <usuario> <mensaje>)");
        out.println(GRAY + "   " + RESET + BOLD + "global-msg <mensaje>" + RESET + "   (o escribe el mensaje directamente)");
        out.println(GRAY + "   " + RESET + BOLD + "help" + RESET + "   (alias: /help, commands, /commands, menu)");
        out.println(GRAY + "   " + RESET + BOLD + "exit" + RESET + "   (alias: /exit)");
    }

    private void processChangeUsername(String newName) {
        if (newName == null || newName.isBlank()) {
            out.println(fmtServer(RED + "Comando inválido." + RESET + " Uso: change-userName <nuevo>"));
            return;
        }
        String old = this.username;
        this.username = safeUsername(newName);
        broadcast(fmtServer(CYAN + old + RESET + " ahora es conocido como " + CYAN + this.username + RESET + "."), true);
        System.out.println(old + " cambió su nombre a " + this.username);
    }

    private void processPrivate(String rest) {
        // rest = usuario y mensaje
        String[] parts = rest.split(" ", 2);
        if (parts.length < 2 || parts[1].isBlank()) {
            out.println(fmtServer(RED + "Comando inválido." + RESET + " Uso: send-msg <usuario> <mensaje>"));
            return;
        }
        String target = parts[0].trim();
        String msg = parts[1].trim();

        boolean delivered = false;
        for (ClientHandler c : allClients) {
            if (c.username.equalsIgnoreCase(target)) {
                c.out.println(fmtDMFrom(this.username, msg)); // receptor
                delivered = true;
                break;
            }
        }
        if (delivered) {
            out.println(fmtDMTo(target, msg)); // eco al remitente
        } else {
            out.println(fmtServer(YELLOW + "Usuario '" + target + "' no encontrado o no conectado." + RESET));
        }
    }

    private void processGlobal(String msg) {
        if (msg.isBlank()) return;
        int recipients = broadcast(fmtChat(this.username, msg), true); // incluir a quien envía
        out.println(GRAY + "   (" + recipients + " usuario" + (recipients==1?"":"s") + " recibieron tu mensaje)" + RESET);
    }

    // Envía a todos y devuelve cuántos recibieron.
    private int broadcast(String message, boolean includeSelf) {
        int count = 0;
        for (ClientHandler client : allClients) {
            if (!includeSelf && client == this) continue;
            client.out.println(message);
            count++;
        }
        return count;
    }
}