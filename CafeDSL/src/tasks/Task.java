package tasks;

import java.util.UUID;
import messaging.Message;
import messaging.Slot;

/**
 * Tarea genérica sobre la que se basan el resto de tareas. También proporciona
 * cierta funcionalidad genérica para facilitar la implementación.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public abstract class Task implements Runnable {

    /**
     * Array que contiene los slots de entrada de la tarea.
     */
    protected Slot[] in;

    /**
     * Array que contiene los slots de salida de la tarea.
     */
    protected Slot[] out;
    
    private final UUID uuid;

    /**
     * Constructor para la creación de tareas.
     *
     * @param inputs array de Slots de entrada a la tarea.
     * @param nOutputs número de salidas de la tarea.
     * @throws Exception Si se produce un error al crear los Slots de salida.
     */
    public Task(Slot[] inputs, int nOutputs) throws Exception {
	this.in = inputs;
	this.out = new Slot[nOutputs];
        this.uuid = UUID.randomUUID();
	try {
	    for (int i = 0; i < nOutputs; i++) {
		out[i] = new Slot();
	    }
	} catch (Exception ex) {
	    Exception e = new Exception("Error al crear la tarea, hay problemas con los slots");
	    e.addSuppressed(ex);
	    throw e;
	}
    }

    /**
     * Devuelve un Slot para conectar la salida de la tarea con la
     * siguiente.
     *
     * @param slot índice del puerto de salida a obtener.
     * @return Slot de salida listo para ser usado.
     * @throws Exception Si se ha introducido un índice inválido.
     */
    public Slot getExitSlot(int slot) throws Exception {
	try {
	    return out[slot];
	} catch (IndexOutOfBoundsException ex) {
	    Exception e = new Exception("Index out of bounds :" + slot + "out of " + out.length);
	    e.addSuppressed(ex);
	    throw e;
	}
	
    }

    /**
     * Envía un mensaje por el puerto especificado.
     *
     * @param m Mensaje a enviar.
     * @param slot Puerto destino.
     * @throws Exception Si hay problemas con el slot o se ha introducido un
     * índice inválido.
     */
    protected void send(Message m, int slot) throws Exception {
	try {
	    out[slot].send(m);
	} catch (IndexOutOfBoundsException ex) {
	    Exception e = new Exception("Index out of bounds :" + slot + "out of " + out.length);
	    e.addSuppressed(ex);
	    throw e;
	}
    }

    /**
     * Espera a recibir un mensaje por el puerto especificado.
     *
     * @param slot Puerto a escuchar.
     * @return Un mensaje.
     * @throws Exception Si hay problemas con el slot o se ha introducido un
     * índice inválido.
     */
    protected Message receive(int slot) throws Exception {
	try {
	    Message ret = in[slot].receive();
	    return ret;
	} catch (IndexOutOfBoundsException ex) {
	    Exception e = new Exception("Index out of bounds :" + slot + "out of " + in.length);
	    e.addSuppressed(ex);
	    throw e;
	}
    }

    /**
     * Devuelve el número de puertos de entrada de la tarea.
     *
     * @return Número de puertos
     */
    public int getInputCount() {
	return in.length;
    }

    /**
     * Devuelve el número de puertos de salida de la tarea.
     *
     * @return Número de puertos.
     */
    public int getOutputCount() {
	return out.length;
    }

    /**
     * Cierra la tarea e intenta propagar un mensaje de apagado a sus vecinos.
     *
     * @throws Exception Si se produce algún error que no sea intentar cerrar un Slot previamente cerrado.
     */
    public void close() throws Exception {
	for (Slot salida : out) {
	    try {
		salida.send(new Message(Message.SHUTDOWN));
		salida.close();
	    } catch (Exception ex) {
		if (!ex.getMessage().toUpperCase().contains("CLOSED"))
		    throw ex;
	    }
	}
    }

    /**
     * Implementar la lógica de la tarea en la subclase.
     */
    @Override
    public abstract void run();
}
