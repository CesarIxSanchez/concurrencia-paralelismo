package Handlers;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ReadHandler extends Handler implements Runnable {

    public ReadHandler(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        String messageRx;
        try {
            while ((messageRx = in.readLine()) != null) {
                System.out.println(messageRx); // imprime el mensaje del servidor
            }
        } catch (SocketException e) {
            System.out.println("Te has desconectado del servidor.");
        } catch (EOFException e) {
            System.out.println("Servidor desconectado (EOF).");
        } catch (IOException ex) {
            System.err.println("Error de lectura: " + ex.getMessage());
        } finally {
            dismiss(); // para asegurar que todo se cierre
        }
    }
}
