package c3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GeneradorTresDirecciones {

    private String variableAsignacion;

    // Método principal que recibe una expresión o sentencia y la convierte en código de tres direcciones
    public String convertirA3Direcciones(String expresion) {
        GeneradorC3D generadorC3D = new GeneradorC3D(); // Instanciar el generador C3D

        if (expresion.contains("not")) {
            procesarNot(expresion, generadorC3D);
        } else if (expresion.contains("and")) {
            procesarAnd(expresion, generadorC3D);
        } else if (expresion.contains("or")) {
            procesarOr(expresion, generadorC3D);
        } else if (expresion.startsWith("do")) {
            procesarDoWhile(expresion, generadorC3D); // Procesar sentencias do-while
        } else if (expresion.contains("while")) {
            procesarWhile(expresion, generadorC3D); // Procesar sentencias while
        } else if (expresion.contains("else if")) {
            procesarIfElseIf(expresion, generadorC3D); // Procesar sentencias if-else if con varios bloques
        } else if (expresion.contains("if") && expresion.contains("else")) {
            procesarIfElse(expresion, generadorC3D); // Procesar sentencias if-else
        } else if (expresion.contains("if")) {
            procesarIf(expresion, generadorC3D); // Procesar sentencias if
        } else {
            if (expresion.contains("=")) {
                String[] partes = expresion.split("=", 2);  // Separar por el igual
                variableAsignacion = partes[0].trim();  // La variable a la izquierda del igual
                expresion = partes[1].trim();  // La expresión aritmética a la derecha del igual
            } else {
                variableAsignacion = null;
            }

            String ultimoTemporal = procesarExpresion(expresion, generadorC3D); // Usar generadorC3D

            // Si hay una asignación, asignar el último temporal a la variable
            if (variableAsignacion != null) {
                generadorC3D.agregarCodigo(variableAsignacion + " = " + ultimoTemporal + ";");
            }
        }

        return generadorC3D.obtenerCodigo(); // Obtener el código generado
    }

    private int precedencia(char operador) {
        return switch (operador) {
            case '+', '-' ->
                1;
            case '*', '/' ->
                2;
            default ->
                0;
        };
    }

    private void generarCodigo(Stack<String> pilaOperandos, char operador, GeneradorC3D generadorC3D) {
        // Obtenemos los dos operandos
        String op2 = pilaOperandos.pop();
        String op1 = pilaOperandos.pop();

        // Generamos un nuevo temporal
        String temporal = generadorC3D.nuevoTemporal();

        // Agregamos la instrucción a GeneradorC3D
        generadorC3D.agregarCodigo(temporal + " = " + op1 + " " + operador + " " + op2 + ";");

        // Guardamos el temporal en la pila de operandos
        pilaOperandos.push(temporal);
    }

    private boolean esOperador(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    //PROCESAMIENTO DE ESTRUCTURAS
    private String procesarExpresion(String expresion, GeneradorC3D generadorC3D) {
        Stack<String> pilaOperandos = new Stack<>();
        Stack<Character> pilaOperadores = new Stack<>();
        int i = 0;

        while (i < expresion.length()) {
            char c = expresion.charAt(i);

            // Si es un dígito o letra, lo tratamos como un operando
            if (Character.isDigit(c) || Character.isLetter(c)) {
                StringBuilder operando = new StringBuilder();
                while (i < expresion.length() && (Character.isDigit(expresion.charAt(i)) || Character.isLetter(expresion.charAt(i)))) {
                    operando.append(expresion.charAt(i));
                    i++;
                }
                pilaOperandos.push(operando.toString());
                continue;
            } else if (esOperador(c)) {
                // Si es un operador, evaluamos la precedencia y generamos código si es necesario
                while (!pilaOperadores.isEmpty() && precedencia(pilaOperadores.peek()) >= precedencia(c)) {
                    generarCodigo(pilaOperandos, pilaOperadores.pop(), generadorC3D);
                }
                pilaOperadores.push(c);
            } else if (c == '(') {
                // Si es un paréntesis abierto, lo agregamos a la pila
                pilaOperadores.push(c);
            } else if (c == ')') {
                // Si es un paréntesis cerrado, procesamos los operadores hasta llegar a un paréntesis abierto
                while (pilaOperadores.peek() != '(') {
                    generarCodigo(pilaOperandos, pilaOperadores.pop(), generadorC3D);
                }
                pilaOperadores.pop(); // Quitamos el paréntesis abierto
            }
            i++;
        }

        // Procesar los operadores restantes en la pila
        while (!pilaOperadores.isEmpty()) {
            generarCodigo(pilaOperandos, pilaOperadores.pop(), generadorC3D);
        }

        // Retornar el último operando generado (que será un temporal o el resultado final)
        return pilaOperandos.pop();
    }

    // Ajuste para manejar estructuras anidadas
    private void procesarIf(String expresion, GeneradorC3D generadorC3D) {
        String condicion = expresion.substring(expresion.indexOf('(') + 1, expresion.indexOf(')')).trim();
        String cuerpoIf = expresion.substring(expresion.indexOf('{') + 1, expresion.lastIndexOf('}')).trim();

        String etiquetaTrue = generadorC3D.nuevaEtiqueta();
        String etiquetaFalse = generadorC3D.nuevaEtiqueta();
        String etiquetaSalidaIf = generadorC3D.nuevaEtiqueta();

        // Evaluar condición
        generadorC3D.agregarCodigo("if " + condicion + " goto " + etiquetaTrue + ";");
        generadorC3D.agregarCodigo("goto " + etiquetaFalse + ";");

        // Procesar el cuerpo del if
        generadorC3D.agregarCodigo(etiquetaTrue + ":");
        procesarBloqueAnidado(cuerpoIf, generadorC3D);  // Llamada al nuevo método para manejar bloques anidados

        // Evitar agregar saltos innecesarios si ya estamos en la etiqueta de salida
        if (!generadorC3D.esUltimaEtiqueta(etiquetaSalidaIf)) {
            generadorC3D.agregarCodigo("goto " + etiquetaSalidaIf + ";");
        }

        // Etiqueta para la salida falsa
        generadorC3D.agregarCodigo(etiquetaFalse + ":");

        // Etiqueta de salida final
        generadorC3D.agregarCodigo(etiquetaSalidaIf + ":");
    }

    private void procesarIfElse(String expresion, GeneradorC3D generadorC3D) {
        String condicion = expresion.substring(expresion.indexOf('(') + 1, expresion.indexOf(')')).trim();
        String cuerpoIf = expresion.substring(expresion.indexOf('{') + 1, expresion.indexOf('}')).trim();

        // Verificar si existe un bloque else
        String cuerpoElse = "";
        if (expresion.contains("else")) {
            int indexElse = expresion.indexOf("else");
            cuerpoElse = expresion.substring(expresion.indexOf('{', indexElse) + 1, expresion.lastIndexOf('}')).trim();
        }

        String etiquetaTrue = generadorC3D.nuevaEtiqueta();  // Etiqueta si la condición es verdadera
        String etiquetaFalse = generadorC3D.nuevaEtiqueta(); // Etiqueta si la condición es falsa (salta al else)
        String etiquetaSalida = generadorC3D.nuevaEtiqueta(); // Etiqueta para salir del bloque if-else

        // Evaluar la condición
        generadorC3D.agregarCodigo("if " + condicion + " goto " + etiquetaTrue + ";");
        generadorC3D.agregarCodigo("goto " + etiquetaFalse + ";");

        // Procesar el cuerpo del if (cuando la condición es verdadera)
        generadorC3D.agregarCodigo(etiquetaTrue + ":");
        procesarCuerpo(cuerpoIf, generadorC3D);
        generadorC3D.agregarCodigo("goto " + etiquetaSalida + ";");

        // Procesar el bloque else (cuando la condición es falsa)
        generadorC3D.agregarCodigo(etiquetaFalse + ":");
        if (!cuerpoElse.isEmpty()) {
            procesarCuerpo(cuerpoElse, generadorC3D);
        }

        // Etiqueta para salir de la estructura if-else
        generadorC3D.agregarCodigo(etiquetaSalida + ":");
    }

    //PROCESAR If-ElseIf-Else con ajustes
    private void procesarIfElseIf(String expresion, GeneradorC3D generadorC3D) {
        String condicionIf = expresion.substring(expresion.indexOf('(') + 1, expresion.indexOf(')')).trim();
        String cuerpoIf = expresion.substring(expresion.indexOf('{') + 1, expresion.indexOf('}')).trim();

        String etiquetaIfTrue = generadorC3D.nuevaEtiqueta();    // Etiqueta si el primer if es verdadero
        String etiquetaIfFalse = generadorC3D.nuevaEtiqueta();   // Etiqueta para el siguiente bloque (else if o else)
        String etiquetaSalida = generadorC3D.nuevaEtiqueta();    // Etiqueta para salir del bloque if

        // Procesar el if
        generadorC3D.agregarCodigo("if " + condicionIf + " goto " + etiquetaIfTrue + ";");
        generadorC3D.agregarCodigo("goto " + etiquetaIfFalse + ";");

        generadorC3D.agregarCodigo(etiquetaIfTrue + ":");
        procesarCuerpo(cuerpoIf, generadorC3D);
        generadorC3D.agregarCodigo("goto " + etiquetaSalida + ";");

        // Procesar los else if
        int indexElseIf = expresion.indexOf("else if");
        List<String> etiquetasSalida = new ArrayList<>(); // Usar una lista para mantener el orden
        etiquetasSalida.add(etiquetaSalida); // Agregar la etiqueta de salida del if
        String etiquetaFinalElseIf = null;

        while (indexElseIf != -1) {
            String condicionElseIf = expresion.substring(expresion.indexOf('(', indexElseIf) + 1, expresion.indexOf(')', indexElseIf)).trim();
            String cuerpoElseIf = expresion.substring(expresion.indexOf('{', indexElseIf) + 1, expresion.indexOf('}', indexElseIf)).trim();

            String etiquetaElseIfTrue = generadorC3D.nuevaEtiqueta();
            String etiquetaElseIfFalse = generadorC3D.nuevaEtiqueta();
            etiquetaFinalElseIf = generadorC3D.nuevaEtiqueta(); // Etiqueta de salida para el else if actual

            generadorC3D.agregarCodigo(etiquetaIfFalse + ":");
            generadorC3D.agregarCodigo("if " + condicionElseIf + " goto " + etiquetaElseIfTrue + ";");
            generadorC3D.agregarCodigo("goto " + etiquetaElseIfFalse + ";");

            generadorC3D.agregarCodigo(etiquetaElseIfTrue + ":");
            procesarCuerpo(cuerpoElseIf, generadorC3D);
            generadorC3D.agregarCodigo("goto " + etiquetaFinalElseIf + ";");

            etiquetasSalida.add(etiquetaFinalElseIf); // Agregar la etiqueta de salida a la lista

            etiquetaIfFalse = etiquetaElseIfFalse;
            indexElseIf = expresion.indexOf("else if", indexElseIf + 1);
        }

        // Procesar el bloque else
        if (expresion.contains("else")) {
            int indexElse = expresion.lastIndexOf("else");
            String cuerpoElse = expresion.substring(expresion.indexOf('{', indexElse) + 1, expresion.lastIndexOf('}')).trim();

            generadorC3D.agregarCodigo(etiquetaIfFalse + ":");
            procesarCuerpo(cuerpoElse, generadorC3D);
        }

        // Imprimir todas las etiquetas de salida en orden (FIFO)
        for (String etiqueta : etiquetasSalida) {
            generadorC3D.agregarCodigo(etiqueta + ":");
        }
    }

    private void procesarWhile(String expresion, GeneradorC3D generadorC3D) {
        String condicion = expresion.substring(expresion.indexOf('(') + 1, expresion.indexOf(')')).trim();
        String cuerpoWhile = expresion.substring(expresion.indexOf('{') + 1, expresion.lastIndexOf('}')).trim();

        String etiquetaTrue = generadorC3D.nuevaEtiqueta();   // Etiqueta para el cuerpo del ciclo
        String etiquetaFalse = generadorC3D.nuevaEtiqueta();  // Etiqueta para salida del ciclo
        String etiquetaInicio = generadorC3D.nuevaEtiqueta(); // Etiqueta de regreso al inicio del ciclo

        // Evaluar condición
        generadorC3D.agregarCodigo(etiquetaInicio + ":");
        generadorC3D.agregarCodigo("if " + condicion + " goto " + etiquetaTrue + ";");
        generadorC3D.agregarCodigo("goto " + etiquetaFalse + ";");

        // Cuerpo del ciclo
        generadorC3D.agregarCodigo(etiquetaTrue + ":");
        procesarBloqueAnidado(cuerpoWhile, generadorC3D);  // Procesar cuerpo anidado
        generadorC3D.agregarCodigo("goto " + etiquetaInicio + ";");

        // Salida del ciclo
        generadorC3D.agregarCodigo(etiquetaFalse + ":");
    }

    private void procesarDoWhile(String expresion, GeneradorC3D generadorC3D) {
        String cuerpoDoWhile = expresion.substring(expresion.indexOf('{') + 1, expresion.lastIndexOf('}')).trim();
        String condicion = expresion.substring(expresion.lastIndexOf('(') + 1, expresion.lastIndexOf(')')).trim();

        String etiquetaInicio = generadorC3D.nuevaEtiqueta();  // Etiqueta para el cuerpo del ciclo
        String etiquetaSalida = generadorC3D.nuevaEtiqueta();  // Etiqueta para salir del ciclo

        // Generar la etiqueta de inicio del ciclo
        generadorC3D.agregarCodigo(etiquetaInicio + ":");

        // Procesar el cuerpo del ciclo
        procesarCuerpo(cuerpoDoWhile, generadorC3D);

        // Evaluar la condición
        generadorC3D.agregarCodigo("if " + condicion + " goto " + etiquetaInicio + ";");
        generadorC3D.agregarCodigo("goto " + etiquetaSalida + ";");

        // Etiqueta de salida
        generadorC3D.agregarCodigo(etiquetaSalida + ":");
    }

    // Método para procesar el cuerpo de los bloques (if, else if, else)
    private void procesarCuerpo(String cuerpo, GeneradorC3D generadorC3D) {
        if (cuerpo.contains("=")) {
            String[] partes = cuerpo.split("=", 2);
            String variableAsig = partes[0].trim();
            String expresion = partes[1].trim();

            String ultimoTemporal = procesarOperacionCompleja(expresion, generadorC3D);
            if (variableAsig != null && !ultimoTemporal.isEmpty()) {
                generadorC3D.agregarCodigo(variableAsig + " = " + ultimoTemporal + ";");
            }
        }
    }

    // MÉTODO para manejar expresiones aritméticas complejas y generar código simplificado
    private String procesarOperacionCompleja(String expresion, GeneradorC3D generadorC3D) {
        Stack<String> pilaOperandos = new Stack<>();
        Stack<Character> pilaOperadores = new Stack<>();
        int i = 0;

        while (i < expresion.length()) {
            char c = expresion.charAt(i);

            if (Character.isDigit(c) || Character.isLetter(c)) {
                StringBuilder operando = new StringBuilder();
                while (i < expresion.length() && (Character.isDigit(expresion.charAt(i)) || Character.isLetter(expresion.charAt(i)))) {
                    operando.append(expresion.charAt(i));
                    i++;
                }
                pilaOperandos.push(operando.toString());
                continue;
            } else if (esOperador(c)) {
                while (!pilaOperadores.isEmpty() && precedencia(pilaOperadores.peek()) >= precedencia(c)) {
                    generarCodigo(pilaOperandos, pilaOperadores.pop(), generadorC3D);
                }
                pilaOperadores.push(c);
            } else if (c == '(') {
                pilaOperadores.push(c);
            } else if (c == ')') {
                while (pilaOperadores.peek() != '(') {
                    generarCodigo(pilaOperandos, pilaOperadores.pop(), generadorC3D);
                }
                pilaOperadores.pop(); // Quitamos el paréntesis abierto
            }
            i++;
        }

        while (!pilaOperadores.isEmpty()) {
            generarCodigo(pilaOperandos, pilaOperadores.pop(), generadorC3D);
        }

        return pilaOperandos.pop();
    }

    //OPERADORES LÓGICOS
    private void procesarNot(String expresion, GeneradorC3D generadorC3D) {
    // Eliminamos el 'not' al inicio de la expresión si existe
    String condicion = expresion.trim();
    if (condicion.startsWith("not")) {
        condicion = condicion.substring(3).trim();  // Quitamos el 'not' y cualquier espacio adicional
    }

    // Generamos las etiquetas necesarias
    String etiquetaVerdadera = generadorC3D.nuevaEtiqueta();  // Si la condición es verdadera
    String etiquetaFalsa = generadorC3D.nuevaEtiqueta();      // Si la condición es falsa

    // Evaluamos la condición y cambiamos las etiquetas
    generadorC3D.agregarCodigo("if " + condicion + " goto " + etiquetaVerdadera + ";");
    generadorC3D.agregarCodigo("goto " + etiquetaFalsa + ";");

    // Etiqueta falsa primero
    generadorC3D.agregarCodigo(etiquetaFalsa + ":");

    // Etiqueta verdadera
    generadorC3D.agregarCodigo(etiquetaVerdadera + ":");
}


    private void procesarAnd(String expresion, GeneradorC3D generadorC3D) {
        String[] partes = expresion.split("and");
        String izquierda = partes[0].trim();
        String derecha = partes[1].trim();

        // Generamos las etiquetas necesarias
        String etiquetaVerdadera1 = generadorC3D.nuevaEtiqueta(); // Etiqueta para la primera condición verdadera
        String etiquetaFalsa1 = generadorC3D.nuevaEtiqueta();     // Etiqueta para la primera condición falsa (salida inmediata)
        String etiquetaVerdadera2 = generadorC3D.nuevaEtiqueta(); // Etiqueta para la segunda condición verdadera
        String etiquetaFalsa2 = generadorC3D.nuevaEtiqueta();     // Etiqueta para la segunda condición falsa (salida inmediata)

        // Primera condición
        generadorC3D.agregarCodigo("if " + izquierda + " goto " + etiquetaVerdadera1 + ";");
        generadorC3D.agregarCodigo("goto " + etiquetaFalsa1 + ";");

        // Segunda condición
        generadorC3D.agregarCodigo(etiquetaVerdadera1 + ":");
        generadorC3D.agregarCodigo("if " + derecha + " goto " + etiquetaVerdadera2 + ";");
        generadorC3D.agregarCodigo("goto " + etiquetaFalsa2 + ";");

        // Salida
        generadorC3D.agregarCodigo(etiquetaVerdadera2 + ":");

        generadorC3D.agregarCodigo(etiquetaFalsa1 + ":");
        generadorC3D.agregarCodigo(etiquetaFalsa2 + ":");

    }

    private void procesarOr(String expresion, GeneradorC3D generadorC3D) {
        String[] partes = expresion.split("or");
        String izquierda = partes[0].trim();
        String derecha = partes[1].trim();

        // Generamos las etiquetas necesarias
        String etiquetaVerdadera1 = generadorC3D.nuevaEtiqueta(); // Si la primera condición es verdadera
        String etiquetaFalsa1 = generadorC3D.nuevaEtiqueta();     // Si la primera condición es falsa
        String etiquetaVerdadera2 = generadorC3D.nuevaEtiqueta(); // Si la segunda condición es verdadera
        String etiquetaFalsa2 = generadorC3D.nuevaEtiqueta();     // Si la segunda condición es falsa (final)

        // Primera condición
        generadorC3D.agregarCodigo("if " + izquierda + " goto " + etiquetaVerdadera1 + ";");
        generadorC3D.agregarCodigo("goto " + etiquetaFalsa1 + ";");

        // Segunda condición
        generadorC3D.agregarCodigo(etiquetaFalsa1 + ":");
        generadorC3D.agregarCodigo("if " + derecha + " goto " + etiquetaVerdadera2 + ";");
        generadorC3D.agregarCodigo("goto " + etiquetaFalsa2 + ";");

        // Salida
        generadorC3D.agregarCodigo(etiquetaVerdadera1 + ":");
        generadorC3D.agregarCodigo(etiquetaVerdadera2 + ":");

        // Etiqueta final falsa
        generadorC3D.agregarCodigo(etiquetaFalsa2 + ":");
    }

    // Método auxiliar para manejar bloques anidados
    private void procesarBloqueAnidado(String bloque, GeneradorC3D generadorC3D) {
        // Este método recibe el cuerpo del bloque (por ejemplo, el contenido de un if o while)
        // y lo procesa de forma recursiva si tiene más estructuras anidadas
        if (bloque.contains("if")) {
            procesarIf(bloque, generadorC3D);
        } else if (bloque.contains("while")) {
            procesarWhile(bloque, generadorC3D);
        } else if (bloque.contains("for")) {
            // Se puede agregar un método para procesar bucles for si lo necesitas
        } else {
            // Procesar expresiones simples
            procesarCuerpo(bloque, generadorC3D);
        }
    }
}
