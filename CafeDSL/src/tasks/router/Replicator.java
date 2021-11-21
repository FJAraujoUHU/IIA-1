package tasks.router;

import messaging.*;
import tasks.Task;

/**
 * Tarea Replicator.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class Replicator extends Task {

    /**
     * Constructor de un Replicator estándar.
     *
     * @param input Slot de entrada.
     * @param outputs Salidas de la tarea.
     */
    public Replicator(Slot input, Slot[] outputs) {
        super(new Slot[]{input}, outputs);
    }

    /**
     * Funcionamiento de la tarea.
     */
    @Override
    public void run() {
        Message m;
        do {                                                                    //ejecutarse hasta recibir la orden de apagado/se cierre un slot
            try {
                m = receive(0);                                                 //La tarea se queda esperando a que le llegue un mensaje por el primer slot
                if (!m.equals(Message.SHUTDOWN)) {
                    for (Slot output : out) {                                   //Reenvía el mensaje
                        output.send(new Message(m));                            //Envía un nuevo clon del mensaje original.
                    }
                }
            } catch (SlotException ex) {
                System.out.println(ex.toString());
                m = Message.SHUTDOWN;
            }
        } while (!m.equals(Message.SHUTDOWN) && this.flow());

        close();
    }
}
