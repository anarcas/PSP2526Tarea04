/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package naranjo_castillo_antonio_tarea4psp;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controla el acceso a los recursos del taller mediante monitorización.
 * Garantiza que solo un número máximo de vehículos ocupen las líneas
 * simultáneamente.
 *
 * @author Antonio Naranjo Castillo
 *
 * @version 1.0
 */
public class Itv {

    private final int MAX_LINEAS = 4;
    private int cochesDentro = 0;
    // Mapa concurrente compartido entre hilos la clave será la matrícula del vehículo y el valor la línea de inspección asignada (1 a 4)
    private static final ConcurrentHashMap<String, Integer> citas = new ConcurrentHashMap<>();
    // Constante que indica si un vehículo se encuentra en espera cuando se registra en el mapa citas, se le atribuye el valor 0
    private static final int PENDIENTE = 0;

    private final String[] pruebas = {
        "Luces", "Frenos", "Emisiones", "Dirección", "Suspensión"
    };

    private final String[] frasesProhibidas = {
        "ok jefe", "lo que tú digas", "a mandar", "como usted mande",
        "vamos al lío", "marchando", "manda usted", "perfecto máquina",
        "de lujo"
    };

    private final String[] frasesPermitidas = {
        "vale", "recibido", "entendido", "procedo", "hecho",
        "si", "correcto", "ok", "de acuerdo"
    };

    // Expresión regular que valida el formato de matrícula
    private static final String REGEX_MATRICULA = "^\\d{4}\\s?[A-Z]{3}$";

    /**
     * Método que valida el formato de matrícula.
     *
     * @param matricula La cadena de texto que representa la matrícula a
     * validar.
     * @return Booleano si la matrícula es válida y no está vacía o nula.
     */
    public static boolean validarMatricula(String matricula) {
        boolean validacion;

        if (matricula == null || matricula.isBlank()) {
            validacion = false;
        } else {
            validacion = matricula.matches(Itv.REGEX_MATRICULA);
        }

        return validacion;
    }

    // Método getter y setter de la variable cochesDentro
    public int getCochesDentro() {
        return cochesDentro;
    }

    public void setCochesDentro(int cochesDentro) {
        this.cochesDentro = cochesDentro;
    }

    /**
     * @return El número máximo de líneas permitidas.
     */
    public int getMAX_LINEAS() {
        return MAX_LINEAS;
    }

    /**
     * @return Estado de espera de los vehículos incluidos de inicio en el mapa
     * citas.
     */
    public static int getPENDIENTE() {
        return PENDIENTE;
    }

    /**
     * Método getter para acceder al mapa de citas.
     *
     * @return Mapa concurrente de matrículas y su estado/línea.
     */
    public static ConcurrentHashMap<String, Integer> getCitas() {
        return citas;
    }

