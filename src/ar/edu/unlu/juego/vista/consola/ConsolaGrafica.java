package ar.edu.unlu.juego.vista.consola;


import ar.edu.unlu.juego.controlador.Controlador;
import ar.edu.unlu.juego.vista.Ivista;

import javax.swing.*;

public class ConsolaGrafica implements Ivista {
    private final JFrame frame;
    private JPanel contentPane;
    private JTextArea txtSalidaConsola;
    private JTextField txtEntrada;
    private JButton btnEnter;
    private Controlador controlador;

    private Flujo flujoActual;

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public ConsolaGrafica() {
        frame = new JFrame("EL DOS");
        frame.setContentPane(contentPane);
        // al cerrar: primero desregistro el observador del servidor, después salgo
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (controlador != null) controlador.cerrar();
                System.exit(0);
            }
        });
        frame.pack();

        btnEnter.addActionListener(e -> enviar());
        txtEntrada.addActionListener(e -> enviar()); // Enter en el campo = apretar el botón

        // crea y autoregistra el controlador: el Controlador llama a setControlador(this)
        new Controlador(this);
    }

    private void enviar() {
        println(txtEntrada.getText());
        procesarEntrada(txtEntrada.getText());
        txtEntrada.setText("");
    }

    private void procesarEntrada(String input) {
        input = input.trim();
        flujoActual = flujoActual.procesarEntrada(input);
        flujoActual.mostarSiguienteTexto();
    }

    public void println(String texto) {
        txtSalidaConsola.append(texto + "\n");
        // mueve el cursor al final para que el scroll baje solo
        txtSalidaConsola.setCaretPosition(txtSalidaConsola.getDocument().getLength());
    }

    public void mostrar() {
        frame.setVisible(true);
    }

    @Override
    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    @Override
    public Controlador getControlador() {
        return controlador;
    }

    @Override
    public void mostrarMenuPrincipal() {
        mostrar();
        flujoActual = new FlujoMenuInicial(this, controlador);
        flujoActual.mostarSiguienteTexto();
    }

    // ===== métodos llamados por Controlador.actualizar(...) (corren en hilo RMI) =====
    // Los envolvemos en invokeLater para tocar Swing desde el EDT.

    @Override
    public void actualizarLobby() {
        // Fase 1 con auto-start: no hace falta refrescar el lobby en vivo.
        // La lista de partidas se pide on-demand al elegir "unirse".
    }

    @Override
    public void esperandoJugadores() {
        SwingUtilities.invokeLater(() -> {
            if (!(flujoActual instanceof FlujoEsperar)) {
                flujoActual = new FlujoEsperar(this, controlador);
                flujoActual.mostarSiguienteTexto();
            }
        });
    }

    @Override
    public void mostrarPartida() {
        SwingUtilities.invokeLater(() -> {
            if (controlador.esMiTurno()) {
                // Si ya estoy jugando (a mitad de turno) NO reinicio el flujo:
                // las ACTUALIZACION_JUEGO que disparan mis propias jugadas se ignoran.
                if (!(flujoActual instanceof FlujoJugador1)) {
                    String yo = controlador.devolverID();
                    flujoActual = new FlujoJugador1(this, controlador, yo, yo);
                    flujoActual.mostarSiguienteTexto();
                }
            } else {
                if (!(flujoActual instanceof FlujoEsperar)) {
                    flujoActual = new FlujoEsperar(this, controlador);
                    flujoActual.mostarSiguienteTexto();
                }
            }
        });
    }

    @Override
    public void terminoJuego(String idGanador) {
        SwingUtilities.invokeLater(() -> {
            flujoActual = new FlujoPerdi(this, controlador, idGanador, "juego");
            flujoActual.mostarSiguienteTexto();
        });
    }

    @Override
    public void terminoPartida(String idJugador) {
        SwingUtilities.invokeLater(() -> {
            flujoActual = new FlujoPerdi(this, controlador, idJugador, "partida");
            flujoActual.mostarSiguienteTexto();
        });
    }

    @Override
    public void oponenteSeFue() {
        SwingUtilities.invokeLater(() -> {
            println("----- EL OTRO JUGADOR SE FUE. LA PARTIDA QUEDÓ GUARDADA. -----");
            flujoActual = new FlujoMenuPrincipal(this, controlador, controlador.devolverID());
            flujoActual.mostarSiguienteTexto();
        });
    }
}
