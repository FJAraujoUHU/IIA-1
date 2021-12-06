package messaging;

/**
 * Excepción que avisa de errores relacionados con la operación de Slots.
 * @author Francisco Javier Araujo Mendoza
 */
public class SlotException extends Exception {
    public SlotException(String message) {
        super(message);
    }
    public SlotException(Throwable cause) {
        super(cause);
    }
    public SlotException(String message, Throwable cause) {
        super(message, cause);
    }
}
