package ar.edu.unlu.juego.modelo;

import ar.edu.unlu.juego.modelo.enums.EstadoPartida;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Partida implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int PUNTOS_PARA_GANAR = 50;
    private int id;
    private int cantidadJugadores;
    private EstadoPartida estado;
    private Mazo mazo;
    private ArrayList<Jugador> jugadoresEnPartida;
    private ArrayList<Carta> cartasBocaArriba;
    private ArrayList<Carta> cartasUsadas;
    private Validacion validacion;
    private int turnoActual;
    private String creador;
    private final ArrayList<String> conectados = new ArrayList<>();

    public Partida(int id, int cantidadJugadores) {
        this.id = id;
        this.cantidadJugadores = cantidadJugadores;
        this.estado = EstadoPartida.EN_ESPERA;
        this.mazo = new Mazo();
        this.mazo.mezclar();
        this.mazo.mezclar();
        this.jugadoresEnPartida = new ArrayList<>();
        this.cartasBocaArriba = new ArrayList<>();
        this.cartasUsadas = new ArrayList<>();
        this.turnoActual = 0;
    }

    public int getId() { return id; }
    public EstadoPartida getEstado() { return estado; }
    public void setEstado(EstadoPartida e) { this.estado = e; }
    public int getCantidadJugadores() { return cantidadJugadores; }

    // --- gestión de jugadores ---
    public void agregarJugador(Jugador j) {
        if (creador == null) creador = j.getId();   // el primero en entrar es el creador
        jugadoresEnPartida.add(j);
        if (!conectados.contains(j.getId())) conectados.add(j.getId());  // entra conectado
    }
    public String getCreador() { return creador; }

    // ===== estado de conexión =====
    public void desconectar(String id) { conectados.remove(id); }
    public void reconectar(String id)  { if (perteneceA(id) && !conectados.contains(id)) conectados.add(id); }
    public boolean todosConectados()   { return conectados.size() == cantidadJugadores; }
    public boolean perteneceA(String id) { return encontrarJugador(id) != null; }
    public void limpiarConexiones()    { conectados.clear(); }   // se usa al recargar de disco

    public List<String> idsJugadores() {
        List<String> ids = new ArrayList<>();
        for (Jugador j : jugadoresEnPartida) ids.add(j.getId());
        return ids;
    }
    public boolean estaCompleta() { return jugadoresEnPartida.size() == cantidadJugadores; }
    public ArrayList<Jugador> getJugadoresEnPartida() { return jugadoresEnPartida; }

    // --- turnos ---
    public void establecerTurnos() { this.turnoActual = 0; }
    public Jugador getTurno() { return jugadoresEnPartida.get(turnoActual); }
    public void siguienteTurno() {
        turnoActual = (turnoActual + 1) % jugadoresEnPartida.size();
    }

    // --- reparto ---
    public void repartirCartas() {
        for (Jugador j : jugadoresEnPartida) j.liberarMano();   // limpia cartas de partidas anteriores
        int totalDeCarta = 7 * jugadoresEnPartida.size();
        int idx = 0;
        for (int i = 0; i < totalDeCarta; i++) {
            Carta c = mazo.darCarta();
            if (c != null) {
                jugadoresEnPartida.get(idx).tomarCarta(c);
                idx = (idx + 1) % jugadoresEnPartida.size();
            }
        }
        for (int i = 0; i < 2; i++) {
            Carta c = mazo.darCarta();
            if (c != null) cartasBocaArriba.add(c);
        }
    }

    public boolean validarCombinacionSimple(String jugadorID,int cartaElegidaJ1, Integer cartaBocaArribaOp) {
        Jugador jugador=encontrarJugador(jugadorID);
        Carta cartaElegida= jugador != null ? jugador.seleccionarCarta(cartaElegidaJ1) : null;
        Carta cartaBocaArriba=cartasBocaArriba.get(cartaBocaArribaOp-1);
        validacion=new Validacion(cartaElegida,cartaBocaArriba);

        return validacion.validarSimple();
    }

    private Jugador encontrarJugador(String id) {
        for (Jugador j : jugadoresEnPartida)
            if (j.getId().equals(id)) return j;
        return null;
    }

    public boolean validarColor(String jugadorID,int cartaElegidaJ1, Integer cartaBocaArribaOp){
        Jugador jugador=encontrarJugador(jugadorID);
        Carta cartaElegida= jugador != null ? jugador.seleccionarCarta(cartaElegidaJ1) : null;
        Carta cartaBocaArriba=cartasBocaArriba.get(cartaBocaArribaOp-1);
        validacion=new Validacion(cartaElegida,cartaBocaArriba);
        return validacion.validarColor();
    }

    public boolean validarCombinacionDoble(boolean isComodin,String jugadorID, int cartaElegidaJ1, int carta2ElegidaJ1, int cartaBocaArribaOp,int numeroCartaBocaArriba) {
        Jugador jugador=encontrarJugador(jugadorID);
        Carta cartaElegida= jugador != null ? jugador.seleccionarCarta(cartaElegidaJ1) : null;
        Carta carta2Elegida= jugador != null ? jugador.seleccionarCarta(carta2ElegidaJ1) : null;
        Carta cartaBocaArriba=cartasBocaArriba.get(cartaBocaArribaOp-1);
        validacion=new Validacion(cartaElegida,carta2Elegida,cartaBocaArriba);

        return validacion.validarDoble(isComodin,numeroCartaBocaArriba);
    }

    public boolean validarComodin(Integer cartaBocaArribaOp) {
        boolean isComodin=false;
        Carta cartaBocaArriba=cartasBocaArriba.get(cartaBocaArribaOp-1);
        if (cartaBocaArriba.getNumero()==0){
            isComodin=true;
        }
        return isComodin;
    }

    public boolean validarColorDoble(String jugadorID, int cartaElegidaJ1, int carta2ElegidaJ1, Integer cartaBocaArribaOp) {
        Jugador jugador=encontrarJugador(jugadorID);
        Carta cartaElegida= jugador != null ? jugador.seleccionarCarta(cartaElegidaJ1) : null;
        Carta carta2Elegida= jugador != null ? jugador.seleccionarCarta(carta2ElegidaJ1) : null;
        Carta cartaBocaArriba=cartasBocaArriba.get(cartaBocaArribaOp-1);
        validacion=new Validacion(cartaElegida,carta2Elegida,cartaBocaArriba);
        return validacion.validarColorDoble();
    }

    public void eliminarCartaUsadas(String jugadorID, int cartaElegidaJ1, Integer cartaBocaArribaOp) {
        Jugador jugador=encontrarJugador(jugadorID);
        Carta cartaElegida= jugador != null ? jugador.seleccionarCarta(cartaElegidaJ1) : null;
        Carta cartaBocaArriba=cartasBocaArriba.get(cartaBocaArribaOp-1);

        //muevo la carta al lugar de cartas usadas
        if (jugador != null) {
            jugador.UsateLaCartaMano(cartaElegida);
        }
        removerCartasUsadas(cartaElegida,cartaBocaArriba);

    }

    public void eliminarCartaUsadas(String idJugador1, int cartaElegidaJ1) {
        Jugador jugador=encontrarJugador(idJugador1);
        if (jugador == null) return;
        Carta cartaElegida=jugador.seleccionarCarta(cartaElegidaJ1);
        jugador.UsateLaCartaMano(cartaElegida);

    }

    private void removerCartasUsadas(Carta cartaUsar, Carta cartaUsarBocaArriba) {
        this.cartasUsadas.add(cartaUsar);
        this.cartasBocaArriba.remove(cartaUsarBocaArriba);
        this.cartasUsadas.add(cartaUsarBocaArriba);
    }

    public void eliminarCartaUsadasDoble(String jugadorID, int cartaElegidaJ1,int carta2ElegidaJ1 ,Integer cartaBocaArribaOp) {
        Jugador jugador=encontrarJugador(jugadorID);
        if (jugador == null) return;

        Carta cartaElegida=jugador.seleccionarCarta(cartaElegidaJ1);
        Carta carta2Elegida=jugador.seleccionarCarta(carta2ElegidaJ1);
        Carta cartaBocaArriba=cartasBocaArriba.get(cartaBocaArribaOp-1);

        jugador.UsateLaCartaMano(cartaElegida);
        jugador.UsateLaCartaMano(carta2Elegida);
        removerCartasUsadasDoble(cartaElegida,carta2Elegida,cartaBocaArriba);
    }

    private void removerCartasUsadasDoble(Carta cartaUsar1, Carta cartaUsar2, Carta cartaUsarBocaArriba) {
        this.cartasUsadas.add(cartaUsar1);
        this.cartasUsadas.add(cartaUsar2);
        this.cartasBocaArriba.remove(cartaUsarBocaArriba);
        this.cartasUsadas.add(cartaUsarBocaArriba);
    }

    public boolean tomarCartaMazo(String idJugador) {
        boolean tomarCarta=false;
        Jugador jugador=encontrarJugador(idJugador);
        if (jugador == null) return false;

        Carta cartaNueva = this.mazo.darCarta();
        if (cartaNueva != null) {
            jugador.tomarCarta(cartaNueva);
            tomarCarta=true;
        } else {
            sinCartasEnElMazo();
        }
        return tomarCarta;
    }

    private void sinCartasEnElMazo() {
        for (Carta cartaAux:this.cartasUsadas){
            this.mazo.tomarCartaMazo(cartaAux);
        }
        this.cartasUsadas.clear();
        this.mazo.mezclar();
        this.mazo.mezclar();
    }

    public void ponerCartaBocaArriba(Integer opcion, String idJugador) {
        Jugador jugador = encontrarJugador(idJugador);
        if (jugador == null) return;
        Carta cartaElegida = jugador.seleccionarCarta(opcion);
        jugador.UsateLaCartaMano(cartaElegida);
        cartasBocaArriba.add(cartaElegida);
    }

    public void rellenarCartaBocaArriba() {
        //verifico si faltan cartas boca arriba, tienen que ser dos siempre

        if (this.cartasBocaArriba.isEmpty()){
            for (int i = 0; i < 2; i++) {
                Carta cartaNuevaBocaArriba=this.mazo.darCarta();
                if (cartaNuevaBocaArriba!=null){
                    this.cartasBocaArriba.add(cartaNuevaBocaArriba);
                }else {
                    sinCartasEnElMazo();
                    cartaNuevaBocaArriba=this.mazo.darCarta();
                    this.cartasBocaArriba.add(cartaNuevaBocaArriba);

                }
            }

        } else if (cartasBocaArriba.size() == 1) {
            Carta cartaNuevaBocaArriba=this.mazo.darCarta();
            if (cartaNuevaBocaArriba!=null){
                this.cartasBocaArriba.add(cartaNuevaBocaArriba);
            }else {
                sinCartasEnElMazo();
                cartaNuevaBocaArriba=this.mazo.darCarta();
                this.cartasBocaArriba.add(cartaNuevaBocaArriba);

            }
        }
    }

    public void hiceJuegoDoble(String idJugador){
        for (Jugador jugadorAux:jugadoresEnPartida) {
            if (!jugadorAux.getId().equals(idJugador)) {
                Carta cartaPorJuegoDoble = this.mazo.darCarta();
                if (cartaPorJuegoDoble != null) {
                    jugadorAux.tomarCarta(cartaPorJuegoDoble);
                } else {
                    sinCartasEnElMazo();
                    cartaPorJuegoDoble = this.mazo.darCarta();
                    jugadorAux.tomarCarta(cartaPorJuegoDoble);
                }
            }
        }
    }

    public ArrayList<Carta> encontrarCartaJugador(String idJugador1) {
        for (Jugador jugadorAux: jugadoresEnPartida){
            if (jugadorAux.getId().equals(idJugador1)){
                return jugadorAux.verCartaDelJugador();
            }
        }
        return null;
    }

    public ArrayList<Carta> CartasBocaArriba() {
        return cartasBocaArriba;
    }

    private void sumarValores(Jugador jugadorASumar) {
        int total=0;
        for (Jugador jugador: jugadoresEnPartida){
            if (!jugador.getId().equals(jugadorASumar.getId())){
                total+=jugador.valorDeCartasDeMano();
            }

        }
        jugadorASumar.sumarPuntos(total);
    }

    private void limpiarManoDeJugadores(Jugador jugadorGanador) {
        for (Jugador jugador: jugadoresEnPartida) {
            if (!jugador.getId().equals(jugadorGanador.getId())) {
                ArrayList<Carta> cartasDeLaMano=jugador.liberarMano();
                for (Carta cartaAux: cartasDeLaMano){
                    mazo.tomarCartaMazo(cartaAux);
                }
                cartasDeLaMano.clear();
            }
        }
    }

    private boolean verificarSiTerminoELjuego() {
        boolean termino=false;
        for (Jugador jugadorAux:jugadoresEnPartida){
            if (jugadorAux.getPuntos() >= PUNTOS_PARA_GANAR) {
                termino = true;
                break;
            }
        }
        return termino;
    }

    private void limpiarPuntosDeJugadores() {
        for (Jugador jugadorAux : jugadoresEnPartida){
            jugadorAux.limpiarPuntos();
        }
    }

    public boolean tareasDeFinDePartida(String idJugador) {
        Jugador jugadorGanoPartida=encontrarJugador(idJugador);
        sumarValores(jugadorGanoPartida);

        sinCartasEnElMazo();

        for (Carta cartaAux: cartasBocaArriba){
            mazo.tomarCartaMazo(cartaAux);
        }
        cartasBocaArriba.clear();

        limpiarManoDeJugadores(jugadorGanoPartida);
        boolean isGanador=verificarSiTerminoELjuego();
        if (isGanador){
            limpiarPuntosDeJugadores();
            jugadoresEnPartida.clear();
        }
        return isGanador;

    }

    public int jugadoresPartidaPuntos(int i) {
        return jugadoresEnPartida.get(i).getPuntos();
    }
    public String jugadoresPartidaNombre(int i) {
        return jugadoresEnPartida.get(i).getId();
    }
    public int jugadoresPartida() {
        return jugadoresEnPartida.size();
    }

    public void reiniciarPartida() {
        for (Jugador jugadorAux: jugadoresEnPartida){
            for (int i=0;i<7;i++){
                Carta cartaAux = this.mazo.darCarta();
                if (cartaAux != null) {
                    jugadorAux.tomarCarta(cartaAux);
                }
            }
        }
        //poner la dos carta boca arriba en la meza
        for (int i = 0; i < 2; i++) {
            Carta cartaBocaArriba = this.mazo.darCarta();
            if (cartaBocaArriba != null) {
                this.cartasBocaArriba.add(cartaBocaArriba);
            }
        }

    }

    public void decirDos(String idJugador) {
        Jugador j = encontrarJugador(idJugador);
        if (j != null) j.avisarDeDos();
    }


    public boolean penalizarSiNoDijoDos(String idJugador) {
        Jugador j = encontrarJugador(idJugador);
        boolean penalizado = false;
        if (j != null && j.catidadDeCartasDeMano() <= 2 && !j.isDecirDos()) {
            for (int i = 0; i < 2; i++) {
                Carta c = mazo.darCarta();
                if (c == null) { sinCartasEnElMazo(); c = mazo.darCarta(); }
                if (c != null) j.tomarCarta(c);
            }
            penalizado = true;
        }
        if (j != null) j.sacarDos();
        return penalizado;
    }


}