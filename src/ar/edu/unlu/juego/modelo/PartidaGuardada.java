package ar.edu.unlu.juego.modelo;

import serializacion.Serializador;

import java.util.HashMap;
import java.util.Map;


public class PartidaGuardada {
    private static PartidaGuardada instancia = null;
    private final Serializador serializador = new Serializador("src/data/partidas_guardadas.dat");
    private Map<Integer, Partida> guardadas;

    public static PartidaGuardada getInstancia() {
        if (instancia == null) instancia = new PartidaGuardada();
        return instancia;
    }


    @SuppressWarnings("unchecked")
    private PartidaGuardada() {
        Object data = serializador.readFirstObject();
        if (data != null) {
            guardadas = (Map<Integer, Partida>) data;
        } else {
            guardadas = new HashMap<>();
            serializador.writeOneObject(guardadas);   // primera vez: crea el archivo
        }
    }

    public void actualizar(Partida p) {               // guarda/pisa una partida
        guardadas.put(p.getId(), p);
        serializador.writeOneObject(guardadas);
    }

    public void borrar(int idPartida) {
        if (guardadas.remove(idPartida) != null) {
            serializador.writeOneObject(guardadas);
        }
    }

    public Map<Integer, Partida> getTodas() { return guardadas; }
}
