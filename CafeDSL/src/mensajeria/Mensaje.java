package mensajeria;

import java.io.Serializable;
import java.util.Random;

/**
 * Clase que representa a los mensajes internos de un problema de integración.
 * Lleva un mensaje en texto plano, una ID interna para identificarlo siempre,
 * y puede asignársele una ID pública para usarlo como Correlation ID.
 * Además, es Serializable para poder enviarlo a través de tuberías y puertos
 * para garantizar la máxima modularidad, y Clonable para poder replicarlos
 * fácilmente.
 * @author Francisco Javier Araujo Mendoza
 */
public class Mensaje implements Serializable, Cloneable {
    private String mensaje;
    private final long idInterna;
    private Integer idPublica;
    
    //Generador de ID internas
    private static final Random generador = new Random(System.nanoTime());
    
    /**
     * Orden/mensaje que el sistema entiende como orden de apagado.
     */
    public static final String APAGAR_SISTEMA = "!!!APAGAR EL SISTEMA!!!";

    /**
     * Constructor de la clase.
     * @param mensaje Mensaje que contiene.
     */
    public Mensaje(String mensaje) {
	this.mensaje = mensaje;
	idInterna = System.nanoTime() + generador.nextLong();
	idPublica = null;
    }

    /**
     * Constructor de la clase, diseñado para mensajes derivados de otro para
     * que conserven ID interna.
     * @param mensaje Mensaje que contiene.
     * @param padre Mensaje del que proviene.
     */
    public Mensaje(String mensaje, Mensaje padre) {
	this.mensaje = mensaje;
	idInterna = padre.idInterna;
	idPublica = null;
    }       
    
    /**
     * Establece un Correlation ID para el mensaje.
     * @param id
     */
    public void setId(int id) {
	idPublica = id;
    }
    
    /**
     * Retira el Correlation ID del mensaje.
     */
    public void resetID()   {
	idPublica = null;
    }
    
    /**
     * Devuelve el Correlation ID del mensaje.
     * @return Correlation ID del mensaje.
     * @throws Exception Si el mensaje no tenía Correlation ID.
     */
    public int getId() throws Exception {
	if (idPublica == null)
	    throw new Exception("No se ha asignado ningún ID");
	else return idPublica;
    }
    
    /**
     * Devuelve el ID interno del mensaje, único e inmutable (útil para trabajar
     * con tareas sin configurar y mensajes sin CID).
     * @return ID interno.
     */
    public long getIdInterna()	{
	return idInterna;
    }
    
    /**
     *
     * @param nuevoMensaje
     */
    public void setMensaje(String nuevoMensaje)	{
	mensaje = nuevoMensaje;
    }
    
    @Override	//para leer el mensaje
    public String toString()	{
	return mensaje;
    }
}
