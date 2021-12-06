package xmlUtils;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Herramientas para trabajar entre elementos DOM (para uso con XPath) y
 * Strings.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class XMLUtils {

    public static String nodeToString(Node n) throws TransformerException {
        Transformer xtrans = TransformerFactory.newInstance().newTransformer();
        xtrans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        xtrans.transform(new DOMSource(n), result);
        return writer.toString();
    }

    public static String nodeListToString(NodeList nl) throws TransformerException {
        Transformer xtrans = TransformerFactory.newInstance().newTransformer();
        xtrans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            xtrans.transform(new DOMSource(n), result);
        }
        return writer.toString();
    }

    public static List<String> nodeListToStringList(NodeList nl) throws TransformerException {
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
    }

    public static Document stringToDocument(String str) throws Exception {
        return DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(str.getBytes()));
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
            if (!n1.isEqualNode(n2))
                return false;    
        }
        return true;
    }
}
