package tasks.transformer;

import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;
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
public class TransformerTest {
    
    Transformer instance;
    Thread instanceThr;
    Slot in, out;
    static final String XSLTTEXT = "<?xml version=\"1.0\"?> <xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"> <xsl:template match=\"/\"> <sql> insert into table values (<xsl:value-of select=\"drink/name\"/>,<xsl:value-of select=\"drink/type\"/>) </sql> </xsl:template> </xsl:stylesheet>";
    static final String ITEMEXAMPLE = "<drink><name>te</name><type>hot</type></drink>";
    static final String EXPECTEDOUTPUT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><sql> insert into table values (te,hot) </sql>";
    
    
    public TransformerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws SlotException, Exception {
        in = new Slot();
        out = new Slot();
        
        instance = new Transformer(in, out, new StreamSource(new StringReader(XSLTTEXT)));
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

        
        Message test = new Message(ITEMEXAMPLE);
        
        in.send(test);
        assertEquals("Output is different from expectation", EXPECTEDOUTPUT, out.receive().toString());
        instance.close();
        instanceThr.join(10000);
        assertFalse("The task has failed to close automatically", instance.flow());
    }
    
}
