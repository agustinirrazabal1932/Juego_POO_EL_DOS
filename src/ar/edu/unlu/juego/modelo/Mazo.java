package ar.edu.unlu.juego.modelo;

import java.util.ArrayList;
import java.util.Random;
import java.io.Serializable;

public class Mazo implements Serializable{
    private static final long serialVersionUID = 1L;
    private ArrayList<Carta> listaDeCarta;

    public Mazo(){
        this.listaDeCarta=new ArrayList<Carta>();
        cargarCartas();
    }
    private void cargarCartas(){
        //cargar cartas azul 1 3 4 5;

        boolean termino=true;
        String color="azul";
        int numero=1;

        while (termino){
            Carta cartaNueva;
            for (int i = 0; i < 3; i++) {
                if(numero!=2) {
                    cartaNueva = new Carta(color, numero);
                    this.listaDeCarta.add(cartaNueva);
                }
            }

            if (numero==5) {
                termino=false;
            }
            numero++;
        }

        //cargar cartas azul 6 7 8 9 10 y comodin;
        termino=true;
        numero=6;
        int numeroComodin=numero;
        while (termino){
            Carta cartaNueva;
            for (int i = 0; i < 2; i++) {

                if (numeroComodin==0) {
                    cartaNueva = new Carta(color, numeroComodin);
                }else{
                    cartaNueva=new Carta(color,numero);
                }
                this.listaDeCarta.add(cartaNueva);
            }

            if (numeroComodin==0){
                termino=false;
            } else if (numero==10) {
                numeroComodin=0;
            }
            numero++;
        }
        //cargar cartas rojas 1 3 4 5;

        termino=true;
        color="rojo";
        numero=1;

        while (termino){
            Carta cartaNueva;
            for (int i = 0; i < 3; i++) {
                if(numero!=2) {
                    cartaNueva = new Carta(color, numero);
                    this.listaDeCarta.add(cartaNueva);
                }
            }

            if (numero==5) {
                termino=false;
            }
            numero++;
        }

        //cargar cartas rojas 6 7 8 9 10 y comodin;
        termino=true;
        numero=6;
        numeroComodin=numero;
        while (termino){
            Carta cartaNueva;
            for (int i = 0; i < 2; i++) {

                if (numeroComodin==0) {
                    cartaNueva = new Carta(color, numeroComodin);
                }else{
                    cartaNueva=new Carta(color,numero);
                }
                this.listaDeCarta.add(cartaNueva);
            }

            if (numeroComodin==0){
                termino=false;
            } else if (numero==10) {
                numeroComodin=0;
            }
            numero++;
        }
        //cargar cartas verde 1 3 4 5;

        termino=true;
        color="verde";
        numero=1;

        while (termino){
            Carta cartaNueva;
            for (int i = 0; i < 3; i++) {
                if(numero!=2) {
                    cartaNueva = new Carta(color, numero);
                    this.listaDeCarta.add(cartaNueva);
                }
            }

            if (numero==5) {
                termino=false;
            }
            numero++;
        }

        //cargar cartas verde 6 7 8 9 10 y comodin;
        termino=true;
        numero=6;
        numeroComodin=numero;
        while (termino){
            Carta cartaNueva;
            for (int i = 0; i < 2; i++) {

                if (numeroComodin==0) {
                    cartaNueva = new Carta(color, numeroComodin);
                }else{
                    cartaNueva=new Carta(color,numero);
                }
                this.listaDeCarta.add(cartaNueva);
            }

            if (numeroComodin==0){
                termino=false;
            } else if (numero==10) {
                numeroComodin=0;
            }
            numero++;
        }
        //cargar cartas amarillas 1 3 4 5;

        termino=true;
        color="amarilla";
        numero=1;

        while (termino){
            Carta cartaNueva;
            for (int i = 0; i < 3; i++) {
                if(numero!=2) {
                    cartaNueva = new Carta(color, numero);
                    this.listaDeCarta.add(cartaNueva);
                }
            }

            if (numero==5) {
                termino=false;
            }
            numero++;
        }

        //cargar cartas amarillas 6 7 8 9 10 y comodin;
        termino=true;
        numero=6;
        numeroComodin=numero;
        while (termino){
            Carta cartaNueva;
            for (int i = 0; i < 2; i++) {

                if (numeroComodin==0) {
                    cartaNueva = new Carta(color, numeroComodin);
                }else{
                    cartaNueva=new Carta(color,numero);
                }
                this.listaDeCarta.add(cartaNueva);
            }

            if (numeroComodin==0){
                termino=false;
            } else if (numero==10) {
                numeroComodin=0;
            }
            numero++;
        }

        //carga comodin 2
        color="multicolor";
        numero=2;
        Carta cartaNueva;
        for (int i = 0; i < 12; i++) {
            cartaNueva = new Carta(color, numero);
            this.listaDeCarta.add(cartaNueva);
        }
    }
    public void mezclar(){
        Random random=new Random();
        ArrayList<Carta> listaMezclada=new ArrayList<Carta>(107);
        for (Carta cartaAux : this.listaDeCarta) {
            int numeroAleatorio= random.nextInt(0,108);
            if (numeroAleatorio>=listaMezclada.size()) {
                listaMezclada.add(cartaAux);
            }
            else{
                listaMezclada.add(numeroAleatorio,cartaAux);
            }
        }
        setListaDeCarta(listaMezclada);
        System.currentTimeMillis();
    }

    private void setListaDeCarta(ArrayList<Carta> listaDeCarta) {
        this.listaDeCarta = listaDeCarta;
    }
    public Carta darCarta(){
        Carta cartaAux=null;
        if (!this.listaDeCarta.isEmpty()) {
            cartaAux = this.listaDeCarta.get(0);
            this.listaDeCarta.remove(0);
        }
        return cartaAux;
    }

    public void VerMazo(){
        int cont=0;
        for (Carta cartaAux: this.listaDeCarta) {
            System.out.println(cartaAux);
            cont++;
        }
        System.out.println("son todas las carta del mazo "+cont);
    }

    public void tomarCartaMazo(Carta carta){
        this.listaDeCarta.add(carta);
    }
}
