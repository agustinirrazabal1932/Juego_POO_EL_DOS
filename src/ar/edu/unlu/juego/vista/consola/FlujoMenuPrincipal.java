package ar.edu.unlu.juego.vista.consola;

import ar.edu.unlu.juego.controlador.Controlador;

public class FlujoMenuPrincipal extends Flujo {
    private String idDelJugador;
    public FlujoMenuPrincipal(ConsolaGrafica vista, Controlador controlador, String IdPrincipal) {
        super(vista, controlador);
        this.idDelJugador=IdPrincipal;
        this.controlador.agregarID(idDelJugador);
    }


    @Override
    public Flujo procesarEntrada(String string) {
        switch (string) {
            case "1" -> mostrarJugadores();
            case "2" -> {
                return new FlujoIniciarJuego(vista,controlador,idDelJugador);
            }
            case "3" -> mostrarMaximosGanadores();
            case "0" -> {
                vista.println("Gracias por jugar "+idDelJugador+"...");
                return new FlujoMenuInicial(vista,controlador);
            }
            default -> vista.println("Opción inválida");
        }
        return this;
    }


    @Override
    public void mostarSiguienteTexto() {
        vista.println("----- Bienvenido Jugador "+idDelJugador+" -----");
        vista.println("Menú Principal:");
        vista.println("1. Mostrar todos los jugadores");
        vista.println("2. Iniciar Partida");
        vista.println("3. Mostrar los jugadores mas Ganadores");
        vista.println("0. Salir");
        vista.println("Seleccione una opción: ");
    }

    public void mostrarJugadores(){
        for (String jugador : controlador.jugadoresRegistrados()){
            vista.println(jugador);
            vista.println("----");
        }
    }

    private void mostrarMaximosGanadores() {
        vista.println("---------------------------------------------------");
        vista.println("------ Max ganadores del Juego El Dos------");
        for (String linea : controlador.ranking()) {   // el orden lo resuelve el controlador
            vista.println(linea);
            vista.println("---------------------------------------------------");
        }
    }
}
