package messaging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
    private final UUID uuid;
    private final List<Message> ancestors;
    private Long publicID;

    /**
     * Orden/mensaje que el sistema entiende como orden de apagado.
     */
    public static Message SHUTDOWN = new Message(null) {
        @Override
        public boolean isShutdown() {
            return true;
        }
    };

    /**
     * Constructor estándar, para mensajes originales.
     *
     * @param message Contenido del mensaje.
     */
    public Message(String message) {
        this.message = message;
        this.uuid = UUID.randomUUID();
        this.ancestors = new ArrayList<>();
        this.publicID = null;
    }

    /**
     * Constructor para crear mensajes hijos, pensados para hacer derivados de
     * otro mensaje.
     *
     * @param message Contenido del mensaje.
     * @param parent Padre del mensaje, cuya información queda registrada en el
     * hijo.
     */
    public Message(String message, Message parent) {
        this.message = message;
        this.uuid = UUID.randomUUID();
        this.publicID = parent.publicID;
        this.ancestors = new ArrayList<>(parent.ancestors);
        this.ancestors.add(parent);
    }

    /**
     * Establece un Correlation ID para el mensaje.
     *
     * @param id Correlation ID a asignar.
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
     * Devuelve el UUID interno del mensaje, único e inmutable.
     *
     * @return UUID identificativo del objeto.
     */
    public UUID getInternalId() {
        return uuid;
    }

    /**
     * Devuelve una lista con todos los ancestros del mensaje, ordenadas de
     * original a más inmediato, siendo el primer índice el mensaje original, y
     * el último, el padre del mensaje.
     *
     * @return Lista de ancestros del mensaje.
     */
    public List<Message> getAncestors() {
        return ancestors;
    }

    /**
     * Devuelve el mensaje padre, si es que lo tiene.
     *
     * @return Mensaje padre en el que se basó su creación, NULL si es original.
     */
    public Message getParent() {
        if (ancestors.isEmpty()) {
            return null;
        } else {
            return ancestors.get(ancestors.size() - 1);
        }
    }

    /**
     * Devuelve si el mensaje es el mensaje reservado de apagado del sistema.
     *
     * @return Si el mensaje es un mensaje de cerrado.
     */
    public boolean isShutdown() {
        return false;
    }

    /**
     * Cambia el mensaje que lleva el objeto, sin cambiar ningún metadato.
     *
     * @param newMessage Nuevo mensaje.
     */
    public void set(String newMessage) {
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

    /**
     * Comprueba si el contenido de los mensajes es igual
     *
     * @param m Mensaje a comparar
     * @return True si son idénticos, False si no.
     */
    public boolean equalContent(Message m) {
        return this.message.equals(m.message);
    }
}
