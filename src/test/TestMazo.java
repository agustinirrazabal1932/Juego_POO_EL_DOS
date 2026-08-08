package test;

import ar.edu.unlu.juego.modelo.Carta;
import ar.edu.unlu.juego.modelo.Mazo;

public class TestMazo {
    public static void main(String[] args) {
        Mazo mazo = new Mazo();

        // El mazo del DOS tiene 107 cartas
        int total = 0;
        Mazo aux = new Mazo();
        while (aux.darCarta() != null) total++;
        System.out.println("el total de las cartas es: "+total);
        Check.check("Mazo arranca con 107 cartas", total == 108);


        // darCarta() reduce el mazo
        Carta c1 = mazo.darCarta();
        Check.check("darCarta() devuelve una carta", c1 != null);

        // Mezclar no pierde cartas
        Mazo m2 = new Mazo();
        m2.mezclar();
        int cont = 0;
        while (m2.darCarta() != null) cont++;
        System.out.println("mezclar cartas de mazo da: "+cont);
        Check.check("mezclar() no pierde cartas (107)", cont == 108);

        // Valores especiales del DOS: comodín (0) = 40, dos (2) = 20
        Carta comodin = new Carta("azul", 0);
        Carta dos = new Carta("multicolor", 2);
        Carta normal = new Carta("rojo", 5);
        Check.check("Comodín (0) vale 40", comodin.getValor() == 40);
        Check.check("Carta 2 vale 20", dos.getValor() == 20);
        Check.check("Carta normal vale su número", normal.getValor() == 5);

        Check.resumen();
    }
}