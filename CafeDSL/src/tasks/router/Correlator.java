package tasks.router;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import messaging.*;
import tasks.Task;

/**
 * Definición genérica de un Correlator
 * @author Francisco Javier Araujo Mendoza
 */
public abstract class Correlator extends Task {

    protected class SlotReader implements Runnable {

        private final Slot in;
        private final List<Message> out;
        private final Correlator parent;
        private boolean alive;

        public SlotReader(Slot slot, List<Message> buf, Correlator parent) {
            this.in = slot;
            this.out = buf;
            this.parent = parent;
            alive = true;
        }
        
        public void stop()  {
            alive = false;
        }

        @Override
        public void run() {
            Message m;
            while (in.availableRead() && alive) {
                try {
                    m = in.receive();                                           //La tarea se queda esperando a que le llegue un mensaje por el primer slot
                    if (m.equals(Message.SHUTDOWN)) {
                        alive = false;
                    }
                    synchronized (parent) {
                        out.add(m);
                        parent.notifyAll();
                    }
                } catch (SlotException ex) {
                    alive = false;
                }
            }
        }
    }

    protected final SlotReader[] workers;
    protected final Thread[] workerThreads;
    protected final List<List<Message>> channels;

    public Correlator(Slot[] inputs, Slot[] outputs) {
        super(inputs, outputs);
        this.workers = new SlotReader[inputs.length];
        this.workerThreads = new Thread[workers.length];
        this.channels = new ArrayList<>(workers.length);
        for (int i = 0; i < workers.length; i++) {
            channels.add(Collections.synchronizedList(new LinkedList<>()));
            workers[i] = new SlotReader(in[i], channels.get(i), this);
            workerThreads[i] = new Thread(workers[i]);
        }
    }

    protected void startAll() {
        for (Thread thr : workerThreads) {
            thr.start();
        }
    }

    protected boolean joinAll(long timeoutMilis) {
        try {
            for (SlotReader s : workers) {
                s.stop();
            }
            for (Thread thr : workerThreads) {
                thr.join(timeoutMilis);
            }
        } catch (InterruptedException ex) {
            return false;
        }
        return true;
    }

    protected int emptyList() {
        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }
    /*
     * Implementar método Run según el tiipo de correlator
     */
}
