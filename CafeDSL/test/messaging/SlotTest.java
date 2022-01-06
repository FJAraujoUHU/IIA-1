package messaging;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test de la clase Slot
 * @author Francisco Javier Araujo Mendoza
 */
public class SlotTest {

    private Slot instance;

    //Clases que envían y reciben para comprobar el funcionamiento concurrente.
    public class SlotReader implements Runnable {

        private final Slot slot;
        private Message m;

        public SlotReader(Slot slot) {
            this.slot = slot;
            m = null;
        }

        public Message getValue() {
            return m;
        }

        @Override
        public void run() {
            try {
                m = slot.receive();
            } catch (SlotException ex) {
                fail(ex.toString());
            }
        }
    }

    public class SlotWriter implements Runnable {

        private final Slot slot;
        private final Message m;

        public SlotWriter(Slot slot, Message m) {
            this.slot = slot;
            this.m = m;
        }

        @Override
        public void run() {
            try {
                slot.send(m);
            } catch (SlotException ex) {
                fail(ex.toString());
            }
        }
    }

    public SlotTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        try {
            instance = new Slot();
        } catch (SlotException ex) {
            fail(ex.toString());
        }
    }

    @After
    public void tearDown() {
        try {
            instance.close();
        } catch (SlotException ex) {
        }
        instance = null;
    }

    /**
     * Test of receive method, of class Slot.
     */
    @Test
    public void testReceive() throws Exception {
        System.out.println("receive");
        Message m = new Message("Hello world!");
        SlotWriter writer = new SlotWriter(instance, m);
        Thread writerThr = new Thread(writer);

        writerThr.start();
        writerThr.join();

        Message result = instance.receive();
        assertEquals(m, result);
    }

    /**
     * Test of send method, of class Slot.
     */
    @Test
    public void testSend() throws Exception {
        System.out.println("send");
        Message m = new Message("Hello world!");
        SlotReader reader = new SlotReader(instance);
        Thread readerThr = new Thread(reader);

        instance.send(m);
        readerThr.start();
        readerThr.join();
        Message result = reader.getValue();
        assertEquals(m, result);
    }

    /**
     * Test of close method, of class Slot.
     */
    @Test
    public void testCloseWrite() throws SlotException {
        System.out.println("close");
        assertTrue("Slot is closed, can't test", instance.availableWrite());
        Boolean exceptionTriggered = false;
        instance.close();
        
        try {
            instance.send(new Message("Hello world!"));
        } catch (SlotException ex) {
            //Debe bloquearlo
            exceptionTriggered = true;
        }

        assertTrue(exceptionTriggered && !instance.availableWrite());
    }
    
    /**
     * Test of close method, of class Slot.
     */
    @Test
    public void testCloseRead() throws SlotException {
        System.out.println("close");
        Boolean exceptionTriggered = false;
        instance.send(new Message("Hello world!"));
        instance.close();
        
        try {
            instance.receive();
        } catch (SlotException ex) {
            fail("Slot has closed its output");
        }
        
        try {
            instance.receive();
            fail("Slot hasn't closed its output");
        } catch (SlotException ex) {
            
        }
        
        
        try {
            instance.send(new Message("Hello world!"));
        } catch (SlotException ex) {
            //Debe bloquearlo
            exceptionTriggered = true;
        }

        assertTrue(exceptionTriggered);
    }

    /**
     * Test of available method, of class Slot.
     */
    @Test
    public void testAvailable() throws SlotException {
        System.out.println("available");
        
        instance.send(new Message("Hello world!"));
        assertTrue(instance.availableWrite());
        instance.close();
        assertFalse(instance.availableWrite());
        assertTrue(instance.availableRead());
    }
}
