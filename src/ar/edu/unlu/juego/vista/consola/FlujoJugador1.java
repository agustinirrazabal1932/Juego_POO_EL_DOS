package ar.edu.unlu.juego.vista.consola;

import ar.edu.unlu.juego.controlador.Controlador;

public class FlujoJugador1 extends Flujo{


    private enum estados{
        INICIAR_TURNO_JUGADOR1,
        COMBINACION_SIMPLE_J1,
        ELIGE_CARTA_BOCA_ARRIBA,
        ELIGE_CARTA_PONER_BOCA_ARRIBA,
        COMBINACION_DOBLE_J1,
        COMBINACION_DOBLE_CARTA2_J1,
        ELIGE_CARTA_BOCA_ARRIBA_JUEGO_DOBLE_J1,
        CARTA_BOCA_ARRIBA_COMODIN,
        TOMAR_CARTA_DE_MAZO_J1,
        PASAR_TURNO,
        FIN_TURNO, FIN_JUEGO, VER_PUNTOS_JUEGO,

    }
    private estados estadoActual=estados.INICIAR_TURNO_JUGADOR1;
    private String idPrincipal;
    private String idJugador1;
    private String idJugador2;
    private int cartaElegidaJ1;
    private int carta2ElegidaJ1;
    private int cartaBocaArriba;
    private boolean juegoSimpleFull=false;
    private boolean juegoDobleFull=false;
    private boolean tomarCarta=false;
    private boolean juegoSimple=false;
    private boolean juegoDoble=false;


    public FlujoJugador1(ConsolaGrafica vista, Controlador controlador,String idJugador1,String idPrincipal) {

        super(vista, controlador);
        this.idJugador1=idJugador1;
        this.idPrincipal=idPrincipal;
    }

    @Override
    public Flujo procesarEntrada(String entrada) {
        switch (estadoActual){
            case VER_PUNTOS_JUEGO -> procesarLosPuntos(entrada);
            case INICIAR_TURNO_JUGADOR1 -> { return procesarTurnoJugador1(entrada); }
            case COMBINACION_SIMPLE_J1 -> procesarIngresoCartaJuegoSimple(entrada);
            case ELIGE_CARTA_BOCA_ARRIBA -> procesarCombinacionJ1(entrada);
            case ELIGE_CARTA_PONER_BOCA_ARRIBA -> { return procesarJuegoConColor(entrada); }
            case COMBINACION_DOBLE_J1 -> procesarIngresoCarta1(entrada);
            case COMBINACION_DOBLE_CARTA2_J1 -> procesarIngresoCarta2(entrada);
            case ELIGE_CARTA_BOCA_ARRIBA_JUEGO_DOBLE_J1 -> procesarCombinacionDoble(entrada);
            case CARTA_BOCA_ARRIBA_COMODIN -> procesarCombinacionDobleConComodin(entrada);
            case PASAR_TURNO -> { return procesarPaso(); }
            case FIN_TURNO -> { return procesarFinTurno(); }
            case FIN_JUEGO -> { return procesarFinPartida(); }
        }
        return this;
    }

    private void procesarLosPuntos(String entrada) {
        int numeroEntrada;
        try {
            numeroEntrada=Integer.parseInt(entrada);
            if (numeroEntrada!=1){
                vista.println("Ingrese una opcion Correcta...");
                estadoActual=estados.VER_PUNTOS_JUEGO;
            }else {

                int totalJugadores=controlador.jugadoresPartida();
                for (int i = 0; i < totalJugadores; i++) {
                    String nombreId=controlador.jugadorNombrePartida(i);
                    int puntosId=controlador.jugadorPuntosPartida(i);
                    vista.println("JUGADOR: "+nombreId+", PUNTOS: "+puntosId);
                    vista.println("------------------------------------------");
                }
                estadoActual=estados.INICIAR_TURNO_JUGADOR1;
            }
        } catch (NumberFormatException e) {
            vista.println("Ingrese un número válido.");
        }
    }

    private Flujo procesarFinPartida() {
        vista.println("termino la Partida...");
        boolean isGanador=controlador.tareasDeFinDePartida(idJugador1);
        if (isGanador){
            vista.println("--- TENEMOS GANADOR DEL JUEGO ---");
            vista.println("--- EL GANADOR DEL JUEGO ES "+idJugador1+" ----");
            return new FlujoMenuPrincipal(vista,controlador, idPrincipal);
        }
        else {
            controlador.Partida2Jugadores();
            return new FlujoJugador1(vista,controlador,idJugador1,idPrincipal);
        }
    }

