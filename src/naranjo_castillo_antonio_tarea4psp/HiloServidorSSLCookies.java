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
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;

/**
 * Procesa las peticiones HTTP entrantes de forma concurrente. Implementa la
 * interfaz Runnable para permitir que cada cliente sea atendido en un hilo
 * propio e independiente.
 * En su método run se ejecuta el procesamiento de la petición HTTP. Lee la
 * cabecera, extrae el cuerpo en peticiones POST y deriva el flujo según la
 * ruta. Incorpora control de sesión mediante cookies HTTP.
 *
 * @author Antonio Naranjo Castillo
 * @version 1.0
 */
public class HiloServidorSSLCookies implements Runnable {

    /**
     * Guarda el código de estado HTTP 200 que indica que la solicitud fue exitosa.
     */
    private static final int OK = 200;
    /**
     * Guarda el código de estado HTTP 404 que indica que la página no se encuentra.
     */
    private static final int NOTFOUND = 404;
    /**
     * Guarda el código de estado HTTP 401 que indica que el acceso no está autorizado.
     */
    private static final int UNAUTH = 401;

    /**
     * Almacena el canal de comunicación seguro utilizado para hablar con el cliente.
     */
    private final SSLSocket socket;
    /**
     * Almacena el componente que registra los eventos y errores importantes del programa.
     */
    private final Logger logger = Logger.getLogger("MiLog");
    /**
     * Guarda el recurso compartido donde se gestionan las citas y las líneas de inspección.
     */
    private final Itv itv;

    /**
     * Almacena la lista de sesiones activas en memoria para que todos los hilos las compartan.
     */
    private static final ConcurrentHashMap<String, Sesion> sesiones = new ConcurrentHashMap<>();

    /**
     * Crea un nuevo hilo de trabajo vinculando el socket del cliente y el recurso de la ITV.
     *
     * @param cliente Conexión activa con el cliente.
     * @param itv Recurso compartido para la gestión de líneas.
     */
    public HiloServidorSSLCookies(SSLSocket cliente, Itv itv) {
        this.socket = cliente;
        this.itv = itv;
    }

