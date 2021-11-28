package tasks.router;

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
public class DistributorTest {
    
    Slot in, out1, out2;
    Distributor instance;
    Thread instanceThr;
    static XPathExpression xpathHot, xpathCold;
    static final String HOTFILTER = "/drink/type = \"hot\"";
    static final String COLDFILTER = "/drink/type = \"cold\"";
    static final String HOTEXAMPLE = "<drink><name>te</name><type>hot</type></drink>";
    static final String COLDEXAMPLE = "<drink><name>cerveza</name><type>cold</type></drink>";
    
    public DistributorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws XPathExpressionException {
        xpathHot = XPathFactory.newInstance().newXPath().compile(HOTFILTER);
        xpathCold = XPathFactory.newInstance().newXPath().compile(COLDFILTER);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws SlotException {
        in = new Slot();
        out1 = new Slot();
        out2 = new Slot();
        instance = new Distributor(in, new Slot[]{out1, out2}, new XPathExpression[]{xpathHot, xpathCold});
        instanceThr = new Thread(instance);
        instanceThr.start();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of run method, of class Distributor.
     */
    @Test
    public void testRun() throws SlotException, InterruptedException {
        System.out.println("run");
        
        Message hot = new Message(HOTEXAMPLE);
        Message cold = new Message(COLDEXAMPLE);
        
        in.send(hot);
        in.send(cold);
        in.send(Message.SHUTDOWN);
        Message m = out1.receive();
        assertTrue("Filter has sent cold to hot", m.equalContent(hot));
        m = out2.receive();
        assertTrue("Filter has sent cold to hot", m.equalContent(cold));
        
        instanceThr.join(10000);
        assertFalse("The task has failed to close automatically", instance.flow());
    }
    
}
