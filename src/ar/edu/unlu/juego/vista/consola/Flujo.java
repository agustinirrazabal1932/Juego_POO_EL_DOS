package ar.edu.unlu.juego.vista.consola;

import ar.edu.unlu.juego.controlador.Controlador;
import ar.edu.unlu.juego.vista.Ivista;

public abstract class Flujo{
    protected final ConsolaGrafica vista;
    protected final Controlador controlador;

    public Flujo(ConsolaGrafica vista, Controlador controlador) {
        this.vista = vista;
        this.controlador = controlador;
    }

    public abstract Flujo procesarEntrada(String string);

    public abstract void mostarSiguienteTexto();
}
