package ar.edu.unlu.juego.vista.consola;

import ar.edu.unlu.juego.controlador.Controlador;

public class FlujoEsperar extends Flujo {
    public FlujoEsperar(ConsolaGrafica vista, Controlador controlador) {
        super(vista, controlador);
    }

    @Override
    public Flujo procesarEntrada(String string) {
        if (string.equals("0")) {
            if (controlador.estoyEsperandoRival()) {           // partida nueva: cancelar y volver
                controlador.cancelarPartida();
                vista.println("----- CANCELASTE LA PARTIDA -----");
                return new FlujoMenuPrincipal(vista, controlador, controlador.devolverID());
            }
            if (controlador.estoyEsperandoReconexion()) {      // partida guardada: salir SIN borrar
                controlador.salirDePartida();
                vista.println("----- SALISTE. LA PARTIDA SIGUE GUARDADA. -----");
                return new FlujoMenuPrincipal(vista, controlador, controlador.devolverID());
            }
        }
        vista.println("----- NO PRESIONE ENTER, ESPERE -----");
        return this;
    }

    @Override
    public void mostarSiguienteTexto() {
        if (controlador.estoyEsperandoRival()) {
            vista.println("----- Esperando a que se una otro jugador... -----");
            vista.println("----- (Ingresá 0 para cancelar la partida y volver) -----");
        } else if (controlador.estoyEsperandoReconexion()) {
            vista.println("----- Esperando a que vuelva el otro jugador... -----");
            vista.println("----- (Ingresá 0 para volver al menú; la partida sigue guardada) -----");
        } else {
            vista.println("----- ESPERE SU TURNO -----");
        }
    }
}
