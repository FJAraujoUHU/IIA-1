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
import tasks.transformer.*;
import tasks.modifier.*;
import tasks.router.*;

/**
 * DSL Completo para café (Como Unidad de JUnit, como ejemplo de uso de la librería)
 * 
 * @author Francisco Javier Araujo Mendoza
 * 
 */
public class CafeDSL {

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

    public CafeDSL() {
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
        barmanFrioConn = new SQLSolicitor("localhost", SOCKET_BF_EXIT, SOCKET_BF_ENTRY, DB_SERVER, DB_PORT, DB_USERNAME, DB_PASSWD, DB_SCHEMA, true);
        barmanCalienteConn = new SQLSolicitor("localhost", SOCKET_BC_EXIT, SOCKET_BC_ENTRY, DB_SERVER, DB_PORT, DB_USERNAME, DB_PASSWD, DB_SCHEMA, true);
        camareroConn = new XMLExitWriter(SOCKET_CAMARERO, FOLDER_CAMARERO);

        //Inicio de los puertos
        comandas = new EntryPort(SOCKET_COMANDAS, slot[0]);
        barmanFrio = new SolicitorPort("localhost", SOCKET_BF_EXIT, SOCKET_BF_ENTRY, slot[8], slot[10]);
        barmanCaliente = new SolicitorPort("localhost", SOCKET_BC_EXIT, SOCKET_BC_ENTRY, slot[9], slot[11]);
        camarero = new ExitPort("localhost", SOCKET_CAMARERO, slot[19]);

        //Inicio de las tareas
        split1 = new Splitter(slot[0], slot[1], split1exp);
        dist2 = new Distributor(slot[1], new Slot[]{slot[2], slot[3]}, new XPathExpression[]{dist2condFria, dist2condCaliente});

        //rama fría
        rep3f = new Replicator(slot[2], new Slot[]{slot[4], slot[6]});
        tr4f = new Translator(slot[4], slot[8], new StreamSource(new StringReader(tr4fXSLT)));
        co5f = new XPathCorrelator(new Slot[]{slot[6], slot[10]}, new Slot[]{slot[12], slot[14]}, co5condCorr);
        ce6f = new ContentEnricher(slot[12], slot[14], slot[16], ce6affix, ce6location);

        //rama caliente
        rep3c = new Replicator(slot[3], new Slot[]{slot[5], slot[7]});
        tr4c = new Translator(slot[5], slot[9], new StreamSource(new StringReader(tr4cXSLT)));
        co5c = new XPathCorrelator(new Slot[]{slot[7], slot[11]}, new Slot[]{slot[13], slot[15]}, co5condCorr);
        ce6c = new ContentEnricher(slot[13], slot[15], slot[17], ce6affix, ce6location);

        //Fin
        me11 = new Merger(new Slot[]{slot[16], slot[17]}, slot[18]);
        ag12 = new Aggregator(slot[18], slot[19], split1);

        
        
        
        
        
        //Arranque del sistema
        executor.execute(comandasConn);
        executor.execute(barmanFrioConn);
        executor.execute(barmanCalienteConn);
        executor.execute(camareroConn);
        
        executor.execute(comandas);
        executor.execute(barmanFrio);
        executor.execute(barmanCaliente);
        executor.execute(camarero);
        
        executor.execute(split1);
        executor.execute(dist2);
        executor.execute(rep3f);
        executor.execute(rep3c);
        executor.execute(tr4f);
        executor.execute(tr4c);
        executor.execute(co5f);
        executor.execute(co5c);
        executor.execute(ce6f);
        executor.execute(ce6c);
        executor.execute(me11);
        executor.execute(ag12);
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
    }
}
