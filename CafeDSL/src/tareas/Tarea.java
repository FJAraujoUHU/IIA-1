package tareas;

import mensajeria.Mensaje;
import mensajeria.Slot;

/**
 * Tarea genérica sobre la que se basan el resto de tareas. También proporciona
 * cierta funcionalidad genérica para facilitar la implementación.
 *
 * @author Francisco Javier Araujo Mendoza
 */
public abstract class Tarea implements Runnable {

    /**
     * Array que contiene los slots de entrada de la tarea.
     */
    protected Slot[] entradas;

    /**
     * Array que contiene los slots de salida de la tarea.
     */
    protected Slot[] salidas;

    
    /**
     * Constructor para la creación de tareas.
     *
     * @param entradas array de Slots de entrada a la tarea.
     * @param nSalidas número de salidas de la tarea.
     * @throws Exception Si se produce un error al crear los Slots de salida.
     */
    public Tarea(Slot[] entradas, int nSalidas) throws Exception {
	this.entradas = entradas;
	this.salidas = new Slot[nSalidas];
	try {
	    for (int i = 0; i < nSalidas; i++) {
		salidas[i] = new Slot();
	    }
	} catch (Exception ex) {
	    Exception e = new Exception("Error al crear la tarea, hay problemas con los slots");
	    e.addSuppressed(ex);
	    throw e;
	}
    }

    /**
     * Devuelve un Slot para conectar la salida de la tarea con la
     * siguiente.
     *
     * @param puerto índice del puerto de salida a obtener.
     * @return Slot de salida listo para ser usado.
     * @throws Exception Si se ha introducido un índice inválido.
     */
    protected Slot getSlotSalida(int puerto) throws Exception {
	try {
	    return salidas[puerto];
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
	    salidas[puerto].enviar(m);
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
    protected Mensaje recibir(int puerto) throws Exception {
	try {
	    Mensaje ret = entradas[puerto].recibir();
	    return ret;
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
     * @throws Exception Si se produce algún error que no sea intentar cerrar un Slot previamente cerrado.
     */
    public void cerrar() throws Exception {
	for (Slot salida : salidas) {
	    try {
		salida.enviar(new Mensaje(Mensaje.APAGAR_SISTEMA));
		salida.cerrar();
	    } catch (Exception ex) {
		if (!ex.getMessage().toUpperCase().contains("CERRADO"))
		    throw ex;
	    }
	}
    }

    @Override
    public abstract void run(); //Implementar la lógica de la tarea.
}
