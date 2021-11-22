/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package xmlUtils;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class XMLUtilsTest {
    
    public XMLUtilsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of nodeToString method, of class XMLUtils.
     */
    @Test
    public void testNodeToString() throws Exception {
        System.out.println("nodeToString");
        Node n = null;
        String expResult = "";
        String result = XMLUtils.nodeToString(n);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of nodeListToString method, of class XMLUtils.
     */
    @Test
    public void testNodeListToString() throws Exception {
        System.out.println("nodeListToString");
        NodeList nl = null;
        String expResult = "";
        String result = XMLUtils.nodeListToString(nl);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of nodeListToStringList method, of class XMLUtils.
     */
    @Test
    public void testNodeListToStringList() throws Exception {
        System.out.println("nodeListToStringList");
        NodeList nl = null;
        List<String> expResult = null;
        List<String> result = XMLUtils.nodeListToStringList(nl);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stringToDocument method, of class XMLUtils.
     */
    @Test
    public void testStringToDocument() throws Exception {
        System.out.println("stringToDocument");
        String str = "";
        Document expResult = null;
        Document result = XMLUtils.stringToDocument(str);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of compareNodeList method, of class XMLUtils.
     */
    @Test
    public void testCompareNodeList() throws Exception {
        System.out.println("compareNodeList");
        String xml1 = "<drink><name>te</name><type>hot</type><id>1</id></drink>";
        String xml2 = "<drink><name>cerveza</name><type>cold</type><id>2</id></drink>";
        NodeList list1 = XMLUtils.stringToDocument(xml1).getChildNodes();
        NodeList list2 = XMLUtils.stringToDocument(xml2).getChildNodes();
        boolean expResult = true;
        boolean result = XMLUtils.compareNodeList(list1, list1);
        assertEquals("Thinks the same list is different from itself", expResult, result);
        expResult = false;
        result = XMLUtils.compareNodeList(list1, list2);
        assertEquals("Returned a false positive", expResult, result);
    }
    
}
