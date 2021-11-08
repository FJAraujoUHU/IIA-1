package mensajeria.puertos;

import mensajeria.*;

/**
 * Puerto de solicitud. Internamente utiliza uno de entrada y uno de salida conectados.
 * Nota: ambos puertos utilizan sockets distintos, el pasado por argumento y el inmediatamente siguiente.
 * @author Francisco Javier Araujo Mendoza
 */
public class PuertoSolicitud {

    private final PuertoEntrada pRespuesta;
    private final PuertoSalida pSolicitud;
    private final Slot entrada, salida;
    private final Thread hiloEntrada, hiloSalida;
    private final String host;
    private final int puertoEntrada, puertoSalida;
    
    /**
     * Constructor del puerto.
     * @param host Dirección a la que conectarse.
     * @param puerto Primer puerto para establecer la conexión (0-65535).
     * @param solicitud Slot por el que enviar las solicitudes.
     * @throws Exception Si se produce algún error al crear los puertos internos.
     */
    public PuertoSolicitud(String host, int puerto, Slot solicitud) throws Exception	{
	this.host = host;
	this.puertoSalida = puerto;
	this.puertoEntrada = puerto+1;
	this.entrada = solicitud;
	this.pSolicitud = new PuertoSalida(this.host, puertoSalida, entrada);
	this.pRespuesta = new PuertoEntrada(puertoEntrada);
	this.hiloSalida = new Thread(pSolicitud);
	this.hiloEntrada = new Thread(pRespuesta);
	this.salida = pRespuesta.getSlotSalida();
    }
    
    /**
     * Devuelve el slot por el que salen las respuestas.
     * @return Slot por el que llegan las respuestas.
     */
    public Slot getSlotSalida()	{
	return salida;
    }
    
    /**
     * Cierra el puerto (y los puertos internos) y espera hasta que se cierren.
     * @throws Exception si hay algún problema cerrando.
     */
    public void cerrar() throws Exception    {
	if (pSolicitud.abierto())
	    pSolicitud.cerrar();
	if (pRespuesta.abierto())
	    pRespuesta.cerrar();
	hiloSalida.join(10000);
	hiloEntrada.join(10000);
    }
    
    /**
     * Devuelve el estado de la conexión
     * @return Si todo va bien.
     */
    public boolean abierto()	{
	return pSolicitud.abierto() && pRespuesta.abierto();
    }
    
    /**
     * Arranca los puertos internos.
     */
    public void start()	{
	hiloSalida.start();
	hiloEntrada.start();
    }
}
