package messaging;

import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

/**
 * Clase que representa a los mensajes internos de un problema de integración.
 * Lleva un mensaje en texto plano, una ID interna para identificarlo siempre,
 * y puede asignársele una ID pública para usarlo como Correlation ID.
 * Además, es Serializable para poder enviarlo a través de tuberías y puertos
 * para garantizar la máxima modularidad, y Clonable para poder replicarlos
 * fácilmente.
 * @author Francisco Javier Araujo Mendoza
 */
public class Message implements Serializable, Cloneable {
    private String message;
    private final long internalID;
    private Integer publicID;
    private final UUID uuid;
    
    //Generador de ID internas
    private static final Random generator = new Random(System.nanoTime());
    
    /**
     * Orden/mensaje que el sistema entiende como orden de apagado.
     */
    public static final String SHUTDOWN = "!!!SYSTEM SHUTDOWN!!!";

    /**
     * Constructor de la clase.
     * @param message Mensaje que contiene.
     */
    public Message(String message) {
	this.message = message;
	internalID = System.nanoTime() + generator.nextLong();
	publicID = null;
        uuid = UUID.randomUUID();
    }

    /**
     * Constructor de la clase, diseñado para mensajes derivados de otro para
     * que conserven ID interna.
     * @param message Mensaje que contiene.
     * @param parent Mensaje del que proviene.
     */
    public Message(String message, Message parent) {
	this.message = message;
	internalID = parent.internalID;
	publicID = null;
        uuid = UUID.randomUUID();
    }       
    
    /**
     * Establece un Correlation ID para el mensaje.
     * @param id
     */
    public void setId(int id) {
	publicID = id;
    }
    
    /**
     * Retira el Correlation ID del mensaje.
     */
    public void resetID()   {
	publicID = null;
    }
    
    /**
     * Devuelve el Correlation ID del mensaje.
     * @return Correlation ID del mensaje.
     * @throws Exception Si el mensaje no tenía Correlation ID.
     */
    public int getId() throws Exception {
	if (publicID == null)
	    throw new Exception("No se ha asignado ningún ID");
	else return publicID;
    }
    
    /**
     * Devuelve el ID interno del mensaje, único e inmutable (útil para trabajar
     * con tareas sin configurar y mensajes sin CID).
     * @return ID interno.
     */
    public long getInternalID()	{
	return internalID;
    }

    /**
     * Devuelve el UUID único del mensaje (es intransferible)
     * @return UUID del objeto.
     */
    public UUID getUUID() {
        return uuid;
    }
    
    /**
     * Cambia el mensaje que lleva el objeto.
     * @param newMessage
     */
    public void setMessage(String newMessage)	{
	message = newMessage;
    }
    
    /**
     * Para leer el mensaje interno
     * @return String del mensaje interno
     */
    @Override
    public String toString()	{
	return message;
    }
}
