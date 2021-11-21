package tasks.router;

import javax.xml.xpath.XPathExpression;
import messaging.Slot;

/**
 * !!!FALTA TODA LA LOGICA!!!
 * Correlator que usa XPath
 * @author Francisco Javier Araujo Mendoza
 */
public class XPathCorrelator extends Correlator {

    private final XPathExpression xpath;
    
    public XPathCorrelator(Slot[] inputs, Slot[] outputs, XPathExpression xpath) {
        super(inputs, outputs);
        this.xpath = xpath;
    }

    @Override
    public void run() {
        this.startAll();
        try {
            do {
                synchronized (this) {
                    if (this.emptyList() != -1) {
                        wait();
                    }

                    /* Lógica */
                    
                }
            } while (flow());              
        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
        }
        close();
    }
    
}
