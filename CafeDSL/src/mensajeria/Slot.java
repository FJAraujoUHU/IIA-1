package mensajeria;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class Slot {

    private PipedInputStream destinoTub;
    private PipedOutputStream origenTub;
    private ObjectInputStream destino;
    private ObjectOutputStream origen;
    private volatile boolean abierto;

    public Slot() throws Exception {
	try {
	    destinoTub = new PipedInputStream();
	    origenTub = new PipedOutputStream(destinoTub);
	    destino = new ObjectInputStream(destinoTub);
	    origen = new ObjectOutputStream(origenTub);
	    abierto = true;
	} catch (IOException ex) {
	    abierto = false;
	    Exception e = new Exception("Error al crear el slot/stream");
	    e.addSuppressed(e);
	    throw e;
	}
    }

    public Mensaje enviar() throws Exception {
	if (!abierto) throw new Exception("El slot está cerrado");
	try {
	    return (Mensaje) destino.readObject();
	} catch (Exception ex) {
	    abierto = false;
	    Exception e = new Exception("Error leyendo desde el slot");
	    e.addSuppressed(ex);
	    throw e;
	}
    }

    public void recibir(Mensaje m) throws Exception {
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

}
