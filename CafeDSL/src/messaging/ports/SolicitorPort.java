package messaging.ports;

import java.util.UUID;
import messaging.Slot;

/**
 * Puerto de solicitud. Internamente utiliza uno de entrada y uno de salida conectados.
 * Nota: ambos puertos utilizan sockets distintos, el pasado por argumento y el inmediatamente siguiente.
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
     * @param host Dirección a la que conectarse.
     * @param socket Primer puerto para establecer la conexión (0-65535).
     * @param request Slot por el que enviar las solicitudes.
     * @throws Exception Si se produce algún error al crear los puertos internos.
     */
    public SolicitorPort(String host, int socket, Slot request) throws Exception	{
	this.host = host;
	this.outSocket = socket;
	this.inSocket = socket+1;
	this.in = request;
	this.reqPort = new ExitPort(this.host, outSocket, in);
	this.respPort = new EntryPort(inSocket);
	this.outThread = new Thread(reqPort);
	this.inThread = new Thread(respPort);
	this.out = respPort.getExitSlot();
        this.uuid = UUID.randomUUID();
    }
    
    /**
     * Devuelve el slot por el que salen las respuestas.
     * @return Slot por el que llegan las respuestas.
     */
    public Slot getExitSlot()	{
	return out;
    }
    
    /**
     * Cierra el puerto (y los puertos internos) y espera hasta que se cierren.
     * @throws Exception si hay algún problema cerrando.
     */
    public void close() throws Exception    {
	if (reqPort.available())
	    reqPort.cerrar();
	if (respPort.available())
	    respPort.close();
	outThread.join(10000);
	inThread.join(10000);
    }
    
    /**
     * Devuelve el estado de la conexión
     * @return Si todo va bien.
     */
    public boolean available()	{
	return reqPort.available() && respPort.available();
    }
    
    /**
     * Arranca los puertos internos.
     */
    public void start()	{
	outThread.start();
	inThread.start();
    }
    
    /**
     * Devuelve el UUID único del slot (es intransferible)
     * @return UUID del objeto.
     */
    public UUID getUUID() {
        return uuid;
    }
}
