package messaging.ports;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import messaging.*;
/**
 * Puerto que escucha en un puerto a que le lleguen mensajes de una aplicación.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class EntryPort implements Runnable {

    private final int port;
    private final Slot slot;
    private volatile boolean enabled;
    private Socket socket;
    private ServerSocket ss;
    private final UUID uuid;

    /**
     * Constructor del puerto
     *
     * @param socket Puerto (socket) al que escuchar. (0-65535)
     * @param out Puerto de salida por el que enviar los mensajes.
     */
    public EntryPort(int socket, Slot out) {
        this.port = socket;
        this.slot = out;
        this.enabled = false;
        this.socket = null;
        this.ss = null;
        uuid = UUID.randomUUID();
    }

    @Override
    public void run() {
        try {
            ss = new ServerSocket(port);
            System.out.println("Awaiting connections...");
            socket = ss.accept(); // Espera a que se establezca una conexión al puerto
            System.out.println("Connection from " + socket.getInetAddress().getCanonicalHostName() + ":" + socket.getPort() + "!");
            enabled = true;

            //Saca el stream de entrada desde el puerto
            InputStream is = socket.getInputStream();
            //Crea un objectInput para sacar mensajes desde ahí
            ObjectInputStream ois = new ObjectInputStream(is);

            Message m;
            do {
                m = (Message) ois.readObject();
                slot.send(m);
            } while (!m.equals(Message.SHUTDOWN) && !socket.isClosed() && enabled);

        } catch (IOException | ClassNotFoundException | SlotException ex) {
            if (enabled) {
                System.out.println(ex); //si el error no ha sido al cerrarse
            }
        } finally {
            enabled = false;
            try {
                close();
            } catch (PortException ex) {
                /*No hacer nada, ya estaba cerrado*/
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }

    /**
     * Termina de recibir los mensajes, y cierra la conexión de manera forzada.
     *
     * @throws PortException Si el puerto ya estaba cerrado.
     * @throws java.io.IOException Si se produce algún error cerrando los sockets.
     */
    public void close() throws PortException, IOException {
        if (!enabled) {
            throw new PortException("Port already closed (UUID = " + uuid + ")");
        }
        enabled = false;
        try {
            if (socket != null) {
                if (!socket.isClosed()) {
                    socket.close();
                }
            }
            if (ss != null) {
                if (!ss.isClosed()) {
                    ss.close();
                }
            }
            if (slot.available()) {
                try {
                    slot.send(Message.SHUTDOWN);
                } catch (SlotException ex) {
                    /*Nunca debería lanzarse porque se comprueba primero si está abierto*/
                }
            }
        } catch (IOException ex) {
            if (slot.available()) {
                try {
                    slot.send(Message.SHUTDOWN);
                } catch (SlotException e) {
                    /*Nunca debería lanzarse porque se comprueba primero si está abierto*/
                }
            }
            throw ex;
        }
    }

    /**
     * Devuelve el estado de la conexión.
     *
     * @return Si el puerto es funcional.
     */
    public boolean available() {
        return enabled;
    }

    /**
     * Devuelve el UUID único del slot (es intransferible)
     *
     * @return UUID del objeto.
     */
    public UUID getUUID() {
        return uuid;
    }
}