    /**
     * Simula el proceso de inspección técnica del vehículo. Gestiona la entrada
     * a línea, los tiempos de espera y el cálculo de probabilidades.
     *
     * @param matricula Identificador del vehículo a inspeccionar.
     * @return Fragmento HTML con el resultado de las pruebas.
     * @throws InterruptedException Si se interrumpe el sueño del hilo.
     */
    public String inspeccionar(String matricula) throws InterruptedException {

        // El hilo intenta entrar en la línea de inspección quedando en espera si están todas ocupadas y actualizando variable contador de coches si entra
        intentarEntrar();
        int numLinea = asignarLineaLibre(matricula);
        System.out.println("Coche " + matricula + " en línea " + numLinea);

        int probabilidad = 60;
        Map<String, String> resultados = new LinkedHashMap<>();
        Random rand = new Random();

        // Simulación de pruebas
        for (String prueba : pruebas) {

            // Espera aleatoria entre 1 y 10 segundos
            int tiempoEspera = 1000 + rand.nextInt(9000);
            Thread.sleep(tiempoEspera);

            String respuesta = fraseAleatoria();

            // Penalización por frases prohibidas
            for (String frase : frasesProhibidas) {
                if (frase.equalsIgnoreCase(respuesta)) {
                    probabilidad -= 10;
                }
            }

            boolean ok = nuevaProbabilidad(probabilidad);
            resultados.put(prueba, (ok ? "Si" : "No") + " (\"" + respuesta + "\" - prob " + probabilidad + "%)");
        }

        // Se genera el resultado de la inspección
        String resultadoHTML = PaginasHTML.generarResultadoHTML(matricula, resultados);

        // Se muestran los resultados por consola
        System.out.println("\n--- Resultado " + Thread.currentThread().getName() + " ---");
        System.out.println("Matrícula: " + matricula);
        for (Map.Entry<String, String> entry : resultados.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("--------------------------------------\n");

        // Se da salida al vehículo y se libera la línea de inspección
        liberarLinea(matricula);
        salir();

        return resultadoHTML;
    }

    // Método que devuelve una nueva probabilidad
    private boolean nuevaProbabilidad(int prob) {
        return Math.random() * 100 < prob;
    }

    // Método que devuelve la frase aleatoria en función de una determinada probabilidad
    private String fraseAleatoria() {
        String frase;
        Random random = new Random();

        if (Math.random() < 0.7) {
            frase = frasesPermitidas[random.nextInt(frasesPermitidas.length)];
        } else {
            frase = frasesProhibidas[random.nextInt(frasesProhibidas.length)];
        }

        return frase;
    }

    /**
     * Bloquea el hilo si las líneas de inspección están llenas. Incrementa el
     * contador de ocupación cuando un hueco queda libre.
     *
     * @throws InterruptedException Si falla el mecanismo de espera.
     */
    public synchronized void intentarEntrar() throws InterruptedException {
        // El hilo coche queda a la espera hasta que una línea quede libre
        while (cochesDentro >= this.getMAX_LINEAS()) {
            System.out.println(Thread.currentThread().getName() + " esperando para entrar.");
            wait();
        }
        // Se actualiza la variable cochesDentro
        this.setCochesDentro(this.getCochesDentro() + 1);
    }

    /**
     * Libera un puesto en el taller y notifica a los hilos en espera.
     */
    public synchronized void salir() {
        // Se actualiza la variable cochesDentro
        this.setCochesDentro(this.getCochesDentro() - 1);
        // Se notifica a todos los hilos que la línea queda liberada
        notifyAll();
    }

    /**
     * Elimina el registro de un vehículo del mapa de seguimiento.
     *
     * @param matricula Matrícula del vehículo que abandona el panel.
     */
    public void liberarLinea(String matricula) {
        // En la tarea anterior lo tenía sincronizado, y no era necesario porque "citas" es un mapa ConcurrentHashMap donde se gestiona la concurrencia por sí mismo.
        citas.remove(matricula);
    }

    /**
     * Busca el primer identificador de línea disponible y lo asigna al
     * vehículo.
     *
     * @param matricula Matrícula registrada.
     * @return El número de línea asignado (1-4) o -1 si no hay hueco.
     */
    public int asignarLineaLibre(String matricula) {
        // Se asigna inicialmente -1 para simular estado sin línea asignada
        int lineaAsignada = -1;

        // Se comprueba si la matrícula del vehículo existe en el mapa citas
        if (Itv.getCitas().containsKey(matricula)) {

            // Ver qué líneas están ocupadas (1 a 4)
            Set<Integer> ocupadas = new HashSet<>(Itv.getCitas().values());

            // Se actualiza la cita asignando a la matrícula el número de línea libre
            for (int i = 1; i <= this.getMAX_LINEAS(); i++) {
                if (!ocupadas.contains(i)) {
                    Itv.getCitas().put(matricula, i);
                    lineaAsignada = i;
                    break;
                }
            }
        }
        // Todas llenas
        return lineaAsignada;
    }

    /**
     * Genera el fragmento HTML que representa el panel de control en tiempo
     * real.
     *
     * @return Código HTML con el estado de las líneas y las citas pendientes.
     */
    public String generarPanel() {
        StringBuilder sb = new StringBuilder();

        // --- Citas (texto simple arriba del panel)
        sb.append("<div style='color:white; font-size:18px;'>");
        sb.append("<strong>CITAS</strong><br>");
        sb.append("-----------------------------<br>");

        for (Map.Entry<String, Integer> entry : Itv.getCitas().entrySet()) {
            // Se añade al texto HTML solo si la matrícula se encuentra en estado pendiente dentro del mapa citas
            if (entry.getValue() == Itv.getPENDIENTE()) {
                sb.append(entry.getKey())
                        .append("<br>");
            }
        }
        sb.append("<br><strong>LINEAS DE INSPECCIÓN</strong><br>")
                .append("-----------------------------<br><br>")
                .append("</div>");

        // --- Panel principal tipo LED
        // Solo se manejan las líneas hábiles
        for (int i = 1; i <= this.getMAX_LINEAS(); i++) {
            String matricula = "LIBRE";
            boolean libre = true;
            boolean encontrada = false;

            for (Map.Entry<String, Integer> entry : Itv.getCitas().entrySet()) {

                if (!encontrada && entry.getValue() == i) {
                    matricula = entry.getKey();
                    libre = false;
                    encontrada = true;
                }
            }

            String color = libre ? "verde" : "rojo";

            sb.append("""
                <div class="linea">
                    <div class="%s">%s</div>
                    <div class="%s">%d</div>
                </div>
            """.formatted(color, matricula, color, i));
        }

        return sb.toString();
    }

}
