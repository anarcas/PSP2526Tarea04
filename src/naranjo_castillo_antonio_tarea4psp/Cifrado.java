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
 *
 * @author anaranjo
 */
public class Cifrado {

    private static final String CLAVE_AES = "1234567890123456";
    private static final String FICHERO_USUARIOS = "usuarios.log";
    private static final String FICHERO_CIFRADO = "usuarios.txt";
    
    /**
     * Lectura del fichero cifrado - NO sincronizado,
     * permite múltiples lecturas simultáneas
     */
    public static String leerFichero() {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKey clave = new SecretKeySpec(CLAVE_AES.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, clave);

            byte[] datosCifrados = Files.readAllBytes(Paths.get(FICHERO_CIFRADO));
            byte[] datosDescifrados = cipher.doFinal(datosCifrados);

            return new String(datosDescifrados);

        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            return null;
        }
    }

    /**
     * Escritura en el fichero - sincronizado,
     * solo un hilo puede escribir a la vez
     */
    public static synchronized boolean escribirFichero(String lineaCifrada) {

            try {
                // Guardamos en Usuarios.log en modo append
                try (FileOutputStream fos = new FileOutputStream(FICHERO_USUARIOS, true)) {
                    fos.write((lineaCifrada + "\n").getBytes());
                }

                // Ciframos y guardamos en UsuariosCifrado.txt
                Cipher cipher = Cipher.getInstance("AES");
                SecretKey clave = new SecretKeySpec(CLAVE_AES.getBytes(), "AES");
                cipher.init(Cipher.ENCRYPT_MODE, clave);

                String contenido = Files.readString(Paths.get(FICHERO_USUARIOS));
                byte[] datosCifrados = cipher.doFinal(contenido.getBytes());

                try (FileOutputStream fos = new FileOutputStream(FICHERO_CIFRADO)) {
                    fos.write(datosCifrados);
                }
                return true;

            } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
                return false;
            }
        
    }

    /**
     * Hashea la contraseña con BCrypt y delega la escritura
     * al método sincronizado escribirFichero()
     */
    public static boolean cifrarBCrypt(String lineaUsuario) {
        boolean resultado = false;

        try {
            String[] partes = lineaUsuario.split(":");
            String email = partes[0];
            String password = partes[1];

            String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
            String lineaCifrada = email + ":" + hash;

            resultado = escribirFichero(lineaCifrada);

        } catch (ArrayIndexOutOfBoundsException e) {
            resultado = false;
        }

        return resultado;
    }

    /**
     * Comprueba credenciales usando lectura no sincronizada
     */
    public static boolean credencialesCorrectas(String email, String password) {
        boolean encontrado = false;

        try {
            if (Validacion.validarEmail(email) && Validacion.validarPassword(password)) {
                String contenido = leerFichero();

                if (contenido != null) {
                    String[] lineas = contenido.split("\n");
                    int i = 0;

                    while (i < lineas.length && !encontrado) {
                        String[] partes = lineas[i].split(":");
                        if (partes.length >= 2) {
                            String emailGuardado = partes[0].trim();
                            String hashGuardado = partes[1].trim();
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
            encontrado = false;
        }

        return encontrado;
    }

    /**
     * Comprueba si el usuario existe usando lectura no sincronizada
     */
    public static boolean usuarioExiste(String email) {
        boolean existe = false;

        try {
            String contenido = leerFichero();

            if (contenido != null) {
                String[] lineas = contenido.split("\n");
                int i = 0;

                while (i < lineas.length && !existe) {
                    String[] partes = lineas[i].split(":");
                    if (partes.length >= 2) {
                        if (email.equals(partes[0].trim())) {
                            existe = true;
                        }
                    }
                    i++;
                }
            }
        } catch (Exception e) {
            existe = false;
        }

        return existe;
    }
}
