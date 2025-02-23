package Analizador_Semantico;

import Interfaz.tablaContextos;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Juan Diaz
 */
public class Semantico {

    // Gestiona los diferentes contextos de análisis semántico (cada bloque de código tiene su contexto)
    private final Contextos gestionContextos;
    // Indica si el siguiente token debería ser el nombre de una variable (tras encontrar un tipo de dato)
    private boolean esperandoDeclaracionDeVariable = false;
    private boolean llaveAbierta = false; // Indica si alguna llave '{' esta abierta
    private final tablaContextos vistaTablaContextos; // Interfaz gráfica para mostrar los contextos y variables declaradas
    // Almacena el contexto actual de cada variable por su nombre
    private final HashMap<String, Integer> contextoVariables = new HashMap<>();
    private boolean contextoCero = false; //Variable para detectar bien el contexto 0 de una variable
    private String[] tokenAnt; //Variable que guarda el token encontrado
    private boolean asigVariable = false; // Indica si hay una asignacion a una variable

    /**
     * Constructor de la clase Semantico.
     *
     * @param vista Referencia a la interfaz de la tabla de contextos para
     * visualización.
     */
    public Semantico(tablaContextos vista) {
        this.gestionContextos = new Contextos();
        this.vistaTablaContextos = vista;
    }

    /**
     * Registra una variable en el contexto actual.
     *
     * @param token Array que contiene el tipo y el nombre de la variable.
     * @throws Exception Si la variable no puede ser declarada (por ejemplo, si
     * ya existe en el contexto).
     */
    private void registrarVariable(String[] token, String valorAsig) throws Exception {
        String tipo = token[0];
        String valor = token[1];

        int contextoActual = contextoVariables.getOrDefault(valor, 0); // Obtiene el contexto actual de la variable

        if (contextoCero) {//Se detecta el contexto 0
            contextoActual = 0;
        }

        // Crear una nueva instancia de VariableInfo con los datos de la variable
        VariableInfo info = new VariableInfo(tipo, valor, contextoActual, valorAsig);

        // Registra la variable en el contexto actual. 
        // Si la variable ya está declarada en este contexto, el método 'declararVariable' lanzará una excepción.
        gestionContextos.declarar_ComprobarVariable(valor, info);

        // Agregar la información de la variable a la tabla de contextos.
        vistaTablaContextos.agregarTabla(tipo, valor, contextoActual, valorAsig);
        //System.out.println("Variable " + valor + " declarada en el contexto número: " + contextoActual);
    }

    /**
     * manejo de los contextos cuando se abre una llave o si la variable esta en
     * un contexto 0.
     */
    private void manejoContextos() {
        if (llaveAbierta) { //si hay una llave abierta le suma '1' al contexto
            contextoVariables.put(tokenAnt[1], contextoVariables.getOrDefault(tokenAnt[1], 0) + 1);
        } else if (contextoVariables.getOrDefault(tokenAnt[1], 0) > 0 && !llaveAbierta) { //Condicion para detectar el contexto 0
            contextoCero = true;
        }
    }

    /**
     * Analiza una lista de tokens para realizar operaciones semánticas como
     * abrir/cerrar contextos y registrar variables.
     *
     * @param tokens Lista de tokens a analizar.
     * @throws Exception Si ocurre un error durante el análisis.
     */
    public void analizarTokens(ArrayList<String[]> tokens) throws Exception {
        for (String[] token : tokens) {
            if (token == null || token[0].isEmpty()) {
                continue; // Ignorar tokens vacíos o saltos de línea
            }
            
            // Aquí sigue el resto de tu lógica para analizar tokens
            switch (token[0]) {
                case "simbolo":
                    switch (token[1]) {
                        case "{":
                            // Abre un nuevo contexto cuando se encuentra una llave abierta
                            gestionContextos.openNewContext();
                            llaveAbierta = true;
                            break;
                        case "}":
                            // Cierra el contexto actual cuando se encuentra una llave cerrada
                            gestionContextos.closeCurrentContext();
                            llaveAbierta = false;
                            break;
                        case "=":
                            asigVariable = true; //si encuaentra una asignacion
                            break;
                        default:
                            break;
                    }
                    break;
                case "Tipo dato":
                    // Espera la declaración de una variable cuando se encuentra un tipo de dato reservado
                    esperandoDeclaracionDeVariable = true;
                    break;
                case "id":
                    // Si se espera una declaración y guarda el token
                    if (esperandoDeclaracionDeVariable) {
                        tokenAnt = token; //guarda el token encontrado
                        esperandoDeclaracionDeVariable = false;
                    }
                    break;
                case "valor":
                    if (asigVariable && tokenAnt != null) {
                        manejoContextos(); //actualiza el contexto de la variable
                        registrarVariable(tokenAnt, token[1]); //registra la variable y su asignacion 'valor'
                        asigVariable = false;
                        contextoCero = false;
                        tokenAnt = null;  // Limpia después de completar la declaración con asignación
                    }
                    break;
                case "delimitador":
                    // Si se encuentra un punto y coma, finaliza la declaración de la variable
                    if (!asigVariable && tokenAnt != null) { //si no hay un valo asignado a la variable lo deja vacio 
                        manejoContextos(); //actualiza el contexto de la variable
                        registrarVariable(tokenAnt, ""); //registra la variable
                        //throw new Errores("Variable '" + tokenAnt[1] + "' No inicializada");
                    }
                    tokenAnt = null;
                    contextoCero = false;
                    esperandoDeclaracionDeVariable = false;
                    break;
                default:
                    // En cualquier otro caso, resetea el indicador de declaración de variable
                    esperandoDeclaracionDeVariable = false;
                    tokenAnt = null;
                    break;
            }
        }
    }
}
