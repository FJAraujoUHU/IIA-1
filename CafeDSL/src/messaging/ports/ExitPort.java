package messaging.ports;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;
import messaging.Message;
import messaging.Slot;

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
	    String content = "";
	    
	    while (!content.equals(Message.SHUTDOWN) && !socket.isClosed() && enabled) {
		m = slot.receive();
		content = m.toString();
		oos.writeObject(m);
	    }
	    oos.flush();
	} catch (Exception ex) {
	    if (enabled) {
		System.out.println(ex); //si el error no ha sido al cerrarse
	    }
	} finally {
	    enabled = false;
	    try {
		cerrar();
	    } catch (Exception ex) {
		if (!ex.getMessage().contains("closed")) {
		    System.out.println(ex);
		}
	    }
	}
    }

    /**
     * Termina de enviar los mensajes, y cierra la conexión.
     *
     * @throws Exception Si se produce un error inesperado.
     */
    public void cerrar() throws Exception {
	if (!enabled) {
	    throw new Exception("Port already closed.");
	}
	enabled = false;
	try {
	    if (socket != null) {
		if (!socket.isClosed()) {
		    socket.close();
		}
	    }
	    if (slot.available()) {
		slot.close();
	    }
	} catch (IOException ex) {
	    Exception e = new Exception("Error closing port (UUID = " + uuid + ")");
	    e.addSuppressed(ex);
	    throw e;
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
