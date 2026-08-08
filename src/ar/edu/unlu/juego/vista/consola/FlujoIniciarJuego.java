package ar.edu.unlu.juego.vista.consola;

import ar.edu.unlu.juego.controlador.Controlador;

public class FlujoIniciarJuego extends Flujo {
    private enum estados {
        MENU,
        ELEGIR_PARTIDA,
        ELEGIR_GUARDADA
    }

    private estados estadoActual = estados.MENU;
    private final String idPrincipal;

    public FlujoIniciarJuego(ConsolaGrafica vista, Controlador controlador, String ID) {
        super(vista, controlador);
        this.idPrincipal = ID;
    }

    @Override
    public Flujo procesarEntrada(String entrada) {
        switch (estadoActual) {
            case MENU -> {
                switch (entrada) {
                    case "1" -> {
                        controlador.crearPartida(2);
                        vista.println("--- Partida creada. Esperando al otro jugador... ---");
                        return new FlujoEsperar(vista, controlador);
                    }
                    case "2" -> estadoActual = estados.ELEGIR_PARTIDA;
                    case "3" -> estadoActual = estados.ELEGIR_GUARDADA;
                    case "0" -> {
                        return new FlujoMenuPrincipal(vista, controlador, idPrincipal);
                    }
                    default -> vista.println("Opción inválida");
                }
            }
            case ELEGIR_PARTIDA -> {
                return procesarElegirPartida(entrada);
            }
            case ELEGIR_GUARDADA -> {
                return procesarElegirGuardada(entrada);
            }
        }
        return this;
    }

    private Flujo procesarElegirPartida(String entrada) {
        if (entrada.equals("0")) {            // volver al menú de Iniciar Partida
            estadoActual = estados.MENU;
            return this;
        }
        int id;
        try {
            id = Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            vista.println("Ingrese un número de partida válido.");
            return this;
        }
        if (controlador.puedoUnirmeAPartida(id)) {
            controlador.unirseAPartida(id);
            vista.println("--- Te uniste a la partida " + id + ". Esperando que arranque... ---");
            return new FlujoEsperar(vista, controlador);
        }
        vista.println("No hay una partida disponible con ese ID. Probá de nuevo.");
        return this;
    }

    private Flujo procesarElegirGuardada(String entrada) {
        if (entrada.equals("0")) {            // volver al menú de Iniciar Partida
            estadoActual = estados.MENU;
            return this;
        }
        int id;
        try {
            id = Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            vista.println("Ingrese un número de partida válido.");
            return this;
        }
        if (controlador.puedoReanudar(id)) {
            controlador.reanudarPartida(id);
            vista.println("--- Reanudaste la partida " + id + ". Esperando que vuelva el otro jugador... ---");
            return new FlujoEsperar(vista, controlador);
        }
        vista.println("No tenés una partida guardada con ese ID. Probá de nuevo.");
        return this;
    }

    @Override
    public void mostarSiguienteTexto() {
        switch (estadoActual) {
            case MENU -> {
                vista.println("--- Iniciar Partida ---");
                vista.println("1. Crear una partida (2 jugadores)");
                vista.println("2. Unirse a una partida existente");
                vista.println("3. Reanudar una partida guardada");
                vista.println("0. Volver al menú");
            }
            case ELEGIR_PARTIDA -> {
                vista.println("--- Partidas disponibles ---");
                java.util.List<String> partidas = controlador.partidasDisponibles();
                if (partidas.isEmpty()) {
                    vista.println("(No hay partidas esperando jugadores. Ingresá 0 para volver y creá una.)");
                } else {
                    for (String linea : partidas) {
                        vista.println(linea);
                    }
                }
                vista.println("Ingrese el ID de la partida a la que quiere unirse (o 0 para volver):");
            }
            case ELEGIR_GUARDADA -> {
                vista.println("--- Mis partidas guardadas ---");
                java.util.List<String> guardadas = controlador.partidasGuardadas();
                if (guardadas.isEmpty()) {
                    vista.println("(No tenés partidas guardadas. Ingresá 0 para volver.)");
                } else {
                    for (String linea : guardadas) {
                        vista.println(linea);
                    }
                }
                vista.println("Ingrese el ID de la partida que quiere reanudar (o 0 para volver):");
            }
        }
    }
}
