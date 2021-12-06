package tasks.modifier;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import messaging.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tasks.Task;
import static xmlUtils.XMLUtils.*;

/**
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class ContentEnricher extends Task {

    private final XPathExpression affix, location;
    
    /**
     * 
     * @param main
     * @param enrichment
     * @param output
     * @param affix Contenido a añadir
     * @param location Dónde añadirlo
     */
    public ContentEnricher(Slot main, Slot enrichment, Slot output, XPathExpression affix, XPathExpression location) {
        super(new Slot[]{main, enrichment}, new Slot[]{output});
        this.affix = affix;
        this.location = location;
    }

    @Override
    public void run() {
        Message main, enrichment, output;
        do {                                                                    //ejecutarse hasta recibir la orden de apagado/se cierre un slot
            try {
                main = receive(0);
                enrichment = receive(1);

                if (!main.isShutdown() && !enrichment.isShutdown()) {
                    try {
                        Document mainDoc = stringToDocument(main.toString());
                        Document enrichmentDoc = stringToDocument(enrichment.toString());
                        Node locationNode = (Node) location.evaluate(mainDoc, XPathConstants.NODE);
                        NodeList affixList = (NodeList) affix.evaluate(enrichmentDoc, XPathConstants.NODESET);
                        
                        for (int i = 0; i < affixList.getLength(); i++) {
                            Node n = affixList.item(i);
                            mainDoc.adoptNode(n);
                            locationNode.appendChild(n);
                        }
                        
                        output = new Message(nodeToString(mainDoc), main);
                        send(output, 0);
                        
                        
                    } catch (XPathExpressionException ex) {
                        Logger.getLogger(ContentEnricher.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        Logger.getLogger(ContentEnricher.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            } catch (SlotException ex) {
                //Si se lanza la excepción, sale del bucle sólo
            }
        } while (this.flow());

        this.close();
    }
    
}
