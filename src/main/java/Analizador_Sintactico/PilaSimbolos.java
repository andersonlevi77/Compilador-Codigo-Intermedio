package Analizador_Sintactico;

/**
 *
 * @author juani
 */
public class PilaSimbolos {

    private class Nodo {

        char valor; //elemento que tiene el nodo
        Nodo anterior;
    }
    
    private Nodo ultimoNodoIngresado; //ultimo nodo ingresado

    public PilaSimbolos() {
        this.ultimoNodoIngresado = null;
    }

    //metodo para insertar dentro de la pila
    public void insertar(char valor) {
        Nodo nuevoNodo = new Nodo();
        nuevoNodo.valor = valor;
        if (ultimoNodoIngresado == null) {
            nuevoNodo.anterior = null;
            ultimoNodoIngresado = nuevoNodo;
        } else {
            nuevoNodo.anterior = ultimoNodoIngresado;
            ultimoNodoIngresado = nuevoNodo;
        }
    }

    //metodo para extraer datos
    public char extraer() {
        if (ultimoNodoIngresado != null) {
            char informacion = ultimoNodoIngresado.valor; //contiene el simbolo
            ultimoNodoIngresado = ultimoNodoIngresado.anterior; //apuntar al siguiente nodo
            return informacion;
        } else {
            return Character.MAX_VALUE;
        }
    }

    //metodo para saber si la pila esta vacia
    public boolean pilaVacia() {
        return ultimoNodoIngresado == null;
    }

}
