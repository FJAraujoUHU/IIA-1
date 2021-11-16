package tasks;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import messaging.*;

/**
 *
 * @author Francisco Javier Araujo Mendoza
 */
@Deprecated
public class SlotReader implements Runnable {

    private final Slot in;
    private final List<Message> out;
    
    public SlotReader(Slot input, List<Message> output) {
        this.in = input;
        this.out = output;
    }
    
    public SlotReader(Slot input) {
        this.in = input;
        this.out = Collections.synchronizedList(new LinkedList<>());
    }
    
    public List<Message> getList() {
        return out;
    }
    
    
    
    @Override
    public void run() {
        Message m;
        while(in.available()) {
            try {
                m = in.receive();                                               //La tarea se queda esperando a que le llegue un mensaje por el primer slot
                synchronized (out) {
                    out.add(m);
                    out.notifyAll();
                }
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
        } 
    }
    
}
