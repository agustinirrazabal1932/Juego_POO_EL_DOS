package ar.edu.unlu.juego.modelo;


import java.io.Serializable;

public class Validacion implements Serializable {
    private static final long serialVersionUID = 1L;
    private Carta cartaElegida1;
    private Carta cartaElegida2;
    private Carta cartaBocaArriba;

    public Validacion(Carta CartaElegida,Carta CartaBocaArriba){
        this.cartaElegida1 =CartaElegida;
        this.cartaBocaArriba =CartaBocaArriba;
    }

    public Validacion(Carta cartaElegida, Carta carta2Elegida, Carta cartaBocaArriba) {
        this.cartaElegida1 =cartaElegida;
        this.cartaElegida2=carta2Elegida;
        this.cartaBocaArriba =cartaBocaArriba;
    }

    public boolean validarSimple(){
        boolean juegoSimple=false;
        if (cartaBocaArriba.getNumero() == 0) {
            juegoSimple=true;
        } else if (cartaBocaArriba.getNumero() == 2) {
            int numeroEnteroCartaMano;

            if (cartaElegida1.getNumero()==0) {

                juegoSimple = true;

            } else {
                numeroEnteroCartaMano =cartaElegida1.getNumero();
                if (numeroEnteroCartaMano == 2) {
                    juegoSimple = true;
                }
            }

        } else {
            if (cartaElegida1.getNumero()==0) {
                juegoSimple = true;
            } else {
                int numeroEnteroCartaMano;
                numeroEnteroCartaMano = cartaElegida1.getNumero();
                if (numeroEnteroCartaMano == cartaBocaArriba.getNumero()) {
                    juegoSimple = true;
                }
            }
        }
        return juegoSimple;
    }

    public boolean validarColor() {
        boolean colorV=false;
        if (cartaBocaArriba.getNumero()==2){
            colorV=true;
        } else if (cartaElegida1.getNumero()==2) {
            colorV=true;
        } else if (cartaBocaArriba.getColor().equals(cartaElegida1.getColor())) {
            colorV=true;
        }
        return colorV;
    }

    public boolean validarDoble(boolean isComodin,int numeroCartaBocaArriba){
        boolean juegoDoble=false;
        if (isComodin) {
            //validar si la suma de la dos carta da el valor de la carta de la meza
            if (cartaElegida1.getNumero()==0) {
                if (cartaElegida2.getNumero()==0) {
                    juegoDoble=true;
                } else {
                    int numeroEnteroCartaMano2;
                    numeroEnteroCartaMano2 = cartaElegida2.getNumero();

                    //valido si la carta dos es menor a el valor de la carta de la meza porque
                    // la otra es comodin

                    if (numeroCartaBocaArriba > numeroEnteroCartaMano2) {
                        juegoDoble=true;
                    }
                }
            } else {
                int numeroEnteroCartaMano1;
                numeroEnteroCartaMano1 = cartaElegida1.getNumero();

                //valido si la segunda carta no es un comodin

                if (cartaElegida2.getNumero()==0) {
                    if (numeroCartaBocaArriba > numeroEnteroCartaMano1) {
                        juegoDoble=true;
                    }
                } else {
                    int numeroEnteroCartaMano2;
                    numeroEnteroCartaMano2 = cartaElegida2.getNumero();

                    //comparo al ser la dos carta normales si la suma da igual
                    // al valor de la carta de la mesa

                    if (numeroCartaBocaArriba == (numeroEnteroCartaMano1 + numeroEnteroCartaMano2)) {
                        juegoDoble=true;
                    }
                }
            }

        } else if (cartaBocaArriba.getNumero() == 2) {
            //validar si la suma de la dos carta da el valor de la carta de la meza
            if (cartaElegida1.getNumero()==0) {
                if (cartaElegida2.getNumero()==0) {
                    //valido si son los dos comodines
                    juegoDoble=true;
                } else {
                    int numeroEnteroCartaMano2;
                    numeroEnteroCartaMano2 = cartaElegida2.getNumero();
                    if (cartaBocaArriba.getNumero() > numeroEnteroCartaMano2) {
                        juegoDoble=true;
                    }
                }
            } else {
                int numeroEnteroCartaMano1;

                numeroEnteroCartaMano1 = cartaElegida1.getNumero();

                if (cartaElegida2.getNumero()==0) {
                    if (cartaBocaArriba.getNumero() > numeroEnteroCartaMano1) {
                        juegoDoble=true;
                    }
                } else {
                    int numeroEnteroCartaMano2;
                    numeroEnteroCartaMano2 = cartaElegida2.getNumero();
                    if (cartaBocaArriba.getNumero() == (numeroEnteroCartaMano2 + numeroEnteroCartaMano1)) {
                       juegoDoble=true;
                    }
                }
            }
        } else {

            //valido si son los dos comodines son la carta de mi mano

            if (cartaElegida1.getNumero()==0) {
                if (cartaElegida2.getNumero()==0) {
                    juegoDoble=true;
                } else {
                    int numeroEnteroCartaMano2;
                    numeroEnteroCartaMano2 = cartaElegida2.getNumero();

                    if (cartaBocaArriba.getNumero() > numeroEnteroCartaMano2) {
                        juegoDoble=true;
                    }
                }
            } else {
                int numeroEnteroCartaMano1;
                numeroEnteroCartaMano1 = cartaElegida1.getNumero();
                if (cartaElegida2.getNumero()==0) {
                    if (cartaBocaArriba.getNumero() > numeroEnteroCartaMano1) {
                        juegoDoble=true;
                    }
                } else {
                    int numeroEnteroCartaMano2;
                    numeroEnteroCartaMano2 = cartaElegida2.getNumero();

                    if (cartaBocaArriba.getNumero() == (numeroEnteroCartaMano2 + numeroEnteroCartaMano1)) {
                        juegoDoble=true;
                    }
                }
            }
        }

        return juegoDoble;
    }

    public boolean validarColorDoble() {
        boolean colorV=false;
        if (cartaBocaArriba.getNumero()==2){
            if (cartaElegida1.getNumero()==2){
                colorV=true;
            }else if (cartaElegida2.getNumero()==2){
                colorV=true;
            }else {
                if (cartaElegida1.getColor().equals(cartaElegida2.getColor())){
                    colorV=true;
                }
            }

        } else if (cartaElegida1.getNumero()==2) {
            if (cartaElegida2.getNumero()==2){
                colorV=true;
            }else {
                if (cartaBocaArriba.getColor().equals(cartaElegida2.getColor())) {
                    colorV = true;
                }
            }
        } else if (cartaElegida2.getNumero()==2){
            if (cartaBocaArriba.getColor().equals(cartaElegida1.getColor())) {
                colorV = true;
            }
        }
        else if (cartaBocaArriba.getColor().equals(cartaElegida1.getColor())
                && cartaBocaArriba.getColor().equals(cartaElegida2.getColor())) {
            colorV=true;
        }
        return colorV;
    }
}
