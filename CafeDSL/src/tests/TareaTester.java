package tests;

import java.io.IOException;
import java.util.Scanner;
import mensajeria.Mensaje;
import mensajeria.Slot;
import tareas.Tarea;

/**
 * Tester de tareas (Para ejecutar, Shift+F6)
 * ESTA CLASE NO DEBERÍA SER LA MAIN POR DEFECTO, COMPROBAR EN PROPIEDADES DEL PROYECTO
 * @author Francisco Javier Araujo Mendoza
 */
public class TareaTester {

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
     * Main para probar el funcionamiento de tareas, implementar según necesidad
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, Exception {

	String txt = "";
	Mensaje m1, m2;
	TareaEjemplo t1, t2;
	Thread thr1, thr2;

	Slot entrada, salida;	//slots para introducir datos y sacarlos de las tareas
	entrada = new Slot();
	
	//definición de tareas y conexión
	t1 = new TareaEjemplo(entrada);			//tarea 1, le paso el slot
	t2 = new TareaEjemplo(t1.getSlotSalida(0));	//tarea 2, conectada a la salida de tarea 1
	salida = t2.getSlotSalida(0);			//me quedo con la salida de tarea 2 para mostrarla
	
	//arranco las tareas
	thr1 = new Thread(t1);
	thr2 = new Thread(t2);
	thr1.start();
	thr2.start();

	Scanner in = new Scanner(System.in);
	while (!txt.equals("-1")) {			    //meter mensaje por pantalla, enviar "-1" para cerrar el sistema
	    txt = in.nextLine();
	    if (!txt.equals("-1"))
		m1 = new Mensaje(txt);
	    else
		m1 = new Mensaje(Mensaje.APAGAR_SISTEMA);
	    entrada.enviar(m1);				    //envía el mensaje por el slot a t1
	    m2 = salida.recibir();			    //espera a recibir la respuesta de t2
	    System.out.println(">" + m2.toString());	    //y la muestra por pantalla
	}

	t1.cerrar();					    //cerrar el sistema
	t2.cerrar();
	thr1.join(10000);
	thr2.join(10000);
    }

}
