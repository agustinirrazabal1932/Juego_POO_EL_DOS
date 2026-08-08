package ar.edu.unlu.juego.vista.consola;

import ar.edu.unlu.juego.controlador.Controlador;

public class FlujoPerdi extends Flujo{
    private String ganador;
    private String perdi;
    public FlujoPerdi(ConsolaGrafica vista, Controlador controlador,String idGanador,String perdi) {
        super(vista, controlador);
        this.ganador=idGanador;
        this.perdi=perdi;
    }

    @Override
    public Flujo procesarEntrada(String string) {
        switch (perdi){
            case "juego" ->{
                return new FlujoMenuPrincipal(vista,controlador, controlador.devolverID());
            }
            case "partida"->{
                return new FlujoEsperar(vista,controlador);
            }
        }
        return this;
    }

    @Override
    public void mostarSiguienteTexto() {
        switch (perdi) {
            case "juego"-> {
                vista.println("---------------------------------------------------------");
                vista.println("----- PERDISTE EL JUEGO, EL GANADOR FUE " + ganador + " ------");
                vista.println("----- PRESIONE ENTER PARA VOLVER AL MENU PRINCIPAL -----");
                vista.println("---------------------------------------------------------");
            }
            case "partida"->{
                vista.println("---------------------------------------------------------");
                vista.println("----- PERDISTE LA PARTIDA, EL GANADOR FUE " + ganador + " ------");
                vista.println("----- PRESIONE ENTER PARA VOLVER A ESPERAR SU TURNO -----");
                vista.println("---------------------------------------------------------");
            }
        }


    }
}
