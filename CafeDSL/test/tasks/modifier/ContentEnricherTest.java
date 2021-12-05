package tasks.modifier;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import messaging.Message;
import messaging.Slot;
import messaging.SlotException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class ContentEnricherTest {
    
    ContentEnricher instance;
    Thread instanceThr;
    Slot inMain, inEnrichment, out;
    static XPathExpression affix, location;
    static final String MAINEXAMPLE = "<drink><name>te</name><type>hot</type></drink>";
    static final String ENRICHMENTEXAMPLE = "<sql><available>yes</available><tag>example</tag></sql>";
    static final String EXPECTEDOUTPUT = "<drink><name>te</name><type>hot</type><available>yes</available><tag>example</tag></drink>";
    static final String AFFIXSTR = "//sql/*";
    static final String LOCATIONSTR = "//drink";
    
    
    public ContentEnricherTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws XPathExpressionException {
        XPathFactory xfact = XPathFactory.newInstance();
        affix = xfact.newXPath().compile(AFFIXSTR);
        location = xfact.newXPath().compile(LOCATIONSTR);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws SlotException, Exception {
        inMain = new Slot();
        inEnrichment = new Slot();
        out = new Slot();
        
        instance = new ContentEnricher(inMain, inEnrichment, out, affix, location);
        instanceThr = new Thread(instance);
        instanceThr.start();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of run method, of class Transformer.
     */
    @Test
    public void testRun() throws SlotException, InterruptedException {
        System.out.println("run");

        
        Message testMain = new Message(MAINEXAMPLE);
        Message testEnrichment = new Message(ENRICHMENTEXAMPLE);
        
        inMain.send(testMain);
        inEnrichment.send(testEnrichment);
        assertEquals("Output is different from expectation", EXPECTEDOUTPUT, out.receive().toString());
        instance.close();
        instanceThr.join(10000);
        assertFalse("The task has failed to close automatically", instance.flow());
    }
    
}
