package messaging.ports;

import java.io.IOException;
import java.util.UUID;
import messaging.Slot;

/**
 * Puerto de solicitud. Internamente utiliza uno de entrada y uno de salida
 * conectados. Nota: ambos puertos utilizan sockets distintos, el pasado por
 * argumento y el inmediatamente siguiente.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class SolicitorPort {

    private final EntryPort respPort;
    private final ExitPort reqPort;
    private final Slot in, out;
    private final Thread inThread, outThread;
    private final String host;
    private final int inSocket, outSocket;
    private final UUID uuid;

    /**
     * Constructor del puerto.
     *
     * @param host Dirección a la que conectarse.
     * @param socket Primer puerto para establecer la conexión (0-65535).
     * @param request Slot por el que enviar las solicitudes.
     * @param response Slot por el que enviar las respuestas.
     */
    public SolicitorPort(String host, int socket, Slot request, Slot response) {
        this.host = host;
        this.outSocket = socket;
        this.inSocket = socket + 1;
        this.in = request;
        this.out = response;
        this.reqPort = new ExitPort(this.host, outSocket, in);
        this.respPort = new EntryPort(inSocket, out);
        this.outThread = new Thread(reqPort);
        this.inThread = new Thread(respPort);
        this.uuid = UUID.randomUUID();
    }

    /**
     * Cierra el puerto (y los puertos internos) y espera hasta que se cierren.
     *
     * @throws java.io.IOException Si hay algún problema interno al cerrar
     * @throws messaging.ports.PortException Si el puerto ya estaba cerrado
     */
    public void close() throws IOException, PortException  {
        if (!reqPort.available() && !respPort.available())
            throw new PortException("Port already closed (UUID = " + uuid + ")");
        
        if (reqPort.available()) {
            try {
                reqPort.close();
            } catch (PortException ex) {
                /*Nunca debería lanzarse porque se comprueba primero si está abierto*/
            }
        }
        if (respPort.available()) {
            try {
                respPort.close();
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
        return reqPort.available() && respPort.available();
    }

    /**
     * Arranca los puertos internos.
     */
    public void start() {
        outThread.start();
        inThread.start();
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
