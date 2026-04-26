/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package naranjo_castillo_antonio_tarea4psp;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *
 * @author anarcas
 */
public class Validacion {

    // Patrón email: texto@texto.texto
    private static final String PATRON_EMAIL = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";

    // Patrón contraseña: mínimo 6 caracteres alfanuméricos
    private static final String PATRON_PASSWORD = "[a-zA-Z0-9]{6,}";

    /**
     * Valida si el email tiene un formato correcto
     *
     * @param email email a validar
     * @return true si el formato es correcto, false en caso contrario
     */
    public static boolean validarEmail(String email) {
        boolean valido = false;

        if (email != null) {
            Pattern pat = Pattern.compile(PATRON_EMAIL);
            Matcher mat = pat.matcher(email);
            valido = mat.matches();
        }

        return valido;
    }

    /**
     * Valida si la contraseña cumple el patrón mínimo de seguridad
     *
     * @param password contraseña a validar
     * @return true si cumple el patrón, false en caso contrario
     */
    public static boolean validarPassword(String password) {
        boolean valido = false;

        if (password != null) {
            Pattern pat = Pattern.compile(PATRON_PASSWORD);
            Matcher mat = pat.matcher(password);
            valido = mat.matches();
        }

        return valido;
    }
}
