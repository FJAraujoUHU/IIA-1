package mensajeria.puertos;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import mensajeria.Mensaje;
import mensajeria.Slot;

/**
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class PuertoSalida implements Runnable {

    private final int puerto;
    private final String host;
    private final Slot slot;
    private volatile boolean activo;
    private Socket socket;

    /**
     * Constructor del puerto
     *
     * @param host Dirección a la que conectarse.
     * @param puerto Puerto del destino. (0-65535)
     * @param entrada Slot por el que recibir mensajes.
     */
    public PuertoSalida(String host, int puerto, Slot entrada) {
	this.puerto = puerto;
	this.host = host;
	this.slot = entrada;
	this.activo = false;
	this.socket = null;
    }

    @Override
    public void run() {
	System.out.println("Conectando a " + host + ":" + puerto + "...");
	try {
	    socket = new Socket(host, puerto);
	    //Saca el stream de salida desde el puerto
	    OutputStream os = socket.getOutputStream();
	    //Crea un objectInput para enviar mensajes.
	    ObjectOutputStream oos = new ObjectOutputStream(os);
	    activo = true;

	    Mensaje m;
	    String contenido = "";
	    
	    while (!contenido.equals(Mensaje.APAGAR_SISTEMA) && !socket.isClosed() && activo) {
		m = slot.recibir();
		contenido = m.toString();
		oos.writeObject(m);
	    }
	    oos.flush();
	} catch (Exception ex) {
	    if (activo) {
		System.out.println(ex); //si el error no ha sido al cerrarse
	    }
	} finally {
	    activo = false;
	    try {
		cerrar();
	    } catch (Exception ex) {
		if (!ex.getMessage().contains("cerrado")) {
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
	if (!activo) {
	    throw new Exception("El puerto ya estaba cerrado");
	}
	activo = false;
	try {
	    if (socket != null) {
		if (!socket.isClosed()) {
		    socket.close();
		}
	    }
	    if (slot.abierto()) {
		slot.cerrar();
	    }
	} catch (IOException ex) {
	    Exception e = new Exception("Error cerrando el puerto.");
	    e.addSuppressed(ex);
	    throw e;
	}
    }

    /**
     * Devuelve el estado de la conexión.
     *
     * @return Si el puerto es funcional.
     */
    public boolean abierto() {
	return activo;
    }
}
