package tests;

import messaging.ports.SolicitorPort;
import messaging.Message;
import messaging.Slot;
import tasks.Task;

/**
 * Tester de puertos, usar junto Mensajero.java (Para ejecutar, Shift+F6) ESTA
 * CLASE NO DEBERÍA SER LA MAIN POR DEFECTO, COMPROBAR EN PROPIEDADES DEL
 * PROYECTO
 *
 * @author Francisco Javier Araujo Mendoza
 */
public class PuertoTester {

    /**
     * Main para probar el funcionamiento de puertos, implementar según
     * necesidad
     *
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {

	//test solicitud
	SolicitorPort ps;
	Slot solicitud, respuesta;
	
	solicitud = new Slot();
        respuesta = new Slot();
	ps = new SolicitorPort("localhost", 7777, solicitud, respuesta);
	
	ps.start();
	for (int i = 0; i < 5; i++) {
	    solicitud.send(new Message(""+i));
	    System.out.println(respuesta.receive());
	}
	
	
	ps.close();
	
//	//Crea un puerto de entrada conectada a una tarea ejemplo, y muestra por pantalla la salida de la tarea
//	String txt = "";
//	Mensaje m;
//	TareaEjemplo te;
//	PuertoEntrada pe;
//	PuertoSalida ps;
//	Thread thr1, thr2, thr3;
//
//	//definición de tareas y conexión
//	pe = new PuertoEntrada(7777);
//	te = new TareaEjemplo(pe.getSlotSalida());
//	ps = new PuertoSalida("localhost", 8888, te.getSlotSalida(0));
//
//	//arranco las tareas
//	thr1 = new Thread(pe);
//	thr2 = new Thread(te);
//	thr3 = new Thread(ps);
//	thr1.start();
//	thr2.start();
//	thr3.start();
//
//	/*try {
//	    while (pe.abierto() && ps.abierto()) {			    //meter mensaje por pantalla, enviar "-1" para cerrar el sistema
//		m = salida.recibir();
//		txt = m.toString();
//		System.out.println(">" + txt);
//	    }
//	} catch (Exception ex) {
//	    if (!ex.getMessage().contains("cerrado")) {
//		throw ex;
//	    }
//	}*/
//
//	if (pe.abierto()) {
//	    pe.cerrar();
//	}
//	thr1.join(10000);
//	thr2.join(10000);
//	thr3.join(10000);
//	if (pe.abierto())
//	    pe.cerrar();
//	if (ps.abierto())
//	    ps.cerrar();
    }

}
