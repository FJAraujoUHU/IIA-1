package tests;

import mensajeria.Mensaje;
import mensajeria.Slot;
import mensajeria.puertos.PuertoEntrada;
import tareas.Tarea;

/**
 * Tester de puertos, usar junto Mensajero.java (Para ejecutar, Shift+F6)
 * ESTA CLASE NO DEBERÍA SER LA MAIN POR DEFECTO, COMPROBAR EN PROPIEDADES DEL PROYECTO
 * @author Francisco Javier Araujo Mendoza
 */
public class PuertoTester {

    //Tarea genérica de un slot de entrada y otro de salida, que añade un "!" a lo que le entra y lo reenvía.
    private static class TareaEjemplo extends Tarea {

	public TareaEjemplo(Slot entrada) throws Exception {
	    super(new Slot[]{entrada}, 1);
	}

	@Override
	public void run() {
	    Mensaje m;
	    String contenido;

	    do {					    //ejecutarse hasta recibir la orden de apagado/se cierre un slot
		try {
		    m = recibir(0);			    //La tarea se queda esperando a que le llegue un mensaje por el primer slot
		    contenido = m.toString();		    //Guarda el contenido en una string
		    m.setMensaje(contenido + "!");	   //Añade un carácter al mensaje
		    enviar(m, 0);			    //Reenvía el mensaje por el primer slot
		} catch (Exception ex) {
		    System.out.println(ex.toString());	    //si hay algún error, mostrarlo por pantalla y seguir ejecutando
		    contenido = Mensaje.APAGAR_SISTEMA;

		}
	    } while (!contenido.equals(Mensaje.APAGAR_SISTEMA) && entradas[0].abierto() && salidas[0].abierto());

	    try {
		cerrar();
	    } catch (Exception ex) {
		System.out.println(ex.toString());
	    }
	}
    }

    /**
     * Main para probar el funcionamiento de puertos, implementar según necesidad
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

	//Crea un puerto de entrada conectada a una tarea ejemplo, y muestra por pantalla la salida de la tarea
	String txt = "";
	Mensaje m;
	TareaEjemplo te;
	PuertoEntrada pe;
	Thread thr1, thr2;

	Slot salida;

	//definición de tareas y conexión
	pe = new PuertoEntrada(7777);
	te = new TareaEjemplo(pe.getSlotSalida());
	salida = te.getSlotSalida(0);

	//arranco las tareas
	thr1 = new Thread(pe);
	thr2 = new Thread(te);
	thr1.start();
	thr2.start();

	try {
	while (!txt.equals(Mensaje.APAGAR_SISTEMA)) {			    //meter mensaje por pantalla, enviar "-1" para cerrar el sistema
	    m = salida.recibir();
	    txt = m.toString();
	    System.out.println(">" + txt);
	}
	} catch (Exception ex) {
	    if (!ex.getMessage().contains("cerrado")) throw ex;
	}

	pe.cerrar();
	thr1.join(10000);
	thr2.join(10000);
    }

}
