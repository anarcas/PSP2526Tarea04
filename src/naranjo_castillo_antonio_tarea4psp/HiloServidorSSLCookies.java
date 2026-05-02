/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package naranjo_castillo_antonio_tarea4psp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;

/**
 * Procesa las peticiones HTTP entrantes de forma concurrente. Implementa la
 * interfaz Runnable para permitir que cada cliente sea atendido en un hilo
 * propio e independiente.
 *
 * En su método run se ejecuta el procesamiento de la petición HTTP. Lee la
 * cabecera, extrae el cuerpo en peticiones POST y deriva el flujo según la
 * ruta. Incorpora control de sesión mediante cookies HTTP.
 *
 * @author Antonio Naranjo Castillo
 * @version 1.0
 */
public class HiloServidorSSLCookies implements Runnable {

    // Códigos de estado HTTP
    private static final int OK = 200;
    private static final int NOTFOUND = 404;
    private static final int UNAUTH = 401;

    private final SSLSocket socket;
    private final Logger logger = Logger.getLogger("MiLog");
    private final Itv itv;

    // Mapa compartido entre todos los hilos: sessionId → Sesion
    private static final ConcurrentHashMap<String, Sesion> sesiones = new ConcurrentHashMap<>();

    /**
     * Constructor de la clase. Inicializa el hilo con el socket del cliente y
     * el recurso compartido.
     *
     * @param cliente Conexión activa con el cliente.
     * @param itv Recurso compartido para la gestión de líneas.
     */
    public HiloServidorSSLCookies(SSLSocket cliente, Itv itv) {
        this.socket = cliente;
        this.itv = itv;
    }

