package xmlUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Herramientas para trabajar entre elementos DOM (para uso con XPath) y
 * Strings.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class XMLUtils {

    public static String nodeToString(Node n) {
        try {
            Transformer xtrans = TransformerFactory.newInstance().newTransformer();
            xtrans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            xtrans.transform(new DOMSource(n), result);
            return writer.toString();
        } catch (TransformerException ex) {
            //Si algo falla inesperadamente, devolver null
            return null;
        }
    }

    public static String nodeListToString(NodeList nl) {
        try {
            Transformer xtrans = TransformerFactory.newInstance().newTransformer();
            xtrans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                xtrans.transform(new DOMSource(n), result);
            }
            return writer.toString();
        } catch (TransformerException ex) {
            //Si algo falla inesperadamente, devolver null
            return null;
        }
    }

    public static List<String> nodeListToStringList(NodeList nl) {
        try {
            Transformer xtrans = TransformerFactory.newInstance().newTransformer();
            xtrans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            List<String> ret = new ArrayList<>();

            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                StringWriter writer = new StringWriter();
                StreamResult result = new StreamResult(writer);
                xtrans.transform(new DOMSource(n), result);
                ret.add(writer.toString());
            }
            return ret;
        } catch (TransformerException ex) {
            //Si algo falla inesperadamente, devolver null
            return null;
        }
    }

    public static Document stringToDocument(String str) throws IOException, SAXException {
        try {
            return DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(str.getBytes()));
        } catch (ParserConfigurationException ex) {
            //No debería pasar nunca, porque no hay ninguna configuración a satisfacer.
            return null;
        }
    }

    public static boolean compareNodeList(NodeList list1, NodeList list2) {
        if (list1 == list2) {
            return true;
        }
        if (list1 == null || list2 == null) {
            return false;
        }
        if (list1.getLength() != list2.getLength()) {
            return false;
        }
        for (int i = 0; i < list1.getLength(); i++) {
            Node n1 = list1.item(i);
            Node n2 = list2.item(i);
            
            //Comprobar si son del mismo tipo
            if (n1.getNodeType() == n2.getNodeType()) {
                if (!n1.isEqualNode(n2))    //Si son iguales, compararlos como iguales.
                    return false;
            } else {
                String n1Value = n1.getNodeValue();
                String n2Value = n2.getNodeValue();
                
                //Si sus valores no son null, compararlos
                if (n1Value != null && n2Value != null) {
                    if (!n1Value.equals(n2Value))
                        return false;
                } else if (n1Value != n2Value) {
                    //Si sólo uno de ellos es null y el otro no, son distintos
                    return false;
                }
            }
        }
        return true;
    }
}