    private Flujo procesarPaso() {
        if (!(juegoSimple || juegoSimpleFull || juegoDoble || juegoDobleFull || tomarCarta)) {
            vista.println("Para terminar su turno tiene que hacer un juego o tomar una carta...");
            estadoActual = estados.INICIAR_TURNO_JUGADOR1;
            return this;
        }
        if (juegoDobleFull) {
            controlador.hiceJuegoDoble(idJugador1);
        }
        if (0 == controlador.cantidadCartaJugador(idJugador1)) {
            estadoActual = estados.FIN_JUEGO;          // se quedó sin cartas -> ganó la partida
            return this;
        }
        if (juegoSimpleFull || juegoDobleFull) {
            estadoActual = estados.ELIGE_CARTA_PONER_BOCA_ARRIBA; // debe dejar una carta boca arriba
            return this;
        }
        vista.println("Pasaste el turno.");
        return procesarFinTurno();                      // termina el turno directo (sin enter extra)
    }

    private Flujo procesarFinTurno() {
        juegoSimpleFull=false;
        juegoSimple=false;
        juegoDoble=false;
        juegoDobleFull=false;
        tomarCarta=false;
        controlador.rellenarCartaBocaArriba();

        if (controlador.penalizarSiNoDijoDos(idJugador1)) {
            vista.println("¡No dijiste DOS! Se te suman 2 cartas.");
        }

        controlador.terminoTurno(idPrincipal);
        return new FlujoEsperar(vista,controlador);
    }

    private void procesarCombinacionDobleConComodin(String entrada) {
        Integer numeroCartaBocaArriba;
        try {
            numeroCartaBocaArriba = Integer.parseInt(entrada);
            if (numeroCartaBocaArriba < 1 ||numeroCartaBocaArriba >10) {
                vista.println("Ingrese un valor que se encuentre en las opciones...");
                estadoActual=estados.CARTA_BOCA_ARRIBA_COMODIN;
            }else{

                boolean validacionJuego = controlador.validarCombinacionDoble(true,idJugador1, cartaElegidaJ1, carta2ElegidaJ1, cartaBocaArriba,numeroCartaBocaArriba);
                if (validacionJuego) {
                    boolean validacionColor = controlador.validarColorDoble(idJugador1, cartaElegidaJ1, carta2ElegidaJ1, cartaBocaArriba);
                    if (validacionColor) {
                        vista.println("---Hizo un juego Doble con color----");
                        estadoActual = estados.INICIAR_TURNO_JUGADOR1;
                        juegoDobleFull = true;

                    } else {
                        vista.println("---Hizo un juego Doble sin color----");
                        juegoDoble=true;
                        estadoActual = estados.INICIAR_TURNO_JUGADOR1;
                    }
                    controlador.eliminarCartaUsadasDoble(idJugador1, cartaElegidaJ1, carta2ElegidaJ1, cartaBocaArriba);

                } else {
                    vista.println("No se pudo armar juego...");
                    vista.println("vuelva a intentarlo...");
                    estadoActual = estados.INICIAR_TURNO_JUGADOR1;
                }


            }


        } catch (NumberFormatException e) {
            vista.println("Ingrese un número válido.");
        }
    }

    private void procesarCombinacionDoble(String entrada) {
        Integer cartaBocaArribaOp;
        try {
            cartaBocaArribaOp = Integer.parseInt(entrada);
            if (cartaBocaArribaOp < 1 || cartaBocaArribaOp > controlador.cantidadCartasMesa()) {
                vista.println("Ingrese un valor que se encuentre en las opciones...");
                estadoActual=estados.INICIAR_TURNO_JUGADOR1;
            }else{
                cartaBocaArriba=cartaBocaArribaOp;
                boolean validarSiLaCartaBocaArribaEsComodin=controlador.validarSiEsComodin(cartaBocaArribaOp);

                if (validarSiLaCartaBocaArribaEsComodin){
                    vista.println("La carta que eligio Boca Arriba es un Comodin");
                    estadoActual=estados.CARTA_BOCA_ARRIBA_COMODIN;
                }
                else {
                    boolean validacionJuego = controlador.validarCombinacionDoble(false,idJugador1, cartaElegidaJ1, carta2ElegidaJ1, cartaBocaArribaOp,0);
                    if (validacionJuego) {
                        boolean validacionColor = controlador.validarColorDoble(idJugador1, cartaElegidaJ1, carta2ElegidaJ1, cartaBocaArribaOp);
                        if (validacionColor) {
                            vista.println("---Hizo un juego Doble con color----");
                            estadoActual = estados.INICIAR_TURNO_JUGADOR1;
                            juegoDobleFull = true;

                        } else {
                            vista.println("---Hizo un juego Doble sin color----");
                            estadoActual = estados.INICIAR_TURNO_JUGADOR1;
                            juegoDoble=true;
                        }
                        controlador.eliminarCartaUsadasDoble(idJugador1, cartaElegidaJ1, carta2ElegidaJ1, cartaBocaArribaOp);


                    } else {
                        vista.println("No se pudo armar juego...");
                        vista.println("vuelva a intentarlo...");
                        estadoActual = estados.INICIAR_TURNO_JUGADOR1;
                    }
                }

            }


        } catch (NumberFormatException e) {
            vista.println("Ingrese un número válido.");
        }
    }

