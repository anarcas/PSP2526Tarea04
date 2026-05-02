/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package naranjo_castillo_antonio_tarea4psp;

/**
 * Representa la sesión activa de un usuario autenticado.
 * Almacena el email del usuario, las últimas matrículas
 * utilizadas en las operaciones de reserva e inspección,
 * y los contadores de operaciones realizadas.
 *
 * @author Antonio Naranjo Castillo
 * @version 1.1
 */
public class Sesion {
    private String ultimaMatriculaReservada;
    private String ultimaMatriculaPasarITV;
    
    // Nuevas variables para contar las operaciones
    private int numReservas;
    private int numInspecciones;

    public Sesion() {
        this.ultimaMatriculaReservada = "";
        this.ultimaMatriculaPasarITV = "";
        this.numReservas = 0;
        this.numInspecciones = 0;
    }

    // Getters y Setters de las matrículas
    public String getUltimaMatriculaReservada() {
        return ultimaMatriculaReservada;
    }

    public void setUltimaMatriculaReservada(String ultimaMatriculaReservada) {
        this.ultimaMatriculaReservada = ultimaMatriculaReservada;
    }

    public String getUltimaMatriculaPasarITV() {
        return ultimaMatriculaPasarITV;
    }

    public void setUltimaMatriculaPasarITV(String ultimaMatriculaPasarITV) {
        this.ultimaMatriculaPasarITV = ultimaMatriculaPasarITV;
    }

    // Getters y Setters de los nuevos contadores
    public int getNumReservas() {
        return numReservas;
    }

    public void setNumReservas(int numReservas) {
        this.numReservas = numReservas;
    }

    public int getNumInspecciones() {
        return numInspecciones;
    }

    public void setNumInspecciones(int numInspecciones) {
        this.numInspecciones = numInspecciones;
    }

    // Métodos de utilidad para incrementar los contadores de uno en uno
    public void incrementarReservas() {
        this.numReservas++;
    }

    public void incrementarInspecciones() {
        this.numInspecciones++;
    }
}