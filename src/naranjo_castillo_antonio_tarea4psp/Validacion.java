/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package naranjo_castillo_antonio_tarea4psp;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Proporciona métodos de utilidad para validar el formato de los datos del
 * usuario.
 *
 * Verifica mediante expresiones regulares que la información introducida en los
 * formularios, como el correo electrónico o la contraseña, cumple con los
 * requisitos mínimos de seguridad y estructura establecidos.
 *
 * @author Antonio Naranjo Castillo
 * @version 1.0
 */
public class Validacion {

    /**
     * Expresión regular que define la estructura correcta de un correo
     * electrónico.
     *
     * El patrón exige una secuencia de caracteres alfanuméricos, un símbolo de
     * arroba @, el nombre del dominio y una extensión final de al menos dos
     * letras.
     *
     */
    private static final String PATRON_EMAIL = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";

    /**
     * Expresión regular que define la estructura correcta de una contraseña
     * segura.
     *
     * El patrón exige que la contraseña tenga un tamaño mínimo de seis
     * caracteres y esté compuesta exclusivamente por letras y números.
     *
     */
    private static final String PATRON_PASSWORD = "[a-zA-Z0-9]{6,}";

    /**
     * Comprueba si el correo electrónico introducido tiene un formato válido.
     *
     * Compara el texto recibido con el patrón de correo electrónico estándar
     * para confirmar que contiene los elementos obligatorios de una dirección
     * web.
     *
     * @param email Texto que contiene la dirección de correo que se desea
     * evaluar.
     * @return El valor lógico true si el texto cumple con la estructura de
     * correo electrónico, o false si el texto es nulo o incorrecto.
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
     * Comprueba si la contraseña introducida cumple con los requisitos mínimos.
     *
     * Compara el texto recibido con el patrón de seguridad para confirmar que
     * posee la longitud y los caracteres alfanuméricos necesarios para el
     * registro.
     *
     * @param password Texto que contiene la clave secreta que se desea evaluar.
     * @return El valor lógico true si la clave cumple con el patrón
     * establecido, o false si el texto es nulo o demasiado corto.
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
