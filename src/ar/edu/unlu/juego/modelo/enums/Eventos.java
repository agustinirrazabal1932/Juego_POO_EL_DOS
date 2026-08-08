package ar.edu.unlu.juego.modelo.enums;

public enum Eventos {
    CAMBIO_BUSCAR_PARTIDA,        // se creó/cambió una partida en el lobby
    CAMBIO_ESPERANDO_JUGADORES,   // entró/salió un jugador antes de empezar
    CAMBIO_TURNO,                 // cambió el turno
    ACTUALIZACION_JUEGO,          // se jugó una carta / cambió la mesa
    FIN_PARTIDA,                  // terminó una partida (no llegó a 200)
    FIN_JUEGO,                    // alguien llegó a 200, gana el juego
    DESCONEXION,                  // un jugador se fue de una partida en curso
    RECONEXION                    // un jugador volvió a una partida guardada (falta el otro)
}