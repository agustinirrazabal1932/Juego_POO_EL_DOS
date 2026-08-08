package test;

import ar.edu.unlu.juego.modelo.Carta;
import ar.edu.unlu.juego.modelo.Validacion;

public class TestValidacion {
    public static void main(String[] args) {
        // SIMPLE: misma carta por número
        Carta mesa   = new Carta("rojo", 10);
        Carta igualN = new Carta("azul", 5);   // mismo número, otro color
        Check.check("Simple: mismo número es válido",
                new Validacion(igualN, mesa).validarSimple());

        // SIMPLE: comodín siempre válido
        Carta comodin = new Carta("azul", 0);
        Check.check("Simple: comodín siempre válido",
                new Validacion(comodin, mesa).validarSimple());

        // SIMPLE: distinto número y no comodín => inválido
        Carta distinta = new Carta("verde", 7);
        Check.check("Simple: distinto número NO es válido",
                !new Validacion(distinta, mesa).validarSimple());

        // COLOR: mismo color es válido
        Carta mismoColor = new Carta("rojo", 9);
        Check.check("Color: mismo color es válido",
                new Validacion(mismoColor, mesa).validarColor());

        // Doble:
        Carta vismoValorDoble = new Carta("azul", 5);
        Carta vismoValorDoble2 = new Carta("amarillo", 5);
        Check.check("Doble: es una combinacion doble simple",
                new Validacion(vismoValorDoble,vismoValorDoble2, mesa).validarDoble(false, mesa.getNumero() ));


        // Doble: con comodin
        Carta comodinValorDoble = new Carta("azul", 0);
        Carta comodinValorDoble2 = new Carta("amarillo", 5);
        Check.check("Doble: es una combinacion doble simple con comodin",
                new Validacion(vismoValorDoble,vismoValorDoble2, mesa).validarDoble(true, mesa.getNumero() ));
        Check.resumen();
    }
}