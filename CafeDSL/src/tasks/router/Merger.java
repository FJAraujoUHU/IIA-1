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
            while (in.available() && out.available()) {                         //Ejecutarse mientras la entrada siga abierta
                try {
                    m = in.receive();
                    if (m.equals(Message.SHUTDOWN)) {                           //Si es un mensaje de apagado, forzar el cierre por si acaso
                        in.close();
                    }
                    synchronized (out) {
                        out.send(m);                                            //Reenviar el mensaje por la salida
                    }
                } catch (Exception ex) {
                    if (!ex.getMessage().contains("Slot already closed"));
                        System.out.println(ex.toString());
                }
            }
            try {
                if (out.available()) {
                    out.close();
                }
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
        }
    }

    private final MergerWorker[] readers;
    private final Thread[] readerThreads;

    /**
     * Constructor de un Merger estándar.
     *
     * @param input Slots de entrada.
     * @throws Exception Si se produce un error al crear los Slots de salida.
     */
    public Merger(Slot[] input) throws Exception {
        super(input, 1);
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
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
}
