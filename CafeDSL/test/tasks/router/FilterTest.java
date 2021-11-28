package tasks.router;

import java.util.concurrent.TimeoutException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import messaging.*;
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
public class FilterTest {
    
    Filter instance;
    Thread instanceThr;
    Slot in, out;
    static XPathExpression xpath;
    static final String HOTFILTER = "/drink/type = \"hot\"";
    static final String HOTEXAMPLE = "<drink><name>te</name><type>hot</type></drink>";
    static final String COLDEXAMPLE = "<drink><name>cerveza</name><type>cold</type></drink>";
    
    public FilterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws XPathExpressionException {
        xpath = XPathFactory.newInstance().newXPath().compile(HOTFILTER);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws SlotException {
        in = new Slot();
        out = new Slot();
        instance = new Filter(in, out, xpath);
        instanceThr = new Thread(instance);
        instanceThr.start();
    }
    
    @After
    public void tearDown() {
    }
    
    /**
     * Test si se cierra
     */
    @Test
    public void testRunShutdown() throws SlotException, InterruptedException, TimeoutException {
        System.out.println("shutdown");

        in.send(Message.SHUTDOWN);
        instanceThr.join(10000);
        assertFalse("The task has failed to close automatically", instance.flow());
    }

    /**
     * Test of run method, of class Filter.
     */
    @Test
    public void testRun() throws SlotException, InterruptedException {
        System.out.println("run");

        Message hot = new Message(HOTEXAMPLE);
        Message cold = new Message(COLDEXAMPLE);
        
        in.send(hot);
        in.send(cold);
        in.send(Message.SHUTDOWN);
        
        Message m;
        Boolean didHotArrive = false;
        
        while (out.availableRead()) {
            m = out.receive();
            if (m.equalContent(hot))
                didHotArrive = true;
            if (m.equalContent(cold))
                fail("Filter has allowed a blocked message");
        }
        assertTrue("Filter has blocked a message", didHotArrive);
        instanceThr.join(10000);
        assertFalse("The task has failed to close automatically", instance.flow());
    }
    
}
