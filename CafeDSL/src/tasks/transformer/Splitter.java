package tasks.transformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
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

    final Map<UUID, String> headerStorage;
    private static final String XSLT_START
            = "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
            + "<xsl:strip-space elements=\"*\"/>"
            + "<xsl:template match=\"/\">";
    private static final String XSLT_END
            = "</xsl:template>"
            + "</xsl:stylesheet>";

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
                    Document mensajeDoc = XMLUtils.stringToDocument(m.toString());
                    //Ejecuta la expresión
                    NodeList itemList = (NodeList) xpath.evaluate(mensajeDoc, XPathConstants.NODESET);
                    //Convierte el resultado en una lista de Strings
                    List<String> list = XMLUtils.nodeListToStringList(itemList);

                    //Convierte cada String en mensaje y genera la cabecera como XSLT
                    String header = m.toString();
                    for (String s : list) {
                        Message item = new Message(s, m);
                        header = header.replaceFirst(Pattern.quote(s), //reemplaza en la cabecera el elemento por una referencia
                                "<xsl:copy-of select=\"//*[DSL_SPLITTER_UUID='" + item.getInternalId().toString() + "']\"/>");
                        send(item, 0);
                    }
                    header = XSLT_START + header + XSLT_END;
                    headerStorage.put(m.getInternalId(), header);
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
