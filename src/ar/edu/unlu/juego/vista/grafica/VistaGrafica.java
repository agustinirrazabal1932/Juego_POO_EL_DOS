package ar.edu.unlu.juego.vista.grafica;

import ar.edu.unlu.juego.controlador.Controlador;
import ar.edu.unlu.juego.modelo.excepciones.ContrasenaIncorrecta;
import ar.edu.unlu.juego.modelo.excepciones.JugadorExistente;
import ar.edu.unlu.juego.modelo.excepciones.JugadorNoExistente;
import ar.edu.unlu.juego.vista.Ivista;
import ar.edu.unlu.juego.vista.grafica.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import java.awt.*;

public class VistaGrafica implements Ivista {
    private final JFrame frame;
    private final JPanel contenedor;
    private final CardLayout cardLayout;   // permite cambiar de "pantalla" (login/menu/...)
    private Controlador controlador;

    private JTextField txtId;
    private JPasswordField txtPass;
    private DefaultListModel<String> modeloDisponibles = new DefaultListModel<>();
    private DefaultListModel<String> modeloGuardadas = new DefaultListModel<>();
    private JLabel lblEspera;


    // Paneles dinámicos de la mesa (se repintan en mostrarPartida)
    private JPanel panelMesaContenido;
    private JPanel panelManoContenido;
    private JLabel lblTurnoInfo;
    private JLabel lblEstadoMesa;
    private JButton bSimple, bDoble, bTomar, bPaso, bDecirDos;
    private JLabel lblFin;
    private JButton bFinMenu;
    // Cache de imágenes (no releer del disco en cada refresh)
    private final Map<String, ImageIcon> cacheCartas = new HashMap<>();

    // Máquina de estados del turno actual
    private enum ModoMesa {
        IDLE, SIMPLE_ELIGE_MANO, SIMPLE_ELIGE_MESA,
        DOBLE_ELIGE_MANO1, DOBLE_ELIGE_MANO2, DOBLE_ELIGE_MESA,
        ELIGE_BOCA_ARRIBA
    }

    private ModoMesa modoMesa = ModoMesa.IDLE;
    private int cartaElegida1 = -1;
    private int cartaElegida2 = -1;
    private boolean juegoSimpleFull = false;
    private boolean juegoDobleFull = false;
    private boolean tomarCarta = false;
    private boolean juegoSimple = false;
    private boolean juegoDoble = false;


