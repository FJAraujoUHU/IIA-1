package messaging.ports;

import messaging.*;

/**
 * Puerto de respuesta. Internamente utiliza uno de entrada y uno de salida
 * conectados.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class ResponderPort extends CommPort {

    /**
     * Constructor del puerto.
     *
     * @param host Dirección a la que conectarse.
     * @param exitSocket Puerto por el que enviar la respuesta al conector.
     * @param entrySocket Puerto por el que recibir la solicitud del conector.
     * @param in Slot por el que enviar las respuestas.
     * @param out Slot por el que recibir las solicitudes.
     */
    public ResponderPort(String host, int exitSocket, int entrySocket, Slot in, Slot out) throws SlotException {
        super(host, exitSocket, entrySocket, in, out);       
    }

    @Override
    public void run() {
        this.start();
        
        Message request, response, output;
        do {
            try {
                request = entrySlot.receive();

                if (!request.isShutdown()) {
                    out.send(request);
                    response = in.receive();
                    output = new Message(response.toString(), request);
                    exitSlot.send(output);
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
