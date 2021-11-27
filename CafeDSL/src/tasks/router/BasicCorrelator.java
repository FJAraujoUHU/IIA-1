package tasks.router;

import java.util.Iterator;
import messaging.*;

/**
 * Correlator que funciona a base de CIDs
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class BasicCorrelator extends Correlator {

    public BasicCorrelator(Slot[] inputs, Slot[] outputs) {
        super(inputs, outputs);
    }

    @Override
    public void run() {
        this.startAll();
        try {
            do {
                synchronized (this) {
                    if (this.emptyList() != -1) {
                        wait();
                    }

                    //Iterador para poder eliminar elementos y recorrer el bucle a la vez.
                    Iterator<Message> it = channels.get(0).iterator();
                    while (it.hasNext()) {
                        Message m = it.next();
                        if (m.getId() != null) {
                            boolean hasCorrelation = channels.stream()
                                    .allMatch(c -> c.stream() //Mirar si todos los canales cumplen...
                                    .anyMatch(m2 -> m.getId().equals(m2.getId()))); //Que alguno de sus mensajes tenga la misma ID

                            if (hasCorrelation) {                                   //Si todos los canales tienen un mensaje, enviar
                                this.send(m, 0);
                                it.remove();
                                //Enviar por el resto de slots
                                for (int i = 1; i < channels.size(); i++) {
                                    Message m2 = channels.get(i).stream()
                                            .filter(m3 -> m.getId().equals(m3.getId()))
                                            .findFirst()
                                            .get();
                                    this.send(m2, i);
                                    channels.get(i).remove(m2);
                                }
                            }
                        }
                    }
                }
            } while (this.flow());
        } catch (SlotException ex) {
            //Salir
        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
        }
        
        close();
    }
}
