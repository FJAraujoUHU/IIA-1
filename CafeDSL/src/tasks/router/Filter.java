package tasks.router;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import messaging.Message;
import messaging.Slot;
import org.w3c.dom.Document;
import tasks.Task;

/**
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class Filter extends Task {
    
    private final XPathExpression xpath;
    
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
                    Document mensajeDoc = xmlUtils.XMLUtils.stringToDocument(m.toString());
                    if ((Boolean) xpath.evaluate(mensajeDoc, XPathConstants.BOOLEAN)) {
                        send(m, 0);
                    }                    
                }
            } catch (Exception ex) {
                System.out.println(ex.toString());
                m = Message.SHUTDOWN;
            }
        } while (!m.equals(Message.SHUTDOWN) && this.flow());
        
        close();
    }
}
