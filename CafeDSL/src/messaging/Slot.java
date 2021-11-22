package messaging;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

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

    private PipedInputStream destPipe;
    private PipedOutputStream origPipe;
    private ObjectInputStream dest;
    private ObjectOutputStream orig;
    private volatile boolean open;
    private final UUID uuid;

    /**
     * Constructor de la clase. Inicia los flujos y la tubería para dejarlos
     * listos para usar.
     *
     * @throws SlotException Si se produce algún error al establecer la tubería.
     */
    public Slot() throws SlotException {
        uuid = UUID.randomUUID();
        try {
            //Es importante el orden de creación de los streams.

            destPipe = new PipedInputStream();
            origPipe = new PipedOutputStream(destPipe);
            orig = new ObjectOutputStream(origPipe);
            dest = new ObjectInputStream(destPipe);
            open = true;
        } catch (IOException ex) {
            open = false;
            throw new SlotException("Error creating slot/stream (UUID = " + uuid + ")", ex);
        }
    }

    /**
     * Lee el primer mensaje que le llega, y si no hay ninguno, espera hasta que
     * llegue.
     *
     * Si el mensaje es un mensaje de cierre, cierra el slot automáticamente.
     *
     * @return Un mensaje.
     * @throws SlotException Si el Slot no está operativo, o se produce algún
     * error de conexión en la espera.
     */
    public Message receive() throws SlotException {
        if (!open) {
            throw new SlotException("Slot is closed. (UUID = " + uuid + ")");
        }
        try {
            Message m = (Message) dest.readObject();
            if (m.equals(Message.SHUTDOWN)) {
                this.close();
            }
            return m;
        } catch (IOException | ClassNotFoundException | SlotException ex) {
            this.close();
            throw new SlotException("Error reading slot (UUID = " + uuid + ")", ex);
        }
    }

    /**
     * Lee el primer mensaje que le llega, y si no hay ninguno, intenta esperar
     * hasta que llegue o se acabe el tiempo de espera especificado.Si el mensaje es un mensaje de cierre, cierra el slot automáticamente.
     *
     *
     * @param timeout Tiempo límite de espera (en mseg)
     * @return Un mensaje.
     * @throws SlotException Si el Slot no está operativo, o se produce algún
     * error de conexión en la espera.
     * @throws java.util.concurrent.TimeoutException Si no ha dado tiempo a leer el mensaje.
     */
    public Message receive(long timeout) throws SlotException, TimeoutException {
        if (!open) {
            throw new SlotException("Slot is closed. (UUID = " + uuid + ")");
        }
        try {
            Supplier<Message> supplier = () -> {
                try {
                    return (Message) dest.readObject();
                } catch (IOException | ClassNotFoundException ex) {
                    return null;
                }
            };
            Future<Message> future = CompletableFuture.supplyAsync(supplier);
            Message m = future.get(timeout, TimeUnit.MILLISECONDS);
            if (m.equals(Message.SHUTDOWN)) {
                this.close();
            }
            return m;
        } catch (SlotException | InterruptedException | ExecutionException ex) {
            this.close();
            throw new SlotException("Error reading slot (UUID = " + uuid + ")", ex);
        }

    }

    /**
     * Envía un mensaje a través del Slot.
     *
     * @param m Mensaje a enviar.
     * @throws SlotException Si el Slot no está operativo, o se produce un error
     * al enviar.
     */
    public void send(Message m) throws SlotException {
        if (!open) {
            throw new SlotException("Slot is closed. (UUID = " + uuid + ")");
        }
        try {
            orig.writeObject(m);
        } catch (IOException ex) {
            this.close();
            throw new SlotException("Error writing to slot. (UUID = " + uuid + ")", ex);
        }
    }

    /**
     * Intenta terminar de enviar los mensajes, y cierra la conexión. Sólo usar
     * para forzar el cierre de un Slot, puesto que los Slots se cierran sólos
     * al sacar un mensaje de cierre; o si se pretende cerrar el slot desde el
     * lado del receptor.
     *
     * @throws SlotException Si se intenta cerrar un Slot previamente cerrado, o
     * se produce un error inesperado.
     */
    public void close() throws SlotException {
        if (!open) {
            throw new SlotException("Slot is closed. (UUID = " + uuid + ")");
        }
        try {
            orig.writeObject(Message.SHUTDOWN);
            orig.flush();
            origPipe.flush();
            open = false;
            orig.close();
            origPipe.close();
        } catch (IOException ex) {
            open = false;
            throw new SlotException("Error closing slot. (UUID = " + uuid + ")", ex);

            /*if (!ex.getMessage().contains("Pipe closed")) {	//Si el error no es porque ya estuviese cerrado
                Exception e = new Exception("Error closing slot. (UUID = " + uuid + ")");
                e.addSuppressed(ex);
                throw e;
            }*/
        }
    }

    /**
     * Devuelve el estado de la conexión.
     *
     * @return Si el slot es funcional.
     */
    public boolean available() {
        return open;
    }

    /**
     * Devuelve el UUID único del slot (es intransferible)
     *
     * @return UUID del objeto.
     */
    public UUID getUUID() {
        return uuid;
    }
}
