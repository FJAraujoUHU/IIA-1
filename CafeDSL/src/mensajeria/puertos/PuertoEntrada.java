package mensajeria.puertos;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import mensajeria.Mensaje;
import mensajeria.Slot;

/**
 * Puerto que escucha en un puerto a que le lleguen mensajes de una aplicación.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class PuertoEntrada implements Runnable {

    private final int puerto;
    private final Slot slot;
    private volatile boolean activo;
    private Socket socket;
    private ServerSocket ss;

    /**
     * Constructor del puerto
     *
     * @param puerto Puerto (socket) al que escuchar. (0-65535)
     * @throws Exception si hay problemas al crear el Slot de salida.
     */
    public PuertoEntrada(int puerto) throws Exception {
	this.puerto = puerto;
	this.slot = new Slot();
	this.activo = false;
	this.socket = null;
	this.ss = null;
    }

    /**
     * Devuelve un Slot para conectar la salida del puerto con una tarea.
     *
     * @return Slot de salida listo para ser usado.
     */
    public Slot getSlotSalida() {
	return slot;
    }

    @Override
    public void run() {
	try {
	    ss = new ServerSocket(puerto);
	    System.out.println("Puerto esperando conexiones...");
	    socket = ss.accept(); // Espera a que se establezca una conexión al puerto
	    System.out.println("Conexión desde " + socket.getInetAddress().getCanonicalHostName() + ":" + socket.getPort() + "!");
	    activo = true;

	    //Saca el stream de entrada desde el puerto
	    InputStream is = socket.getInputStream();
	    //Crea un objectInput para sacar mensajes desde ahí
	    ObjectInputStream ois = new ObjectInputStream(is);

	    Mensaje m;
	    String contenido = "";
	    while (!contenido.equals(Mensaje.APAGAR_SISTEMA) && !socket.isClosed() && activo) {
		m = (Mensaje) ois.readObject();
		contenido = m.toString();
		slot.enviar(m);
	    }
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
     * Termina de recibir los mensajes, y cierra la conexión de manera forzada.
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
	    if (ss != null) {
		if (!ss.isClosed()) {
		    ss.close();
		}
	    }
	    if (slot.abierto()) {
		slot.enviar(new Mensaje(Mensaje.APAGAR_SISTEMA));
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
