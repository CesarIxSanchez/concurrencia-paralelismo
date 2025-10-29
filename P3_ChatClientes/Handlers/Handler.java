package Handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalTime;

public class Handler {

    protected Socket socket;
    protected BufferedReader in;
    protected PrintWriter out;

    // paleta ANSI para terminal
    public static final String RESET  = "\u001B[0m";
    public static final String BOLD   = "\u001B[1m";
    public static final String DIM    = "\u001B[2m";
    public static final String RED    = "\u001B[31m";
    public static final String YELLOW = "\u001B[33m";
    public static final String GREEN  = "\u001B[32m";
    public static final String CYAN   = "\u001B[36m";
    public static final String MAGENTA= "\u001B[35m";
    public static final String GRAY   = "\u001B[90m";

    public Handler(Socket socket, String IP){
        this(socket);
    }

    public Handler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException ex) {
            System.err.println("Error al crear handlers: " + ex.getMessage());
        }

    }

    public void dismiss() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            System.err.println("Error al cerrar el socket: " + ex.getMessage());
        }
    }

    // utilidades de formato
    public static String ts() {
        return LocalTime.now().withNano(0).toString(); // HH:mm:ss
    }
    public static String fmtChat(String user, String msg) {
        return GRAY + "[" + ts() + "] " + RESET + BOLD + user + RESET + " > " + msg;
    }
    public static String fmtServer(String msg) {
        return GRAY + "[" + ts() + "] " + RESET + BOLD + "SERVER" + RESET + " :: " + msg;
    }
    public static String fmtDMFrom(String user, String msg) {
        return GRAY + "[" + ts() + "] " + RESET + MAGENTA + "[DM] " + RESET + BOLD + user + RESET + " > " + msg;
    }
    public static String fmtDMTo(String user, String msg) {
        return GRAY + "[" + ts() + "] " + RESET + MAGENTA + "[DM→" + user + "] " + RESET + msg;
    }

}
