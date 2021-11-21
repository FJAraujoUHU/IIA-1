package tasks.router;

import messaging.*;
import tasks.*;

/**
 * Tarea Merger.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class Merger extends Task {

    private class MergerWorker implements Runnable {

        private final Slot in, out;

        public MergerWorker(Slot in, Slot out) {
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {
            Message m;
            do {                                                                //ejecutarse hasta recibir la orden de apagado/se cierre un slot
                try {
                    m = in.receive();
                    if (!m.equals(Message.SHUTDOWN)) {
                        synchronized (out) {out.send(m);}
                    }
                } catch (SlotException ex) {                              //Si uno de los slots está cerrado
                    m = Message.SHUTDOWN;
                }
            } while (!m.equals(Message.SHUTDOWN) && in.available() && out.available());

            if (in.available()) {                                               //Cierra la entrada si sigue abierta
                try {
                    in.close();
                } catch (SlotException ex) {
                    //No hacer nada, no debería llegar.
                }
            }
        }
    }

    private final MergerWorker[] readers;
    private final Thread[] readerThreads;

    /**
     * Constructor de un Merger estándar.
     *
     * @param input Slots de entrada.
     * @param output Slot de salida.
     */
    public Merger(Slot[] input, Slot output) {
        super(input, new Slot[]{output});
        readers = new MergerWorker[input.length];
        readerThreads = new Thread[readers.length];
        for (int i = 0; i < input.length; i++) {
            readers[i] = new MergerWorker(input[i], out[0]);
            readerThreads[i] = new Thread(readers[i]);
        }
    }

    /**
     * Funcionamiento de la tarea.
     */
    @Override
    public void run() {

        for (Thread t : readerThreads) {
            t.start();
        }
        try {
            for (Thread t : readerThreads) {
                t.join();
            }
            this.close();
        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
        }
    }
}