    private void procesarIngresoCarta2(String entrada) {
        Integer opcion;
        try {
            opcion = Integer.parseInt(entrada);
            if (opcion < 1 || opcion > controlador.cantidadCartaJugador(idJugador1) || opcion== cartaElegidaJ1) {
                vista.println("Ingrese un valor que se encuentre en las opciones...");
            }else{
                carta2ElegidaJ1=opcion;
                estadoActual=estados.ELIGE_CARTA_BOCA_ARRIBA_JUEGO_DOBLE_J1;
            }

        } catch (NumberFormatException e) {
            vista.println("Ingrese un número válido.");
        }
    }

    private void procesarIngresoCarta1(String entrada) {
        Integer opcion;
        try {
            opcion = Integer.parseInt(entrada);
            if (opcion < 1 || opcion > controlador.cantidadCartaJugador(idJugador1)) {
                vista.println("Ingrese un valor que se encuentre en las opciones...");
            }else{
                cartaElegidaJ1=opcion;
                estadoActual=estados.COMBINACION_DOBLE_CARTA2_J1;
            }

        } catch (NumberFormatException e) {
            vista.println("Ingrese un número válido.");
        }
    }

    private Flujo procesarJuegoConColor(String entrada) {
        Integer opcion;
        try {
            opcion = Integer.parseInt(entrada);
            if (opcion < 1 || opcion > controlador.cantidadCartaJugador(idJugador1)) {
                vista.println("Ingrese un valor que se encuentre en las opciones...");
                return this;
            }
            controlador.rellenarCartaBocaArriba();
            controlador.ponerBocaArriba(opcion, idJugador1); // ya saca la carta de la mano
            if (0 == controlador.cantidadCartaJugador(idJugador1)) {
                estadoActual = estados.FIN_JUEGO;
                return this;
            }
            return procesarFinTurno();                 // termina el turno directo (sin enter extra)
        } catch (NumberFormatException e) {
            vista.println("Ingrese un número válido.");
            return this;
        }
    }

    private void procesarCombinacionJ1(String entrada) {
        Integer cartaBocaArribaOp;
        try {
            cartaBocaArribaOp = Integer.parseInt(entrada);
            if (cartaBocaArribaOp < 1 || cartaBocaArribaOp > controlador.cantidadCartasMesa()) {
                vista.println("Ingrese un valor que se encuentre en las opciones...");
                estadoActual=estados.INICIAR_TURNO_JUGADOR1;
            }else{
                boolean validacionJuego=controlador.validarCombinacionSimple(idJugador1,cartaElegidaJ1,cartaBocaArribaOp);
                if (validacionJuego){
                    boolean validacionColor=controlador.validarColor(idJugador1,cartaElegidaJ1,cartaBocaArribaOp);
                    if (validacionColor){
                        vista.println("---Hizo un juego simple con color----");
                        estadoActual=estados.INICIAR_TURNO_JUGADOR1;
                        juegoSimpleFull=true;

                    }else {
                        vista.println("---Hizo un juego simple sin color----");
                        juegoSimple=true;
                        estadoActual=estados.INICIAR_TURNO_JUGADOR1;
                    }
                    controlador.eliminarCartaUsadas(idJugador1,cartaElegidaJ1,cartaBocaArribaOp);

                }else{
                    vista.println("No se pudo armar juego...");
                    vista.println("vuelva a intentarlo...");
                    estadoActual=estados.INICIAR_TURNO_JUGADOR1;
                }

            }


        } catch (NumberFormatException e) {
            vista.println("Ingrese un número válido.");
        }
    }

    private void procesarIngresoCartaJuegoSimple(String entrada) {
        Integer opcion;
        try {
            opcion = Integer.parseInt(entrada);
            if (opcion < 1 ||opcion > controlador.cantidadCartaJugador(idJugador1)) {
                vista.println("Ingrese un valor que se encuentre en las opciones...");
            }else{
                cartaElegidaJ1=opcion;
                estadoActual=estados.ELIGE_CARTA_BOCA_ARRIBA;
            }

        } catch (NumberFormatException e) {
            vista.println("Ingrese un número válido.");
        }
    }