    @Override
    public void run() {

        try (SSLSocket s = this.socket; BufferedReader entrada = new BufferedReader(new InputStreamReader(s.getInputStream())); PrintWriter salida = new PrintWriter(s.getOutputStream(), true)) {

            // --- Lectura de la línea de petición ---
            String peticion = entrada.readLine();
            System.out.println("Petición: " + peticion);

            if (peticion == null || (!peticion.startsWith("GET") && !peticion.startsWith("POST"))) {
                return;
            }

            String ruta = peticion.split(" ")[1];
            String tipo = peticion.split(" ")[0];
            System.out.println("Ruta: " + ruta + " | Tipo: " + tipo);

            // --- Lectura de cabeceras ---
            int contentLength = 0;
            String sessionId = null;
            String linea;

            while (!(linea = entrada.readLine()).isBlank()) {
                if (linea.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(linea.split(":")[1].trim());
                } else if (linea.startsWith("Cookie:")) {
                    String[] cookies = linea.substring(8).split("; ");
                    for (String cookie : cookies) {
                        if (cookie.startsWith("sessionId=")) {
                            sessionId = cookie.substring(10);
                        }
                    }
                }
                System.out.println("Metadato: " + linea);
            }
            System.out.println("Content-Length = " + contentLength);

            // --- Resolución de sesión ---
            Sesion miSesion = (sessionId != null) ? sesiones.get(sessionId) : null;

            // --- Lectura del cuerpo ---
            StringBuilder cuerpo = new StringBuilder();
            for (int i = 0; i < contentLength; i++) {
                cuerpo.append((char) entrada.read());
            }
            System.out.println("Cuerpo: " + cuerpo);

            // --- Flujo de rutas ---
            String respuestaHTML;

            if (ruta.equals("/")) {

                // Página de login: no requiere sesión
                respuestaHTML = construirRespuesta(OK, PaginasHTML.login("", ""), sessionId);

            } else if (ruta.equals("/inicio") && tipo.equals("POST")) {

                String email = cuerpo.toString().split("&")[0].split("=")[1].replace("%40", "@");
                String password = cuerpo.toString().split("&")[1].split("=")[1];
                System.out.println("Debug --> usuario: " + email + " | contraseña: " + password);

                if (Cifrado.usuarioExiste(email) && Cifrado.credencialesCorrectas(email, password)) {
                    // Login correcto: se genera un sessionId único
                    sessionId = UUID.randomUUID().toString();
                    miSesion = new Sesion();
                    sesiones.put(sessionId, miSesion);
                    System.out.println("Nueva sesión creada: " + sessionId);
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlIndex(itv.generarPanel()), sessionId);
                } else {
                    logger.warning("Login incorrecto: " + email);
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.login("Usuario o contraseña incorrectos", "red"), null);
                }

            } else if (ruta.equals("/inicio") && tipo.equals("GET")) {

                // GET /inicio solo está permitido con sesión activa (viene del auto-refresh)
                if (sessionId == null || miSesion == null) {
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlIntruso(), null);
                } else {
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlIndex(itv.generarPanel()), sessionId);
                }

            } else if (ruta.equals("/registro") && tipo.equals("POST")) {

                String email = cuerpo.toString().split("&")[0].split("=")[1].replace("%40", "@");
                String password = cuerpo.toString().split("&")[1].split("=")[1];
                System.out.println("Debug --> usuario: " + email + " | contraseña: " + password);

                if (!Validacion.validarEmail(email)) {
                    logger.warning("Usuario (email) no cumple requisitos: " + email);
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.login("Formato del email incorrecto", "red"), sessionId);

                } else if (!Validacion.validarPassword(password)) {
                    logger.warning("Contraseña no cumple requisitos: " + password);
                    respuestaHTML = construirRespuesta(OK,
                            PaginasHTML.login("Formato de contraseña incorrecto -> Debe tener mínimo 6 caracteres alfanuméricos", "red"), sessionId);

                } else if (Cifrado.usuarioExiste(email)) {
                    logger.warning("Usuario (email) ya existe en el registro: " + email);
                    respuestaHTML = construirRespuesta(OK,
                            PaginasHTML.login("El usuario " + email + " ya está registrado", "red"), sessionId);

                } else {
                    String lineaUsuario = email + ":" + password;
                    if (Cifrado.cifrarBCrypt(lineaUsuario)) {
                        respuestaHTML = construirRespuesta(OK,
                                PaginasHTML.login("Registro del usuario realizado con éxito -> Inicio de sesión disponible para " + email, "blue"), sessionId);
                    } else {
                        respuestaHTML = construirRespuesta(OK,
                                PaginasHTML.login("Registro realizado sin éxito -> Inténtelo de nuevo", "red"), sessionId);
                    }
                }

            } else if (ruta.startsWith("/reservar") && tipo.equals("GET")) {

                if (sessionId == null || miSesion == null) {
                    respuestaHTML = construirRespuesta(UNAUTH, PaginasHTML.htmlIntruso(), null);
                } else {
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlReservar("", miSesion.getUltimaMatriculaReservada(), miSesion.getNumReservas()), sessionId);
                }

            } else if (ruta.startsWith("/reservar") && tipo.equals("POST")) {

                if (sessionId == null || miSesion == null) {
                    respuestaHTML = construirRespuesta(UNAUTH, PaginasHTML.htmlIntruso(), null);
                } else {
                    // Firefox sustituye el espacio en blanco por '+'
                    String matricula = cuerpo.toString().split("=")[1].replace('+', ' ').trim().toUpperCase();
                    String mensajeError = "";

                    if (!Itv.validarMatricula(matricula)) {
                        mensajeError = "<p style='color:yellow;'>ERROR: Formato de matrícula inválido. Use el formato 1234 ABC.</p>";
                    } else if (Itv.getCitas().containsKey(matricula)) {
                        mensajeError = "<p style='color:yellow;'>ERROR: La matrícula " + matricula + " ya tiene una cita registrada.</p>";
                    }

                    miSesion.setUltimaMatriculaReservada(matricula);
                    miSesion.incrementarReservas();

                    if (!mensajeError.isEmpty()) {
                        respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlReservar(mensajeError, miSesion.getUltimaMatriculaReservada(), miSesion.getNumReservas()), sessionId);
                    } else {
                        Itv.getCitas().put(matricula, Itv.getPENDIENTE());
                        respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlIndex(itv.generarPanel()), sessionId);
                    }
                }

            } else if (ruta.startsWith("/pasar") && tipo.equals("GET")) {

                if (sessionId == null || miSesion == null) {
                    respuestaHTML = construirRespuesta(UNAUTH, PaginasHTML.htmlIntruso(), null);
                } else {
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlPasarITV("", "", miSesion.getUltimaMatriculaPasarITV(), miSesion.getNumInspecciones()), sessionId);
                }

            } else if (ruta.startsWith("/pasar") && tipo.equals("POST")) {

                if (sessionId == null || miSesion == null) {
                    respuestaHTML = construirRespuesta(UNAUTH, PaginasHTML.htmlIntruso(), null);
                } else {
                    String matriculaSeleccionada = cuerpo.toString().split("&")[0].split("=")[1].replace('+', ' ').trim().toUpperCase();
                    System.out.println("Matrícula seleccionada: " + matriculaSeleccionada);

                    if (Itv.getCitas().containsKey(matriculaSeleccionada)) {

                        // Guardamos la matrícula en el historial de la sesión
                        miSesion.setUltimaMatriculaPasarITV(matriculaSeleccionada);
                        // Incrementamos el contador de inspecciones
                        miSesion.incrementarInspecciones();

                        String mensajeInspeccion = itv.inspeccionar(matriculaSeleccionada);

                        respuestaHTML = construirRespuesta(OK,
                                PaginasHTML.htmlPasarITV(matriculaSeleccionada, PaginasHTML.htmlResultado(mensajeInspeccion), miSesion.getUltimaMatriculaPasarITV(), miSesion.getNumInspecciones()), sessionId);
                    } else {
                        String errorMatricula = "<p style='color:yellow;'>ERROR: Matrícula "
                                + matriculaSeleccionada + " no encontrada en la lista de reservas.</p>";
                        respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlPasarITV(matriculaSeleccionada, errorMatricula, miSesion.getUltimaMatriculaPasarITV(), miSesion.getNumInspecciones()), sessionId);
                    }
                }

            } else if (ruta.equals("/cerrarSesion")) {
                // 1º Eliminar del Hashmap
                if (sessionId != null) {
                    sesiones.remove(sessionId);
                    System.out.println("Sesión eliminada: " + sessionId);
                }
                // 2º Eliminar Cookie (Max-Age=0) mediante redirección
                respuestaHTML = construirRedireccion();

            } else {

                respuestaHTML = construirRespuesta(NOTFOUND, PaginasHTML.html_notFound, sessionId);

            }

            System.out.println("RespuestaHTML: \n" + respuestaHTML);
            salida.print(respuestaHTML);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error de entrada/salida: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado en el servidor: " + e.getMessage());
        }
    }

    /**
     * Genera la cabecera HTTP y concatena el contenido HTML. Si sessionId no es
     * nulo, incluye la cookie de sesión con duración de 1 hora.
     *
     * @param codigo Código de estado HTTP.
     * @param contenido Cuerpo del mensaje en formato HTML.
     * @param sessionId Identificador de sesión, o null si no hay sesión activa.
     * @return Cadena completa con el protocolo HTTP.
     */
    private static String construirRespuesta(int codigo, String contenido, String sessionId) {

        String lineaEstado;
        if (codigo == OK) {
            lineaEstado = "HTTP/1.1 200 OK";
        } else if (codigo == UNAUTH) {
            lineaEstado = "HTTP/1.1 401 Unauthorized";
        } else {
            lineaEstado = "HTTP/1.1 404 Not Found";
        }

        String cookieHeader = (sessionId != null)
                ? "Set-Cookie: sessionId=" + sessionId + "; Path=/; Max-Age=3600\r\n"
                : "Set-Cookie: sessionId=; Path=/; Max-Age=0\r\n";

        return lineaEstado + "\r\n"
                + "Content-Type: text/html; charset=UTF-8\r\n"
                + "Content-Length: " + contenido.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                + cookieHeader
                + "\r\n"
                + contenido;
    }

    private static String construirRespuesta2(int codigo, String contenido, String sessionId) {
        return (codigo == 200 ? "HTTP/1.1 200 OK" : "HTTP/1.1 404 Not Found") + "\r\n" //Línea inicial
                + "Content-Type: text/html; charset=UTF-8" + "\r\n" //Metadatos
                + "Content-Length: " + contenido.getBytes(java.nio.charset.StandardCharsets.UTF_8).length + "\r\n"
                + "Set-Cookie: sessionId=" + sessionId + "; Path=/; Max-Age=3600" + "\r\n"
                + "\r\n" //Línea vacía
                + contenido;
    }

    /**
     * Genera una redirección HTTP 302 hacia la página de login borrando la
     * cookie de sesión al establecer Max-Age=0.
     *
     * @return Cadena con la cabecera de redirección HTTP.
     */
    private static String construirRedireccion() {
        return "HTTP/1.1 302 Found\r\n"
                + "Location: /\r\n"
                + "Set-Cookie: sessionId=; Path=/; Max-Age=0\r\n"
                + "\r\n";
    }
}
