package c3d;

public class GeneradorEtiquetasTemporales {

    private int temporalCount;
    private int etiquetaCount;

    public GeneradorEtiquetasTemporales() {
        this.temporalCount = 1;
        this.etiquetaCount = 1;
    }

    public String generarTemporal() {
        return "t" + (temporalCount++);
    }

    public String generarEtiqueta() {
        return "L" + (etiquetaCount++);
    }
}