    private Flujo procesarTurnoJugador1(String entrada) {
        switch (entrada){
            case "1" -> estadoActual = estados.COMBINACION_SIMPLE_J1;
            case "2" -> estadoActual = estados.COMBINACION_DOBLE_J1;
            case "3" -> tomarCartaDelMazo();
            case "4" -> { return procesarPaso(); }
            case "5" -> estadoActual = estados.VER_PUNTOS_JUEGO;
            case "6" -> {
                controlador.decirDos(idJugador1);
                vista.println("¡Dijiste DOS!");
            }
            default -> vista.println("ingrese un valor valido...");
        }
        return this;
    }

    private void tomarCartaDelMazo() {
        if (!tomarCarta && !juegoDobleFull && !juegoSimpleFull) {
            controlador.tomarCartaMazo(idJugador1);
            tomarCarta = true;
            vista.println("---Tomaste una carta del mazo----");
        } else {
            vista.println("No podés tomar una carta: ya hiciste un juego o ya tomaste una.");
        }
        estadoActual = estados.INICIAR_TURNO_JUGADOR1;   // vuelve al menú mostrando la mano actualizada
    }
    @Override
    public void mostarSiguienteTexto() {
        switch (estadoActual){
            case INICIAR_TURNO_JUGADOR1 -> {
                vista.println("---------------------------------------------------");
                vista.println("---Es el Turno del Jugador "+idJugador1+"---");
                vista.println("---Las Cartas de Su Mano----");
                for (String carta : controlador.manoJugador(idJugador1)) {
                    vista.println(carta);
                }
                vista.println("---Las Cartas Boca Arriba---");
                if (controlador.cantidadCartasMesa() == 0) {
                    vista.println("No hay Cartas disponible para usar");
                }
                for (String carta : controlador.cartasMesa()) {
                    vista.println(carta);
                }
                vista.println("1. Combinacion Simple");
                vista.println("2. Combinacion Doble");
                vista.println("3. Tomar Carta");
                vista.println("4. Paso");
                vista.println("5. Ver puntos del juego");
                if (controlador.cantidadCartaJugador(idJugador1) <= 2) {
                    vista.println("6. Decir Dos");
                }
            }
            case VER_PUNTOS_JUEGO -> vista.println("para ver los puntos ingrese 1...");
            case COMBINACION_SIMPLE_J1 -> {
                int contador = 1;
                vista.println("---Las Cartas de Su Mano----");
                for (String carta : controlador.manoJugador(idJugador1)) {
                    vista.println(contador + "." + carta);
                    contador++;
                }
                vista.println("Seleccione el numero de la opcion de la carta que desea...");
            }
            case ELIGE_CARTA_BOCA_ARRIBA -> {
                int contador = 1;
                vista.println("---Las Cartas Boca Arriba---");
                for (String carta : controlador.cartasMesa()) {
                    vista.println(contador + "." + carta);
                    contador++;
                }
                vista.println("Seleccione el numero de la opcion de la carta que desea...");
            }
            case ELIGE_CARTA_PONER_BOCA_ARRIBA -> {
                int contador = 1;
                vista.println("---Eliga una Carta de tu Mano para Poner Boca Arriba---");
                for (String carta : controlador.manoJugador(idJugador1)) {
                    vista.println(contador + "." + carta);
                    contador++;
                }
                vista.println("Seleccione el numero de la opcion de la carta que desea...");
            }
            case COMBINACION_DOBLE_J1 -> {
                int contador = 1;
                vista.println("---Las Cartas de Su Mano----");
                for (String carta : controlador.manoJugador(idJugador1)) {
                    vista.println(contador + "." + carta);
                    contador++;
                }
                vista.println("Seleccione el numero de la opcion de la carta que desea para la carta 1...");
            }
            case COMBINACION_DOBLE_CARTA2_J1 -> {
                int contador = 1;
                vista.println("---Las Cartas de Su Mano----");
                for (String carta : controlador.manoJugador(idJugador1)) {
                    if (contador != cartaElegidaJ1) {      // salteo la carta ya elegida
                        vista.println(contador + "." + carta);
                    }
                    contador++;
                }
                vista.println("Seleccione el numero de la opcion de la carta que desea para la carta 2...");
            }
            case ELIGE_CARTA_BOCA_ARRIBA_JUEGO_DOBLE_J1 -> {
                int contador = 1;
                vista.println("---Las Cartas Boca Arriba---");
                for (String carta : controlador.cartasMesa()) {
                    vista.println(contador + "." + carta);
                    contador++;
                }
                vista.println("Seleccione el numero de la opcion de la carta que desea para el juego doble...");
            }
            case CARTA_BOCA_ARRIBA_COMODIN -> vista.println("Ingrese un valor del 1 al 10 incluidos...");
            case FIN_JUEGO -> vista.println("---El ganador de la partida fue el jugador "+idJugador1+"---");
        }

    }
}
