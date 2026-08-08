package ar.edu.unlu.juego.vista.consola;

import ar.edu.unlu.juego.controlador.Controlador;
import ar.edu.unlu.juego.modelo.excepciones.JugadorExistente;
import ar.edu.unlu.juego.modelo.excepciones.JugadorNoExistente;
import ar.edu.unlu.juego.modelo.excepciones.ContrasenaIncorrecta;

public class FlujoMenuInicial extends Flujo {

    private enum estados {
        MENSAJE_ENTRADA,
        INGRESAR_ID, INGRESAR_PASSWORD,                       // login
        INGRESAR_REGISTRO, INGRESAR_APELLIDO, INGRESAR_NUEVO_ID, INGRESE_NUEVO_CONTRA  // registro
    }

    private estados estadosActual = estados.MENSAJE_ENTRADA;
    private String nombre;
    private String apellido;
    private String id;     // id en proceso (sirve para login y para registro)

    public FlujoMenuInicial(ConsolaGrafica vista, Controlador controlador) {
        super(vista, controlador);
    }

    @Override
    public Flujo procesarEntrada(String string) {
        switch (estadosActual) {
            case MENSAJE_ENTRADA -> procesarSiEstaRegistrado(string);
            case INGRESAR_ID -> procesarID(string);
            case INGRESAR_PASSWORD -> {
                return procesarPasswordLogin(string);
            }
            case INGRESAR_REGISTRO -> procesarRegistro(string);
            case INGRESAR_APELLIDO -> procesarApellido(string);
            case INGRESAR_NUEVO_ID -> procesarNuevoID(string);
            case INGRESE_NUEVO_CONTRA -> {
                return procesarNuevoContra(string);
            }
        }
        return this;
    }

    private void procesarSiEstaRegistrado(String string) {
        switch (string) {
            case "1" -> estadosActual = estados.INGRESAR_ID;
            case "2" -> estadosActual = estados.INGRESAR_REGISTRO;
            default -> vista.println("Opción inválida");
        }
    }

    // ===== LOGIN =====
    private void procesarID(String string) {
        if (string.isEmpty()) {
            vista.println("----- NO PUEDE INGRESAR ID VACIO -----");
        } else if (!controlador.encontrarJugador(string)) {
            vista.println("----- EL ID NO ESTA REGISTRADO -----");
            estadosActual = estados.MENSAJE_ENTRADA;      // vuelvo al inicio sin pedir contraseña
        } else {
            id = string;                                  // existe: guardo y pido contraseña
            estadosActual = estados.INGRESAR_PASSWORD;
        }
    }

    private Flujo procesarPasswordLogin(String string) {
        try {
            controlador.iniciarSesion(id, string);        // valida existencia + contraseña
            vista.println("----- BIENVENIDO " + id + " -----");
            return new FlujoMenuPrincipal(vista, controlador, id);
        } catch (JugadorNoExistente e) {
            vista.println("----- EL ID NO ESTA REGISTRADO -----");
            estadosActual = estados.MENSAJE_ENTRADA;
        } catch (ContrasenaIncorrecta e) {
            vista.println("----- CONTRASEÑA INCORRECTA -----");
            estadosActual = estados.INGRESAR_ID;
        }
        return this;
    }

    // ===== REGISTRO =====
    private void procesarRegistro(String string) {
        if (string.isEmpty()) {
            vista.println("----- NO PUEDE INGRESAR NOMBRE VACIO -----");
        } else {
            nombre = string;
            estadosActual = estados.INGRESAR_APELLIDO;
        }
    }

    private void procesarApellido(String string) {
        if (string.isEmpty()) {
            vista.println("----- NO PUEDE INGRESAR APELLIDO VACIO -----");
        } else {
            apellido = string;
            estadosActual = estados.INGRESAR_NUEVO_ID;
        }
    }

    private void procesarNuevoID(String string) {
        if (string.isEmpty()) {
            vista.println("----- NO PUEDE INGRESAR ID VACIO -----");
        } else if (controlador.encontrarJugador(string)) {
            vista.println("----- EL ID ESTA REGISTRADO POR OTRO JUGADOR -----");
        } else {
            id = string;
            estadosActual = estados.INGRESE_NUEVO_CONTRA;
        }
    }

    private Flujo procesarNuevoContra(String string) {
        if (string.isEmpty()) {
            vista.println("----- NO PUEDE INGRESAR CONTRASEÑA VACIA -----");
            return this;
        }
        try {
            controlador.cargarUsuario(nombre, apellido, id, string);
            vista.println("----- REGISTRO EXITOSO -----");
            return new FlujoMenuPrincipal(vista, controlador, id);   // <-- id, NO la contraseña
        } catch (JugadorExistente e) {
            vista.println("----- ESE ID YA EXISTE -----");
            estadosActual = estados.INGRESAR_NUEVO_ID;
            return this;
        }
    }

    @Override
    public void mostarSiguienteTexto() {
        switch (estadosActual) {
            case MENSAJE_ENTRADA -> {
                vista.println("----- BIENVENIDO AL JUEGO EL DOS -----");
                vista.println("----- ¿ESTA REGISTRADO EN EL JUEGO? -----");
                vista.println("----- 1. SI");
                vista.println("----- 2. NO");
            }
            case INGRESAR_ID -> vista.println("----- INGRESE SU ID ABAJO -----");
            case INGRESAR_PASSWORD -> vista.println("----- INGRESE SU CONTRASEÑA ABAJO -----");
            case INGRESAR_REGISTRO -> vista.println("----- INGRESE SU NOMBRE ABAJO -----");
            case INGRESAR_APELLIDO -> vista.println("----- INGRESE SU APELLIDO ABAJO -----");
            case INGRESAR_NUEVO_ID -> vista.println("----- INGRESE EL ID QUE VA A USAR ABAJO -----");
            case INGRESE_NUEVO_CONTRA -> vista.println("----- INGRESE UNA CONTRASEÑA ABAJO -----");
        }
    }
}
