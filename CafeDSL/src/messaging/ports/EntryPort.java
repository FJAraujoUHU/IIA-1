package messaging.ports;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import messaging.Message;
import messaging.Slot;

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
     * @throws Exception si hay problemas al crear el Slot de salida.
     */
    public EntryPort(int socket) throws Exception {
	this.port = socket;
	this.slot = new Slot();
	this.enabled = false;
	this.socket = null;
	this.ss = null;
        uuid = UUID.randomUUID();
    }

    /**
     * Devuelve un Slot para conectar la salida del puerto con una tarea.
     *
     * @return Slot de salida listo para ser usado.
     */
    public Slot getExitSlot() {
	return slot;
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
	    String content = "";
	    while (!content.equals(Message.SHUTDOWN) && !socket.isClosed() && enabled) {
		m = (Message) ois.readObject();
		content = m.toString();
		slot.send(m);
	    }
	} catch (Exception ex) {
	    if (enabled) {
		System.out.println(ex); //si el error no ha sido al cerrarse
	    }
	} finally {
	    enabled = false;
	    try {
		close();
	    } catch (Exception ex) {
		if (!ex.getMessage().contains("closed")) {
		    System.out.println(ex);
		}
	    }
	}
    }

    /**
     * Termina de recibir los mensajes, y cierra la conexión de manera forzada.
     *
     * @throws Exception Si se produce un error inesperado.
     */
    public void close() throws Exception {
	if (!enabled) {
	    throw new Exception("Port already closed (UUID = " + uuid + ")");
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
		slot.send(new Message(Message.SHUTDOWN));
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
