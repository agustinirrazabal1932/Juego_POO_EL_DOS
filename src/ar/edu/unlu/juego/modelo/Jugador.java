package ar.edu.unlu.juego.modelo;


import java.io.Serializable;
import java.util.ArrayList;

public class Jugador implements Serializable{
    private static final long serialVersionUID = 1L;
    private String id;
    private String nombre;
    private String apellido;
    private String password;
    private int puntos;
    private int juegoGanados;
    private ArrayList<Carta> cartasDeLaMano;
    private boolean decirDos;

    public Jugador(String nombre,String apellido,String id,String password){
        this.apellido=apellido;
        this.nombre=nombre;
        this.id=id;
        this.password = password;
        this.puntos=0;
        this.juegoGanados =0;
        this.cartasDeLaMano=new ArrayList<Carta>();
        this.decirDos=false;
    }

    public String getId() {
        return id;
    }
    public void tomarCarta(Carta carta){
        this.cartasDeLaMano.add(carta);
    }

//    public void verCartaDeLaMano(){
//        for (Carta cartaMano:this.cartasDeLaMano){
//            System.out.println(cartaMano);
//        }
//    }
//
//    public boolean buscarCartaMano(String color,String numero){
//        boolean estaLaCarta=false;
//        int numeroEntero;
//        numero.replaceAll(" ", "");
//        if (numero.equals("#")){
//            numeroEntero=0;
//        }else {
//            numeroEntero= Integer.parseInt(numero);
//
//        }
//
//        boolean paraHacerUnaVez=false;
//        for (Carta cartaMano:this.cartasDeLaMano){
//            if (cartaMano.getNumero()==numeroEntero && cartaMano.getColor().equals(color) && !paraHacerUnaVez){
//                    estaLaCarta=true;
//                    paraHacerUnaVez=true;
//            }
//        }
//        return estaLaCarta;
//    }

    public void UsateLaCartaMano(Carta cartaUsada){
        this.cartasDeLaMano.remove(cartaUsada);

    }

    public String getNombre() {
        return nombre;
    }

    public int getJuegoGanados() {
        return juegoGanados;
    }

    public String getApellido() {
        return apellido;
    }

    public int getPuntos() {
        return puntos;
    }

    public int catidadDeCartasDeMano() {
        return this.cartasDeLaMano.size();
    }

    public boolean isDecirDos() {
        return decirDos;
    }

    public void avisarDeDos() {
        this.decirDos=true;
        System.out.println("¡¡¡¡El jugador "+ this.id+" DIJO DOS!!!!");
    }

    public void sacarDos() {
        this.decirDos =false;
    }

    public int valorDeCartasDeMano() {
        int acumuladorDePuntos=0;
        for (Carta cartaAux: cartasDeLaMano){
            acumuladorDePuntos+=cartaAux.getValor();
        }
        return acumuladorDePuntos;
    }

    public  ArrayList<Carta> liberarMano(){
        ArrayList<Carta>listaAux=new ArrayList<Carta>();
        for (Carta cartaAux : cartasDeLaMano){
            listaAux.add(cartaAux);
        }
        cartasDeLaMano.clear();
        return listaAux;
    }

    public void sumarPuntos(int total) {
        this.puntos+=total;
    }
    public void sumarJuegoGanado(){
        this.juegoGanados++;
    }

    public void limpiarPuntos(){
        this.puntos=0;
    }

    @Override
    public String toString() {
        String cadena="";
        cadena+="Nombre: "+getNombre()+", Apellido: "+getApellido()+", ID: "+getId()+", JuegoGanados: "+getJuegoGanados();
        return cadena;
    }

//    public void agregarGanadas(int juegos) {
//        this.juegoGanados=juegos;
//    }

    public ArrayList<Carta> verCartaDelJugador(){
        return cartasDeLaMano;
    }

    public Carta seleccionarCarta(int cartaElegidaJ1) {
        return cartasDeLaMano.get(cartaElegidaJ1-1);
    }

    public String getPassword() { return password; }
}
