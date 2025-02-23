package c3d;

public class ListasVF {

    public String LV;
    public String LF;
    public String C3D;

    public ListasVF(String lv, String lf, String c3d) {
        this.LV = lv;
        this.LF = lf;
        this.C3D = c3d;
    }

    public void actualizarEtiquetas(String nuevaLV, String nuevaLF) {
        this.LV = nuevaLV;
        this.LF = nuevaLF;
    }
}
