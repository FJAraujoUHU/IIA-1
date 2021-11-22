package tasks;

import java.util.UUID;
import messaging.Message;
import messaging.Slot;
import messaging.SlotException;

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
     * @throws IndexOutOfBoundsException Si se ha introducido un índice inválido.
     * @throws messaging.SlotException Si el Slot indicado estaba cerrado.
     */
    protected void send(Message m, int slot) throws IndexOutOfBoundsException, SlotException {
	try {
	    out[slot].send(m);
	} catch (IndexOutOfBoundsException ex) {
            //Para dar más info sobre el error
            IndexOutOfBoundsException e = new IndexOutOfBoundsException("Index out of bounds :" + slot + "out of " + out.length);
	    throw e;
	}
    }

    /**
     * Espera a recibir un mensaje por el puerto especificado.
     *
     * @param slot Puerto a escuchar.
     * @return Un mensaje.
     * @throws IndexOutOfBoundsException Si se ha introducido un índice inválido.
     * @throws messaging.SlotException Si el Slot indicado estaba cerrado.
     */
    protected Message receive(int slot) throws IndexOutOfBoundsException, SlotException {
	try {
	    return in[slot].receive();
	} catch (IndexOutOfBoundsException ex) {
            //Para dar más info sobre el error
	    IndexOutOfBoundsException e = new IndexOutOfBoundsException("Index out of bounds :" + slot + "out of " + out.length);
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
     */
    public void close() {
        for (Slot input : in) {
            try {
                if (input.available()) {
                    input.close();
                }
	    } catch (SlotException ex) {
                //Si el slot realmente estaba cerrado, ignorar
	    }
        }
	for (Slot output : out) {
	    try {
                if (output.available()) {
                    output.send(Message.SHUTDOWN);
                }
	    } catch (SlotException ex) {
                //Si el slot realmente estaba cerrado, ignorar
	    }
	}
    }
    
    /**
     * Comprueba que todas las entradas y salidas están abiertas.
     * @return True si todas están abiertas, False si alguna está cerrada.
     */
    public boolean flow()   {
        for (Slot input : in)
            if (!input.available()) return false;
        for (Slot output : out)
            if (!output.available()) return false;
        return true;
    }

    /**
     * Implementar la lógica de la tarea en la subclase.
     */
    @Override
    public abstract void run();
}
