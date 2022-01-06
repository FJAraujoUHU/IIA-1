package connector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import messaging.*;
import messaging.ports.ExitPort;

/**
 * Lector de archivos XML para enviar al DSL. Comprueba periódicamente si hay
 * archivos XML en el directorio especificado, y los envía.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class XMLEntryLoader implements Runnable {

    final ExitPort output;
    final Thread portThr;
    final Path path;
    final Slot slot;
    final UUID uuid;
    static final int CHECK_INTERVAL_MS = 2500; //Tiempo entre checks para ver si hay nuevos archivos

    public XMLEntryLoader(String DSLHostname, int DSLSocket, String pathToFolder) throws SlotException, IOException {
        this.slot = new Slot();
        this.output = new ExitPort(DSLHostname, DSLSocket, slot);
        this.portThr = new Thread(output);
        this.path = Paths.get(pathToFolder);
        this.uuid = UUID.randomUUID();

        if (Files.notExists(path) || !Files.isDirectory(path)) {
            Files.createDirectory(path);
        }
    }

    public void close() {
        try {
            if (slot.availableWrite()) {
                slot.close();
            }
        } catch (SlotException ex) {
            //No debería fallar al realizar las comprobaciones previas, ignorar
        }

    }

    @Override
    public void run() {
        portThr.start();

        List<Path> items;
        while (slot.availableWrite()) {
            try {
                //Si existe un archivo ".end", terminar
                if (Files.exists(path.resolve(".end"))) {
                    slot.send(Message.SHUTDOWN);
                    Files.delete(path.resolve(".end"));
                } else {
                    //Carga los archivos XML disponibles en la carpeta
                    items = Files.list(path)
                            .filter(file
                                    -> !Files.isDirectory(file) && file.toUri()
                            .toString()
                            .toUpperCase()
                            .endsWith(".XML")
                            ).collect(Collectors.toList());
                    //Los lee y envía
                    for (Path item : items) {
                        slot.send(new Message(Files.readString(item)));
                        //Borra las comandas al leerlas
                        Files.delete(item);
                    }
                    Thread.sleep(CHECK_INTERVAL_MS);
                }
            } catch (IOException ex) {
                //Si falla al leer
                Logger.getLogger(XMLEntryLoader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SlotException ex) {
                //Ignorar, termina solo
            } catch (InterruptedException ex) {
                //Si falla al hacer wait, ignorar
            }
        }
        this.close();
        try {
            portThr.join(10000);
        } catch (InterruptedException ex) {
            //Ignorar, se está cerrando
        }
    }
}
