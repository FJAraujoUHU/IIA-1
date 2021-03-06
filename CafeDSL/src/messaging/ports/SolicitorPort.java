package messaging.ports;

import messaging.*;

/**
 * Puerto de solicitud. Internamente utiliza uno de entrada y uno de salida
 * conectados.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class SolicitorPort extends CommPort {

    /**
     * Constructor del puerto.
     *
     * @param host Dirección a la que conectarse.
     * @param exitSocket Puerto por el que enviar las solicitudes.
     * @param entrySocket Puerto por el que esperar la respuesta.
     * @param in Slot por el que enviar las solicitudes.
     * @param out Slot por el que recibir las respuestas.
     */
    public SolicitorPort(String host, int exitSocket, int entrySocket, Slot in, Slot out) throws SlotException {
        super(host, exitSocket, entrySocket, in, out);       
    }

    @Override
    public void run() {
        this.start();
        
        Message request, response, output;
        do {
            try {
                request = in.receive();

                if (!request.isShutdown()) {
                    exitSlot.send(request);
                    response = entrySlot.receive();
                    output = new Message(response.toString(), request);
                    out.send(output);
                }

            } catch (SlotException ex) {
                //Si se lanza la excepción, sale del bucle sólo
            }
        } while (this.flow());

        try {
            this.close();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
