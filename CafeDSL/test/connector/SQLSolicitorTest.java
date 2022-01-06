package connector;

import messaging.*;
import messaging.ports.EntryPort;
import messaging.ports.ExitPort;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class SQLSolicitorTest {
    
    SQLSolicitor instance;
    Thread instanceThr;
    static int REQUESTPORT = 7777;
    static int RESPONSEPORT = 7778;
    static String SQLSELECT = "<sql>SELECT * FROM noexiste</sql>";
    static String SQLFUNCTION = "<sql>SELECT servirFria('cerveza')</sql>";
    
    
    
    public SQLSolicitorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws Exception {
        //in = new Slot();
        //out = new Slot();
        instance = new SQLSolicitor("localhost", RESPONSEPORT, REQUESTPORT, "b0ve.com", 3306, "cafe-05", "pass", "cafe05", false);
        instanceThr = new Thread(instance);
        instanceThr.start();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of close method, of class SQLSolicitor.
     */
    /*@Test
    public void testClose() throws Exception {
        System.out.println("close");
        boolean commit = false;
        SQLSolicitorPort instance = null;
        instance.close(commit);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of run method, of class SQLSolicitorPort.
     */
    @Test
    public void testRun() throws Exception {
        System.out.println("run");

        
        Message query = new Message(SQLFUNCTION);
        Message select = new Message(SQLSELECT);
        Message result;
        
        /*in.send(query);
        in.send(select);
        result = out.receive();
        System.out.println(result.toString());
        result = out.receive();
        System.out.println(result.toString());*/
        //instance.close(false);
        instanceThr.join();
    }
    
    
    @Test
    public void testShutdown() throws Exception {
        System.out.println("shutdown");

        Slot querySlot, resultSlot;
        querySlot = new Slot();
        resultSlot = new Slot();
       
        ExitPort queryPort = new ExitPort("localhost", RESPONSEPORT, querySlot);
        EntryPort resultPort = new EntryPort(REQUESTPORT, resultSlot);
        Thread exitThr = new Thread(queryPort);
        Thread entryThr = new Thread(resultPort);
        exitThr.start();
        entryThr.start();
        
        
        
        Message query = new Message(SQLFUNCTION);
        Message select = new Message(SQLSELECT);
        Message result;
        
        //querySlot.send(select);
        querySlot.send(Message.SHUTDOWN);
        /*in.send(query);
        in.send(select);
        result = out.receive();
        System.out.println(result.toString());
        result = out.receive();
        System.out.println(result.toString());*/
        //instance.close(false);
        //System.out.println(resultSlot.receive().toString());
        //result = resultSlot.receive();
        instanceThr.join();
        exitThr.join();
        entryThr.join();
    }
}
