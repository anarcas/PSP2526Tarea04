/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package naranjo_castillo_antonio_tarea4psp;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 * Representa el punto de entrada principal del servidor seguro para la ITV.
 *
 * Se encarga de inicializar el contexto criptográfico SSL, arrancar el socket
 * de escucha en el puerto seguro y gestionar la concurrencia de la aplicación
 * mediante la creación de un nuevo hilo para cada cliente que se conecta.
 *
 * @author Antonio Naranjo Castillo
 * @version 1.0
 */
public class ServidorSSL {

    /**
     * Registrador de eventos del sistema que almacena las incidencias en el
     * archivo de texto.
     */
    private static final Logger logger = configurarLogger();

    /**
     * Arranca el servicio principal del servidor de la ITV. Crea la instancia
     * del recurso compartido que controla el estado de las líneas, inicializa
     * el socket del servidor seguro y entra en un ciclo continuo para aceptar
     * conexiones entrantes de los navegadores web.
     *
     * @param args Argumentos de la línea de comandos pasados al iniciar el
     * programa.
     * @throws IOException Si ocurre un problema de entrada o salida al aceptar
     * conexiones.
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here

        // Instancia el recurso compartido que controlará el estado de las líneas
        Itv itvInfierno = new Itv();

        // Define el puerto de escucha para el servidor HTTP
        int puerto = 12349;

        try {
            SSLServerSocket socketServidorSsl = crearServidorSSL();

            System.out.println("Servidor SSL ITV INFIERNO arrancado en https://localhost:" + puerto);
            // Mantiene el servidor activo escuchando nuevas peticiones de forma indefinida
            while (true) {
                try {
                    // Acepta la conexión entrante del navegador (cliente)
                    SSLSocket socketSsl = (SSLSocket) socketServidorSsl.accept();
                    System.out.println("Cliente conectado");

                    // Crea e inicia un nuevo hilo para procesar la petición HTTP de forma asíncrona
                    Thread hiloServidor = new Thread(new HiloServidorSSLCookies(socketSsl, itvInfierno));
                    hiloServidor.start(); // Inicia el hilo;
                } catch (IOException e) {
                    logger.warning("Error aceptando cliente: " + e.getMessage());
                }
            }
        } catch (RuntimeException e) {
            logger.severe("No se pudo iniciar el servidor SSL: " + e.getMessage());
        }
    }

    /**
     * Configura el entorno de seguridad y crea el socket del servidor seguro.
     * 
     * Carga el almacén de claves desde el archivo del disco, recupera el certificado
     * digital del servidor, inicializa el gestor de claves de tipo SunX509 y devuelve
     * un socket preparado para escuchar en el puerto seguro especificado.
     *
     * @return El socket del servidor configurado con el protocolo de cifrado SSL/TLS.
     * @throws RuntimeException Si ocurre cualquier error relacionado con la carga de las
     * claves, el formato del almacén o el algoritmo de cifrado.
     */
    private static SSLServerSocket crearServidorSSL() {

        try {
            // 1. KEYSTORE
            KeyStore keyStore = KeyStore.getInstance("JKS");

            try (FileInputStream keyFile = new FileInputStream("AlmacenSSL")) {
                keyStore.load(keyFile, "123456".toCharArray());
            }

            // 2. KEY MANAGER
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, "123456".toCharArray());

            // 3. SSL CONTEXT
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);

            // 4. SOCKET SERVER
            SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
            return (SSLServerSocket) factory.createServerSocket(12349);

        } catch (KeyStoreException e) {
            throw new RuntimeException("Error con el KeyStore (formato o tipo incorrecto)", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo SSL no soportado", e);
        } catch (CertificateException e) {
            throw new RuntimeException("Error en el certificado del almacén", e);
        } catch (UnrecoverableKeyException e) {
            throw new RuntimeException("No se puede acceder a la clave privada", e);
        } catch (KeyManagementException e) {
            throw new RuntimeException("Error inicializando el contexto SSL", e);
        } catch (IOException e) {
            throw new RuntimeException("Error de entrada/salida al cargar el keystore", e);
        }
    }

    /**
     * Inicializa y parametriza el sistema de registro de eventos en archivo.
     * 
     * Crea un manejador de archivos de texto que añade información de manera continua
     * y aplica un formato de fecha y hora personalizado para cada registro guardado.
     *
     * @return El objeto Logger completamente configurado y listo para registrar eventos.
     */
    private static Logger configurarLogger() {

        Logger logger = Logger.getLogger("MiLog");

        try {
            // Se crea el manejador de archivo en modo "append"
            FileHandler fh = new FileHandler("log.txt", true);

            // Se define el formato de fecha y hora personalizado según la imagen
            fh.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"));
                    return String.format("%s - %s%n",
                            fechaHora,
                            record.getMessage());
                }
            });

            // Se añade el manejador al logger
            logger.addHandler(fh);
            logger.setUseParentHandlers(true);
            // Se establece el nivel de log
            logger.setLevel(Level.ALL);

        } catch (IOException | SecurityException e) {
            System.err.println("No puedo configurar el logger");
        }

        return logger;
    }

}