    /**
     * Inicia el procesamiento de la petición web del cliente en un hilo independiente.
     */
    @Override
    public void run() {

        // Se abren los canales de entrada y salida de datos para comunicarse por el socket.
        try (SSLSocket s = this.socket; 
             BufferedReader entrada = new BufferedReader(new InputStreamReader(s.getInputStream())); 
             PrintWriter salida = new PrintWriter(s.getOutputStream(), true)) {

            // Se lee la primera línea de la petición enviada por el navegador.
            String peticion = entrada.readLine();
            System.out.println("Petición: " + peticion);

            // Si la petición viene vacía o no es válida, el hilo detiene su ejecución.
            if (peticion == null || (!peticion.startsWith("GET") && !peticion.startsWith("POST"))) {
                return;
            }

            // Se extrae la ruta solicitada y el tipo de acción (GET o POST).
            String ruta = peticion.split(" ")[1];
            String tipo = peticion.split(" ")[0];
            System.out.println("Ruta: " + ruta + " | Tipo: " + tipo);

            // Se preparan las variables para leer los metadatos de la cabecera HTTP.
            int contentLength = 0;
            String sessionId = null;
            String linea;

            // Se leen todas las líneas de cabecera hasta encontrar una línea en blanco.
            while (!(linea = entrada.readLine()).isBlank()) {
                // Se busca la cabecera que indica el tamaño del contenido enviado.
                if (linea.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(linea.split(":")[1].trim());
                    // Se busca la cabecera que contiene las cookies de sesión.
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

            // Se intenta recuperar la sesión activa a partir de la cookie recibida.
            Sesion miSesion = (sessionId != null) ? sesiones.get(sessionId) : null;

            // Se lee el cuerpo del mensaje si la petición contiene datos adicionales.
            StringBuilder cuerpo = new StringBuilder();
            for (int i = 0; i < contentLength; i++) {
                cuerpo.append((char) entrada.read());
            }
            System.out.println("Cuerpo: " + cuerpo);

            // Variable para almacenar el código HTML que se le enviará al usuario.
            String respuestaHTML;

            // Se evalúa la ruta solicitada para decidir qué página mostrar.
            if (ruta.equals("/")) {

                // Se muestra la página de acceso inicial, la cual no requiere una sesión activa.
                respuestaHTML = construirRespuesta(OK, PaginasHTML.login("", ""), sessionId);

            } else if (ruta.equals("/inicio") && tipo.equals("POST")) {

                // Se extraen las credenciales enviadas desde el formulario de acceso.
                String email = cuerpo.toString().split("&")[0].split("=")[1].replace("%40", "@");
                String password = cuerpo.toString().split("&")[1].split("=")[1];
                System.out.println("Debug --> usuario: " + email + " | contraseña: " + password);

                // Se verifica si el usuario existe y si su contraseña es válida.
                if (Cifrado.usuarioExiste(email) && Cifrado.credencialesCorrectas(email, password)) {
                    // Si el acceso es correcto, se genera un identificador de sesión único.
                    sessionId = UUID.randomUUID().toString();
                    miSesion = new Sesion();
                    sesiones.put(sessionId, miSesion);
                    System.out.println("Nueva sesión creada: " + sessionId);
                    // Se envía al usuario a la página de inicio de la ITV.
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlIndex(itv.generarPanel()), sessionId);
                } else {
                    // Si el acceso falla, se registra la advertencia y se vuelve a mostrar el login con un mensaje de error.
                    logger.warning("Login incorrecto: " + email);
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.login("Usuario o contraseña incorrectos", "red"), null);
                }

            } else if (ruta.equals("/inicio") && tipo.equals("GET")) {

                // Se comprueba que el usuario tenga una sesión válida antes de mostrar la página principal.
                if (sessionId == null || miSesion == null) {
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlIntruso(), null);
                } else {
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlIndex(itv.generarPanel()), sessionId);
                }

            } else if (ruta.equals("/registro") && tipo.equals("POST")) {

                // Se extraen los datos del formulario para dar de alta un nuevo usuario.
                String email = cuerpo.toString().split("&")[0].split("=")[1].replace("%40", "@");
                String password = cuerpo.toString().split("&")[1].split("=")[1];
                System.out.println("Debug --> usuario: " + email + " | contraseña: " + password);

                // Se valida que el correo cumpla con el formato correcto.
                if (!Validacion.validarEmail(email)) {
                    logger.warning("Usuario (email) no cumple requisitos: " + email);
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.login("Formato del email incorrecto", "red"), sessionId);
                    // Se valida que la contraseña sea lo suficientemente segura.
                } else if (!Validacion.validarPassword(password)) {
                    logger.warning("Contraseña no cumple requisitos: " + password);
                    respuestaHTML = construirRespuesta(OK,
                            PaginasHTML.login("Formato de contraseña incorrecto -> Debe tener mínimo 6 caracteres alfanuméricos", "red"), sessionId);
                    // Se comprueba que el correo no esté ya registrado en el sistema.
                } else if (Cifrado.usuarioExiste(email)) {
                    logger.warning("Usuario (email) ya existe en el registro: " + email);
                    respuestaHTML = construirRespuesta(OK,
                            PaginasHTML.login("El usuario " + email + " ya está registrado", "red"), sessionId);
                    // Si todo está correcto, se procede a guardar el nuevo usuario cifrando su contraseña.
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
                // Se valida la sesión del usuario antes de permitirle acceder a la reserva de citas.
                if (sessionId == null || miSesion == null) {
                    respuestaHTML = construirRespuesta(UNAUTH, PaginasHTML.htmlIntruso(), null);
                } else {
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlReservar("", miSesion.getUltimaMatriculaReservada(), miSesion.getNumReservas()), sessionId);
                }

            } else if (ruta.startsWith("/reservar") && tipo.equals("POST")) {
                // Se valida la sesión antes de procesar el formulario de reserva de cita.
                if (sessionId == null || miSesion == null) {
                    respuestaHTML = construirRespuesta(UNAUTH, PaginasHTML.htmlIntruso(), null);
                } else {
                    // Se extrae y limpia la matrícula enviada por el formulario. Firefox sustituye el espacio en blanco por '+'
                    String matricula = cuerpo.toString().split("=")[1].replace('+', ' ').trim().toUpperCase();
                    String mensajeError = "";

                    // Se valida el formato de la matrícula introducida.
                    if (!Itv.validarMatricula(matricula)) {
                        mensajeError = "<p style='color:yellow;'>ERROR: Formato de matrícula inválido. Use el formato 1234 ABC.</p>";
                        // Se comprueba que la matrícula no tenga ya una cita previa asignada.
                    } else if (Itv.getCitas().containsKey(matricula)) {
                        mensajeError = "<p style='color:yellow;'>ERROR: La matrícula " + matricula + " ya tiene una cita registrada.</p>";
                    }

                    // Se actualizan los datos de la sesión del usuario.
                    miSesion.setUltimaMatriculaReservada(matricula);
                    miSesion.incrementarReservas();

                    // Si hay un error, se vuelve a mostrar la página de reserva con el mensaje de aviso.
                    if (!mensajeError.isEmpty()) {
                        respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlReservar(mensajeError, miSesion.getUltimaMatriculaReservada(), miSesion.getNumReservas()), sessionId);
                        // Si todo es válido, se registra la cita y se redirige a la página principal.
                    } else {
                        Itv.getCitas().put(matricula, Itv.getPENDIENTE());
                        respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlIndex(itv.generarPanel()), sessionId);
                    }
                }

            } else if (ruta.startsWith("/pasar") && tipo.equals("GET")) {

                // Se valida la sesión del usuario antes de permitirle acceder a la inspección de la ITV.
                if (sessionId == null || miSesion == null) {
                    respuestaHTML = construirRespuesta(UNAUTH, PaginasHTML.htmlIntruso(), null);
                } else {
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlPasarITV("", "", miSesion.getUltimaMatriculaPasarITV(), miSesion.getNumInspecciones()), sessionId);
                }

            } else if (ruta.startsWith("/pasar") && tipo.equals("POST")) {

                // Se valida la sesión antes de realizar la inspección del vehículo.
                if (sessionId == null || miSesion == null) {
                    respuestaHTML = construirRespuesta(UNAUTH, PaginasHTML.htmlIntruso(), null);
                } else {
                    // Se extrae la matrícula elegida por el usuario.
                    String matriculaSeleccionada = cuerpo.toString().split("&")[0].split("=")[1].replace('+', ' ').trim().toUpperCase();
                    System.out.println("Matrícula seleccionada: " + matriculaSeleccionada);

                    // Se verifica que la matrícula esté previamente registrada en la lista de citas.
                    if (Itv.getCitas().containsKey(matriculaSeleccionada)) {

                        // Se guarda la matrícula en el historial de la sesión activa.
                        miSesion.setUltimaMatriculaPasarITV(matriculaSeleccionada);
                        miSesion.incrementarInspecciones();

                        // Se asigna la línea de inspección y se obtiene el resultado.
                        String mensajeInspeccion = itv.inspeccionar(matriculaSeleccionada);

                        respuestaHTML = construirRespuesta(OK,PaginasHTML.htmlPasarITV(matriculaSeleccionada, PaginasHTML.htmlResultado(mensajeInspeccion), miSesion.getUltimaMatriculaPasarITV(), miSesion.getNumInspecciones()), sessionId);
                    } else {
                        // Si la matrícula no existe en las citas, se muestra un mensaje de error.
                        String errorMatricula = "<p style='color:yellow;'>ERROR: Matrícula "
                                + matriculaSeleccionada + " no encontrada en la lista de reservas.</p>";
                        respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlPasarITV(matriculaSeleccionada, errorMatricula, miSesion.getUltimaMatriculaPasarITV(), miSesion.getNumInspecciones()), sessionId);
                    }
                }

            } else if (ruta.equals("/cerrarSesion")) {
                // Se elimina la sesión activa de la lista en memoria si el identificador es válido.
                if (sessionId != null) {
                    sesiones.remove(sessionId);
                    System.out.println("Sesión eliminada: " + sessionId);
                }
                // Se genera la redirección de salida para borrar la cookie del navegador.
                respuestaHTML = construirRedireccion();

            } else {
                // Si la ruta no coincide con ninguna de las opciones anteriores, se envía un error 404.
                respuestaHTML = construirRespuesta(NOTFOUND, PaginasHTML.html_notFound, sessionId);

            }

            // Se imprime la respuesta por consola para depuración y se envía al cliente.
            System.out.println("RespuestaHTML: \n" + respuestaHTML);
            salida.print(respuestaHTML);

        } catch (IOException e) {
            // Se registran en el log los fallos relacionados con el envío o la recepción de datos.
            System.err.println("Error de entrada/salida: " + e.getMessage());
        } catch (Exception e) {
            // Se registran en el log cualquier otro fallo imprevisto del programa.
            System.err.println("Error inesperado en el servidor: " + e.getMessage());
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

        // Se asigna la primera línea del mensaje HTTP según el código de estado.
        String lineaEstado;
        if (codigo == OK) {
            lineaEstado = "HTTP/1.1 200 OK";
        } else if (codigo == UNAUTH) {
            lineaEstado = "HTTP/1.1 401 Unauthorized";
        } else {
            lineaEstado = "HTTP/1.1 404 Not Found";
        }

        // Se decide si se envía una cookie nueva o si se solicita su eliminación al navegador.
        String cookieHeader = (sessionId != null)
                ? "Set-Cookie: sessionId=" + sessionId + "; Path=/; Max-Age=3600\r\n"
                : "Set-Cookie: sessionId=; Path=/; Max-Age=0\r\n";

        // Se construye el mensaje completo uniendo los metadatos HTTP y el contenido HTML.
        return lineaEstado + "\r\n"
                + "Content-Type: text/html; charset=UTF-8\r\n"
                + "Content-Length: " + contenido.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                + cookieHeader
                + "\r\n"
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
