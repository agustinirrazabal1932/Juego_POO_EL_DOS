package ar.edu.unlu.juego.modelo;

import ar.edu.unlu.juego.modelo.enums.EstadoPartida;
import ar.edu.unlu.juego.modelo.enums.Eventos;
import ar.edu.unlu.juego.modelo.excepciones.ContrasenaIncorrecta;
import ar.edu.unlu.juego.modelo.excepciones.JugadorExistente;
import ar.edu.unlu.juego.modelo.excepciones.JugadorNoExistente;
import ar.edu.unlu.juego.modelo.interfaces.IModelo;
import ar.edu.unlu.rmimvc.observer.IObservadorRemoto;
import ar.edu.unlu.rmimvc.observer.ObservableRemoto;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Modelo extends ObservableRemoto implements IModelo, Serializable {
    private static final long serialVersionUID = 1L;
    private static IModelo instancia = null;

    private final Sesion sesion;
    private final PartidaGuardada partidasGuardadas;
    private final Map<Integer, Partida> partidas;
    private int proximoIdPartida = 1;

    public static IModelo getInstancia() throws RemoteException {
        if (instancia == null) instancia = new Modelo();
        return instancia;
    }

    private Modelo() throws RemoteException {
        super();
        this.sesion = Sesion.getInstancia();
        this.partidasGuardadas = PartidaGuardada.getInstancia();
        this.partidas = new HashMap<>();
        cargarGuardadasEnMemoria();   // recupera partidas en curso persistidas en disco
    }

    // Recarga las partidas guardadas al arrancar el server (todos quedan desconectados)
    private void cargarGuardadasEnMemoria() {
        int maxId = proximoIdPartida - 1;
        for (Map.Entry<Integer, Partida> e : partidasGuardadas.getTodas().entrySet()) {
            e.getValue().limpiarConexiones();
            partidas.put(e.getKey(), e.getValue());
            if (e.getKey() > maxId) maxId = e.getKey();
        }
        proximoIdPartida = maxId + 1;   // evita reusar IDs
    }

    // Persiste una partida solo si está en juego (las de espera no se guardan a disco)
    private void guardar(int idPartida) {
        Partida p = partidas.get(idPartida);
        if (p != null && p.getEstado() == EstadoPartida.EN_JUEGO) partidasGuardadas.actualizar(p);
    }

    // ===== USUARIOS =====
    @Override
    public void cargarUsuario(String nombre, String apellido, String id, String password) throws RemoteException, JugadorExistente {
        sesion.registrar(nombre, apellido, id, password);
    }

    @Override
    public void iniciarSesion(String id, String password) throws RemoteException, JugadorNoExistente, ContrasenaIncorrecta {
        sesion.iniciarSesion(id, password);
    }

    @Override
    public List<Jugador> verTodosLosJugadores() throws RemoteException { return sesion.getJugadores(); }

    @Override
    public boolean existeJugador(String id) throws RemoteException { return sesion.existe(id); }

    private Jugador encontrarJugador(String id) { return sesion.buscar(id); }

    // ===== LOBBY =====
    @Override
    public Partida crearPartida(int cantidadJugadores) throws RemoteException {
        Partida p = new Partida(proximoIdPartida++, cantidadJugadores);
        partidas.put(p.getId(), p);
        notificarObservadores(new ManejadorEventos(p.getId(), Eventos.CAMBIO_BUSCAR_PARTIDA));
        return p;
    }

    @Override
    public Partida getPartida(int id) throws RemoteException { return partidas.get(id); }

    @Override
    public List<Partida> getPartidas() throws RemoteException { return new ArrayList<>(partidas.values()); }

    @Override
    public void agregarJugadorAPartida(int idPartida, String idJugador) throws RemoteException {
        Partida p = partidas.get(idPartida);
        p.agregarJugador(encontrarJugador(idJugador));
        notificarObservadores(new ManejadorEventos(idPartida, Eventos.CAMBIO_ESPERANDO_JUGADORES));
        if (p.estaCompleta()) {
            empezarPartida(idPartida);
        }
    }

    @Override
    public void cancelarPartida(int idPartida) throws RemoteException {
        Partida p = partidas.get(idPartida);
        if (p != null && p.getEstado() == EstadoPartida.EN_ESPERA) {
            partidas.remove(idPartida);
            notificarObservadores(new ManejadorEventos(idPartida, Eventos.CAMBIO_BUSCAR_PARTIDA));
        }
    }

    @Override
    public void empezarPartida(int idPartida) throws RemoteException {
        Partida p = partidas.get(idPartida);
        if (p.getEstado() != EstadoPartida.EN_JUEGO) {
            p.setEstado(EstadoPartida.EN_JUEGO);
            p.establecerTurnos();
            p.repartirCartas();
            guardar(idPartida);
            notificarObservadores(new ManejadorEventos(idPartida, Eventos.CAMBIO_TURNO));
        }
    }

    // ===== TURNO =====
    @Override
    public Jugador getTurno(int idP) throws RemoteException {
        Partida p = partidas.get(idP);
        return p != null ? p.getTurno() : null;
    }

    @Override
    public void pasarTurno(int idP) throws RemoteException {
        partidas.get(idP).siguienteTurno();
        guardar(idP);   // guarda el estado al cerrar el turno
        notificarObservadores(new ManejadorEventos(idP, Eventos.CAMBIO_TURNO));
    }

    // ===== VALIDACIONES (solo leen -> NO notifican) =====
    @Override
    public boolean validarCombinacionSimple(int idPartida, String idJugador, int cartaElegida, Integer cartaBocaArriba) throws RemoteException {
        return partidas.get(idPartida).validarCombinacionSimple(idJugador, cartaElegida, cartaBocaArriba);
    }

    @Override
    public boolean validarColor(int idPartida, String idJugador, int cartaElegida, Integer cartaBocaArriba) throws RemoteException {
        return partidas.get(idPartida).validarColor(idJugador, cartaElegida, cartaBocaArriba);
    }

    @Override
    public boolean validarCombinacionDoble(int idPartida, boolean isComodin, String idJugador, int carta1, int carta2, int cartaBocaArriba, int numeroCartaBocaArriba) throws RemoteException {
        return partidas.get(idPartida).validarCombinacionDoble(isComodin, idJugador, carta1, carta2, cartaBocaArriba, numeroCartaBocaArriba);
    }

    @Override
    public boolean validarColorDoble(int idPartida, String idJugador, int carta1, int carta2, Integer cartaBocaArriba) throws RemoteException {
        return partidas.get(idPartida).validarColorDoble(idJugador, carta1, carta2, cartaBocaArriba);
    }

    @Override
    public boolean validarComodin(int idPartida, Integer cartaBocaArriba) throws RemoteException {
        return partidas.get(idPartida).validarComodin(cartaBocaArriba);
    }

    // ===== ACCIONES QUE CAMBIAN EL ESTADO -> delega y notifica =====
    @Override
    public void eliminarCartaUsadas(int idP, String idJ, int carta, Integer bocaArriba) throws RemoteException {
        partidas.get(idP).eliminarCartaUsadas(idJ, carta, bocaArriba);
        notificarObservadores(new ManejadorEventos(idP, Eventos.ACTUALIZACION_JUEGO));
    }

    @Override
    public void eliminarCartaUsadas(int idPartida, String idJugador, int cartaElegida) throws RemoteException {
        partidas.get(idPartida).eliminarCartaUsadas(idJugador, cartaElegida);
        notificarObservadores(new ManejadorEventos(idPartida, Eventos.ACTUALIZACION_JUEGO));
    }

    @Override
    public void eliminarCartaUsadasDoble(int idPartida, String idJugador, int carta1, int carta2, Integer cartaBocaArriba) throws RemoteException {
        partidas.get(idPartida).eliminarCartaUsadasDoble(idJugador, carta1, carta2, cartaBocaArriba);
        notificarObservadores(new ManejadorEventos(idPartida, Eventos.ACTUALIZACION_JUEGO));
    }

    @Override
    public void ponerCartaBocaArriba(int idPartida, Integer opcion, String idJugador) throws RemoteException {
        partidas.get(idPartida).ponerCartaBocaArriba(opcion, idJugador);
        notificarObservadores(new ManejadorEventos(idPartida, Eventos.ACTUALIZACION_JUEGO));
    }

    @Override
    public void rellenarCartaBocaArriba(int idPartida) throws RemoteException {
        partidas.get(idPartida).rellenarCartaBocaArriba();
        notificarObservadores(new ManejadorEventos(idPartida, Eventos.ACTUALIZACION_JUEGO));
    }

    @Override
    public boolean tomarCartaMazo(int idPartida, String idJugador) throws RemoteException {
        boolean tomo = partidas.get(idPartida).tomarCartaMazo(idJugador);
        notificarObservadores(new ManejadorEventos(idPartida, Eventos.ACTUALIZACION_JUEGO));
        return tomo;
    }

    @Override
    public void hiceJuegoDoble(int idPartida, String idJugador) throws RemoteException {
        partidas.get(idPartida).hiceJuegoDoble(idJugador);
        notificarObservadores(new ManejadorEventos(idPartida, Eventos.ACTUALIZACION_JUEGO));
    }

    @Override
    public boolean tareasDeFinDePartida(int idP, String idJ) throws RemoteException {
        boolean ganoJuego = partidas.get(idP).tareasDeFinDePartida(idJ); // sin notificar adentro
        if (ganoJuego) {
            Jugador ganador = encontrarJugador(idJ);
            if (ganador != null) {
                ganador.sumarJuegoGanado();
                sesion.guardar();
            }
            partidas.remove(idP);          // el juego terminó: ya no es reanudable
            partidasGuardadas.borrar(idP);
        }
        ManejadorEventos ev = new ManejadorEventos(idP, ganoJuego ? Eventos.FIN_JUEGO : Eventos.FIN_PARTIDA);
        ev.setIdGanador(idJ);
        notificarObservadores(ev);
        return ganoJuego;
    }


    // ===== DESCONEXIÓN / RECONEXIÓN =====
    @Override
    public void desconectarJugador(String idJugador, int idPartida) throws RemoteException {
        Partida p = partidas.get(idPartida);
        if (p == null) return;
        if (p.getEstado() == EstadoPartida.EN_ESPERA) {
            partidas.remove(idPartida);                 // se fue esperando rival, se cancela
        } else {

            p.limpiarConexiones();
            partidasGuardadas.actualizar(p);
            notificarObservadores(new ManejadorEventos(idPartida, Eventos.DESCONEXION));
        }
        notificarObservadores(new ManejadorEventos(idPartida, Eventos.CAMBIO_BUSCAR_PARTIDA));
    }

    @Override
    public void reconectarJugador(String idJugador, int idPartida) throws RemoteException {
        Partida p = partidas.get(idPartida);
        if (p == null || !p.perteneceA(idJugador)) return;   // solo jugadores originales
        p.reconectar(idJugador);
        partidasGuardadas.actualizar(p);
        if (p.todosConectados()) {
            notificarObservadores(new ManejadorEventos(idPartida, Eventos.CAMBIO_TURNO));  // siguen los dos
        } else {
            notificarObservadores(new ManejadorEventos(idPartida, Eventos.RECONEXION));    // espera al otro
        }
        notificarObservadores(new ManejadorEventos(idPartida, Eventos.CAMBIO_BUSCAR_PARTIDA));
    }

    @Override
    public List<Partida> getPartidasGuardadasDe(String idJugador) throws RemoteException {
        List<Partida> res = new ArrayList<>();
        for (Partida p : partidas.values()) {
            if (p.getEstado() == EstadoPartida.EN_JUEGO && p.perteneceA(idJugador) && !p.todosConectados()) {
                res.add(p);
            }
        }
        return res;
    }

    @Override
    public void cerrar(IObservadorRemoto observador) throws RemoteException {
        removerObservador(observador);
    }

    @Override
    public void reiniciarPartida(int idPartida) throws RemoteException {
        partidas.get(idPartida).reiniciarPartida();
        guardar(idPartida);
    }

    @Override
    public void decirDos(int idPartida, String idJugador) throws RemoteException {
        partidas.get(idPartida).decirDos(idJugador);
        notificarObservadores(new ManejadorEventos(idPartida, Eventos.ACTUALIZACION_JUEGO));
    }
    @Override
    public boolean penalizarSiNoDijoDos(int idPartida, String idJugador) throws RemoteException {
        boolean penalizado = partidas.get(idPartida).penalizarSiNoDijoDos(idJugador);
        notificarObservadores(new ManejadorEventos(idPartida, Eventos.ACTUALIZACION_JUEGO));
        return penalizado;
    }
}
