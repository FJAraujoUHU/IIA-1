package messaging.ports;

/**
 * Excepción que avisa de errores relacionados con la operación de puertos.
 * @author Francisco Javier Araujo Mendoza
 */
public class PortException extends Exception {
    public PortException(String message) {
        super(message);
    }
    public PortException(Throwable cause) {
        super(cause);
    }
    public PortException(String message, Throwable cause) {
        super(message, cause);
    }
}
