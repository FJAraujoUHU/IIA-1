/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
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
public class XMLExitWriterTest {
    
    XMLExitWriter instance;
    Thread instanceThr;
    static final int PORT = 7777;
    static final String PATH_TO_OUTPUT = "./XMLTesting";
    
    
    public XMLExitWriterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws SlotException, IOException {
        instance = new XMLExitWriter(PORT, PATH_TO_OUTPUT);
        instanceThr = new Thread(instance);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of run method, of class XMLExitWriter.
     */
    @Test
    public void testRun() throws InterruptedException {
        System.out.println("run");
        instanceThr.start();
        instanceThr.join();
    }
    
}
