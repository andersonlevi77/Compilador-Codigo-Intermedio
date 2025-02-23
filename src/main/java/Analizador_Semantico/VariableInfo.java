package Analizador_Semantico;

/**
 *
 * @author Juan Diaz
 */

/**
 * Clase que representa la información asociada a una variable. Almacena el
 * tipo, valor inicial y el contexto en el que la variable es declarada.
 */
public class VariableInfo {

    private String tipo;    // Tipo de dato de la variable, como "int", "String", etc.
    private String valor; // Valor inicial de la variable
    private int contexto;   // Número del contexto en el que la variable es declarada.
    private String asignacion;

    /**
     * Constructor para crear una nueva VariableInfo.
     *
     * @param tipo El tipo de la variable.
     * @param valor El valor inicial de la variable.
     * @param contextoInicial El contexto en el que la variable es declarada.
     * @param asignacion El valor asignado a la variable.
     */
    public VariableInfo(String tipo, String valor, int contextoInicial, String asignacion) {
        this.tipo = tipo;
        this.valor = valor;
        this.contexto = contextoInicial;
        this.asignacion = asignacion;
    }

    public String getAsignacion() {
        return asignacion;
    }

    public void setAsignacion(String asignacion) {
        this.asignacion = asignacion;
    }

    /**
     * Devuelve el tipo de la variable.
     *
     * @return Tipo de la variable.
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Establece el tipo de la variable.
     *
     * @param tipo El nuevo tipo de la variable.
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * Devuelve el valor inicial de la variable.
     *
     * @return Valor inicial de la variable.
     */
    public String getValor() {
        return valor;
    }

    /**
     * Establece un nuevo valor para la variable.
     *
     * @param valor El nuevo valor de la variable.
     */
    public void setValor(String valor) {
        this.valor = valor;
    }

    /**
     * Devuelve el contexto en el que la variable fue declarada.
     *
     * @return Contexto de la declaración.
     */
    public int getContexto() {
        return contexto;
    }

    /**
     * Establece un nuevo contexto para la variable.
     *
     * @param contexto El nuevo contexto de la variable.
     */
    public void setContexto(int contexto) {
        this.contexto = contexto;
    }
}
