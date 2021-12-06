package xmlUtils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
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
