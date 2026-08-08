package ar.edu.unlu.juego.modelo.excepciones;

public class JugadorExistente extends Exception {
    public JugadorExistente(String mensaje) { super(mensaje); }
}