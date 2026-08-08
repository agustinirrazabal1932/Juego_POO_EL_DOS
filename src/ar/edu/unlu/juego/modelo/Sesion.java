package ar.edu.unlu.juego.modelo;

import ar.edu.unlu.juego.modelo.excepciones.JugadorExistente;
import ar.edu.unlu.juego.modelo.excepciones.JugadorNoExistente;
import ar.edu.unlu.juego.modelo.excepciones.ContrasenaIncorrecta;
import serializacion.Serializador;

import java.util.ArrayList;
import java.util.List;

public class Sesion {
    private static Sesion instancia = null;
    private final Serializador serializador = new Serializador("src/data/jugadores.dat");
    private ArrayList<Jugador> jugadores;

    public static Sesion getInstancia() {
        if (instancia == null) instancia = new Sesion();
        return instancia;
    }

    @SuppressWarnings("unchecked")
    private Sesion() {
        Object data = serializador.readFirstObject();
        if (data != null) {
            jugadores = (ArrayList<Jugador>) data;   // carga lo persistido
        } else {
            jugadores = new ArrayList<>();            // primera vez: archivo vacío
            serializador.writeOneObject(jugadores);
        }
    }

    public void registrar(String nombre, String apellido, String id, String password) throws JugadorExistente {
        if (buscar(id) != null) throw new JugadorExistente("El ID ya está registrado.");
        jugadores.add(new Jugador(nombre, apellido, id, password));
        serializador.writeOneObject(jugadores);       // persiste
    }

    public void iniciarSesion(String id, String password) throws JugadorNoExistente, ContrasenaIncorrecta {
        Jugador j = buscar(id);
        if (j == null) throw new JugadorNoExistente("No existe un jugador con ese ID.");
        if (!j.getPassword().equals(password)) throw new ContrasenaIncorrecta("Contraseña incorrecta.");
    }

    public Jugador buscar(String id) {
        for (Jugador j : jugadores) if (j.getId().equals(id)) return j;
        return null;
    }

    public boolean existe(String id) { return buscar(id) != null; }

    public List<Jugador> getJugadores() { return jugadores; }

    public void guardar() { serializador.writeOneObject(jugadores); } // re-persistir (ej: tras sumar victoria)
}