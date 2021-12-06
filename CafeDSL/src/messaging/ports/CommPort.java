package messaging.ports;

import java.io.IOException;
import java.util.UUID;
import messaging.Slot;
import messaging.SlotException;

/**
 * Puerto de comunicación genérico, con entrada y salida. Incluye un slot de
 * entrada y uno de salida, y forma la base que usan los puertos de respuesta y
 * de solicitud.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public abstract class CommPort implements Runnable {

    protected final EntryPort entryPort;
    protected final ExitPort exitPort;
    protected final Slot in, out, entrySlot, exitSlot;
    protected final Thread entryThread, exitThread;
    protected final String host;
    protected final int entrySocket, exitSocket;
    protected final UUID uuid;

    /**
     * Constructor del puerto.
     *
     * @param host Dirección a la que conectarse.
     * @param exitSocket Puerto que usará el DSL para enviar mensajes.
     * @param entrySocket Puerto que usará el DSL para recibir mensajes.
     * @param in Slot por el que el DSL envía mensajes.
     * @param out Slot por el que el DSL recibe mensajes
     */
    public CommPort(String host, int exitSocket, int entrySocket, Slot in, Slot out) throws SlotException {
        this.host = host;
        this.exitSocket = exitSocket;
        this.entrySocket = entrySocket;
        this.in = in;
        this.out = out;
        this.entrySlot = new Slot();
        this.exitSlot = new Slot();
        this.exitPort = new ExitPort(this.host, exitSocket, exitSlot);
        this.entryPort = new EntryPort(entrySocket, entrySlot);
        this.exitThread = new Thread(exitPort);
        this.entryThread = new Thread(entryPort);
        this.uuid = UUID.randomUUID();
    }

    /**
     * Cierra el puerto (y los puertos internos) y espera hasta que se cierren.
     *
     * @throws java.io.IOException Si hay algún problema interno al cerrar
     * @throws messaging.ports.PortException Si el puerto ya estaba cerrado
     */
    public void close() throws IOException, PortException {
        if (!exitPort.available() && !entryPort.available()) {
            throw new PortException("Port already closed (UUID = " + uuid + ")");
        }

        if (exitPort.available()) {
            try {
                exitPort.close();
            } catch (PortException ex) {
                /*Nunca debería lanzarse porque se comprueba primero si está abierto*/
            }
        }
        if (entryPort.available()) {
            try {
                entryPort.close();
            } catch (PortException ex) {
                /*Nunca debería lanzarse porque se comprueba primero si está abierto*/
            }
        }
    }

    /**
     * Devuelve el estado de la conexión
     *
     * @return Si todo va bien.
     */
    public boolean available() {
        return exitPort.available() && entryPort.available();
    }

    public boolean flow() {
        if (!in.availableRead()) {
            return false;
        }
        if (!entrySlot.availableRead()) {
            return false;
        }
        if (!out.availableWrite()) {
            return false;
        }
        if (!exitSlot.availableWrite()) {
            return false;
        }
        return true;
    }

    /**
     * Arranca los puertos internos.
     */
    protected void start() {
        exitThread.start();
        entryThread.start();
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
