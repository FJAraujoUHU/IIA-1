package connector;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import messaging.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import static xmlUtils.XMLUtils.*;

/**
 * WIP
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class SQLSolicitor implements Runnable {

    Connection conn;
    Slot in, out;

    public SQLSolicitor(Slot in, Slot out, String hostname, int port, String user, String password, String schema, boolean autocommit) throws ClassNotFoundException, SQLException {
        String url = "jdbc:mysql://" + hostname + ":" + port + "/" + schema + "?user=" + user + "&password=" + password;

        //Class.forName("com.mysql.jdbc.driver");
        conn = DriverManager.getConnection(url);
        conn.setAutoCommit(autocommit);
        this.in = in;
        this.out = out;
    }

    public void close(boolean commit) throws SQLException {
        if (!conn.getAutoCommit()) {
            if (commit) {
                conn.commit();
            } else {
                conn.rollback();
            }
        }
        conn.close();
    }

    public void close() throws SQLException {
        conn.close();
    }

    @Override
    public void run() {
        try {
            Message m;
            do {
                try {
                    m = in.receive();
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
                                out.send(output);
                            }
                        }

                    } catch (SlotException ex) {
                        //Si se lanza la excepción, sale del bucle sólo
                    } catch (ParserConfigurationException ex) {
                        //Lo lanza la creación del documento vacío, nunca se va a producir
                        Message error = new Message("<sql><resultSet/></sql>", m);
                        out.send(error);
                    } catch (SAXException | IOException ex) {
                        //Si no ha podido extraer el documento del mensaje de entrada
                        Message error = new Message("<sql><resultSet/></sql>", m);
                        out.send(error);
                    } catch (SQLException ex) {
                        //Si la sentencia no es correcta
                        Message error = new Message("<sql><resultSet><SQLError>"+ ex.getErrorCode() +"</SQLError></resultSet></sql>", m);
                        out.send(error);
                    }
                    
                } catch (SlotException ex) {
                    //Si se lanza, sale del bucle sólo
                }

            } while (!conn.isClosed() && in.availableRead() && out.availableWrite());
            this.close(true);

        } catch (SQLException ex) {
            Logger.getLogger(SQLSolicitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
