package connector;

import java.io.IOException;
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
public class XMLEntryLoaderTest {
    
    XMLEntryLoader instance;
    Thread instanceThr;
    static final int PORT = 7777;
    
    
    public XMLEntryLoaderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws SlotException, IOException {
        instance = new XMLEntryLoader("localhost", PORT, "./XMLTesting");
        instanceThr = new Thread(instance);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of run method, of class XMLEntryLoader.
     */
    @Test
    public void testRun() throws InterruptedException {
        System.out.println("run");
        instanceThr.start();
        instanceThr.join();
        
        
    }
    
}
