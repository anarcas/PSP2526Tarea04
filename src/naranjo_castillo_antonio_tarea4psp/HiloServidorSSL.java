/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package naranjo_castillo_antonio_tarea4psp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.SSLSocket;
import java.util.logging.Logger;

/**
 * Procesa las peticiones HTTP entrantes de forma concurrente. Implementa la
 * interfaz Runnable para permitir que cada cliente sea atendido en un hilo
 * propio e independiente.
 *
 * En su método run se ejecuta el procesamiento de la petición HTTP. Lee la
 * cabecera, extrae el cuerpo en peticiones POST y deriva el flujo según la
 * ruta.
 *
 * @author Antonio Naranjo Castillo
 * @version 1.0
 */
public class HiloServidorSSL implements Runnable {

    // Declaración de variables inmutables
    private final int OK = 200;
    private final int NOTFOUND = 404;

    private final SSLSocket socket;
    private final Logger logger = Logger.getLogger("MiLog");

    private final Itv itv;

    /**
     * Constructor de la clase. Inicializa el hilo con el socket del cliente y
     * el recurso compartido.
     *
     * @param cliente Conexión activa con el cliente.
     * @param itv Recurso compartido para la gestión de líneas.
     */
    public HiloServidorSSL(SSLSocket cliente, Itv itv) {
        this.socket = cliente;
        this.itv = itv;
    }

