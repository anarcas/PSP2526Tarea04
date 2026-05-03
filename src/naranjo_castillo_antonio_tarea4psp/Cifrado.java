/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package naranjo_castillo_antonio_tarea4psp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Gestiona los procesos de seguridad de la aplicación, como el cifrado de archivos 
 * y la protección de contraseñas de los usuarios.
 * 
 * @author Antonio Naranjo Castillo
 * @version 1.0
 * @since 03/05/2026
 */
public class Cifrado {

    /**
     * Guarda la clave secreta de 16 caracteres que se utiliza para el algoritmo de cifrado simétrico AES.
     */
    private static final String CLAVE_AES = "1234567890123456";
    /**
     * Guarda el nombre del archivo de texto plano donde se anotan las líneas de los usuarios.
     */
    private static final String FICHERO_USUARIOS = "usuarios.log";
    /**
     * Guarda el nombre del archivo final donde se almacena toda la información cifrada mediante AES.
     */
    private static final String FICHERO_CIFRADO = "usuarios.txt";
    
    /**
     * Lee el contenido del archivo cifrado y lo convierte a texto legible.
     * Al no estar sincronizado, permite que varios hilos lean los datos a la vez.
     * @return El contenido descifrado como una cadena de texto, o null si ocurre un error.
     */
    public static String leerFichero() {
        try {
            // Se prepara el motor de cifrado indicando que se usará el algoritmo AES.
            Cipher cipher = Cipher.getInstance("AES");
            // Se construye la clave secreta a partir de la cadena de texto de 16 bytes.
            SecretKey clave = new SecretKeySpec(CLAVE_AES.getBytes(), "AES");
            // Se inicializa el motor de cifrado en modo descifrado utilizando la clave generada.
            cipher.init(Cipher.DECRYPT_MODE, clave);

            // Se leen todos los bytes directamente del archivo que contiene los datos protegidos.
            byte[] datosCifrados = Files.readAllBytes(Paths.get(FICHERO_CIFRADO));
            // Se procesan los bytes del archivo para transformarlos en datos legibles.
            byte[] datosDescifrados = cipher.doFinal(datosCifrados);

            // Se devuelve el resultado final convertido en una cadena de texto común.
            return new String(datosDescifrados);

        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            // Si algo falla durante la lectura o el descifrado, se devuelve un valor nulo.
            return null;
        }
    }

