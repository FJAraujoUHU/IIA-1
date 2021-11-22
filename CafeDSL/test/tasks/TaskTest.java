package tasks;

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
public class TaskTest {

    
    public class TaskImpl extends Task {

        public static final String ADDED = "yep";

        public TaskImpl(Slot in, Slot out) {
            super(new Slot[]{in}, new Slot[]{out});
        }

        @Override
        public void run() {
            Message m;
            String contenido;

            do {                                                                    //ejecutarse hasta recibir la orden de apagado/se cierre un slot
                try {
                    m = receive(0);                                                 //La tarea se queda esperando a que le llegue un mensaje por el primer slot
                    contenido = m.toString();

                    if (!m.equals(Message.SHUTDOWN)) {
                        m.setMessage(contenido + ADDED);                                              //Cambia el ID del mensaje y postautoincrementa
                        send(m, 0);                                                 //Envía el mensaje
                    }

                } catch (SlotException ex) {
                    m = Message.SHUTDOWN;
                }
            } while (!m.equals(Message.SHUTDOWN) && this.flow());

            this.close();
        }
    }

    private Task instance;
    private Thread instanceThr;
    private Slot in, out;

    public TaskTest() {
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
        instance = new TaskImpl(in, out);
        instanceThr = new Thread(instance);
    }

    @After
    public void tearDown() {
        instance.close();
        instance = null;
        instanceThr = null;
        in = null;
        out = null;
    }

    /**
     * Test of close method, of class Task.
     */
    @Test
    public void testClose() throws InterruptedException {
        System.out.println("close");
        assertTrue("Task should have all slots open, but some aren't functioning", instance.flow());
        instance.close();
        instanceThr.join(10000);
        assertFalse("Task should have slots closed, but all slots are still open", instance.flow());
        assertFalse("Input slot should be closed, but it's open", in.available());
        assertTrue("Output slot should have been left open, but it's closed", out.available());
    }

    /**
     * Test of flow method, of class Task.
     */
    @Test
    public void testFlow() throws InterruptedException {
        System.out.println("flow");
        assertTrue(instance.flow());
        instance.close();
        instanceThr.join(10000);
        assertFalse(instance.flow());
    }

    /**
     * Test of run method, of class Task.
     */
    @Test
    public void testRun() throws SlotException, InterruptedException {
        System.out.println("run");

        instanceThr.start();
        String msg = "Hello world!";
        Message m = new Message(msg);

        in.send(m);
        m = out.receive();
        String result = m.toString();
        String expResult = msg + TaskImpl.ADDED;
        assertEquals("Output message doesn't match expectations", expResult, result);

        assertTrue("The task has closed its ports prematurely", instance.flow());
        in.send(Message.SHUTDOWN);
        instanceThr.join(10000);
        assertFalse("The task has failed to close automatically", instance.flow());
    }
}
