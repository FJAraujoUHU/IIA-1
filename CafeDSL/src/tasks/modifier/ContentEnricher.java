package tasks.modifier;

import messaging.*;
import tasks.Task;

/**
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class ContentEnricher extends Task {

    
    
    
    public ContentEnricher(Slot main, Slot enrichment, Slot output) {
        super(new Slot[]{main, enrichment}, new Slot[]{output});
        
        //probablemente añadir una expresión xd
    }

    @Override
    public void run() {
        Message main, enrichment, output;
        do {                                                                    //ejecutarse hasta recibir la orden de apagado/se cierre un slot
            try {
                main = receive(0);
                enrichment = receive(1);

                if (!main.equals(Message.SHUTDOWN) && !enrichment.equals(Message.SHUTDOWN)) {
                    
                    //logica de la tarea
                    
                    
                    
                    
                    send(output, 0);
                }

            } catch (SlotException ex) {
                //Si se lanza la excepción, sale del bucle sólo
            }
        } while (this.flow());

        this.close();
    }
    
}
