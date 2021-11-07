package tareas;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Scanner;
import mensajeria.Mensaje;

/**
 * Tester de tareas (Para ejecutar, Shift+F6)
 * ESTA CLASE NO DEBERÍA SER LA MAIN POR DEFECTO, COMPROBAR EN PROPIEDADES DEL PROYECTO
 * @author Francisco Javier Araujo Mendoza
 */
public class TareaTester {

    //Tarea genérica de un slot de entrada y otro de salida, que añade un "!" a lo que le entra y lo reenvía.
    private static class TareaEjemplo extends Tarea {
	public TareaEjemplo(PipedInputStream entradaTub) throws Exception {
	    super(new PipedInputStream[]{entradaTub}, 1, 1);
	}

	@Override
	public void run() {
	    Mensaje m;
	    String contenido = Mensaje.APAGAR_SISTEMA;

	    do {					    //ejecutarse hasta recibir la orden de apagado
		try {
		    m = leer(0);			    //La tarea se queda esperando a que le llegue un mensaje por el primer slot
		    contenido = m.toString();		    //Guarda el contenido en una string
		    m.setMensaje(contenido + "!");	   //Añade un carácter al mensaje
		    enviar(m, 0);			    //Reenvía el mensaje por el primer slot
		} catch (Exception ex) {
		    System.out.println(ex.toString());	    //si hay algún error, mostrarlo por pantalla y seguir ejecutando
		}
	    } while (!contenido.equals(Mensaje.APAGAR_SISTEMA));

	    cerrar();
	}
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, Exception {

	String txt = "";
	Mensaje m1, m2;
	TareaEjemplo t1, t2;

	PipedInputStream pipeIn = new PipedInputStream();
	PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);
	ObjectOutputStream out = new ObjectOutputStream(pipeOut);
	
	PipedInputStream pipeIn2;
	
	
	t1 = new TareaEjemplo(pipeIn);
	pipeIn2 = t1.getSlotSalida(0);
	t1.abrir();
	Thread thr = new Thread(t1);
	thr.start();

	Scanner in = new Scanner(System.in);
	ObjectInputStream ins = new ObjectInputStream(pipeIn2);
	while (!txt.equals("-1")) {
	    txt = in.nextLine();
	    if (!txt.equals("-1"))
		m1 = new Mensaje(txt);
	    else
		m1 = new Mensaje(Mensaje.APAGAR_SISTEMA);
	    out.writeObject(m1);
	    m2 = (Mensaje) ins.readObject();
	    System.out.println("+"+m2.toString());
	}

	t1.cerrar();
	thr.join(10000);
	pipeOut.flush();
	pipeOut.close();
    }

}
