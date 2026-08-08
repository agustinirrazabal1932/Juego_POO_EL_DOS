package ar.edu.unlu.juego.modelo.interfaces;

import ar.edu.unlu.juego.modelo.Jugador;
import ar.edu.unlu.juego.modelo.Partida;
import ar.edu.unlu.rmimvc.observer.IObservableRemoto;
import ar.edu.unlu.rmimvc.observer.IObservadorRemoto;
import ar.edu.unlu.juego.modelo.excepciones.JugadorExistente;
import ar.edu.unlu.juego.modelo.excepciones.JugadorNoExistente;
import ar.edu.unlu.juego.modelo.excepciones.ContrasenaIncorrecta;

import java.rmi.RemoteException;
import java.util.List;



public interface IModelo extends IObservableRemoto {

    // ===== USUARIOS =====
    void cargarUsuario(String nombre, String apellido, String id, String password) throws RemoteException, JugadorExistente;
    void iniciarSesion(String id, String password) throws RemoteException, JugadorNoExistente, ContrasenaIncorrecta;
    boolean existeJugador(String id) throws RemoteException;
    List<Jugador> verTodosLosJugadores() throws RemoteException;

    // ===== LOBBY =====
    Partida crearPartida(int cantidadJugadores) throws RemoteException;
    List<Partida> getPartidas() throws RemoteException;
    Partida getPartida(int idPartida) throws RemoteException;          // <-- snapshot para la vista
    void agregarJugadorAPartida(int idPartida, String idJugador) throws RemoteException;
    void cancelarPartida(int idPartida) throws RemoteException;
    void empezarPartida(int idPartida) throws RemoteException;

    // ===== TURNO (atajo; también sale de getPartida(id).getTurno()) =====
    Jugador getTurno(int idPartida) throws RemoteException;

    // ===== VALIDACIONES (no cambian estado, devuelven boolean) =====
    boolean validarCombinacionSimple(int idPartida, String idJugador, int cartaElegida, Integer cartaBocaArriba) throws RemoteException;
    boolean validarColor(int idPartida, String idJugador, int cartaElegida, Integer cartaBocaArriba) throws RemoteException;
    boolean validarCombinacionDoble(int idPartida, boolean isComodin, String idJugador, int carta1, int carta2, int cartaBocaArriba, int numeroCartaBocaArriba) throws RemoteException;
    boolean validarColorDoble(int idPartida, String idJugador, int carta1, int carta2, Integer cartaBocaArriba) throws RemoteException;
    boolean validarComodin(int idPartida, Integer cartaBocaArriba) throws RemoteException;

    // ===== ACCIONES QUE CAMBIAN EL ESTADO (el Modelo notifica eventos al hacerlas) =====
    void eliminarCartaUsadas(int idPartida, String idJugador, int cartaElegida, Integer cartaBocaArriba) throws RemoteException;
    void eliminarCartaUsadas(int idPartida, String idJugador, int cartaElegida) throws RemoteException; // sobrecarga sin bocaArriba
    void eliminarCartaUsadasDoble(int idPartida, String idJugador, int carta1, int carta2, Integer cartaBocaArriba) throws RemoteException;
    void ponerCartaBocaArriba(int idPartida, Integer opcion, String idJugador) throws RemoteException;
    void rellenarCartaBocaArriba(int idPartida) throws RemoteException;
    boolean tomarCartaMazo(int idPartida, String idJugador) throws RemoteException;
    void hiceJuegoDoble(int idPartida, String idJugador) throws RemoteException;
    void pasarTurno(int idPartida) throws RemoteException;
    boolean tareasDeFinDePartida(int idPartida, String idJugador) throws RemoteException;
    void reiniciarPartida(int idPartida) throws RemoteException;

    // ===== desconexión / reconexión / reanudar =====
    void desconectarJugador(String idJugador, int idPartida) throws RemoteException;
    void reconectarJugador(String idJugador, int idPartida) throws RemoteException;
    List<Partida> getPartidasGuardadasDe(String idJugador) throws RemoteException;

    // ===== conexión: el cliente se desregistra al cerrar la ventana =====
    void cerrar(IObservadorRemoto observador) throws RemoteException;

    void decirDos(int idPartida, String idJugador) throws RemoteException;

    boolean penalizarSiNoDijoDos(int idPartida, String idJugador) throws RemoteException;
}
