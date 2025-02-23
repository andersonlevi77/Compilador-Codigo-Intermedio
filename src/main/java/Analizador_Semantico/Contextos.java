package Analizador_Semantico;

import Excepciones.Errores;
import java.util.HashMap;
import java.util.Stack;

/**
 *
 * @author Juan Diaz
 */

/**
 * Clase que maneja contextos para el análisis semántico. Utiliza una pila para
 * gestionar los diferentes contextos durante el análisis.
 */
public class Contextos {

    private final Stack<HashMap<String, VariableInfo>> contextStack;

    /**
     * Constructor que inicializa la pila y crea un contexto global.
     */
    public Contextos() {
        contextStack = new Stack<>();
        contextStack.push(new HashMap<>()); // Crear un contexto global inicial
    }

    /**
     * Abre un nuevo contexto empujando un nuevo HashMap en la pila.
     */
    public void openNewContext() {
        contextStack.push(new HashMap<>());
    }

    /**
     * Cierra el contexto actual eliminándolo de la pila.
     */
    public void closeCurrentContext() {
        if (!contextStack.isEmpty()) {
            contextStack.pop();
        }
    }

    /**
     * Declara una nueva variable en el contexto actual. id: Identificador de la
     * variable. info: Información de la variable a declarar. Verdadero si la
     * variable fue declarada correctamente. Exception Si la variable ya está
     * declarada en el contexto actual.
     */
    public boolean declarar_ComprobarVariable(String id, VariableInfo info) throws Exception {
        HashMap<String, VariableInfo> currentContext = contextStack.peek();
        if (currentContext.containsKey(id)) {
            throw new Errores("La variable '" + id + "' ya esta declarada en un Contexto");
        }
        currentContext.put(id, info);
        return true;
    }

    /**
     * Obtiene el tamaño de la pila.
     *
     * @return tamaño de la pila.
     */
    
    public int getCurrentContextNumber() {
        return contextStack.size();
    }
}