    @Override
    public void run() {

        try (Socket s = this.socket; BufferedReader entrada = new BufferedReader(new InputStreamReader(s.getInputStream())); PrintWriter salida = new PrintWriter(s.getOutputStream(), true)) {

            // Se recoge la petición
            String peticion = entrada.readLine();
            System.out.println("Petición: " + peticion);

            // Se procesan las peticiones tipo GET o POST (y que no son nulas)
            if (peticion != null && (peticion.startsWith("GET") || peticion.startsWith("POST"))) {
                // Se recoge la ruta
                String ruta = peticion.split(" ")[1];
                System.out.println("Ruta: " + ruta);

                // Lectura de cabeceras
                int contentLength = 0;
                String linea;

                while (!(linea = entrada.readLine()).isBlank()) {

                    if (linea.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(linea.split(":")[1].trim());
                    }
                    System.out.println("Metadato: " + linea);
                }
                System.out.println("Content-Length = " + contentLength);
                System.out.println("Línea en blanco");

                // Lectura del cuerpo
                StringBuilder cuerpo = new StringBuilder();

                if (contentLength > 0) {

                    for (int i = 0; i < contentLength; i++) {
                        cuerpo.append((char) entrada.read());

                    }

                }
                System.out.println("Cuerpo: " + cuerpo);

                // Comienza el flujo de trabajo, se atienden las peticiones
                String respuestaHTML;

                if (ruta.equals("/")) {

                    // Página principal se muestra el formulario vacío
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.login(""));

                } else if (ruta.equals("/inicio") && peticion.startsWith("POST")) {
                    String email = cuerpo.toString().split("&")[0].split("=")[1].replace("%40", "@");
                    String password = cuerpo.toString().split("&")[1].split("=")[1];
                    System.out.println("Debug --> usuario: " + email + " | contraseña: " + password);

                    if (Cifrado.usuarioExiste(email) && Cifrado.credencialesCorrectas(email, password)) {
                        respuestaHTML = construirRespuesta(200, PaginasHTML.htmlIndex(itv.generarPanel()));

                    } else {
                        logger.warning("Login incorrecto: " + email);
                        respuestaHTML = construirRespuesta(200, PaginasHTML.login("Usuario o contraseña incorrectos"));
                    }

                } else if (ruta.equals("/inicio") && peticion.startsWith("GET")) {

                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlIndex(itv.generarPanel()));

                } else if (ruta.equals("/registro") && peticion.startsWith("POST")) {
                    String email = cuerpo.toString().split("&")[0].split("=")[1].replace("%40", "@");
                    String password = cuerpo.toString().split("&")[1].split("=")[1];
                    System.out.println("Debug --> usuario: " + email + " | contraseña: " + password);

                    if (!Validacion.validarEmail(email)) {
                        // Email con formato incorrecto
                        logger.warning("Usuario (email) no cumple requisitos : " + email);
                        respuestaHTML = construirRespuesta(200, PaginasHTML.login("El formato del email no es válido"));

                    } else if (!Validacion.validarPassword(password)) {
                        // Contraseña que no cumple el patrón
                        logger.warning("Contraseña no cumple requisitos: " + password);
                        respuestaHTML = construirRespuesta(200, PaginasHTML.login("La contraseña debe tener mínimo 6 caracteres alfanuméricos"));

                    } else if (Cifrado.usuarioExiste(email)) {
                        // El usuario ya está registrado
                        logger.warning("Usuario (email) ya existe en el registro : " + email);
                        respuestaHTML = construirRespuesta(200, PaginasHTML.login("El usuario ya está registrado"));

                    } else {
                        // Todo correcto → registramos
                        String lineaUsuario = email + ":" + password;
                        if (Cifrado.cifrarBCrypt(lineaUsuario)) {
                            respuestaHTML = construirRespuesta(200,
                                    PaginasHTML.login("Registro exitoso, ya puedes iniciar sesión"));
                        } else {
                            respuestaHTML = construirRespuesta(200,
                                    PaginasHTML.login("Error en el registro, inténtalo de nuevo"));
                        }
                    }

                    // Se muestra la página de reservas
                } else if (ruta.startsWith("/reservar") && peticion.startsWith("GET")) {

                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlReservar(""));

                    // Se recogen las matrículas de la página de reservas y se almacenan en un mapa, finalmente se muestra la página principal para ver que se encuentran en lista de espera
                } else if (ruta.startsWith("/reservar") && peticion.startsWith("POST")) {

                    // Firefox cambia el espacio en blanco por +
                    String matricula = cuerpo.toString().split("=")[1].replace('+', ' ').trim().toUpperCase();
                    String mensajeError = "";

//                    // Debug --> Ver caracteres individuales, Firefox cambia el espacio por +
//                    for (char c : matricula.toCharArray()) {
//                        System.out.println("Char: " + c + " (Código: " + (int) c + ")");
//                    }
                    // Primero se valida el formato de la matrícula
                    if (!Itv.validarMatricula(matricula)) {
                        mensajeError = "<p style='color:yellow;'>ERROR: Formato de matrícula "
                                + "inválido. Use el formato 1234 ABC.</p>";

                        // Si el formato es correcto, se comprueba que no esté ya registrada
                    } else if (Itv.getCitas().containsKey(matricula)) {
                        mensajeError = "<p style='color:yellow;'>ERROR: La matrícula "
                                + matricula + " ya tiene una cita registrada.</p>";
                    }

                    // Si hay cualquier error se devuelve el formulario con el mensaje
                    if (!mensajeError.isEmpty()) {
                        respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlReservar(mensajeError));

                        // Si todo es correcto se registra y se redirige al panel principal
                    } else {
                        Itv.getCitas().put(matricula, Itv.getPENDIENTE());
                        respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlIndex(itv.generarPanel()));
                    }

                    // Se muestra la página para pasar la ITV a los coches a inspeccionar
                } else if (ruta.startsWith("/pasar") && peticion.startsWith("GET")) {

                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlPasarITV("", ""));

                    // Se recogen la matrícula del coche que se envía a inspeccionar
                } else if (ruta.startsWith("/pasar") && peticion.startsWith("POST")) {

                    System.out.println("Linea separación: " + cuerpo.toString());

                    String matriculaSeleccionada = cuerpo.toString().split("&")[0].split("=")[1].replace('+', ' ').trim().toUpperCase();
                    System.out.println("Matrícula seleccionada: " + matriculaSeleccionada);

                    // Se verifica que existe tal reserva, si la matrícula seleccionada se encuentra en el mapa citas
                    if (Itv.getCitas().containsKey(matriculaSeleccionada)) {
                        // Este método bloquea el vehículo hasta que haya hueco en una línea desocupada y devuelve el resultado de la inspección en la misma página pasar ITV
                        String mensajeInspeccion = itv.inspeccionar(matriculaSeleccionada);
                        respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlPasarITV(matriculaSeleccionada,
                                PaginasHTML.htmlResultado(mensajeInspeccion)));

                        // Si la matrícula no se encuentra en el mapa citas se devuelve un mensaje de error
                    } else {
                        String respuestaHTMLerror = "<p style='color:yellow;'>ERROR: Matrícula" + matriculaSeleccionada + " no encontrada en la lista de reservas.</p>";
                        respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlPasarITV(matriculaSeleccionada, respuestaHTMLerror));
                    }

                    // Para otros casos se lanza página no encontrada
                } else {

                    respuestaHTML = construirRespuesta(NOTFOUND, PaginasHTML.html_notFound);

                }

                System.out.println("RespuestaHTML: \n" + respuestaHTML);

                salida.print(respuestaHTML);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Genera la cabecera HTTP y concatena el contenido HTML.
     *
     * @param codigo Código de estado HTTP.
     * @param contenido Cuerpo del mensaje en formato HTML.
     * @return Cadena completa con el protocolo HTTP.
     */
    private static String construirRespuesta(int codigo, String contenido) {
        return (codigo == 200 ? "HTTP/1.1 200 OK" : "HTTP/1.1 404 Not Found") + "\r\n" // Línea inicial
                + "Content-Type: text/html; charset=UTF-8" + "\r\n" // Metadatos
                + "Content-Length: " + contenido.getBytes(StandardCharsets.UTF_8).length + "\r\n" // getBytes para longitud exacta
                + "\r\n" // Línea vacía
                + contenido;
    }

}
