package tareas;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import mensajeria.Mensaje;

/**
 * Tarea genérica sobre la que se basan el resto de tareas. También proporciona
 * cierta funcionalidad genérica para facilitar la implementación.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public abstract class Tarea implements Runnable {

    //streams que proporcionan conexión entre tareas
    /**
     * Streams/Extremos de las tuberías de entrada.
     */
    protected PipedInputStream[] entradasTub;

    /**
     * Streams/Extremos de las tuberías de salida.
     */
    protected PipedOutputStream[] salidasTub;

    //streams que extrae los mensajes del stream tubería (las tuberías trabajan byte a byte)
    /**
     * Streams que sacan objetos de las tuberías de entrada.
     */
    protected ObjectInputStream[] entradas;

    /**
     * Streams que tiran objetos por las tuberías de salida.
     */
    protected ObjectOutputStream[] salidas;


    /**
     * Constructor para la creación de tareas.
     *
     * @param entradaTub array de tuberías de entrada a la tarea.
     * @param entradas número de entradas de la tarea.
     * @param salidas número de salidas de la tarea.
     * @throws Exception
     */
    protected Tarea(PipedInputStream[] entradaTub, int entradas, int salidas) throws Exception {
	try {
	    //Conecta las tuberías de entrada e inicia los streams de objetos de entrada
	    this.entradasTub = entradaTub;
	    this.entradas = new ObjectInputStream[entradas];
	    for (int i = 0; i < entradas; i++) {
		this.entradas[i] = new ObjectInputStream(entradaTub[i]);
	    }

	    //Inicia los streams de salida, las tuberías se conectan cuando se establece la conexión en el destino.
	    salidasTub = new PipedOutputStream[salidas];
	    this.salidas = new ObjectOutputStream[salidas];
	    for (int i = 0; i < salidas; i++) {
		this.salidas[i] = new ObjectOutputStream(salidasTub[i]);
	    }
	} catch (IOException ex) {	//si se produce algún error con los streams
	    Exception e = new Exception("Error al crear la tarea, hay problemas con los slots");
	    e.addSuppressed(ex);
	    throw e;
	}
    }

    /**
     * Devuelve un slot/tubería para conectar la salida de la tarea con la
     * siguiente. Útil para crear el array de tuberías.
     *
     * @param puerto
     * @return
     * @throws Exception
     */
    protected PipedInputStream getSlotSalida(int puerto) throws Exception {
	try {
	    return new PipedInputStream(salidasTub[puerto]);
	} catch (IOException ex) {
	    cerrar();
	    Exception e = new Exception("Error al crear el slot");
	    e.addSuppressed(e);
	    throw e;
	} catch (IndexOutOfBoundsException ex) {
	    Exception e = new Exception("Índice del puerto inválido :" + puerto + "de " + salidas.length);
	    e.addSuppressed(ex);
	    throw e;
	}
    }

    /**
     * Envía un mensaje por el puerto especificado.
     *
     * @param m Mensaje a enviar.
     * @param puerto Puerto destino.
     * @throws Exception Si hay problemas con el slot o se ha introducido un
     * índice inválido.
     */
    protected void enviar(Mensaje m, int puerto) throws Exception {
	try {
	    salidas[puerto].writeObject(m);
	} catch (IOException ex) {
	    cerrar();
	    Exception e = new Exception("Error al intentar enviar un mensaje, slot caído");
	    e.addSuppressed(ex);
	    throw e;
	} catch (IndexOutOfBoundsException ex) {
	    Exception e = new Exception("Índice del puerto inválido :" + puerto + "de " + salidas.length);
	    e.addSuppressed(ex);
	    throw e;
	}
    }

    /**
     * Espera a recibir un mensaje por el puerto especificado.
     *
     * @param puerto Puerto a escuchar.
     * @return Un mensaje.
     * @throws Exception Si hay problemas con el slot o se ha introducido un
     * índice inválido.
     */
    protected Mensaje leer(int puerto) throws Exception {
	try {
	    Mensaje ret = (Mensaje) entradas[puerto].readObject();
	    return ret;
	} catch (IOException ex) {
	    cerrar();
	    Exception e = new Exception("Error al intentar recibir un mensaje, slot caído");
	    e.addSuppressed(ex);
	    throw e;
	} catch (IndexOutOfBoundsException ex) {
	    Exception e = new Exception("Índice del puerto inválido :" + puerto + "de " + entradas.length);
	    e.addSuppressed(ex);
	    throw e;
	}
    }

    /**
     * Devuelve el número de puertos de entrada de la tarea.
     *
     * @return Número de puertos
     */
    public int getNEntradas() {
	return entradas.length;
    }

    /**
     * Devuelve el número de puertos de salida de la tarea.
     *
     * @return Número de puertos.
     */
    public int getNSalidas() {
	return salidas.length;
    }

    /**
     * Cierra la tarea e intenta propagar un mensaje de apagado a sus vecinos.
     *
     * @return True si ha podido enviar el mensaje sin problemas, False si algún
     * slot está caído.
     */
    public boolean cerrar() {
	boolean ret = true;
	for (ObjectOutputStream salida : salidas)
	    try {
	    salida.writeObject(new Mensaje(Mensaje.APAGAR_SISTEMA));
	    salida.flush();
	    salida.close();
	} catch (IOException ex) {
	    Logger.getLogger(Tarea.class.getName()).log(Level.SEVERE, null, ex);
	    ret = false;
	}
	for (PipedOutputStream salida : salidasTub) {
	    try {
		salida.flush();
		salida.close();
	    } catch (IOException ex) {
		Logger.getLogger(Tarea.class.getName()).log(Level.SEVERE, null, ex);
		ret = false;
	    }
	}
	for (ObjectInputStream entrada : entradas) {
	    try {
		entrada.close();
	    } catch (IOException ex) {
		Logger.getLogger(Tarea.class.getName()).log(Level.SEVERE, null, ex);
		ret = false;
	    }
	}
	for (PipedInputStream entrada : entradasTub) {
	    try {
		entrada.close();
	    } catch (IOException ex) {
		Logger.getLogger(Tarea.class.getName()).log(Level.SEVERE, null, ex);
		ret = false;
	    }
	}
	return ret;
    }

    @Override
    public abstract void run();	//Implementar la lógica de la tarea.
//    {
//	/** Ejemplo básico de uso, reemplazar en las clases hija **/
//	
//	Mensaje m;
//	String contenido = Mensaje.APAGAR_SISTEMA;
//	
//	do {					    //ejecutarse hasta recibir la orden de apagado
//	    try {
//		m = leer(0);			    //La tarea se queda esperando a que le llegue un mensaje por el primer slot
//		contenido = m.toString();	    //Guarda el contenido en una string
//		System.out.println(contenido);	    //Lo muestra por pantalla
//		enviar(m, 0);			    //Reenvía el mensaje por el primer slot
//	    } catch (Exception ex) {
//		System.out.println(ex.toString());  //si hay algún error, mostrarlo por pantalla y seguir ejecutando
//	    }
//	} while (contenido.equals(Mensaje.MSG_APAGAR_SISTEMA));
//	
//	cerrar();				    //Una vez ha recibido la orden de apagado, apagarse y propagar el mensaje
//    }

}
