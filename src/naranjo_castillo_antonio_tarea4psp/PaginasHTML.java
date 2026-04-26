/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package naranjo_castillo_antonio_tarea4psp;

import java.util.Map;

/**
 * Almacena las constantes y métodos estáticos que definen la interfaz web. Se
 * encarga de la presentación HTML y el estilo CSS de la aplicación.
 *
 * @author Antonio Naranjo Castillo
 * @version 1.0
 */
public class PaginasHTML {

    /**
     * Define la estructura de la página principal.
     *
     * @param panelHTML Contenido dinámico del panel de líneas.
     * @return Página de inicio completa.
     */
    public static String htmlIndex(String panelHTML) {
        return "<html><head>"
                + "<title>ITV del Infierno</title>"
                + "<meta charset='UTF-8'>"
                + "<meta http-equiv='refresh' content='2'>"
                + "<style>"
                + "body {"
                + "  font-family: Arial, sans-serif;"
                + "  background: linear-gradient(135deg, #000000, #440000);"
                + "  color: white;"
                + "  text-align: center;"
                + "  padding-bottom: 40px;"
                + "}"
                + ".boton {"
                + "  background: #ff3333;"
                + "  color: white;"
                + "  padding: 20px 40px;"
                + "  font-size: 22px;"
                + "  margin: 20px;"
                + "  border: none;"
                + "  border-radius: 10px;"
                + "  cursor: pointer;"
                + "  transition: 0.3s;"
                + "}"
                + ".boton:hover { background:#cc0000; transform: scale(1.05); }"
                + ".panel {"
                + "  background: black;"
                + "  width: 60%;"
                + "  margin: auto;"
                + "  padding: 25px;"
                + "  border: 8px solid #333;"
                + "  color: #00ff00;"
                + "  font-family: 'Courier New', monospace;"
                + "  box-shadow: 0 0 15px #ff000055;"
                + "}"
                + ".panel-titulo {"
                + "  text-align: center;"
                + "  font-size: 30px;"
                + "  font-weight: bold;"
                + "  color: #ff3333;"
                + "  border-bottom: 4px solid #ff3333;"
                + "  padding-bottom: 12px;"
                + "  margin-bottom: 20px;"
                + "  letter-spacing: 3px;"
                + "}"
                + ".encabezado, .linea {"
                + "  display: grid;"
                + "  grid-template-columns: 70% 30%;"
                + "  font-size: 22px;"
                + "  letter-spacing: 2px;"
                + "  padding: 6px 0;"
                + "}"
                + ".linea { font-size: 24px; }"
                + ".verde { color: #00ff00; }"
                + ".rojo { color: #ff3333; }"
                + "</style>"
                + "</head><body>"
                + "<h1 style='color:#ff5555;'>ITV del Infierno</h1>"
                + "<form action='/reservar' method='GET'>"
                + "<button class='boton'>Reservar Cita</button>"
                + "</form>"
                + "<form action='/pasar' method='GET'>"
                + "<button class='boton'>Pasar ITV</button>"
                + "</form>"
                + "<h2 style='color:#ff8888;'>Panel de Llamadas</h2>"
                + "<div class='panel'>"
                + "<div class='panel-titulo'>LÍNEAS DE INSPECCIÓN</div>"
                + panelHTML
                + "</div></body></html>";
    }

    public static String htmlReservar(String mensajeError) {
        return "<html><head><meta charset='UTF-8'><title>Reservar Cita</title>"
                + "<style>"
                + "body { background: linear-gradient(135deg,#550000,#220000); "
                + "       color:white; font-family:Arial; text-align:center; padding-top:40px; }"
                + "input { padding:10px; font-size:18px; border-radius:8px; border:none; }"
                + "button { background:#ff4444; color:white; padding:12px 25px; "
                + "        border:none; border-radius:8px; font-size:18px; cursor:pointer; }"
                + "button:hover { background:#cc0000; }"
                + "a { color:#ffaaaa; font-size:20px; }"
                + "</style></head>"
                + "<body>"
                + "<h1>Reservar Cita ITV</h1>"
                + mensajeError
                + "<form action='/reservar' method='POST'>"
                + "Matricula: <input type='text' name='matricula' required>"
                + "<br><br>"
                + "<button type='submit'>Confirmar</button>"
                + "</form>"
                + "<br><a href='/'>Volver</a>"
                + "</body></html>";
    }



