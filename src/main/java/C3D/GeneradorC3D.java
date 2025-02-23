package c3d;

/**
 *
 * @author Juan Diaz
 */
public class GeneradorC3D {

    private StringBuilder codigo;
    private GeneradorEtiquetasTemporales generador;

    public GeneradorC3D() {
        codigo = new StringBuilder();
        generador = new GeneradorEtiquetasTemporales();
    }

    // Agregar código al buffer
    public void agregarCodigo(String linea) {
        codigo.append(linea).append("\n");
    }

    // Generar una etiqueta
    public String nuevaEtiqueta() {
        return generador.generarEtiqueta();
    }

    // Generar un temporal
    public String nuevoTemporal() {
        return generador.generarTemporal();
    }

    // Obtener el código generado
    public String obtenerCodigo() {
        return codigo.toString();
    }

    // Reiniciar el código generado
    public void limpiarCodigo() {
        codigo.setLength(0);  // Vacía el buffer de código
    }

    // Método para verificar si la última línea generada es una etiqueta
    public boolean esUltimaEtiqueta(String etiqueta) {
        // Convertir el StringBuilder a una cadena completa y luego dividirla en líneas
        String[] lineas = codigo.toString().split("\n");

        if (lineas.length == 0) {
            return false;
        }

        // Obtenemos la última línea y la verificamos
        String ultimaLinea = lineas[lineas.length - 1].trim();
        return ultimaLinea.equals(etiqueta + ":");
    }
}
