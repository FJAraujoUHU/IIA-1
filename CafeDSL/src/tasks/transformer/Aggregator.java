package tasks.transformer;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import messaging.Message;
import messaging.Slot;
import messaging.SlotException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import tasks.Task;
import xmlUtils.*;

/**
 * Tarea Aggregator.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class Aggregator extends Task {

    private final Splitter split;
    private final Map<SplitterHeader, List<Message>> buf;

    /**
     * Constructor de un Aggregator estándar
     *
     * @param input Slot de entrada
     * @param output Slot de salida
     * @param split Splitter que separó los mensajes originales.
     */
    public Aggregator(Slot input, Slot output, Splitter split) {
        super(input, output);
        this.split = split;
        this.buf = new HashMap<>();
    }

    @Override
    public void run() {
        Message m;
        do {                                                                    //ejecutarse hasta recibir la orden de apagado/se cierre un slot
            try {
                m = receive(0);                                                 //La tarea se queda esperando a que le llegue un mensaje

                if (!m.isShutdown()) {
                    Message forLambda = m;                                      //Expresiones lambda necesitan una referencia efectiva final, usar objeto como wrapper
                    SplitterHeader header = split.headers.stream()
                            .filter(
                                    h -> h.isNeeded(forLambda) != null
                            ).findFirst()
                            .orElse(null);

                    if (header != null) {                                       //Si ha encontrado que el mensaje pertenece a una cabecera
                        if (!buf.containsKey(header)) {
                            buf.put(header, new ArrayList<>(header.getNRefs()));//Crear lista para ir almacenando los mensajes
                        }
                        buf.get(header).add(m);                                 //Añadir elemento a la lista necesaria
                        if (buf.get(header).size() == header.getNRefs()) {      //Si en la lista están todos los elementos necesarios

                            //Crear documento con todos los mensajes a añadir y sus UUIDs
                            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                            Document messages = db.newDocument();
                            Element root = messages.createElement("root");
                            messages.appendChild(root);

                            for (Message msg : buf.get(header)) {
                                //Crea un documento por cada mensaje
                                Document doc = db.parse(new ByteArrayInputStream(msg.toString().getBytes()));
                                //Crea un tag para indicar el UUID
                                Element uuidTag = doc.createElement(Splitter.UUID_TAG);
                                //añade el UUID que pide la cabecera
                                uuidTag.appendChild(doc.createTextNode(header.isNeeded(msg).toString()));
                                //añade el tag al documento
                                Element docRoot = doc.getDocumentElement();
                                docRoot.appendChild(uuidTag);

                                //Añade el documento del mensaje al documento de mensajes
                                root.appendChild(messages.importNode(docRoot, true));
                            }

                            //Prepara la salida para recuperar la transformación
                            StringWriter output = new StringWriter();
                            Result res = new StreamResult(output);
                            header.getXSLTHeader().transform(new DOMSource(messages), res);
                            String resultWithTags = output.toString();
                            output = new StringWriter();
                            res = new StreamResult(output);
                            header.getMetadataRemover().transform(new DOMSource(XMLUtils.stringToDocument(resultWithTags)), res);

                            Message originalParent = m.getAncestors().stream()
                                    .filter(
                                        ancestor -> ancestor.getInternalId().equals(header.getOriginal())
                                    )
                                    .findFirst().get();

                            Message result = new Message(output.toString(), originalParent);
                            this.send(result, 0);
                            //Libera memoria
                            buf.remove(header).clear();
                            split.headers.remove(header);
                        }

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
