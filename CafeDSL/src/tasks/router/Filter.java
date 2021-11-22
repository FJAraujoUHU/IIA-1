package tasks.router;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import messaging.Message;
import messaging.Slot;
import org.w3c.dom.Document;
import tasks.Task;
import xmlUtils.XMLUtils;

/**
 * Tarea Filter
 * @author Francisco Javier Araujo Mendoza
 */
public class Filter extends Task {

    private final XPathExpression xpath;

    /**
     * Constructor estándar
     * @param input Slot de entrada
     * @param output Slot de salida
     * @param xpath Expresión que han de cumplir los mensajes para pasar
     */
    public Filter(Slot input, Slot output, XPathExpression xpath) {
        super(new Slot[]{input}, new Slot[]{output});
        this.xpath = xpath;
    }

    @Override
    public void run() {
        Message m;
        do {
            try {
                m = receive(0);
                if (!m.equals(Message.SHUTDOWN)) {
                    Document mensajeDoc = XMLUtils.stringToDocument(m.toString());
                    if ((Boolean) xpath.evaluate(mensajeDoc, XPathConstants.BOOLEAN)) {
                        send(m, 0);
                    }
                }
            } catch (Exception ex) {
                m = Message.SHUTDOWN;
            }
        } while (!m.equals(Message.SHUTDOWN) && this.flow());

        close();
    }
}
