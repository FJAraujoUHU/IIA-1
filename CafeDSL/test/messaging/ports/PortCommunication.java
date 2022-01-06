package messaging.ports;

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
 * Test de comunicación entre puertos de entrada y de salida
 * @author Francisco Javier Araujo Mendoza
 */
public class PortCommunication {
    
    Slot in, out;
    int entrySocket = 7777;
    EntryPort entry;
    ExitPort exit;
    Thread entryThr, exitThr;
    
    public PortCommunication() {
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
        exit = new ExitPort("localhost", 7777, in);
        entry = new EntryPort(7777, out);
        entryThr = new Thread(entry);
        exitThr = new Thread(exit);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testMsg() throws Exception {
        System.out.println("run");
        String msg = "test";
        
        entryThr.start();
        exitThr.start();
        in.send(new Message(msg));
        assertTrue(out.receive().toString().equals(msg));
    }
    
    @Test
    public void testShutdown() throws Exception {
        System.out.println("run");
        String msg = "test";
        
        entryThr.start();
        exitThr.start();
        in.send(new Message(msg));
        in.send(Message.SHUTDOWN);
        System.out.println("Received " + out.receive().toString());
        assertTrue(out.availableWrite());
    }
    
}
