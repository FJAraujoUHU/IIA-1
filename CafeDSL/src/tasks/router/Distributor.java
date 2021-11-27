package tasks.router;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import messaging.*;
import org.w3c.dom.Document;
import tasks.Task;
import xmlUtils.XMLUtils;

/**
 * Tarea Distributor.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class Distributor extends Task {

    private XPathExpression[] conditions;

    /**
     * Constructor de un Distributor estándar.
     *
     * @param input Slot de entrada.
     * @param outputs Salidas de la tarea.
     * @param conditions Expresiones XPath que debe cumplir para ser enviado por
     * el slot del mismo índice.
     */
    public Distributor(Slot input, Slot[] outputs, XPathExpression[] conditions) {
        super(new Slot[]{input}, outputs);
        this.conditions = conditions;
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
                    try {
                        Document messageAsDoc = XMLUtils.stringToDocument(m.toString());
                        for (int i = 0; i < out.length; i++) {
                            if ((Boolean) conditions[i].evaluate(messageAsDoc, XPathConstants.BOOLEAN)) {
                                out[i].send(new Message(m));
                            }
                        }
                    } catch (Exception ex) {
                        //Discard message
                    }
                }
            } catch (SlotException ex) {
                //Si se lanza la excepción, sale del bucle sólo
            }
        } while (this.flow());

        close();
    }
}
