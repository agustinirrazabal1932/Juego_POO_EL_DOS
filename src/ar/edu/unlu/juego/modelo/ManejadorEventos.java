package ar.edu.unlu.juego.modelo;

import ar.edu.unlu.juego.modelo.enums.Eventos;
import java.io.Serializable;

public class ManejadorEventos implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int idPartida;
    private final Eventos evento;
    private String idGanador; // opcional, para FIN_PARTIDA / FIN_JUEGO

    public ManejadorEventos(int idPartida, Eventos evento) {
        this.idPartida = idPartida;
        this.evento = evento;
    }

    public int getIdPartida() { return idPartida; }
    public Eventos getEvento() { return evento; }
    public String getIdGanador() { return idGanador; }
    public void setIdGanador(String id) { this.idGanador = id; }
}