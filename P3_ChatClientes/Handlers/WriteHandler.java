package Handlers;

import java.net.Socket;
import java.util.Scanner;

public class WriteHandler extends Handler implements Runnable {

    public WriteHandler(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        try {
            while (out!= null) {
                String line = sc.nextLine();
                out.println(line);
                if ("/exit".equalsIgnoreCase(line) || "exit".equalsIgnoreCase(line)) break;
            }
        } finally {
            dismiss();
        }
    }
}
