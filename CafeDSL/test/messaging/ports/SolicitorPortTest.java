package messaging.ports;

import messaging.Message;
import messaging.Slot;
import messaging.SlotException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class SolicitorPortTest {
    
    Slot in, out;
    SolicitorPort instance;
    Thread instanceThr;
    static final String HOST = "localhost";
    static final int PORT = 7777;
    
    
    public SolicitorPortTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws SlotException {
        in = new Slot();
        out = new Slot();
        instance = new SolicitorPort(HOST, PORT, PORT+1, in, out);
        instanceThr = new Thread(instance);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testRun() throws Exception {
        System.out.println("run");
        
        instanceThr.start();
        in.send(new Message("<sql>SELECT * FROM bebidasFrias"));
        System.out.println(out.receive().toString());
    }
    
}
