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
    protected final Slot[] in;

    /**
     * Array que contiene los slots de salida de la tarea.
     */
    protected final Slot[] out;
    
    protected final UUID uuid;

    /**
     * Constructor para la creación de tareas.
     *
     * @param inputs array de Slots de entrada a la tarea.
     * @param outputs array de Slots de salida a la tarea.
     */
    public Task(Slot[] inputs, Slot[] outputs) {
	this.in = inputs;
	this.out = outputs;
        this.uuid = UUID.randomUUID();
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
     * Devuelve el UUID único de la tarea (es intransferible)
     * @return UUID del objeto.
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Cierra la tarea e intenta propagar un mensaje de apagado a sus vecinos.
     *
     * @throws Exception Si se produce algún error que no sea intentar cerrar un Slot previamente cerrado.
     */
    public void close() throws Exception {
	for (Slot output : out) {
	    try {
                if (output.available()) {
                    output.close();
                }
	    } catch (Exception ex) {
		if (!ex.getMessage().toUpperCase().contains("CLOSED"))
		    throw ex;
	    }
	}
    }
    
    /**
     * Comprueba que todas las entradas y salidas están abiertas.
     * @return True si todas están abiertas, False si alguna está cerrada.
     */
    public boolean flow()   {
        boolean ret = true;
        for (Slot input : in)
            ret = ret && input.available();
        for (Slot output : out)
            ret = ret && output.available();
        return ret;
    }

    /**
     * Implementar la lógica de la tarea en la subclase.
     */
    @Override
    public abstract void run();
}
