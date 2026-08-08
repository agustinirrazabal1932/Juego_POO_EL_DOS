package test;

import ar.edu.unlu.juego.modelo.Sesion;
import ar.edu.unlu.juego.modelo.excepciones.*;

public class TestSesion {
    public static void main(String[] args) {
        Sesion s = Sesion.getInstancia();
        String id = "test_" + System.currentTimeMillis();   // único por corrida

        try { s.registrar("N", "A", id, "1234"); } catch (JugadorExistente e) {}
        Check.check("Registra un jugador", s.existe(id));

        boolean dup = false;
        try { s.registrar("N", "A", id, "x"); } catch (JugadorExistente e) { dup = true; }
        Check.check("Registrar duplicado lanza JugadorExistente", dup);

        boolean noExiste = false;
        try { s.iniciarSesion("no_existe_xyz", "x"); }
        catch (JugadorNoExistente e) { noExiste = true; }
        catch (ContrasenaIncorrecta e) {}
        Check.check("Login de ID inexistente lanza JugadorNoExistente", noExiste);

        boolean badPass = false;
        try { s.iniciarSesion(id, "mal"); }
        catch (ContrasenaIncorrecta e) { badPass = true; }
        catch (JugadorNoExistente e) {}
        Check.check("Password incorrecta lanza PasswordIncorrecta", badPass);

        boolean ok = true;
        try { s.iniciarSesion(id, "1234"); } catch (Exception e) { ok = false; }
        Check.check("Login correcto no lanza", ok);

        Check.resumen();
    }
}