    public VistaGrafica() {
        frame = new JFrame("EL DOS");
        frame.setSize(780, 580);
        frame.setLocationRelativeTo(null);
        // al cerrar: desregistro del server (igual que la consola) y salgo
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (controlador != null) controlador.cerrar();
                System.exit(0);
            }
        });

        cardLayout = new CardLayout();
        contenedor = new JPanel(cardLayout);
        contenedor.add(crearPanelLogin(), "login");
        contenedor.add(crearPanelMenu(), "menu");
        contenedor.add(crearPanelLobby(), "lobby");
        contenedor.add(crearPanelEspera(), "espera");
        contenedor.add(crearPanelMesa(), "mesa");
        contenedor.add(crearPanelFin(), "fin");
        frame.setContentPane(contenedor);

        new Controlador(this);   // se autorregistra vía setControlador(this)
    }

    private void mostrar(String panel) {
        SwingUtilities.invokeLater(() -> cardLayout.show(contenedor, panel));
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(frame, msg);
    }

    // ===== Ivista =====
    @Override
    public void setControlador(Controlador c) {
        this.controlador = c;
    }

    @Override
    public Controlador getControlador() {
        return controlador;
    }

    @Override
    public void mostrarMenuPrincipal() {
        frame.setVisible(true);
        mostrar("login");
    }

    @Override
    public void actualizarLobby() {
        SwingUtilities.invokeLater(() -> {
            refrescarDisponibles();
            refrescarGuardadas();
        });
    }

    @Override
    public void esperandoJugadores() {
        SwingUtilities.invokeLater(() -> {
            lblEspera.setText(controlador.estoyEsperandoReconexion()
                    ? "Esperando que vuelva el otro jugador..."
                    : "Esperando que se una alguien...");
            mostrar("espera");
        });
    }

    @Override
    public void mostrarPartida() {
        SwingUtilities.invokeLater(() -> {
            if (controlador.getIdPartidaActual() == -1) return;  // partida ya terminada/cerrada
            String yo = controlador.devolverID();
            boolean miTurno = controlador.esMiTurno();

            // — Info de turno y puntajes —
            StringBuilder info = new StringBuilder("<html><center>");
            int total = controlador.jugadoresPartida();
            for (int i = 0; i < total; i++) {
                info.append(controlador.jugadorNombrePartida(i))
                        .append(": ").append(controlador.jugadorPuntosPartida(i)).append(" pts &nbsp;&nbsp;");
            }
            info.append(miTurno ? "— <b>TU TURNO</b>" : "— Turno del oponente");
            info.append("</center></html>");
            lblTurnoInfo.setText(info.toString());

            // — Cartas de la mesa —
            panelMesaContenido.removeAll();
            List<String> clavesMesa = controlador.cartasMesaImagenes();
            for (int i = 0; i < clavesMesa.size(); i++) {
                final int idx = i + 1;
                JButton btn = new JButton(getCartaIcon(clavesMesa.get(i)));
                btn.setToolTipText(clavesMesa.get(i));
                btn.setBorderPainted(true);
                btn.setContentAreaFilled(false);
                btn.addActionListener(e -> clickMesa(idx));
                panelMesaContenido.add(btn);
            }
            panelMesaContenido.revalidate();
            panelMesaContenido.repaint();

            // — Tu mano —
            panelManoContenido.removeAll();
            List<String> clavesMano = controlador.manoJugadorImagenes(yo);
            for (int i = 0; i < clavesMano.size(); i++) {
                final int idx = i + 1;
                JButton btn = new JButton(getCartaIcon(clavesMano.get(i)));
                btn.setToolTipText(clavesMano.get(i));
                btn.setBorderPainted(true);
                btn.setContentAreaFilled(false);
                btn.addActionListener(e -> clickMano(idx));
                panelManoContenido.add(btn);
            }
            panelManoContenido.revalidate();
            panelManoContenido.repaint();

            // — Habilitar/deshabilitar botones —
            boolean puedeActuar = miTurno && modoMesa == ModoMesa.IDLE;
            bSimple.setEnabled(puedeActuar);
            bDoble.setEnabled(puedeActuar);
            bTomar.setEnabled(puedeActuar && !juegoSimpleFull && !juegoDobleFull && !tomarCarta);
            bPaso.setEnabled(puedeActuar);
            bDecirDos.setEnabled(miTurno && controlador.cantidadCartaJugador(yo) <= 2);

            if (!miTurno) {
                resetEstadoMesa();
                lblEstadoMesa.setText("<html><center>Esperá tu turno</center></html>");
            } else if (modoMesa == ModoMesa.IDLE) {
                lblEstadoMesa.setText("<html><center>Elegí una acción</center></html>");
            }

            mostrar("mesa");
        });
    }

    @Override
    public void terminoPartida(String idGanador) {
        SwingUtilities.invokeLater(() -> {
            resetEstadoMesa();
            bFinMenu.setVisible(false);
            lblFin.setText("<html><center>Perdiste la ronda.<br>Ganador: <b>" + idGanador + "</b><br>Esperando siguiente ronda...</center></html>");
            mostrar("fin");
        });
    }

    @Override
    public void terminoJuego(String idGanador) {
        SwingUtilities.invokeLater(() -> {
            resetEstadoMesa();
            bFinMenu.setVisible(true);
            lblFin.setText("<html><center>¡Fin del juego!<br>Ganador: <b>" + idGanador + "</b></center></html>");
            mostrar("fin");
        });
    }

    @Override
    public void oponenteSeFue() {
        SwingUtilities.invokeLater(() -> {
            resetEstadoMesa();
            bFinMenu.setVisible(true);
            lblFin.setText("<html><center>El oponente se desconectó.<br>La partida quedó guardada.</center></html>");
            mostrar("fin");
        });
    }

    // ===== Panel LOGIN =====
    private JPanel crearPanelLogin() {
//        JPanel p = new JPanel(new GridBagLayout());
        PanelFondo p = new PanelFondo("src/ar/edu/unlu/juego/vista/grafica/recursos/fondoJuegoDos.png");
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        JLabel titulo = new JLabel("LOGIN");
        titulo.setFont(new Font("Impact", Font.PLAIN, 42));
        titulo.setForeground(new Color(10, 0, 0, 255));
        titulo.setOpaque(false);
        p.add(titulo, c);

        c.gridwidth = 1;
        c.gridy = 1;
        c.gridx = 0;
        p.add(new JLabel(cargarIcono("src/ar/edu/unlu/juego/vista/grafica/recursos/icono_user.png", 50)), c);
        c.gridx = 1;
        txtId = new JTextField(14);
        p.add(txtId, c);
        c.gridy = 2;
        c.gridx = 0;
        p.add(new JLabel(cargarIcono("src/ar/edu/unlu/juego/vista/grafica/recursos/icono_pass.png", 50)), c);
        c.gridx = 1;
        txtPass = new JPasswordField(14);
        p.add(txtPass, c);

        c.gridy = 3;
        c.gridx = 0;
        JButton bLogin = new BotonEstilo("Iniciar sesión", new Color(40, 160, 70));
        bLogin.addActionListener(e -> iniciarSesion());
        p.add(bLogin, c);
        c.gridx = 1;
        JButton bReg = new BotonEstilo("Registrarse", new Color(40, 100, 200));
        bReg.addActionListener(e -> registrarse());
        p.add(bReg, c);
        return p;
    }

    private void iniciarSesion() {
        String id = txtId.getText().trim();
        String pass = new String(txtPass.getPassword());
        if (id.isEmpty()) {
            aviso("Ingresá un ID.");
            return;
        }
        if (!controlador.encontrarJugador(id)) {
            aviso("El ID no está registrado.");
            return;
        }
        try {
            controlador.iniciarSesion(id, pass);
            controlador.agregarID(id);
            frame.setTitle("EL DOS — " + id);
            mostrar("menu");
        } catch (ContrasenaIncorrecta e) {
            aviso("Contraseña incorrecta.");
        } catch (JugadorNoExistente e) {
            aviso("El ID no está registrado.");
        }
    }

    private void registrarse() {
        String id = txtId.getText().trim();
        String pass = new String(txtPass.getPassword());
        if (id.isEmpty() || pass.isEmpty()) {
            aviso("Completá ID y contraseña.");
            return;
        }
        String nombre = JOptionPane.showInputDialog(frame, "Nombre:");
        if (nombre == null || nombre.trim().isEmpty()) return;
        String apellido = JOptionPane.showInputDialog(frame, "Apellido:");
        if (apellido == null || apellido.trim().isEmpty()) return;
        try {
            controlador.cargarUsuario(nombre.trim(), apellido.trim(), id, pass);
            aviso("Registrado. Ahora iniciá sesión.");
        } catch (JugadorExistente e) {
            aviso("Ese ID ya existe.");
        }
    }

    // ===== Panel MENU =====
    private JPanel crearPanelMenu() {
        PanelFondo p = new PanelFondo("src/ar/edu/unlu/juego/vista/grafica/recursos/fondoJuegoDos.png");
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);

        c.gridx = 0;
        c.gridy = 0;
        JLabel t = new JLabel("MENÚ PRINCIPAL");
        t.setFont(new Font("Impact", Font.PLAIN, 42));
        t.setForeground(new Color(10, 0, 0, 255));
        t.setOpaque(false);
        p.add(t, c);

        c.gridy = 1;
        JButton bJugar = new BotonEstilo("Jugar", new Color(40, 160, 70));
        bJugar.addActionListener(e -> {
            refrescarDisponibles();
            refrescarGuardadas();
            mostrar("lobby");
        });
        p.add(bJugar, c);

        c.gridy = 2;
        JButton bRank = new BotonEstilo("Ranking", new Color(40, 100, 200));
        bRank.addActionListener(e -> mostrarRanking());
        p.add(bRank, c);

        c.gridy = 3;
        JButton bSalir = new BotonEstilo("Salir", new Color(200, 60, 60));
        bSalir.addActionListener(e -> {
            if (controlador != null) controlador.cerrar();
            System.exit(0);
        });
        p.add(bSalir, c);
        return p;
    }

    private void mostrarRanking() {
        StringBuilder sb = new StringBuilder();
        for (String linea : controlador.ranking()) sb.append(linea).append("\n");
        if (sb.length() == 0) sb.append("(sin jugadores)");
        JOptionPane.showMessageDialog(frame, sb.toString(), "Ranking", JOptionPane.INFORMATION_MESSAGE);
    }



    private JPanel crearPanelLobby() {
        PanelFondo p = new PanelFondo("src/ar/edu/unlu/juego/vista/grafica/recursos/fondoJuegoDos.png");
        p.setLayout(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // — Encabezado —
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        header.setOpaque(false);
        JLabel titulo = new JLabel("LOBBY");
        titulo.setFont(new Font("Impact", Font.PLAIN, 42));
        titulo.setForeground(new Color(10, 0, 0, 255));
        titulo.setOpaque(false);
        header.add(titulo);
        JButton bVolver = new BotonEstilo("← Menú", new Color(110, 110, 110));
        bVolver.addActionListener(e -> mostrar("menu"));
        header.add(bVolver);
        p.add(header, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(1, 3, 8, 0));
        centro.setOpaque(false);

        // — Columna 1: Crear —
        JPanel pCrear = panelSeccion(new GridBagLayout());
        pCrear.setBorder(tituloBorde("Nueva partida"));
        JButton bCrear = new BotonEstilo("Crear (2 jugadores)", new Color(40, 130, 200));
        bCrear.addActionListener(e -> {
            controlador.crearPartida(2);
            mostrar("espera");
        });
        pCrear.add(bCrear);
        centro.add(pCrear);

        // — Columna 2: Unirse —
        JPanel pUnirse = panelSeccion(new BorderLayout(4, 4));
        pUnirse.setBorder(tituloBorde("Unirse a partida"));
        JList<String> listaDisp = new JList<>(modeloDisponibles);
        listaDisp.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pUnirse.add(new JScrollPane(listaDisp), BorderLayout.CENTER);
        JPanel botonesU = new JPanel(new GridLayout(1, 2, 4, 0));
        botonesU.setOpaque(false);
        JButton bActDisp = new BotonEstilo("Actualizar", new Color(110, 110, 110));
        bActDisp.addActionListener(e -> refrescarDisponibles());
        JButton bUnirse = new BotonEstilo("Unirse", new Color(40, 160, 70));
        bUnirse.addActionListener(e -> {
            String sel = listaDisp.getSelectedValue();
            if (sel == null) { aviso("Elegí una partida."); return; }
            int id = parsearId(sel);
            if (!controlador.puedoUnirmeAPartida(id)) { aviso("No podés unirte (ya está llena o no existe)."); return; }
            controlador.unirseAPartida(id);
        });
        botonesU.add(bActDisp);
        botonesU.add(bUnirse);
        pUnirse.add(botonesU, BorderLayout.SOUTH);
        centro.add(pUnirse);

        // — Columna 3: Reanudar —
        JPanel pReanudar = panelSeccion(new BorderLayout(4, 4));
        pReanudar.setBorder(tituloBorde("Reanudar guardada"));
        JList<String> listaGuard = new JList<>(modeloGuardadas);
        listaGuard.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pReanudar.add(new JScrollPane(listaGuard), BorderLayout.CENTER);
        JPanel botonesR = new JPanel(new GridLayout(1, 2, 4, 0));
        botonesR.setOpaque(false);
        JButton bActGuard = new BotonEstilo("Actualizar", new Color(110, 110, 110));
        bActGuard.addActionListener(e -> refrescarGuardadas());
        JButton bReanudar = new BotonEstilo("Reanudar", new Color(200, 130, 40));
        bReanudar.addActionListener(e -> {
            String sel = listaGuard.getSelectedValue();
            if (sel == null) { aviso("Elegí una partida."); return; }
            int id = parsearId(sel);
            if (!controlador.puedoReanudar(id)) { aviso("No podés reanudar esa partida."); return; }
            controlador.reanudarPartida(id);
            mostrar("espera");
        });
        botonesR.add(bActGuard);
        botonesR.add(bReanudar);
        pReanudar.add(botonesR, BorderLayout.SOUTH);
        centro.add(pReanudar);

        p.add(centro, BorderLayout.CENTER);
        return p;
    }

    private javax.swing.border.TitledBorder tituloBorde(String texto) {
        javax.swing.border.TitledBorder b = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 160)), texto);
        b.setTitleFont(new Font("Impact", Font.PLAIN, 20));
        b.setTitleColor(Color.WHITE);
        return b;
    }

    private JPanel panelSeccion(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 110));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    private JPanel crearPanelEspera() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12, 12, 12, 12);

        c.gridx = 0; c.gridy = 0;
        lblEspera = new JLabel("Esperando jugadores...", SwingConstants.CENTER);
        lblEspera.setFont(new Font("Arial", Font.BOLD, 20));
        p.add(lblEspera, c);

        c.gridy = 1;
        JButton bCancelar = new BotonEstilo("Cancelar / Volver", new Color(200, 60, 60));
        bCancelar.addActionListener(e -> {
            if (controlador.estoyEsperandoRival()) {
                controlador.cancelarPartida();
            } else if (controlador.estoyEsperandoReconexion()) {
                controlador.salirDePartida();
            }
            refrescarDisponibles();
            refrescarGuardadas();
            mostrar("lobby");
        });
        p.add(bCancelar, c);
        return p;
    }

    private JPanel crearPanelMesa() {
        PanelFondo p = new PanelFondo("src/ar/edu/unlu/juego/vista/grafica/recursos/fondoJuegoDos.png");
        p.setLayout(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // — NORTH: info de turno —
        lblTurnoInfo = new JLabel("", SwingConstants.CENTER);
        lblTurnoInfo.setFont(new Font("Impact", Font.PLAIN, 16));
        lblTurnoInfo.setForeground(Color.WHITE);
        lblTurnoInfo.setOpaque(false);
        p.add(lblTurnoInfo, BorderLayout.NORTH);

        // — CENTER: cartas boca arriba (mesa) —
        panelMesaContenido = panelSeccion(new FlowLayout(FlowLayout.CENTER, 6, 6));
        panelMesaContenido.setBorder(tituloBorde("Mesa (cartas boca arriba)"));
        JScrollPane mesaScroll = new JScrollPane(panelMesaContenido);
        mesaScroll.setOpaque(false);
        mesaScroll.getViewport().setOpaque(false);
        mesaScroll.setBorder(null);
        p.add(mesaScroll, BorderLayout.CENTER);

        // — EAST: botones de acción —
        JPanel pBotones = panelSeccion(null);
        pBotones.setLayout(new BoxLayout(pBotones, BoxLayout.Y_AXIS));
        pBotones.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        bSimple   = new BotonEstilo("Juego Simple", new Color(40, 130, 200));
        bDoble    = new BotonEstilo("Juego Doble",  new Color(120, 60, 160));
        bTomar    = new BotonEstilo("Tomar Carta",  new Color(200, 130, 40));
        bPaso     = new BotonEstilo("Paso",         new Color(110, 110, 110));
        bDecirDos = new BotonEstilo("¡Decir DOS!",  new Color(200, 60, 60));

        bSimple.addActionListener(e -> accionSimple());
        bDoble.addActionListener(e -> accionDoble());
        bTomar.addActionListener(e -> accionTomarCarta());
        bPaso.addActionListener(e -> accionPaso());
        bDecirDos.addActionListener(e -> accionDecirDos());

        lblEstadoMesa = new JLabel("<html><center>Esperá tu turno</center></html>", SwingConstants.CENTER);
        lblEstadoMesa.setFont(new Font("Arial", Font.PLAIN, 12));
        lblEstadoMesa.setForeground(Color.WHITE);
        lblEstadoMesa.setOpaque(false);

        for (JButton b : new JButton[]{bSimple, bDoble, bTomar, bPaso, bDecirDos}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setMaximumSize(new Dimension(140, 36));
            pBotones.add(b);
            pBotones.add(Box.createVerticalStrut(6));
        }
        pBotones.add(Box.createVerticalStrut(10));
        lblEstadoMesa.setAlignmentX(Component.CENTER_ALIGNMENT);
        pBotones.add(lblEstadoMesa);
        p.add(pBotones, BorderLayout.EAST);

        // — SOUTH: tu mano —
        panelManoContenido = panelSeccion(new FlowLayout(FlowLayout.CENTER, 6, 6));
        panelManoContenido.setBorder(tituloBorde("Tu mano"));
        JScrollPane manoScroll = new JScrollPane(panelManoContenido);
        manoScroll.setOpaque(false);
        manoScroll.getViewport().setOpaque(false);
        manoScroll.setBorder(null);
        manoScroll.setPreferredSize(new Dimension(0, 135));
        p.add(manoScroll, BorderLayout.SOUTH);

        return p;
    }

    private JPanel crearPanelFin() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12, 12, 12, 12);

        c.gridx = 0; c.gridy = 0;
        lblFin = new JLabel("", SwingConstants.CENTER);
        lblFin.setFont(new Font("Arial", Font.BOLD, 24));
        p.add(lblFin, c);

        c.gridy = 1;
        bFinMenu = new BotonEstilo("Volver al menú", new Color(40, 130, 200));
        bFinMenu.addActionListener(e -> mostrar("menu"));
        p.add(bFinMenu, c);
        return p;
    }


    private void refrescarDisponibles() {
        modeloDisponibles.clear();
        for (String s : controlador.partidasDisponibles()) modeloDisponibles.addElement(s);
    }

    private void refrescarGuardadas() {
        modeloGuardadas.clear();
        for (String s : controlador.partidasGuardadas()) modeloGuardadas.addElement(s);
    }

    // Las líneas tienen formato "ID 3 - creada por ..."
    private int parsearId(String linea) {
        return Integer.parseInt(linea.split("\\s+")[1]);
    }

    private ImageIcon getCartaIcon(String clave) {
        return cacheCartas.computeIfAbsent(clave, k -> {
            String path = "src/ar/edu/unlu/juego/vista/grafica/recursos/cartas/" + k + ".png";
            Image img = new ImageIcon(path).getImage().getScaledInstance(66, 93, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        });
    }

    private void clickMano(int idx) {
        String yo = controlador.devolverID();
        switch (modoMesa) {
            case SIMPLE_ELIGE_MANO -> {
                cartaElegida1 = idx;
                modoMesa = ModoMesa.SIMPLE_ELIGE_MESA;
                lblEstadoMesa.setText("<html><center>Ahora elegí<br>una carta de la MESA</center></html>");
            }
            case DOBLE_ELIGE_MANO1 -> {
                cartaElegida1 = idx;
                modoMesa = ModoMesa.DOBLE_ELIGE_MANO2;
                lblEstadoMesa.setText("<html><center>Elegí la carta 2<br>de tu mano</center></html>");
            }
            case DOBLE_ELIGE_MANO2 -> {
                if (idx == cartaElegida1) { aviso("Ya elegiste esa carta."); return; }
                cartaElegida2 = idx;
                modoMesa = ModoMesa.DOBLE_ELIGE_MESA;
                lblEstadoMesa.setText("<html><center>Ahora elegí<br>una carta de la MESA</center></html>");
            }
            case ELIGE_BOCA_ARRIBA -> {
                controlador.rellenarCartaBocaArriba();
                controlador.ponerBocaArriba(idx, yo);
                if (controlador.cantidadCartaJugador(yo) == 0) {
                    accionFinJuego();
                } else {
                    accionFinTurno();
                }
            }
            default -> {} // clics en mano ignorados si no se esperan
        }
    }

    private void clickMesa(int idx) {
        String yo = controlador.devolverID();
        switch (modoMesa) {
            case SIMPLE_ELIGE_MESA -> {
                boolean valido = controlador.validarCombinacionSimple(yo, cartaElegida1, idx);
                if (!valido) {
                    aviso("Combinación inválida. Intentá de nuevo.");
                    modoMesa = ModoMesa.IDLE;
                    lblEstadoMesa.setText("<html><center>Elegí una acción</center></html>");
                    return;
                }
                boolean conColor = controlador.validarColor(yo, cartaElegida1, idx);
                controlador.eliminarCartaUsadas(yo, cartaElegida1, idx);
                if (conColor) juegoSimpleFull = true; else juegoSimple = true;
                modoMesa = ModoMesa.IDLE;
                lblEstadoMesa.setText(conColor
                        ? "<html><center>¡Simple con color!<br>No podés tomar. Elegí Paso</center></html>"
                        : "<html><center>¡Jugada simple!<br>Podés seguir o elegí Paso</center></html>");
            }
            case DOBLE_ELIGE_MESA -> {
                boolean esComodin = controlador.validarSiEsComodin(idx);
                int numComodin = 0;
                if (esComodin) {
                    String input = JOptionPane.showInputDialog(frame,
                            "La carta de la mesa es comodín.\nIngresá un número del 1 al 10:");
                    if (input == null) { modoMesa = ModoMesa.IDLE; return; }
                    try {
                        numComodin = Integer.parseInt(input.trim());
                    } catch (NumberFormatException e) {
                        aviso("Número inválido."); modoMesa = ModoMesa.IDLE; return;
                    }
                    if (numComodin < 1 || numComodin > 10) {
                        aviso("El número debe estar entre 1 y 10."); modoMesa = ModoMesa.IDLE; return;
                    }
                }
                boolean valido = controlador.validarCombinacionDoble(esComodin, yo,
                        cartaElegida1, cartaElegida2, idx, numComodin);
                if (!valido) {
                    aviso("Combinación inválida. Intentá de nuevo.");
                    modoMesa = ModoMesa.IDLE;
                    lblEstadoMesa.setText("<html><center>Elegí una acción</center></html>");
                    return;
                }
                boolean conColor = controlador.validarColorDoble(yo, cartaElegida1, cartaElegida2, idx);
                controlador.eliminarCartaUsadasDoble(yo, cartaElegida1, cartaElegida2, idx);
                if (conColor) juegoDobleFull = true; else juegoDoble = true;
                modoMesa = ModoMesa.IDLE;
                lblEstadoMesa.setText(conColor
                        ? "<html><center>¡Doble con color!<br>No podés tomar. Elegí Paso</center></html>"
                        : "<html><center>¡Jugada doble!<br>Podés seguir o elegí Paso</center></html>");
            }
            default -> {} // clics en mesa ignorados si no se esperan
        }
    }

    private void accionSimple() {
        modoMesa = ModoMesa.SIMPLE_ELIGE_MANO;
        lblEstadoMesa.setText("<html><center>Elegí una carta<br>de tu MANO</center></html>");
    }

    private void accionDoble() {
        modoMesa = ModoMesa.DOBLE_ELIGE_MANO1;
        lblEstadoMesa.setText("<html><center>Elegí la carta 1<br>de tu mano</center></html>");
    }

    private void accionTomarCarta() {
        if (tomarCarta || juegoSimpleFull || juegoDobleFull) {
            aviso("Ya hiciste un juego o tomaste una carta."); return;
        }
        controlador.tomarCartaMazo(controlador.devolverID());
        tomarCarta = true;
        // ACTUALIZACION_JUEGO dispara mostrarPartida() automáticamente
    }

    private void accionDecirDos() {
        controlador.decirDos(controlador.devolverID());
        aviso("¡Dijiste DOS!");
    }

    private void accionPaso() {
        String yo = controlador.devolverID();
        if (!juegoSimple && !juegoSimpleFull && !juegoDoble && !juegoDobleFull && !tomarCarta) {
            aviso("Primero hacé un juego o tomá una carta."); return;
        }
        if (juegoDobleFull) controlador.hiceJuegoDoble(yo);

        if (controlador.cantidadCartaJugador(yo) == 0) {
            accionFinJuego(); return;
        }
        if (juegoSimpleFull || juegoDobleFull) {
            modoMesa = ModoMesa.ELIGE_BOCA_ARRIBA;
            lblEstadoMesa.setText("<html><center>Elegí una carta<br>para poner boca arriba</center></html>");
            return;
        }
        accionFinTurno();
    }

    private void accionFinTurno() {
        String yo = controlador.devolverID();
        resetEstadoMesa();
        controlador.rellenarCartaBocaArriba();
        if (controlador.penalizarSiNoDijoDos(yo)) {
            aviso("¡No dijiste DOS! Se te suman 2 cartas.");
        }
        controlador.terminoTurno(yo);
        // CAMBIO_TURNO dispara mostrarPartida() para ambos jugadores
    }

    private void accionFinJuego() {
        String yo = controlador.devolverID();
        boolean ganoTodo = controlador.tareasDeFinDePartida(yo);
        if (ganoTodo) {
            resetEstadoMesa();
            SwingUtilities.invokeLater(() -> {
                bFinMenu.setVisible(true);
                lblFin.setText("<html><center>¡GANASTE EL JUEGO!<br>Felicitaciones, " + yo + "</center></html>");
                mostrar("fin");
            });
        } else {
            // Ganó la ronda, pero no el juego completo → nueva ronda
            resetEstadoMesa();
            controlador.Partida2Jugadores();
            mostrarPartida();  // reiniciarPartida no dispara eventos, refrescar manualmente
        }
    }

    private void resetEstadoMesa() {
        modoMesa      = ModoMesa.IDLE;
        cartaElegida1 = -1;
        cartaElegida2 = -1;
        juegoSimpleFull = false;
        juegoDobleFull  = false;
        tomarCarta      = false;
        juegoSimple     = false;
        juegoDoble      = false;
    }

    private ImageIcon cargarIcono(String ruta, int size) {
        Image img = new ImageIcon(ruta).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }






}
