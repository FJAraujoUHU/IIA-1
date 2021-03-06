package tasks.transformer;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import messaging.Message;
import messaging.Slot;
import messaging.SlotException;
import tasks.Task;

/**
 * Tarea Translator.
 * @author Francisco Javier Araujo Mendoza
 */
public class Translator extends Task {

    private final javax.xml.transform.Transformer xslt;

    /**
     * Constructor de la clase.
     * @param input Slot de entrada.
     * @param output Slot de salida.
     * @param xslt Objeto que contenga el XSLT con el que hacer la transformación.
     * @throws TransformerConfigurationException Si el XSLT está mal formado.
     */
    public Translator(Slot input, Slot output, Source xslt) throws TransformerConfigurationException {
        super(input, output);
        this.xslt = TransformerFactory.newInstance().newTransformer(xslt);
    }

    @Override
    public void run() {
        Message m;
        do {                                                                    //ejecutarse hasta recibir la orden de apagado/se cierre un slot
            try {
                m = receive(0);

                if (!m.isShutdown()) {
                    //Convierte el mensaje en una entrada para el transformador
                    Source xmlIn = new StreamSource(new StringReader(m.toString()));
                    //Prepara la salida para recuperar la transformación
                    StringWriter output = new StringWriter();
                    Result res = new StreamResult(output);
                    //Transforma
                    xslt.transform(xmlIn, res);
                    //Crea un nuevo mensaje hijo con la transformación y lo envía
                    m.set(output.toString());
                    this.send(m, 0);
                }

            } catch (SlotException ex) {
                //Si se lanza la excepción, sale del bucle sólo
            } catch (TransformerException ex) {
                System.out.println(ex.toString());
            }
        } while (this.flow());
        this.close();
    }

}
