package Excepciones;

import javax.swing.JOptionPane;

/**
 *
 * @author juani
 */
public class Errores extends Exception {

    public Errores() {
        super();
    }

    //constructor que envia un mensaje
    public Errores(String msg) {
        super(msg);
        JOptionPane.showMessageDialog(null, msg, "ADVERTENCIA",JOptionPane.WARNING_MESSAGE );
    }
}
