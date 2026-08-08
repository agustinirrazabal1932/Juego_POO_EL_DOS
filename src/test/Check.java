package test;

public class Check {
    static int pass = 0, fail = 0;

    public static void check(String nombre, boolean cond) {
        if (cond) { pass++; System.out.println("  OK   " + nombre); }
        else      { fail++; System.out.println(" FAIL  " + nombre); }
    }

    public static void resumen() {
        System.out.println("\n==> " + pass + " OK, " + fail + " FAIL");
        if (fail > 0) System.out.println("    Revisá los FAIL de arriba.");
    }
}