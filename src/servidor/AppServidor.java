package servidor;

import ar.edu.unlu.juego.modelo.Modelo;
import ar.edu.unlu.juego.modelo.interfaces.IModelo;
import ar.edu.unlu.rmimvc.servidor.Servidor;
import java.rmi.RemoteException;

public class AppServidor {
    public static void main(String[] args) throws RemoteException {
        IModelo modelo = Modelo.getInstancia();
        Servidor servidor = new Servidor("127.0.0.1", 8888);
        try {
            servidor.iniciar(modelo);
        } catch (Exception e) { e.printStackTrace(); }
    }
}