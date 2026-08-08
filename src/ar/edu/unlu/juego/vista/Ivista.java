package ar.edu.unlu.juego.vista;

import ar.edu.unlu.juego.controlador.Controlador;

import javax.swing.*;

public interface Ivista {
    void setControlador(Controlador controlador);
    Controlador getControlador();      // lo usa AppCliente para registrar el observador
    void mostrarMenuPrincipal();

    // los llama Controlador.actualizar(...) cuando llega un evento del servidor
    void actualizarLobby();
    void esperandoJugadores();
    void mostrarPartida();
    void terminoPartida(String idGanador);
    void terminoJuego(String idGanador);
    void oponenteSeFue();



}