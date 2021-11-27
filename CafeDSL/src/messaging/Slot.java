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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
    private AtomicBoolean open;
    private AtomicInteger queue;
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
            queue = new AtomicInteger(0);
            open = new AtomicBoolean(true);
        } catch (IOException ex) {
            open = new AtomicBoolean(false);
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
        if (!open.get() && queue.get() < 1) {
            throw new SlotException("Slot is closed. (UUID = " + uuid + ")");
        }
        try {
            Message m = (Message) dest.readObject();
            queue.decrementAndGet();
            if (!open.get() && queue.get() < 1) {
                this.finallyClose();
            }
            return m;
        } catch (IOException | ClassNotFoundException ex) {
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
        if (!open.get() && queue.get() < 1) {
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
            queue.decrementAndGet();
            if (!open.get() && queue.get() < 1) {
                this.finallyClose();
            }
            return m;
        } catch (InterruptedException | ExecutionException ex) {
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
        if (!open.get()) {
            throw new SlotException("Slot is closed. (UUID = " + uuid + ")");
        }
        try {
            if (m.equals(Message.SHUTDOWN))
                this.close();
            else {
                orig.writeObject(m);
                queue.incrementAndGet();
            }
        } catch (IOException ex) {
            this.close();
            throw new SlotException("Error writing to slot. (UUID = " + uuid + ")", ex);
        }
    }

    /**
     * Cierra el Slot, de forma que no permite que le lleguen nuevos mensajes,
     * pero permite usar la salida hasta que no queden más mensajes.
     *
     * @throws SlotException Si se intenta cerrar un Slot previamente cerrado, o
     * se produce un error inesperado.
     */
    public void close() throws SlotException {
        if (!open.get()) {
            throw new SlotException("Slot is closed. (UUID = " + uuid + ")");
        }
        try {
            orig.writeObject(Message.SHUTDOWN);
            orig.flush();
            origPipe.flush();
            open.set(false);
        } catch (IOException ex) {
            open.set(false);
            throw new SlotException("Error closing slot. (UUID = " + uuid + ")", ex);
        }
    }
    
    /**
     * Cierra definitivamente el Slot.
     */
    public void finallyClose() {
        try {
            open.set(false);
            orig.close();
            origPipe.close();
        } catch (IOException ex) {
            open.set(false);
        }
    }

    /**
     * Devuelve el estado de la conexión.
     *
     * @return Si el slot es funcional.
     */
    public boolean availableRead() {
        if (open.get())
            return true;
        else return (queue.get() > 0);
    }
    
    public boolean availableWrite() {
        return open.get();
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
