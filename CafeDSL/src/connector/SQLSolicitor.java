package connector;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import messaging.*;
import messaging.ports.EntryPort;
import messaging.ports.ExitPort;
import messaging.ports.PortException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import static xmlUtils.XMLUtils.*;

/**
 * Conector diseñado para realizar peticiones a una BBDD MySQL.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class SQLSolicitor implements Runnable {

    Connection conn;
    final EntryPort entryPort;
    final ExitPort exitPort;
    final Slot entrySlot, exitSlot;
    final Thread entryThread, exitThread;
    final UUID uuid;

    /**
     * Constructor.
     *
     * @param DSLHostname Dominio donde se hospeda el DSL
     * @param DSLExitSocket Puerto que tiene configurado el DSL ExitPort
     * @param DSLEntrySocket Puerto que tiene configurado el DSL EntryPort
     * @param SQLHostname Dominio del servidor de la BD
     * @param SQLPort Puerto de la BD
     * @param user Usuario de la BD
     * @param password Contraseña de la BD
     * @param schema Schema a usar
     * @param autocommit Si debe activarse el autocommit para la conexión.
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws SlotException
     */
    public SQLSolicitor(String DSLHostname, int DSLExitSocket, int DSLEntrySocket, String SQLHostname, int SQLPort, String user, String password, String schema, boolean autocommit) throws ClassNotFoundException, SQLException, SlotException {
        String url = "jdbc:mysql://" + SQLHostname + ":" + SQLPort + "/" + schema + "?user=" + user + "&password=" + password;

        uuid = UUID.randomUUID();
        entrySlot = new Slot();
        exitSlot = new Slot();
        entryPort = new EntryPort(DSLExitSocket, entrySlot);
        exitPort = new ExitPort(DSLHostname, DSLEntrySocket, exitSlot);
        entryThread = new Thread(entryPort);
        exitThread = new Thread(exitPort);

        conn = DriverManager.getConnection(url);
        conn.setAutoCommit(autocommit);
    }

    public void close(boolean commit) throws SQLException, IOException {
        if (!conn.getAutoCommit()) {
            if (commit) {
                conn.commit();
            } else {
                conn.rollback();
            }
        }
        conn.close();
        if (exitPort.available()) {
            try {
                exitPort.close();
            } catch (PortException ex) {
                //Nunca debería lanzarse porque se comprueba primero si está abierto
            }
        }
        if (entryPort.available()) {
            try {
                entryPort.close();
            } catch (PortException ex) {
                //Nunca debería lanzarse porque se comprueba primero si está abierto
            }
        }
        try {
            entryThread.join(10000);
            exitThread.join(10000);
        } catch (InterruptedException ex) {
            //Ignorar
        }
    }

    public void close() throws IOException, PortException, SQLException {
        conn.close();
        if (exitPort.available()) {
            try {
                exitPort.close();
            } catch (PortException ex) {
                //Nunca debería lanzarse porque se comprueba primero si está abierto
            }
        }
        if (entryPort.available()) {
            try {
                entryPort.close();
            } catch (PortException ex) {
                //Nunca debería lanzarse porque se comprueba primero si está abierto
            }
        }
        try {
            entryThread.join(10000);
            exitThread.join(10000);
        } catch (InterruptedException ex) {
            //Ignorar
        }

    }

    @Override
    public void run() {
        entryThread.start();
        exitThread.start();

        try {
            Message m;
            do {
                try {
                    m = entrySlot.receive();
                    try {
                        if (!m.isShutdown()) {

                            Node StatementNode //Extrae la llamada desde el mensaje
                                    = stringToDocument(m.toString())
                                            .getElementsByTagName("sql")
                                            .item(0);

                            if (StatementNode != null) {    //Si ha podido extraerla correctamente

                                CallableStatement st = conn.prepareCall(StatementNode.getTextContent());
                                st.execute();

                                //Convierte los resultados a XML
                                ResultSet results = st.getResultSet();
                                ResultSetMetaData md = results.getMetaData();
                                Document outputDoc
                                        = DocumentBuilderFactory
                                                .newInstance()
                                                .newDocumentBuilder()
                                                .newDocument();
                                Node root = outputDoc.createElement("sql");
                                outputDoc.appendChild(root);
                                Node rs = outputDoc.createElement("resultSet");
                                root.appendChild(rs);
                                //Va fila a fila llenando
                                while (results.next()) {    //Mientras haya más filas
                                    Element row = outputDoc.createElement("result");
                                    //Llenar cada columna de la fila
                                    for (int i = 1; i <= md.getColumnCount(); i++) {
                                        Element item = outputDoc.createElement("item");
                                        item.setAttribute("colName", md.getColumnLabel(i));
                                        item.setAttribute("javaType", md.getColumnClassName(i));
                                        item.setAttribute("SQLType", md.getColumnTypeName(i));
                                        item.setTextContent(results.getString(i));
                                        row.appendChild(item);
                                    }
                                    rs.appendChild(row);
                                }
                                //Mete el XML en un mensaje
                                Message output = new Message(nodeToString(outputDoc), m);
                                exitSlot.send(output);
                            }
                        }
                    } catch (SlotException ex) {
                        //Si se lanza la excepción, sale del bucle sólo
                    } catch (ParserConfigurationException ex) {
                        //Lo lanza la creación del documento vacío, nunca se va a producir
                        Message error = new Message("<sql><resultSet/></sql>", m);
                        exitSlot.send(error);
                    } catch (SAXException | IOException ex) {
                        //Si no ha podido extraer el documento del mensaje de entrada
                        Message error = new Message("<sql><resultSet/></sql>", m);
                        exitSlot.send(error);
                    } catch (SQLException ex) {
                        //Si la sentencia no es correcta
                        Message error = new Message("<sql><resultSet><SQLError>" + ex.getErrorCode() + "</SQLError></resultSet></sql>", m);
                        exitSlot.send(error);
                    }

                } catch (SlotException ex) {
                    //Si se lanza, sale del bucle sólo
                }

            } while (!conn.isClosed() && entrySlot.availableRead() && exitSlot.availableWrite());

        } catch (SQLException ex) {
            Logger.getLogger(SQLSolicitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        try {
            this.close(true);
        } catch (SQLException | IOException ex) {
            Logger.getLogger(SQLSolicitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
