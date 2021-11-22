package messaging;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Clase que representa a los mensajes internos de un problema de integración.
 * Lleva un mensaje en texto plano, una ID interna para identificarlo siempre, y
 * puede asignársele una ID pública para usarlo como Correlation ID. Además, es
 * Serializable para poder enviarlo a través de tuberías y puertos para
 * garantizar la máxima modularidad, y Clonable para poder replicarlos
 * fácilmente.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class Message implements Serializable {

    private String message;
    private final long internalID;
    private Long publicID;
    private final UUID uuid;

    /**
     * Orden/mensaje que el sistema entiende como orden de apagado.
     */
    public static final String SHUTDOWN_STR = "!!!SYSTEM SHUTDOWN!!!";
    public static final Message SHUTDOWN = new Message(SHUTDOWN_STR);

    /**
     * Constructor de la clase.
     *
     * @param message Mensaje que contiene.
     */
    public Message(String message) {
        this.message = message;
        publicID = null;
        uuid = UUID.randomUUID();
        internalID = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();   //XOR para obfuscar el número
    }

    /**
     * Constructor de la clase, diseñado para mensajes derivados de otro para
     * que conserven ID interna.
     *
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
     * Constructor de la clase, diseñado para devolver un clon de otro mensaje.
     * El clon no es perfecto porque tiene un UUID distinto.
     *
     * @param parent Mensaje a clonar.
     */
    public Message(Message parent) {
        this.message = parent.message;
        this.internalID = parent.internalID;
        this.publicID = parent.publicID;
        this.uuid = UUID.randomUUID();
    }

    /**
     * Establece un Correlation ID para el mensaje.
     *
     * @param id
     */
    public void setId(long id) {
        publicID = id;
    }

    /**
     * Retira el Correlation ID del mensaje.
     */
    public void resetID() {
        publicID = null;
    }

    /**
     * Devuelve el Correlation ID del mensaje. (Si no tiene, devuelve NULL)
     *
     * @return Correlation ID del mensaje.
     */
    public Long getId() {
        return publicID;
    }

    /**
     * Devuelve el ID interno del mensaje, único e inmutable (útil para trabajar
     * con tareas sin configurar y mensajes sin CID).
     *
     * @return ID interno.
     */
    public long getInternalID() {
        return internalID;
    }

    /**
     * Devuelve el UUID único del mensaje (es intransferible)
     *
     * @return UUID del objeto.
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Cambia el mensaje que lleva el objeto.
     *
     * @param newMessage
     */
    public void setMessage(String newMessage) {
        message = newMessage;
    }

    /**
     * Para leer el mensaje interno
     *
     * @return String del mensaje interno
     */
    @Override
    public String toString() {
        return message;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.message);
        hash = 83 * hash + Objects.hashCode(this.uuid);
        return hash;
    }

    /**
     * Compara dos mensajes a través de metadatos. Si se pretende comparar el
     * contenido del mensaje, usar otros métodos.
     *
     * @param obj Mensaje con el que comparar
     * @return Si hacen referencia al mismo mensaje, o si tienen el mismo UUID.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Message other = (Message) obj;
        if (!Objects.equals(this.message, other.message)) {
            return false;
        }
        if (this.toString().equals(SHUTDOWN_STR) && other.toString().equals(SHUTDOWN_STR)) {
            return true;
        }
        return Objects.equals(this.uuid, other.uuid);
    }

}
