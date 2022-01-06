package tasks.transformer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import messaging.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import tasks.Task;
import xmlUtils.*;

/**
 * Tarea Splitter.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class Splitter extends Task {

    private final XPathExpression xpath;
    final List<SplitterHeader> headers;
    
    private static final String XSLT_START
            = "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
            + "<xsl:strip-space elements=\"*\"/>"
            + "<xsl:template match=\"/\">";
    private static final String XSLT_END
            = "</xsl:template>"
            + "</xsl:stylesheet>";
    public static final String UUID_TAG = "DSL_SPLITTER_UUID";
    

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
        headers = new ArrayList<>();

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
                    Document mensajeDoc = XMLUtils.stringToDocument(m.toString());
                    //Ejecuta la expresión
                    NodeList itemList = (NodeList) xpath.evaluate(mensajeDoc, XPathConstants.NODESET);
                    //Convierte el resultado en una lista de Strings
                    List<String> list = XMLUtils.nodeListToStringList(itemList);

                    //Convierte cada String en mensaje y genera la cabecera como XSLT
                    String header = m.toString();
                    List<UUID> uuids = new ArrayList<>(list.size());
                    Deque<Message> output = new ArrayDeque<>(list.size());
                    for (String s : list) {
                        Message item = new Message(s, m);
                        header = header.replaceFirst(Pattern.quote(s),          //reemplaza en la cabecera el elemento por una referencia
                                "<xsl:copy-of select=\"//*["+ UUID_TAG +"='" + item.getInternalId().toString() + "']\"/>");
                        uuids.add(item.getInternalId());                        //Apunta la UUID del nuevo mensaje y lo guarda en una lista
                        output.add(item);
                    }
                    header = XSLT_START + header + XSLT_END;
                    headers.add(new SplitterHeader(uuids, header, m.getInternalId()));
                    for(Message item : output) {
                        send(item,0);
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
