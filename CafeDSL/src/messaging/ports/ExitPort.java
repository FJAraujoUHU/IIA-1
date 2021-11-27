package messaging.ports;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;
import messaging.Message;
import messaging.Slot;
import messaging.SlotException;

/**
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class ExitPort implements Runnable {

    private final int port;
    private final String host;
    private final Slot slot;
    private volatile boolean enabled;
    private Socket socket;
    private final UUID uuid;

    /**
     * Constructor del puerto
     *
     * @param host Dirección a la que conectarse.
     * @param puerto Puerto del destino. (0-65535)
     * @param entrada Slot por el que recibir mensajes.
     */
    public ExitPort(String host, int puerto, Slot entrada) {
	this.port = puerto;
	this.host = host;
	this.slot = entrada;
	this.enabled = false;
	this.socket = null;
        uuid = UUID.randomUUID();
    }

    @Override
    public void run() {
	System.out.println("Connecting to " + host + ":" + port + "...");
	try {
	    socket = new Socket(host, port);
	    //Saca el stream de salida desde el puerto
	    OutputStream os = socket.getOutputStream();
	    //Crea un objectInput para enviar mensajes.
	    ObjectOutputStream oos = new ObjectOutputStream(os);
	    enabled = true;

	    Message m;
            do {
                m = slot.receive();
		oos.writeObject(m);
            } while (slot.availableRead() && !socket.isClosed() && enabled);
                    
	    oos.flush();
	} catch (IOException | SlotException ex) {
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
     * Cierra la conexión forzadamente.
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
            slot.finallyClose();
        } catch (IOException ex) {
            slot.finallyClose();
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
     * @return UUID del objeto.
     */
    public UUID getUUID() {
        return uuid;
    }
}
