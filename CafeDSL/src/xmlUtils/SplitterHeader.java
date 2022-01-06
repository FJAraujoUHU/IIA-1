package xmlUtils;

import java.io.StringReader;
import java.util.List;
import java.util.UUID;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import messaging.Message;

/**
 * Clase que almacena la cabecera producida por un Splitter y sus metadatos,
 * para que luego la use un Aggregator.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class SplitterHeader {

    private final UUID original;
    private final List<UUID> references;
    private final String XSLTHeader;

    public SplitterHeader(List<UUID> references, String XSLTHeader, UUID originalMsg) {
        this.references = references;
        this.XSLTHeader = XSLTHeader;
        this.original = originalMsg;
    }

    /**
     * Comprueba si el mensaje es un candidato a ser reintroducido
     *
     * @param m Mensaje a comprobar
     * @return La UUID bajo la que podría ser insertado, o NULL si no es un
     * candidato.
     */
    public UUID isNeeded(Message m) {
        if (references.contains(m.getInternalId())) {
            return m.getInternalId();
        } else {
            List<Message> ancestors = m.getAncestors();
            for (int i = ancestors.size() - 1; i >= 0; i--) {
                if (references.contains(ancestors.get(i).getInternalId())) {
                    return ancestors.get(i).getInternalId();
                }
            }
            return null;
        }
    }

    /**
     * Devuelve el UUID del mensaje del que se generó la cabecera.
     *
     * @return UUID del mensaje original.
     */
    public UUID getOriginal() {
        return original;
    }

    /**
     * Devuelve el número de referencias necesarias
     *
     * @return
     */
    public int getNRefs() {
        return references.size();
    }

    /**
     * Devuelve la cabecera XSLT como transformadora, lista para usar.
     *
     * @return Un Transformer con el XSLT integrado para poder reintegrar el
     * mensaje.
     * @throws TransformerConfigurationException Si hay un error en el XSLT.
     */
    public javax.xml.transform.Transformer getXSLTHeader() throws TransformerConfigurationException {
        return TransformerFactory
                .newInstance()
                .newTransformer(
                        new StreamSource(
                                new StringReader(XSLTHeader)
                        )
                );
    }

    /**
     * Devuelve un transformador que se encarga de quitar las etiquetas de
     * metadatos de la cabecera.
     *
     * @return Transformador capaz de eliminar los metadatos
     */
    public javax.xml.transform.Transformer getMetadataRemover() {
        try {
            return TransformerFactory
                    .newInstance()
                    .newTransformer(
                            new StreamSource(
                                    new StringReader(
                                            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                                            + "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">\n"
                                            + "<xsl:strip-space elements=\"*\"/>\n"
                                            + "<xsl:template match=\"@*|node()\">\n"
                                            + "    <xsl:copy>\n"
                                            + "        <xsl:apply-templates select=\"@*|node()\"/>\n"
                                            + "    </xsl:copy>\n"
                                            + "</xsl:template>\n"
                                            + "<xsl:template match=\"DSL_SPLITTER_UUID\"/>\n"
                                            + "</xsl:stylesheet>")
                            )
                    );
        } catch (TransformerConfigurationException ex) {
            //Compila una constante, no puede fallar
            return null;
        }
    }

    /**
     * Devuelve la cabecera como texto plano.
     *
     * @return Cabecera del mensaje con referencias a los mensajes.
     */
    @Override
    public String toString() {
        return XSLTHeader;
    }

}
