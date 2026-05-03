/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package naranjo_castillo_antonio_tarea4psp;

/**
 * Almacena los datos de la sesión activa de un usuario en la aplicación.
 *
 * Guarda de forma temporal la información sobre las últimas matrículas
 * utilizadas y lleva la cuenta del número de operaciones que realiza el
 * usuario.
 *
 * @author Antonio Naranjo Castillo
 * @version 1.1
 */
public class Sesion {

    /**
     * Guarda el texto de la última matrícula utilizada para una reserva.
     */
    private String ultimaMatriculaReservada;
    /**
     * Guarda el texto de la última matrícula utilizada al pasar la inspección.
     */
    private String ultimaMatriculaPasarITV;

    /**
     * Cuenta la cantidad de reservas de cita realizadas por el usuario.
     */
    private int numReservas;
    /**
     * Cuenta la cantidad de inspecciones de ITV completadas por el usuario.
     */
    private int numInspecciones;

    /**
     * Crea una nueva sesión de usuario con valores por defecto.
     *
     * Inicializa las matrículas como cadenas vacías y establece los contadores
     * de operaciones a cero.
     */
    public Sesion() {
        this.ultimaMatriculaReservada = "";
        this.ultimaMatriculaPasarITV = "";
        this.numReservas = 0;
        this.numInspecciones = 0;
    }

    /**
     * Devuelve la última matrícula que el usuario ha reservado.
     *
     * @return El texto con la matrícula de la última reserva.
     */
    public String getUltimaMatriculaReservada() {
        return ultimaMatriculaReservada;
    }

    /**
     * Actualiza la última matrícula reservada por el usuario.
     *
     * @param ultimaMatriculaReservada El nuevo texto de la matrícula reservada.
     */
    public void setUltimaMatriculaReservada(String ultimaMatriculaReservada) {
        this.ultimaMatriculaReservada = ultimaMatriculaReservada;
    }

    /**
     * Devuelve la última matrícula con la que el usuario ha pasado la inspección.
     *
     * @return El texto con la matrícula de la última inspección.
     */
    public String getUltimaMatriculaPasarITV() {
        return ultimaMatriculaPasarITV;
    }

    /**
     * Actualiza la última matrícula con la que se pasa la inspección.
     *
     * @param ultimaMatriculaPasarITV El nuevo texto de la matrícula de inspección.
     */
    public void setUltimaMatriculaPasarITV(String ultimaMatriculaPasarITV) {
        this.ultimaMatriculaPasarITV = ultimaMatriculaPasarITV;
    }

   /**
     * Devuelve el número de reservas totales acumuladas por el usuario.
     *
     * @return La cantidad de reservas realizadas.
     */
    public int getNumReservas() {
        return numReservas;
    }

    /**
     * Actualiza el número de reservas totales acumuladas por el usuario.
     *
     * @param numReservas El nuevo valor numérico del contador de reservas.
     */
    public void setNumReservas(int numReservas) {
        this.numReservas = numReservas;
    }

    /**
     * Devuelve el número de inspecciones totales acumuladas por el usuario.
     *
     * @return La cantidad de inspecciones realizadas.
     */
    public int getNumInspecciones() {
        return numInspecciones;
    }

    /**
     * Actualiza el número de inspecciones totales acumuladas por el usuario.
     *
     * @param numInspecciones El nuevo valor numérico del contador de inspecciones.
     */
    public void setNumInspecciones(int numInspecciones) {
        this.numInspecciones = numInspecciones;
    }

    /**
     * Aumenta en uno el contador de reservas de la sesión.
     */
    public void incrementarReservas() {
        this.numReservas++;
    }

    /**
     * Aumenta en uno el contador de inspecciones de la sesión.
     */
    public void incrementarInspecciones() {
        this.numInspecciones++;
    }
}
