package tasks.transformer;

import messaging.*;
import tasks.Task;

/**
 * Tarea Splitter.
 *
 * Va buscando la etiqueta que se le especifica, y va mandando mensajes cada vez
 * que encuentra dicha etiqueta con el subárbol de dicha etiqueta.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class Splitter extends Task {

    private final String tag;

    /**
     * Constructor de un Splitter estándar.
     *
     * @param input Slot de entrada.
     * @param tag Etiqueta sobre la que hacer los splits.
     * @throws Exception Si se produce un error al crear los Slots de salida.
     */
    public Splitter(Slot input, String tag) throws Exception {
        super(new Slot[]{input}, 1);
        if (tag.startsWith("<") && tag.endsWith(">")) {
            //Si se ha pasado en formato XML, quitar las marcas
            this.tag = tag.substring(1, tag.length() - 1);
        } else {
            this.tag = tag;
        }
    }

    /**
     * Funcionamiento de la tarea.
     */
    @Override
    public void run() {
        Message m;
        String contenido;

        do {                                                                    //ejecutarse hasta recibir la orden de apagado/se cierre un slot
            try {
                m = receive(0);                                                 //La tarea se queda esperando a que le llegue un mensaje por el primer slot
                contenido = m.toString();                                       //Guarda el contenido en una string
                if (!m.equals(Message.SHUTDOWN)) {
                    //Se ejecuta el bucle mientras siga encontrando la etiqueta
                    while (contenido.contains("<" + tag + ">") && contenido.contains("</" + tag + ">")) {
                        String subarbol = contenido.substring(contenido.indexOf("<" + tag + ">"), contenido.indexOf("</" + tag + ">") + 8);
                        send(new Message(subarbol, m), 0);
                        //Va consumiendo el mensaje
                        contenido = contenido.substring(contenido.indexOf("</" + tag + ">") + 8);
                    }
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
