package mensajeria;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Clase que representa a los slots que comunican a las distintas tareas del
 * sistema. Usa internamente PipedStreams (tuberías) para comunicar diferentes
 * hilos (Tareas) y ObjectStreams para simplificar la transmisión de objetos
 * (mensajes). Además, lleva el control del estado de la conexión.
 * 
 * Nota: es muy importante no usar ambos extremos del slot en un mismo hilo, o
 * se producen errores inesperados (limitación de PipedStreams).
 * 
 * @author Francisco Javier Araujo Mendoza
 */
public class Slot {

    private PipedInputStream destinoTub;
    private PipedOutputStream origenTub;
    private ObjectInputStream destino;
    private ObjectOutputStream origen;
    private volatile boolean abierto;

    /**
     * Constructor de la clase. Inicia los flujos y la tubería para dejarlos
     * listos para usar.
     * 
     * @throws Exception Si se produce algún error al establecer la tubería. 
     */
    public Slot() throws Exception {
	try {
	    //Es importante el orden de creación de los streams.
	    destinoTub = new PipedInputStream();
	    origenTub = new PipedOutputStream(destinoTub);
	    origen = new ObjectOutputStream(origenTub);
	    destino = new ObjectInputStream(destinoTub);
	    abierto = true;
	} catch (IOException ex) {
	    abierto = false;
	    Exception e = new Exception("Error al crear el slot/stream");
	    e.addSuppressed(e);
	    throw e;
	}
    }

    /**
     * Lee el primer mensaje que le llega, y si no hay ninguno, espera hasta que
     * llegue.
     * 
     * @return Un mensaje.
     * @throws Exception Si el Slot no está operativo, o se produce algún error
     * de conexión en la espera.
     */
    public Mensaje recibir() throws Exception {
	if (!abierto) throw new Exception("El slot está cerrado");
	try {
	    Mensaje m = (Mensaje) destino.readObject();
	    return m;
	} catch (Exception ex) {
	    abierto = false;
	    Exception e = new Exception("Error leyendo desde el slot");
	    e.addSuppressed(ex);
	    throw e;
	}
    }

    /**
     * Envía un mensaje a través del Slot.
     * 
     * @param m Mensaje a enviar.
     * @throws Exception Si el Slot no está operativo, o se produce un error al
     * enviar.
     */
    public void enviar(Mensaje m) throws Exception {
	if (!abierto) throw new Exception("El slot está cerrado");
	try {
	    origen.writeObject(m);
	} catch (Exception ex) {
	    abierto = false;
	    Exception e = new Exception("Error enviando al slot (mensaje " + m.getIdInterna() + ")");
	    e.addSuppressed(ex);
	    throw e;
	}
    }

    /**
     * Termina de enviar los mensajes, y cierra la conexión.
     * 
     * @throws Exception Si se intenta cerrar un Slot previamente cerrado, o se
     * produce un error inesperado.
     */
    public void cerrar() throws Exception {
	if (!abierto) throw new Exception("El slot ya estaba cerrado");
	try {
	    origen.flush();
	    origenTub.flush();
	    abierto = false;
	    origen.close();
	    origenTub.close();
	} catch (Exception ex) {
	    abierto = false;
	    if (!ex.getMessage().contains("Pipe closed")) {	//Si el error no es porque ya estuviese cerrado
		Exception e = new Exception("Error cerrando el slot.");
		e.addSuppressed(ex);
		throw e;
	    }
	}
    }
    
    /**
     * Devuelve el estado de la conexión.
     * 
     * @return Si el slot es funcional.
     */
    public boolean abierto()	{
	return abierto;
    }
}
