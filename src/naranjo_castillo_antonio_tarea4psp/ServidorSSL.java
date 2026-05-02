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
 * Representa el servidor principal que gestiona las conexiones HTTP para la
 * ITV. Esta clase inicializa el recurso compartido y escucha peticiones en un
 * puerto específico.
 *
 * @author Antonio Naranjo Castillo
 *
 * @version 1.0
 */
public class ServidorSSL {

    private static final Logger logger = configurarLogger();

    /**
     * Arranca el servicio, instancia el recurso compartido 'Itv' y entra en un
     * bucle infinito para aceptar clientes, delegando cada uno a un hilo
     * independiente.
     *
     * @param args Argumentos de la línea de comandos (no utilizados).
     * @throws IOException Si ocurre un error al abrir el socket o aceptar
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
                    Thread hiloServidor = new Thread(new HiloServidorSSLCookies(socketSsl,itvInfierno));
                    hiloServidor.start(); // Inicia el hilo;
                } catch (IOException e) {
                    logger.warning("Error aceptando cliente: " + e.getMessage());
                }
            }
        } catch (RuntimeException e) {
            logger.severe("No se pudo iniciar el servidor SSL: " + e.getMessage());
        }
    }

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

    private static Logger configurarLogger() {

        Logger logger = Logger.getLogger("MiLog");

        try {
            // Se crea el manejador de archivo en modo "append"
            FileHandler fh = new FileHandler("log.txt", true);

            // Se define el formato de fecha y hora personalizado según la imagen
            fh.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    String fechaHora = record.getInstant().toString();
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
