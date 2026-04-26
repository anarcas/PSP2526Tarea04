/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package naranjo_castillo_antonio_tarea4psp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Representa el servidor principal que gestiona las conexiones HTTP para la
 * ITV. Esta clase inicializa el recurso compartido y escucha peticiones en un
 * puerto específico.
 *
 * @author Antonio Naranjo Castillo
 *
 * @version 1.0
 */
public class ServidorHTTP {

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
        ServerSocket servidor = new ServerSocket(puerto);
        
        System.out.println("Servidor ITV INFIERNO arrancado en http://localhost:" + puerto);
        // Mantiene el servidor activo escuchando nuevas peticiones de forma indefinida
        while (true) {
            // Acepta la conexión entrante del navegador (cliente)
            Socket cliente = servidor.accept();
            
            // Crea e inicia un nuevo hilo para procesar la petición HTTP de forma asíncrona
            new Thread(new HiloServidor(cliente, itvInfierno)).start();
        }

    }

}
