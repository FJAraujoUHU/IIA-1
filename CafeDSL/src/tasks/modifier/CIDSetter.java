package tasks.modifier;

import messaging.*;
import tasks.Task;

/**
 * Tarea Content ID Setter.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class CIDSetter extends Task {

    private long id;

    /**
     * Constructor de un Content ID Setter estándar.Las IDs que va asignando son
     * secuenciales a partir de un número basado en el UUID de la tarea, ya que
     * se genera de manera aleatoria.
     *
     * @param input Slot de entrada.
     * @param output Slot de salida
     */
    public CIDSetter(Slot input, Slot output) {
        super(new Slot[]{input}, new Slot[]{output});
        this.id = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();   //XOR para obfuscar el número
    }

    /**
     * Constructor de un Content ID Setter estándar. Las IDs que va asignando
     * son secuenciales a partir del número especificado.
     *
     * @param input Slot de entrada.
     * @param output Slot de salida.
     * @param id ID inicial.
     */
    public CIDSetter(Slot input, Slot output, long id) {
        super(new Slot[]{input}, new Slot[]{output});
        this.id = id;
    }

    public long getID() {
        return id;
    }

    public void setID(long newID) {
        this.id = newID;
    }

    @Override
    public void run() {
        Message m;
        do {                                                                    //ejecutarse hasta recibir la orden de apagado/se cierre un slot
            try {
                m = receive(0);                                                 //La tarea se queda esperando a que le llegue un mensaje por el primer slot

                if (!m.equals(Message.SHUTDOWN)) {
                    m.setId(id++);                                              //Cambia el ID del mensaje y postautoincrementa
                    send(m, 0);                                                 //Envía el mensaje
                }

            } catch (Exception ex) {
                System.out.println(ex.toString());
                m = Message.SHUTDOWN;
            }
        } while (!m.equals(Message.SHUTDOWN) && this.flow());

        try {
            close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
}
