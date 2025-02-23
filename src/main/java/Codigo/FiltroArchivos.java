package Codigo;

import java.io.File;
import javax.swing.filechooser.*;

/**
 *
 * @author juani
 */
public class FiltroArchivos extends FileFilter {

    private final String extension;
    private final String descripcion;

    public FiltroArchivos(String extension, String descripcion) {
        this.extension = extension;
        this.descripcion = descripcion;
    }

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        return file.getName().endsWith(extension);
    }

    @Override
    public String getDescription() {
        return descripcion + String.format(" (*%s)", extension);
    }
}
