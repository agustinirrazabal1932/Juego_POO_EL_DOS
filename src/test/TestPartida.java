package test;

import ar.edu.unlu.juego.modelo.Partida;
import ar.edu.unlu.juego.modelo.Jugador;

public class TestPartida {
    public static void main(String[] args) throws Exception {
        Partida p = new Partida(1,2);
        Jugador j1= new Jugador("ana","pp","011","1234");
        Jugador j2= new Jugador("beto","pp","012","1234");
        p.agregarJugador(j1);
        p.agregarJugador(j2);
        p.establecerTurnos();
        p.repartirCartas();

        // Reparto del DOS: 7 cartas por jugador + 2 boca arriba
        Jugador j = p.getTurno();
        Check.check("Cada jugador arranca con 7 cartas",
                j.catidadDeCartasDeMano() == 7);
        Check.check("Hay 2 cartas boca arriba",
                p.CartasBocaArriba().size() == 2);

        // Turnos: cambia de jugador
        String antes = p.getTurno().getNombre();
        p.siguienteTurno();
        Check.check("siguienteTurno() cambia el jugador en turno",
                !p.getTurno().getNombre().equals(antes));

        // BUG A CORREGIR: poner boca arriba NO debe duplicar la carta.
        // Total de cartas del jugador antes y después de poner 1 boca arriba
        // debe bajar en 1 (no quedar igual).
        Partida p2 = new Partida(2,2);
        Jugador j3= new Jugador("juan","pp","013", "1234");
        Jugador j4= new Jugador("jorge","pp","014", "1234");
        p2.agregarJugador(j3); p2.agregarJugador(j4);
        p2.establecerTurnos(); p2.repartirCartas();
        int antesCartas = p2.getTurno().catidadDeCartasDeMano();
         p2.ponerCartaBocaArriba(1, j3.getId() );  // <- llamá como quede tu API
         Check.check("ponerCartaBocaArriba saca la carta de la mano",
                 p2.getTurno().catidadDeCartasDeMano() == antesCartas - 1);

        Check.resumen();
    }
}