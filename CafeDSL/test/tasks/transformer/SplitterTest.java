package tasks.transformer;

import java.util.ArrayList;
import java.util.List;
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
public class SplitterTest {
    
    Splitter instance;
    Thread instanceThr;
    Slot in, out;
    static final String ORDER5 = "<cafe_order><order_id>5</order_id><drinks><drink><name>te</name><type>hot</type></drink><drink><name>te</name><type>hot</type></drink><drink><name>tonica</name><type>cold</type></drink><drink><name>coca-cola</name><type>cold</type></drink><drink><name>guarana</name><type>cold</type></drink></drinks></cafe_order>";
    static XPathExpression xpath;
    static final String SPLIT = "//drink";

    
    
    public SplitterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws XPathExpressionException {
        xpath = XPathFactory.newInstance().newXPath().compile(SPLIT);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws SlotException {
        in = new Slot();
        out = new Slot();
        instance = new Splitter(in, out, xpath);
        instanceThr = new Thread(instance);
        instanceThr.start();
        System.gc();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of run method, of class Splitter.
     */
    @Test
    public void testRun() throws SlotException, TimeoutException, InterruptedException {
        System.out.println("run");
        Message order = new Message(ORDER5);
        in.send(order);
        Message m;
        for (int i = 0; i < 5; i++) {
            m = out.receive(2500);
            System.out.println(m.toString() + "|| UUID: " + m.getInternalId().toString());
        }
        instance.close();
        instanceThr.join(10000);
        assertFalse("Splitter has split too much", out.availableRead());
        System.out.println(instance.headerStorage.get(order.getInternalId()));
        
    }
    
}
