package connector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import messaging.*;
import messaging.ports.EntryPort;
import messaging.ports.PortException;

/**
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class XMLExitWriter implements Runnable {

    final EntryPort input;
    final Thread portThr;
    final Path path;
    final Slot slot;
    final UUID uuid;

    public XMLExitWriter(int ListeningSocket, String pathToFolder) throws SlotException, IOException {
        this.path = Paths.get(pathToFolder);
        this.slot = new Slot();
        this.input = new EntryPort(ListeningSocket, slot);
        this.portThr = new Thread(input);
        this.uuid = UUID.randomUUID();

        if (Files.notExists(path) || !Files.isDirectory(path)) {
            Files.createDirectory(path);
        }
    }

    
    
    
    
    
    @Override
    public void run() {
        portThr.start();
        
        Message m;
        //Mientras siga abierto
        while(slot.availableRead()) {
            try {
                m = slot.receive();
                if (m.isShutdown()) {
                    if (input.available()) {
                        input.close();
                    }
                } else {
                    Path newXML = path.resolve(m.getInternalId() + ".xml");
                    Files.writeString(newXML, m.toString());
                }
            } catch (SlotException ex) {
                //El conector termina automáticamente, se puede ignorar
            } catch (PortException ex) {
                //Se realiza la comprobación previamente, se puede ignorar
            } catch (IOException ex) {
                //Si falla la escritura del archivo
                Logger.getLogger(XMLExitWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        if (input.available()) {
            try {
                input.close();
            } catch (PortException | IOException ex) {
                //Forma parte del cierre, se puede ignorar
            }
        }
        try {
            portThr.join(10000);
        } catch (InterruptedException ex) {
            //idem
        }
    }

}