    /**
     * Escribe una nueva línea en los archivos de registro y actualiza el archivo cifrado.
     * Está sincronizado para evitar que dos hilos escriban al mismo tiempo y dañen los datos.
     * @param lineaCifrada La nueva línea de texto que contiene el email y el hash de la contraseña.
     * @return true si la operación se realiza con éxito, false si ocurre algún problema.
     */
    public static synchronized boolean escribirFichero(String lineaCifrada) {

            try {
                // Se abre el archivo de texto plano para añadir la nueva línea al final del contenido existente.
                try (FileOutputStream fos = new FileOutputStream(FICHERO_USUARIOS, true)) {
                    fos.write((lineaCifrada + "\n").getBytes());
                }

                // Se prepara el motor de cifrado AES para proteger de nuevo todo el archivo de texto plano.
                Cipher cipher = Cipher.getInstance("AES");
                SecretKey clave = new SecretKeySpec(CLAVE_AES.getBytes(), "AES");
                cipher.init(Cipher.ENCRYPT_MODE, clave);

                // Se lee el contenido completo actualizado del archivo de texto plano.
                String contenido = Files.readString(Paths.get(FICHERO_USUARIOS));
                // Se cifran todos los datos leídos mediante el algoritmo AES.
                byte[] datosCifrados = cipher.doFinal(contenido.getBytes());

                // Se guardan los datos cifrados sobrescribiendo por completo el archivo protegido.
                try (FileOutputStream fos = new FileOutputStream(FICHERO_CIFRADO)) {
                    fos.write(datosCifrados);
                }
                
                // Devuelve verdadero para confirmar que la escritura se completó correctamente.
                return true;

            } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
                // Si ocurre algún fallo en el proceso de guardado o cifrado, se devuelve falso.
                return false;
            }
        
    }

    /**
     * Aplica una función de resumen (hash) a la contraseña del usuario y solicita su guardado.
     * Utiliza el algoritmo seguro BCrypt antes de enviar la información al archivo.
     * @param lineaUsuario Cadena de texto con el formato "email:contraseña".
     * @return true si el usuario se registra y guarda correctamente, false en caso contrario.
     */
    public static boolean cifrarBCrypt(String lineaUsuario) {
        
        boolean resultado = false;

        try {
            // Se separa la cadena de entrada usando los dos puntos como separador.
            String[] partes = lineaUsuario.split(":");
            String email = partes[0];
            String password = partes[1];

            // Se genera un hash seguro de la contraseña utilizando BCrypt con un factor de trabajo de 12.
            String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
            // Se vuelve a unir el email con la nueva contraseña ya protegida por el hash.
            String lineaCifrada = email + ":" + hash;

            // Se envía la información al método sincronizado para su almacenamiento físico.
            resultado = escribirFichero(lineaCifrada);

        } catch (ArrayIndexOutOfBoundsException e) {
            // Si la cadena de entrada no tiene el formato esperado, el proceso falla.
            resultado = false;
        }

        return resultado;
    }

    /**
     * Comprueba si el email y la contraseña coinciden con los registros guardados.
     * @param email Correo electrónico que el usuario introduce para identificarse.
     * @param password Contraseña que el usuario introduce para identificarse.
     * @return true si las credenciales son válidas y correctas, false si no coinciden.
     */
    public static boolean credencialesCorrectas(String email, String password) {
        
        boolean encontrado = false;

        try {
            // Se comprueba primero si el email y la contraseña cumplen con los requisitos mínimos de formato.
            if (Validacion.validarEmail(email) && Validacion.validarPassword(password)) {
                // Se obtiene todo el texto descifrado del archivo protegido.
                String contenido = leerFichero();

                if (contenido != null) {
                    // Se separa el texto en líneas individuales para evaluar usuario por usuario.
                    String[] lineas = contenido.split("\n");
                    int i = 0;

                    // Se recorre cada línea del archivo hasta dar con el usuario o terminar la lista.
                    while (i < lineas.length && !encontrado) {
                        String[] partes = lineas[i].split(":");
                        if (partes.length >= 2) {
                            String emailGuardado = partes[0].trim();
                            String hashGuardado = partes[1].trim();
                            // Se comprueba si el email coincide y si la contraseña introducida genera el mismo hash.
                            if (email.equals(emailGuardado) 
                                && BCrypt.checkpw(password, hashGuardado)) {
                                encontrado = true;
                            }
                        }
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            // En caso de cualquier error imprevisto, se considera que las credenciales no son correctas.
            encontrado = false;
        }

        return encontrado;
    }

    /**
     * Comprueba si un correo electrónico ya se encuentra registrado en el sistema.
     * @param email Correo electrónico que se desea buscar en los registros.
     * @return true si el correo ya existe en el archivo, false en caso contrario.
     */
    public static boolean usuarioExiste(String email) {
        boolean existe = false;

        try {
            // Se obtiene todo el texto descifrado del archivo de usuarios.
            String contenido = leerFichero();

            if (contenido != null) {
                // Se separa el texto por líneas para procesar los datos de cada usuario registrado.
                String[] lineas = contenido.split("\n");
                int i = 0;

                // Se revisan los registros de arriba a abajo buscando una coincidencia del email.
                while (i < lineas.length && !existe) {
                    String[] partes = lineas[i].split(":");
                    if (partes.length >= 2) {
                        // Se comprueba si el correo buscado coincide con el correo de la línea actual.
                        if (email.equals(partes[0].trim())) {
                            existe = true;
                        }
                    }
                    i++;
                }
            }
        } catch (Exception e) {
            // Ante cualquier fallo en el proceso de comprobación, se asume que no existe el usuario.
            existe = false;
        }

        return existe;
    }
}
