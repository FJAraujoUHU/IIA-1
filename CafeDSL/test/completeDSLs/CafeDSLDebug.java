package completeDSLs;

import connector.*;
import java.io.StringReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import messaging.*;
import messaging.ports.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import tasks.transformer.*;
import tasks.modifier.*;
import tasks.router.*;

/**
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class CafeDSLDebug {

    //Valores dependientes de la configuración
    static final String DB_SERVER = "b0ve.com";
    static final int DB_PORT = 3306;
    static final String DB_USERNAME = "cafe-05";
    static final String DB_PASSWD = "pass";
    static final String DB_SCHEMA = "cafe05";
    static final String FOLDER_CAMARERO = "./camarero";
    static final String FOLDER_COMANDAS = "./comandas";
    static final int SOCKET_COMANDAS = 7777;
    static final int SOCKET_CAMARERO = 7778;
    static final int SOCKET_BF_ENTRY = 7780;
    static final int SOCKET_BF_EXIT = 7781;
    static final int SOCKET_BC_ENTRY = 7782;
    static final int SOCKET_BC_EXIT = 7783;

    //Ejecutor de las tareas
    static ThreadPoolExecutor executor;
    //Factoría de xpath
    static XPathFactory XPFact;
    //Definición de los slots
    static Slot[] slot;
    //Definición de los puertos
    EntryPort comandas;
    SolicitorPort barmanFrio, barmanCaliente;
    ExitPort camarero;
    //Definición de los conectores
    XMLEntryLoader comandasConn;
    SQLSolicitor barmanFrioConn, barmanCalienteConn;
    XMLExitWriter camareroConn;
    //Definición de las tareas
    static XPathExpression split1exp;
    Splitter split1;
    static XPathExpression dist2condFria, dist2condCaliente;
    Distributor dist2;
    Replicator rep3f, rep3c;
    String tr4fXSLT = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"><xsl:template match=\"/\"><sql>select servirFria('<xsl:value-of select=\"//name\"/>') as '<xsl:value-of select=\"//name\"/>'</sql></xsl:template></xsl:stylesheet>";
    String tr4cXSLT = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"><xsl:template match=\"/\"><sql>select servirCaliente('<xsl:value-of select=\"//name\"/>') as '<xsl:value-of select=\"//name\"/>'</sql></xsl:template></xsl:stylesheet>";
    Translator tr4f, tr4c;
    static XPathExpression ce6affix, ce6location;
    ContentEnricher ce6f, ce6c;
    static XPathExpression co5condCorr;
    Correlator co5f, co5c;
    Merger me11;
    Aggregator ag12;

    public CafeDSLDebug() {
    }

    @BeforeClass
    public static void setUpClass() throws XPathExpressionException {
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        slot = new Slot[20];
        XPFact = XPathFactory.newInstance();
        split1exp = XPFact.newXPath().compile("//drink");
        dist2condFria = XPFact.newXPath().compile("/drink/type = \"cold\"");
        dist2condCaliente = XPFact.newXPath().compile("/drink/type = \"hot\"");
        co5condCorr = XPFact.newXPath().compile("//item/@colName | //name/text()");
        ce6affix = XPFact.newXPath().compile("/sql");
        ce6location = XPFact.newXPath().compile("/drink");
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws SlotException {
        for (int i = 0; i < slot.length; i++) {
            slot[i] = new Slot();
        }
    }

    @After
    public void tearDown() {

    }

    /**
     * Test of run method, of class Task.
     */
    @Test
    public void testRun() throws Exception {
        System.out.println("run");

        //Inicio de los conectores
        comandasConn = new XMLEntryLoader("localhost", SOCKET_COMANDAS, FOLDER_COMANDAS);

        //Inicio de los puertos
        comandas = new EntryPort(SOCKET_COMANDAS, slot[0]);
        
        executor.execute(comandas);
        executor.execute(comandasConn);
        

        while (slot[0].availableWrite()) {
            if (slot[0].availableRead()) {
                Message m = slot[0].receive();
                if (m.isShutdown()) {
                    System.out.println("Recibida orden de cierre");
                } else {
                    System.out.println("Salida: " + m.toString());
                }   
            }         
        }
        executor.shutdown();
        /*if (!executor.awaitTermination(30, TimeUnit.SECONDS))
            executor.shutdownNow();*/
    }
}
