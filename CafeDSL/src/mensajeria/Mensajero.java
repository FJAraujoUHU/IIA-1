package mensajeria;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tester/placeholder de conector de aplicaciones (Para ejecutar, Shift+F6)
 * ESTA CLASE NO DEBERÍA SER LA MAIN POR DEFECTO, COMPROBAR EN PROPIEDADES DEL PROYECTO
 * @author Francisco Javier Araujo Mendoza
 */
public class Mensajero {

    public static void main(String[] args) {
	int puerto = 7777;
	String host = "localhost";
	boolean cliente;

	String input;
	Scanner sc = new Scanner(System.in);

	System.out.println("Es cliente? (S/n):");
	input = sc.nextLine();
	cliente = !input.toUpperCase().equals("N");
	System.out.println("Puerto (7777):");
	input = sc.nextLine();
	if (!input.isBlank()) {
	    try {
		puerto = Integer.parseInt(input);
	    } catch (NumberFormatException numberFormatException) {
		System.out.println("!Puerto introducido inválido (" + input + "), usando puerto por defecto (7777)...");
		puerto = 7777;
	    }
	    if (puerto > 65535 || puerto < 1) {
		System.out.println("!Puerto introducido inválido (" + puerto + "), usando puerto por defecto (7777)...");
		puerto = 7777;
	    }
	}

	if (cliente) {   //Si es cliente
	    System.out.println("Host a conectar (localhost):");
	    input = sc.nextLine();
	    if (!input.isBlank()) {
		host = input;
	    }

	    System.out.println("Conectando a " + host + ":" + puerto + "...");

	    Socket socket = null;
	    try {
		socket = new Socket(host, puerto);
		System.out.println("Conectado, enviar \"-1\" para cerrar conexión");
		// get the output stream from the socket.
		OutputStream os = socket.getOutputStream();
		// create an object output stream from the output stream so we can send an object through it
		ObjectOutputStream oos = new ObjectOutputStream(os);

		input = "";
		while (!input.equals(Mensaje.APAGAR_SISTEMA)) {
		    input = sc.nextLine();
		    if (input.equals("-1")) input = Mensaje.APAGAR_SISTEMA;
		    Mensaje m = new Mensaje(input);
		    System.out.println("Enviando \"" + m + "\", ID=" + m.getIdInterna() + "...");
		    oos.writeObject(m);
		}
		System.out.println("Cerrando puerto y saliendo...");
		socket.close();
	    } catch (IOException ex) {
		System.out.println("!!!Error de conexión!!!");
		Logger.getLogger(Mensajero.class.getName()).log(Level.SEVERE, null, ex);
	    } finally {
		sc.close();
		if (socket != null)
		    if (!socket.isClosed()) {
			try {
			    socket.close();
			} catch (IOException ex) {
			    System.out.println("!!!Error al cerrar!!!");
			    Logger.getLogger(Mensajero.class.getName()).log(Level.SEVERE, null, ex);
			}
		    }
	    }
	} else {
	    ServerSocket ss = null;
	    Socket socket = null;
	    try {
		//Si es servidor
		sc.close();
		ss = new ServerSocket(puerto);
		System.out.println("Puerto esperando conexiones...");
		socket = ss.accept(); // Espera a que se establezca una conexión al puerto
		System.out.println("Conexión desde " + socket.getInetAddress().getCanonicalHostName() + ":" + socket.getPort() + "!");

		//Saca el stream de entrada desde el puerto
		InputStream is = socket.getInputStream();
		//Crea un objectInput para sacar mensajes desde ahí
		ObjectInputStream ois = new ObjectInputStream(is);
		
		Mensaje m;
		while (!input.equals(Mensaje.APAGAR_SISTEMA) && !socket.isClosed()) {
		    m = (Mensaje) ois.readObject();
		    System.out.println("Mensaje de " + socket.getInetAddress().getCanonicalHostName() + ":" + socket.getPort() + " ID=" + m.getIdInterna() + ":");
		    System.out.println(m);
		    input = m.toString();
		}
		

		System.out.println("Cerrando conexión y saliendo...");
		ss.close();
		socket.close();
	    } catch (IOException | ClassNotFoundException ex) {
		System.out.println("!!!Error de conexión!!!");
		Logger.getLogger(Mensajero.class.getName()).log(Level.SEVERE, null, ex);
	    } finally {
		try {
		    if (socket != null)
			if (!socket.isClosed())
			    socket.close();
		    if (ss != null)
			if (!ss.isClosed())
			    ss.close();
		} catch (IOException ex) {
		    System.out.println("!!!Error al cerrar!!!");
		    Logger.getLogger(Mensajero.class.getName()).log(Level.SEVERE, null, ex);
		}
	    }
	}
    }

}
