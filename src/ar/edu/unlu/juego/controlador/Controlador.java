package ar.edu.unlu.juego.controlador;

import ar.edu.unlu.juego.modelo.Carta;
import ar.edu.unlu.juego.modelo.Jugador;
import ar.edu.unlu.juego.modelo.ManejadorEventos;
import ar.edu.unlu.juego.modelo.Partida;
import ar.edu.unlu.juego.modelo.enums.EstadoPartida;
import ar.edu.unlu.juego.modelo.excepciones.ContrasenaIncorrecta;
import ar.edu.unlu.juego.modelo.excepciones.JugadorExistente;
import ar.edu.unlu.juego.modelo.excepciones.JugadorNoExistente;
import ar.edu.unlu.juego.modelo.interfaces.IModelo;
import ar.edu.unlu.juego.vista.Ivista;
import ar.edu.unlu.rmimvc.cliente.IControladorRemoto;
import ar.edu.unlu.rmimvc.observer.IObservableRemoto;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class Controlador implements IControladorRemoto {
    private Ivista vistaJuego;
    private IModelo modelo;
    private int idPartidaActual = -1;
    private String idJugador = "";

    public Controlador(Ivista vista) {
        this.vistaJuego = vista;
        vista.setControlador(this);   // la vista queda con this; getControlador() lo devuelve
    }

    @Override
    public <T extends IObservableRemoto> void setModeloRemoto(T modeloRemoto) throws RemoteException {
        this.modelo = (IModelo) modeloRemoto;
    }

    public int getIdPartidaActual() { return idPartidaActual; }

    // ===== identidad del jugador (lo setea el login) =====
    public void agregarID(String id) { this.idJugador = id; }
    public String devolverID() { return idJugador; }

    // ===== usuarios =====
    public void cargarUsuario(String n, String a, String id, String password) throws JugadorExistente {
        try { modelo.cargarUsuario(n, a, id, password); }
        catch (RemoteException e) { e.printStackTrace(); }
    }

    public void iniciarSesion(String id, String password) throws JugadorNoExistente, ContrasenaIncorrecta {
        try { modelo.iniciarSesion(id, password); }
        catch (RemoteException e) { e.printStackTrace(); }
    }

    public List<String> jugadoresRegistrados() {
        List<String> res = new ArrayList<>();
        try { for (Jugador j : modelo.verTodosLosJugadores()) res.add(j.toString()); }
        catch (RemoteException e) { e.printStackTrace(); }
        return res;
    }

    public List<String> ranking() {
        List<String> res = new ArrayList<>();
        try {
            ArrayList<Jugador> jugs = new ArrayList<>(modelo.verTodosLosJugadores());
            jugs.sort((a, b) -> b.getJuegoGanados() - a.getJuegoGanados()); // mayor a menor
            int i = 1;
            for (Jugador j : jugs) { res.add(i + ". " + j); i++; }
        } catch (RemoteException e) { e.printStackTrace(); }
        return res;
    }

    public boolean encontrarJugador(String id) {
        try { return modelo.existeJugador(id); } catch (RemoteException e) { e.printStackTrace(); return false; }
    }

    // ===== lobby =====
    public void crearPartida(int cant) {
        try {
            idPartidaActual = modelo.crearPartida(cant).getId();
            modelo.agregarJugadorAPartida(idPartidaActual, idJugador); // el creador entra
        } catch (RemoteException e) { e.printStackTrace(); }
    }
    public List<String> partidasDisponibles() {
        List<String> res = new ArrayList<>();
        try {
            for (Partida p : modelo.getPartidas()) {
                if (p.getEstado() == EstadoPartida.EN_ESPERA) {
                    res.add("ID " + p.getId() + " - creada por " + p.getCreador()
                            + " - jugadores " + p.jugadoresPartida() + "/" + p.getCantidadJugadores());
                }
            }
        } catch (RemoteException e) { e.printStackTrace(); }
        return res;
    }

    public boolean puedoUnirmeAPartida(int id) {
        try {
            for (Partida p : modelo.getPartidas())
                if (p.getId() == id && p.getEstado() == EstadoPartida.EN_ESPERA) return true;
        } catch (RemoteException e) { e.printStackTrace(); }
        return false;
    }

    public void unirseAPartida(int idP) {
        try { idPartidaActual = idP; modelo.agregarJugadorAPartida(idP, idJugador); }
        catch (RemoteException e) { e.printStackTrace(); }
    }

    public void cancelarPartida() {
        try { modelo.cancelarPartida(idPartidaActual); idPartidaActual = -1; }
        catch (RemoteException e) { e.printStackTrace(); }
    }

    // ===== reanudar partidas guardadas =====
    public List<String> partidasGuardadas() {        // las que YO puedo reanudar
        List<String> res = new ArrayList<>();
        try {
            for (Partida p : modelo.getPartidasGuardadasDe(idJugador)) {
                res.add("ID " + p.getId() + " - jugadores " + String.join(", ", p.idsJugadores())
                        + " - (esperando que vuelvan)");
            }
        } catch (RemoteException e) { e.printStackTrace(); }
        return res;
    }
    public boolean puedoReanudar(int id) {
        try {
            for (Partida p : modelo.getPartidasGuardadasDe(idJugador))
                if (p.getId() == id) return true;
        } catch (RemoteException e) { e.printStackTrace(); }
        return false;
    }
    public void reanudarPartida(int id) {
        try { idPartidaActual = id; modelo.reconectarJugador(idJugador, id); }
        catch (RemoteException e) { e.printStackTrace(); }
    }

    // ===== lecturas para la vista =====
    private Partida getPartidaActual() {   // privado: solo lo usa el propio controlador
        try { return modelo.getPartida(idPartidaActual); }
        catch (RemoteException e) { e.printStackTrace(); return null; }
    }
    public boolean esMiTurno() {
        try {
            Jugador turno = modelo.getTurno(idPartidaActual);
            return turno != null && turno.getId().equals(idJugador);
        }
        catch (RemoteException e) { e.printStackTrace(); return false; }
    }
    public boolean estoyEsperandoRival() {
        Partida p = getPartidaActual();
        return p != null && p.getEstado() == EstadoPartida.EN_ESPERA;
    }
    public boolean estoyEsperandoReconexion() {
        Partida p = getPartidaActual();
        return p != null && p.getEstado() == EstadoPartida.EN_JUEGO && !p.todosConectados();
    }
    public void salirDePartida() {
        int id = idPartidaActual;
        idPartidaActual = -1;
        try { if (id != -1) modelo.desconectarJugador(idJugador, id); }
        catch (RemoteException e) { e.printStackTrace(); }
    }
    public List<String> manoJugador(String id) {
        List<String> res = new ArrayList<>();
        Partida p = getPartidaActual();
        if (p != null && p.encontrarCartaJugador(id) != null)
            for (Carta c : p.encontrarCartaJugador(id)) res.add(c.toString());
        return res;
    }
    public List<String> cartasMesa() {
        List<String> res = new ArrayList<>();
        Partida p = getPartidaActual();
        if (p != null) for (Carta c : p.CartasBocaArriba()) res.add(c.toString());
        return res;
    }

    public List<String> manoJugadorImagenes(String id) {
        List<String> res = new ArrayList<>();
        Partida p = getPartidaActual();
        if (p != null && p.encontrarCartaJugador(id) != null)
            for (Carta c : p.encontrarCartaJugador(id))
                res.add(c.getColor() + "_" + c.getNumero());
        return res;
    }

    public List<String> cartasMesaImagenes() {
        List<String> res = new ArrayList<>();
        Partida p = getPartidaActual();
        if (p != null)
            for (Carta c : p.CartasBocaArriba())
                res.add(c.getColor() + "_" + c.getNumero());
        return res;
    }


    public int cantidadCartasMesa() {
        Partida p = getPartidaActual();
        return p == null ? 0 : p.CartasBocaArriba().size();
    }
    public int cantidadCartaJugador(String id) {
        Partida p = getPartidaActual();
        return (p == null || p.encontrarCartaJugador(id) == null) ? 0 : p.encontrarCartaJugador(id).size();
    }
    public int jugadoresPartida() {
        Partida p = getPartidaActual(); return p == null ? 0 : p.jugadoresPartida();
    }
    public String jugadorNombrePartida(int i) {
        Partida p = getPartidaActual(); return p == null ? "" : p.jugadoresPartidaNombre(i);
    }
    public int jugadorPuntosPartida(int i) {
        Partida p = getPartidaActual(); return p == null ? 0 : p.jugadoresPartidaPuntos(i);
    }

    // ===== validaciones  =====
    public boolean validarCombinacionSimple(String idJ, int carta, Integer boca) {
        try { return modelo.validarCombinacionSimple(idPartidaActual, idJ, carta, boca); }
        catch (RemoteException e) { e.printStackTrace(); return false; }
    }
    public boolean validarColor(String idJ, int carta, Integer boca) {
        try { return modelo.validarColor(idPartidaActual, idJ, carta, boca); }
        catch (RemoteException e) { e.printStackTrace(); return false; }
    }
    public boolean validarCombinacionDoble(boolean comodin, String idJ, int c1, int c2, int boca, int numComodin) {
        try { return modelo.validarCombinacionDoble(idPartidaActual, comodin, idJ, c1, c2, boca, numComodin); }
        catch (RemoteException e) { e.printStackTrace(); return false; }
    }
    public boolean validarColorDoble(String idJ, int c1, int c2, Integer boca) {
        try { return modelo.validarColorDoble(idPartidaActual, idJ, c1, c2, boca); }
        catch (RemoteException e) { e.printStackTrace(); return false; }
    }
    public boolean validarSiEsComodin(Integer boca) {
        try { return modelo.validarComodin(idPartidaActual, boca); }
        catch (RemoteException e) { e.printStackTrace(); return false; }
    }

    // ===== acciones que cambian el estado =====
    public void eliminarCartaUsadas(String idJ, int carta, Integer boca) {
        try { modelo.eliminarCartaUsadas(idPartidaActual, idJ, carta, boca); } catch (RemoteException e) { e.printStackTrace(); }
    }
    public void eliminarCartaUsadas(String idJ, int carta) {
        try { modelo.eliminarCartaUsadas(idPartidaActual, idJ, carta); } catch (RemoteException e) { e.printStackTrace(); }
    }
    public void eliminarCartaUsadasDoble(String idJ, int c1, int c2, Integer boca) {
        try { modelo.eliminarCartaUsadasDoble(idPartidaActual, idJ, c1, c2, boca); } catch (RemoteException e) { e.printStackTrace(); }
    }
    public void ponerBocaArriba(Integer opcion, String idJ) {
        try { modelo.ponerCartaBocaArriba(idPartidaActual, opcion, idJ); } catch (RemoteException e) { e.printStackTrace(); }
    }
    public void rellenarCartaBocaArriba() {
        try { modelo.rellenarCartaBocaArriba(idPartidaActual); } catch (RemoteException e) { e.printStackTrace(); }
    }
    public boolean tomarCartaMazo(String idJ) {
        try { return modelo.tomarCartaMazo(idPartidaActual, idJ); } catch (RemoteException e) { e.printStackTrace(); return false; }
    }
    public void hiceJuegoDoble(String idJ) {
        try { modelo.hiceJuegoDoble(idPartidaActual, idJ); } catch (RemoteException e) { e.printStackTrace(); }
    }
    public void terminoTurno(String idPrincipal) {
        try { modelo.pasarTurno(idPartidaActual); } catch (RemoteException e) { e.printStackTrace(); }
    }
    public boolean tareasDeFinDePartida(String idJ) {
        try { return modelo.tareasDeFinDePartida(idPartidaActual, idJ); } catch (RemoteException e) { e.printStackTrace(); return false; }
    }

    public void Partida2Jugadores() {
        try { modelo.reiniciarPartida(idPartidaActual); } catch (RemoteException e) { e.printStackTrace(); }
    }

    // ===== cierre: desregistra este controlador como observador del modelo =====
    public void cerrar() {
        try {
            if (idPartidaActual != -1) modelo.desconectarJugador(idJugador, idPartidaActual);
            modelo.cerrar(this);
        } catch (RemoteException e) { e.printStackTrace(); }
    }

    // ===== observer remoto =====
    @Override
    public void actualizar(IObservableRemoto observable, Object o) throws RemoteException {
        if (!(o instanceof ManejadorEventos)) return;
        ManejadorEventos ev = (ManejadorEventos) o;
        switch (ev.getEvento()) {
            case CAMBIO_BUSCAR_PARTIDA -> vistaJuego.actualizarLobby();
            case CAMBIO_ESPERANDO_JUGADORES -> { if (ev.getIdPartida() == idPartidaActual) vistaJuego.esperandoJugadores(); }
            case CAMBIO_TURNO, ACTUALIZACION_JUEGO -> { if (ev.getIdPartida() == idPartidaActual) vistaJuego.mostrarPartida(); }

            case FIN_PARTIDA -> { if (ev.getIdPartida() == idPartidaActual && !idJugador.equals(ev.getIdGanador())) vistaJuego.terminoPartida(ev.getIdGanador()); }
            case FIN_JUEGO -> { if (ev.getIdPartida() == idPartidaActual && !idJugador.equals(ev.getIdGanador())) vistaJuego.terminoJuego(ev.getIdGanador()); }
            case DESCONEXION -> {
                if (ev.getIdPartida() == idPartidaActual) {
                    idPartidaActual = -1;
                    vistaJuego.oponenteSeFue();
                }
            }
            case RECONEXION -> { if (ev.getIdPartida() == idPartidaActual) vistaJuego.esperandoJugadores(); }
        }
    }

    public void decirDos(String idJ) {
        try { modelo.decirDos(idPartidaActual, idJ); } catch (RemoteException e) { e.printStackTrace(); }
    }
    public boolean penalizarSiNoDijoDos(String idJ) {
        try { return modelo.penalizarSiNoDijoDos(idPartidaActual, idJ); }
        catch (RemoteException e) { e.printStackTrace(); return false; }
    }
}