    public static String htmlPasarITV(String matriculaPrellenada, String resultadoFragmento) {
        String valor = (matriculaPrellenada != null) ? matriculaPrellenada : "";

        return "<html><head><meta charset='UTF-8'><title>Pasar ITV</title>"
                + "<style>"
                + "body { background: linear-gradient(135deg, #000000, #440000); color:white; font-family:Arial; text-align:center; padding-top:40px; }"
                + "h1 { color:#ff4444; text-shadow:0 0 10px #ff0000; }"
                + "input { padding:15px; font-size:24px; border-radius:10px; border:3px solid #ff0000; background:#330000; color:white; width:280px; text-align:center; }"
                + "button { background:#ff3333; color:white; padding:15px 35px; border:none; border-radius:10px; font-size:22px; cursor:pointer; transition:0.3s; }"
                + "button:hover { background:#cc0000; transform:scale(1.05); }"
                + "a { color:#ffaaaa; font-size:22px; text-decoration:none; }"
                + "a:hover { text-decoration:underline; }"
                + "</style></head><body>"
                + "<h1>Entrada a la ITV del Infierno</h1>"
                + "<form action='/pasar' method='POST'>"
                + "Matrícula:<br><br>"
                + "<input type='text' name='matricula' required value='" + valor + "'>"
                + "<br><br>"
                + "<button type='submit'>Entrar a línea</button>"
                + "</form>"
                + "<div class='resultado'>" + resultadoFragmento + "</div>"
                + "<br><a href='/'>Volver</a>"
                + "</body></html>";
    }

    public static final String html_notFound
            = "<html><head><title>Error 404</title><meta charset=UTF-8>"
            + "<link rel=icon href=data:,/>"
            + "<style>"
            + "body{font-family:Arial;background:linear-gradient(135deg,#ff9a9e,#fad0c4);"
            + "text-align:center;padding-top:60px;}"
            + "h1{color:#333;font-size:48px;margin-bottom:10px;}"
            + "p{font-size:20px;color:#555;}"
            + "a.button{display:inline-block;padding:12px 25px;background:#e74c3c;color:white;"
            + "text-decoration:none;border-radius:8px;font-size:18px;transition:0.3s;margin-top:20px;}"
            + "a.button:hover{background:#c0392b;}"
            + "</style></head>"
            + "<body>"
            + "<h1>Error 404</h1>"
            + "<p>La página que buscas no existe o no se encuentra disponible.</p>"
            + "<a class='button' href='/'>Volver al inicio</a>"
            + "</body></html>";
    
        public static String htmlResultado(String contenido) {
        return "<div style='width:100%; text-align:center;'>"
                + "<h1 style='font-size:28px; margin-bottom:20px; color:#ffdddd;'>Resultado de la Inspección</h1>"
                + "<div class='box' style='background:#550000; padding:15px; width:45%; margin:auto; border-radius:10px; box-shadow:0 0 10px #00000088; font-size:20px; color:white;'>"
                + contenido
                + "</div>"
                + "<div class='taller' style='margin-top:20px; background:#ffeecc; color:#442200; padding:12px; border-radius:10px; width:40%; margin-left:auto; margin-right:auto; box-shadow:0 0 8px #ffcc66; border:2px solid #ffbb55; font-family:\"Comic Sans MS\";'>"
                + "<h3 style='color:#aa5500; margin-bottom:4px; font-size:13px;'>🔧 Taller \\\"EL Oportuno\\\" 🔧</h3>"
                + "<p style='font-size:10px;'>¿No has pasado la ITV? Vaya, qué lástima…</p>"
                + "<p style='font-size:10px;'><b>Estamos justo al lado</b>, pura coincidencia</p>"
                + "<p style='font-size:10px;'><i>5% descuento con el código:<span style='font-size:3px;'>Deja de hacer zoom, y ponte con la tarea. Vas a suspender</span></i></p>"
                + "<small style='font-size:10px;'>(Si usa el código, se lo arreglará el becario)</small>"
                + "</div>"
                + "</div>";
    }
    
       /**
     * Construye estructura HTML del resultado de la inspección.
     *
     * @param matricula Identificador del coche.
     * @param resultados Mapa con los nombres de las pruebas y sus valores.
     * @return Cadena formateada para su inserción en el HTML.
     */
    public static String generarResultadoHTML(String matricula, Map<String, String> resultados) {
        
        // Se declaran/instancian las variables
        StringBuilder sb = new StringBuilder();
        boolean aprobada = true;

        sb.append("<strong>Matrícula: ").append(matricula).append("</strong><br><br>");

        for (Map.Entry<String, String> entry : resultados.entrySet()) {
            sb.append("<b>").append(entry.getKey()).append(":</b> ")
                    .append(entry.getValue()).append("<br>");

            if (entry.getValue().startsWith("No")) {
                aprobada = false;
            }
        }

        // Montar el mensaje resultado de salida en un color apropiado
        sb.append("<br><hr>");
        if (aprobada) {
            sb.append("<h2 style='color:#00ff00;'>ITV SUPERADA</h2>");
        } else {
            sb.append("<h2 style='color:#ff0000;'>ITV NO SUPERADA</h2>");
        }

        return sb.toString();
    }
}
