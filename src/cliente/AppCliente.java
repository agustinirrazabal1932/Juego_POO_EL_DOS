package cliente;

import ar.edu.unlu.juego.vista.Ivista;
import ar.edu.unlu.juego.vista.consola.ConsolaGrafica;
import ar.edu.unlu.rmimvc.cliente.Cliente;
import ar.edu.unlu.juego.vista.grafica.VistaGrafica;
import javax.swing.JOptionPane;
import java.rmi.RemoteException;

public class AppCliente {
    public static void main(String[] args) throws RemoteException {
        String[] opciones = {"Gráfica", "Consola"};
        String sel = (String) JOptionPane.showInputDialog(null, "Tipo de vista", "EL DOS",
                JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        Ivista vista = "Consola".equals(sel) ? new ConsolaGrafica() : new VistaGrafica();
        String port = JOptionPane.showInputDialog("Puerto del cliente", "9999");
        Cliente c = new Cliente("127.0.0.1", Integer.parseInt(port), "127.0.0.1", 8888);
        vista.mostrarMenuPrincipal();
        try {
            c.iniciar(vista.getControlador());
        } catch (Exception e) { e.printStackTrace(); }
    }
}