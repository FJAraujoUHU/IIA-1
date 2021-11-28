package tasks.transformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import messaging.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import tasks.Task;
import xmlUtils.XMLUtils;

/**
 * FALTA GUARDAR LA CABECERA, USAR XSLT
 * 
 * Tarea Splitter.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class Splitter extends Task {

    private final XPathExpression xpath;
    
    private final Map<UUID, String> headerStorage;

    /**
     * Constructor de un Splitter estándar.
     *
     * @param input Slot de entrada.
     * @param output Slot de salida.
     * @param xpath Expresión para hacer la división
     */
    public Splitter(Slot input, Slot output, XPathExpression xpath) {
        super(new Slot[]{input}, new Slot[]{output});
        this.xpath = xpath;
        headerStorage = new HashMap<>();
        
    }

    /**
     * Funcionamiento de la tarea.
     */
    @Override
    public void run() {
        Message m;
        do {                                                                    //ejecutarse hasta recibir la orden de apagado/se cierre un slot
            try {
                m = receive(0);                                                 //La tarea se queda esperando a que le llegue un mensaje

                if (!m.isShutdown()) {
                    headerStorage.put(m.getInternalId(), m.toString());         //Almacenar mensaje original/cabecera
                    Document mensajeDoc = XMLUtils.stringToDocument(m.toString());
                    //Ejecuta la expresión
                    NodeList itemList = (NodeList) xpath.evaluate(mensajeDoc, XPathConstants.NODESET);
                    //Convierte el resultado en una lista de Strings
                    List<String> list = XMLUtils.nodeListToStringList(itemList);
                    //Convierte cada String en mensaje y lo envía
                    for (String s : list) {
                        send(new Message(s,m) ,0);
                    }
                }
            } catch (SlotException ex) {
                //Si se lanza la excepción, sale del bucle sólo
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
        } while (this.flow());

        this.close();
    }
}
