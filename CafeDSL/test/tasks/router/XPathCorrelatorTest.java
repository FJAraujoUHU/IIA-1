/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
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
public class XPathCorrelatorTest {
    
    Slot in1, in2, out1, out2;
    Correlator instance;
    Thread instanceThr;
    static XPathExpression xpath;
    static final String CORRELATION = "/drink/type | /drink/id";
    static final String HOTEXAMPLE1 = "<drink><name>te</name><type>hot</type><id>1</id></drink>";
    static final String HOTEXAMPLE2 = "<drink><name>cafe</name><type>hot</type><id>1</id></drink>";
    static final String COLDEXAMPLE1 = "<drink><name>cerveza</name><type>cold</type><id>2</id></drink>";
    static final String COLDEXAMPLE2 = "<drink><name>cola</name><type>cold</type><id>2</id></drink>";
    
    
    public XPathCorrelatorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws XPathExpressionException {
        xpath = XPathFactory.newInstance().newXPath().compile(CORRELATION);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws SlotException {
        in1 = new Slot();
        in2 = new Slot();
        out1 = new Slot();
        out2 = new Slot();
        instance = new XPathCorrelator(new Slot[]{in1,in2}, new Slot[]{out1,out2}, xpath);
        instanceThr = new Thread(instance);
        System.gc();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of run method, of class BasicCorrelator.
     */
    @Test
    public void testRun() throws SlotException, InterruptedException {
        System.out.println("run");

        instanceThr.start();
        Message hot1 = new Message(HOTEXAMPLE1);
        Message hot2 = new Message(HOTEXAMPLE2);
        
        Message cold1 = new Message(COLDEXAMPLE1);
        Message cold2 = new Message(COLDEXAMPLE2);
        
        
        in1.send(hot1);
        in2.send(cold1);
        in1.send(cold2);
        in2.send(hot2);
        
        
        
        Message m;
        assertEquals("Hasn't correlated", out1.receive().equals(hot1), out2.receive().equals(hot2));
        assertEquals("Hasn't correlated", out1.receive().equals(cold1), out2.receive().equals(cold2));
        
        in1.send(Message.SHUTDOWN);
        
        instance.close();
        
        instanceThr.join(10000);
        assertFalse("The task has failed to close automatically", instance.flow());
    }
    
}
