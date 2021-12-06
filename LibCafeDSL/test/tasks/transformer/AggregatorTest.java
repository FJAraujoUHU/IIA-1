package tasks.transformer;

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
public class AggregatorTest {
    
    Aggregator instance;
    Splitter SplitInstance;
    Thread instanceThr, SplitInstanceThr;
    Slot in, out, connector;
    static final String ORDER5 = "<cafe_order><order_id>5</order_id><drinks><drink><name>te</name><type>hot</type></drink><drink><name>te</name><type>hot</type></drink><drink><name>tonica</name><type>cold</type></drink><drink><name>coca-cola</name><type>cold</type></drink><drink><name>guarana</name><type>cold</type></drink></drinks></cafe_order>";
    static XPathExpression SplitterXPath;
    static final String SPLIT = "//drink";
    
    public AggregatorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws XPathExpressionException {
        SplitterXPath = XPathFactory.newInstance().newXPath().compile(SPLIT);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws SlotException {
        in = new Slot();
        out = new Slot();
        connector = new Slot();
        SplitInstance = new Splitter(in, connector, SplitterXPath);
        instance = new Aggregator(connector, out, SplitInstance);
        instanceThr = new Thread(instance);
        SplitInstanceThr = new Thread(SplitInstance);
        
        SplitInstanceThr.start();
        System.gc();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of run method, of class Aggregator.
     */
    @Test
    public void testRun() throws Exception {
        System.out.println("run");
        Message order = new Message(ORDER5);
        in.send(order);
        Message m;
        instanceThr.start();
        String output = out.receive(10000).toString();
        assertTrue("The reaggregation has failed",output.contains(ORDER5));
        System.out.println(output);
        in.send(Message.SHUTDOWN);
        SplitInstance.close();
        instanceThr.join(10000);
        assertFalse(instance.flow());
        
    }
    
}
