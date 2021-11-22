package tasks.router;

import java.util.Iterator;
import java.util.function.Predicate;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import messaging.Message;
import messaging.Slot;
import org.w3c.dom.NodeList;
import xmlUtils.XMLUtils;

/**
 * Correlator que usa XPath
 *
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

                    //Iterador para poder eliminar elementos y recorrer el bucle a la vez.
                    Iterator<Message> it = channels.get(0).iterator();
                    while (it.hasNext()) {
                        Message m = it.next();
                        if (!m.equals(Message.SHUTDOWN)) {
                            try {
                                //Extrae los datos que se van a usar como correlación
                                NodeList correlator = (NodeList) xpath.evaluate(XMLUtils.stringToDocument(m.toString()), XPathConstants.NODESET);

                                //Mirar si en todos los canales hay algún mensaje que coincida con los parámetros de correlación
                                boolean hasCorrelation = channels.stream() //Para trabajar con todos los canales a la vez
                                        .allMatch(c -> c.stream() //Mirar si en cada canal se cumple...
                                        .anyMatch(new Predicate<Message>() {        //...que algún mensaje...
                                            @Override
                                            public boolean test(Message m2) {
                                                try {
                                                    //Extraer los datos a comparar
                                                    if (m2.equals(Message.SHUTDOWN)) throw new Exception();
                                                    NodeList list = (NodeList) xpath.evaluate(XMLUtils.stringToDocument(m2.toString()), XPathConstants.NODESET);
                                                    return XMLUtils.compareNodeList(correlator, list); //coincidan los "correlandos"
                                                } catch (Exception ex) {
                                                    //si ha fallado, deducir que no se cumple
                                                    return false;
                                                }
                                            }
                                        }));

                                if (hasCorrelation) {                               //Si todos los canales tienen un mensaje, enviar
                                    this.send(m, 0);
                                    it.remove();
                                    //Enviar por el resto de slots
                                    for (int i = 1; i < channels.size(); i++) {
                                        //Buscar el mensaje que coincidía y enviarlo.
                                        Message m2 = channels.get(i).stream()
                                                .filter(new Predicate<Message>() {
                                                    @Override
                                                    public boolean test(Message m2) {
                                                        try {
                                                            if (m2.equals(Message.SHUTDOWN)) throw new Exception();
                                                            NodeList list = (NodeList) xpath.evaluate(XMLUtils.stringToDocument(m2.toString()), XPathConstants.NODESET);
                                                            return XMLUtils.compareNodeList(correlator, list);
                                                        } catch (Exception ex) {
                                                            return false;
                                                        }
                                                    }
                                                })
                                                .findFirst()
                                                .get();
                                        this.send(m2, i);
                                        channels.get(i).remove(m2);
                                    }
                                }

                            } catch (Exception ex) {
                                //Dejar que la salida del bucle se encargue
                            }
                        }
                    }
                }
            } while (flow());
        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
        }
        joinAll(2500);
        close();
    }

}
