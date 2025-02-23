package Interfaz;

import Codigo.*;
import Analizador_Sintactico.*;
import Analizador_Semantico.*;
import Excepciones.Errores;
import c3d.GeneradorC3D;
import c3d.GeneradorTresDirecciones;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Anderson
 */
public class Interfaz extends javax.swing.JFrame {

    DefaultTableModel tabla;
    DefaultTableModel modeloReservadas = new DefaultTableModel();
    private List<String> reglasSemanticas;
    // Modelo para la tabla de símbolos
    DefaultTableModel modeloSimbolos = new DefaultTableModel();
    private String tokenAnterior = null;
    private int lineaTokenAnterior = -1;
    tablaContextos vistaContextos = new tablaContextos();
    tablaSintactica vistaSintactica = new tablaSintactica();
    


    public Interfaz() {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setTitle("Compilador");
        tabla = new DefaultTableModel();
        String[] titulo = new String[]{"No.Fila", "No.Columna", "Nombre", "Simbolo", "Tipo"};
        tabla.setColumnIdentifiers(titulo);

        // Inicializar los modelos de las tablas
        modeloReservadas.setColumnIdentifiers(new String[]{"No.Fila", "No.Columna", "Nombre", "Simbolo", "Tipo"});
        modeloSimbolos.setColumnIdentifiers(new String[]{"No.Fila", "No.Columna", "Nombre", "Simbolo", "Tipo"});

        // Asignar los modelos a las tablas
        tblDatos.setModel(modeloReservadas);
        tblSimbolos.setModel(modeloSimbolos);
        // Inicializar las reglas semánticas    
        inicializarReglasSemanticas();
      
    }

    private void EstiloJfilechooser() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Interfaz.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void inicializarReglasSemanticas() {
    reglasSemanticas = new ArrayList<>();
    
    //reglas semánticas básicas
    reglasSemanticas.add("int\\s+\\w+\\s*=\\s*\\d+;");   // Regla para declaraciones enteras
    reglasSemanticas.add("double\\s+\\w+\\s*=\\s*\\d+(\\.\\d+)?;"); // Regla para declaraciones de doble precisión
    reglasSemanticas.add("char\\s+\\w+\\s*=\\s*'.';"); // Regla para declaraciones de caracteres
    reglasSemanticas.add("\\w+\\s*=\\s*\\w+;"); // Regla para asignaciones generales
    reglasSemanticas.add("int\\s+\\w+;");   // Regla para declaraciones de enteros sin asignación
    reglasSemanticas.add("double\\s+\\w+;"); // Regla para declaraciones de dobles sin asignación
    reglasSemanticas.add("char\\s+\\w+;");   // Regla para declaraciones de caracteres sin asignación
    
    // Declaraciones de booleanos
    reglasSemanticas.add("boolean\\s+\\w+\\s*=\\s*(true|false);"); // Regla para declaraciones de booleanos
    reglasSemanticas.add("boolean\\s+\\w+;"); // Regla para declaraciones de booleanos sin asignación

    // Declaraciones de Strings
    reglasSemanticas.add("String\\s+\\w+\\s*=\\s*\"[^\"]*\";"); // Regla para declaraciones de String con asignación
    reglasSemanticas.add("String\\s+\\w+;"); // Regla para declaraciones de String sin asignación

    // Estructura condicional if-else
    reglasSemanticas.add("if\\s*\\(.*\\)\\s*\\{.*\\}"); // Regla para if con bloque de código
    reglasSemanticas.add("if\\s*\\(.*\\)\\s*\\{.*\\}\\s*else\\s*\\{.*\\}"); // Regla para if-else con bloques de código

    // Ciclos while y for
    reglasSemanticas.add("while\\s*\\(.*\\)\\s*\\{.*\\}"); // Regla para while con bloque de código
    reglasSemanticas.add("for\\s*\\(.*;.*;.*\\)\\s*\\{.*\\}"); // Regla para for con bloque de código

    // Asignaciones con operaciones aritméticas
    reglasSemanticas.add("\\w+\\s*=\\s*\\w+\\s*\\+\\s*\\w+;"); // Regla para suma
    reglasSemanticas.add("\\w+\\s*=\\s*\\w+\\s*-\\s*\\w+;"); // Regla para resta
    reglasSemanticas.add("\\w+\\s*=\\s*\\w+\\s*\\*\\s*\\w+;"); // Regla para multiplicación
    reglasSemanticas.add("\\w+\\s*=\\s*\\w+\\s*/\\s*\\w+;"); // Regla para división
    
    // Declaraciones de arreglos
    reglasSemanticas.add("int\\[\\]\\s+\\w+\\s*=\\s*new\\s+int\\s*\\[\\s*\\d+\\s*\\];"); // Regla para declaración de arreglos de enteros
    reglasSemanticas.add("String\\[\\]\\s+\\w+\\s*=\\s*\\{\\s*\"[^\"]*\"\\s*(,\\s*\"[^\"]*\")*\\};"); // Regla para declaración de arreglos de Strings
    reglasSemanticas.add("double\\[\\]\\s+\\w+\\s*=\\s*new\\s+double\\[\\s*\\]\\{\\s*\\d+(\\.\\d+)?\\s*(,\\s*\\d+(\\.\\d+)?)*\\};"); // Regla para declaración de arreglos de dobles

    // Declaraciones de clases
    reglasSemanticas.add("class\\s+\\w+\\s*\\{.*\\}"); // Regla para declaración de clases

    // Declaraciones de métodos
    reglasSemanticas.add("(public|private|protected)\\s+\\w+\\s+\\w+\\s*\\(.*\\)\\s*\\{.*\\}"); // Regla para declaración de métodos

    // Reglas adicionales para operadores compuestos
        reglasSemanticas.add("\\w+\\s*\\+=\\s*\\d+;");
        reglasSemanticas.add("\\w+\\s*-=\\s*\\d+(\\.\\d+)?;");
        reglasSemanticas.add("\\w+\\s*\\*=\\s*\\d+(\\.\\d+)?;");
        reglasSemanticas.add("\\w+\\s*/=\\s*\\d+(\\.\\d+)?;");

    // Declaraciones switch-case
    reglasSemanticas.add("switch\\s*\\(.*\\)\\s*\\{\\s*case\\s+\\w+\\s*:\\s*.*\\s*break;.*\\}"); // Regla para switch-case

    // Operadores lógicos en expresiones condicionales
    reglasSemanticas.add("if\\s*\\(\\s*\\w+\\s*(&&|\\|\\|)\\s*\\w+\\s*\\)\\s*\\{.*\\}"); // Regla para if con operadores lógicos

    // Operadores unarios
    reglasSemanticas.add("\\w+\\s*\\+\\+;"); // Regla para incremento
    reglasSemanticas.add("\\w+\\s*--;"); // Regla para decremento
    reglasSemanticas.add("\\+\\+\\w+;"); // Regla para incremento prefix
    reglasSemanticas.add("--\\w+;"); // Regla para decremento prefix
    
    
    }
   
       private boolean verificarRegla(String linea, String regla) {
        // Aquí puedes definir la lógica de cómo se compara una línea con la regla.
        return linea.matches(regla);
    }
     
    // Método para verificar la sintaxis y semántica usando las reglas definidas
    private boolean verificarSintaxisSemantica(String instruccion) {
        for (String regla : reglasSemanticas) {
        if (instruccion.matches(regla)) {
            return true;  // Si alguna regla coincide, la instrucción es válida
        }
        }
        return false;  // Si ninguna regla coincide, hay un error sintáctico
    }
       
       public void analizarSintaxis() {
   String texto = txtDatos.getText();
    String[] lineas = texto.split("\\n");
    boolean error = false;
    StringBuilder errores = new StringBuilder();

    for (int i = 0; i < lineas.length; i++) {
        String linea = lineas[i].trim();

        // Ignora las líneas vacías
        if (linea.isEmpty()) {
            continue;
        }

        boolean reglaCumplida = false;

        for (String regla : reglasSemanticas) {
            if (linea.matches(regla)) {
                reglaCumplida = true;
                break;
            }
        }
        
        // Verifica si la línea tiene un operador compuesto
        if (linea.matches("\\w+\\s*(\\+=|-=|\\*=|/=)\\s*\\d+(\\.\\d+)?\\s*;")) {
            reglaCumplida = true;
        }

        if (!reglaCumplida) {
            errores.append("Error sintáctico en la línea ").append(i + 1).append(": ").append(lineas[i]).append("\n");
            error = true;
}
    }

    if (error) {
        txtSintactico.setText(errores.toString());
        txtSintactico.setForeground(Color.red);
    } else {
        txtSintactico.setText("Compilado");
        txtSintactico.setForeground(Color.green);
    }
}


    private void agregarDtTabla(int pos, int col, String nombre, String simbolo, String tipo) {
        tabla.addRow(new Object[]{pos, col, nombre, simbolo, tipo});
    }

    private void limpiarTabla_Datos() {
        DefaultTableModel modeloTabla = (DefaultTableModel) tblDatos.getModel();
        modeloTabla.setRowCount(0);
         // Limpiar la tabla tblSimbolos
        DefaultTableModel modeloTablaSimbolos = (DefaultTableModel) tblSimbolos.getModel();
        modeloTablaSimbolos.setRowCount(0);
        //Limpiar areas de texto
        txtDatos.setText(null);
        txtSintactico.setText(null);
        txtconvertido.setText(null);
         
    }

    private void actualizarTabla() throws Errores {
        //Obtiene el array de los tokens econtrados
        Tokens reservadasEsc = new Tokens();
        reservadasEsc.detectarCaracteresIncorrectosRW(txtDatos.getText());
        ArrayList<String> listaTokens = reservadasEsc.separacionTokens(txtDatos.getText());

        //System.out.println("TK: " + listaTokens); //imprimir listaTokens
        //Itera sobre cada token y lo separa por cada separador que encuentre
        for (String tokenLinea : listaTokens) {
            String[] partes = tokenLinea.split("\\°|\\¬"); // Divide el token, el número de línea y número de columna
            System.out.println("Tokens: " + Arrays.toString(partes)); //imprime los tokens
            String token = partes[0];
            int numeroLinea = Integer.parseInt(partes[1]);
            int numeroColumna = Integer.parseInt(partes[2]);

            //Identifica posibles excepciones
            if (token.matches(
                    //Números no validos
                    "(?:\\d+\\.\\d+)(?:\\.\\d+)+|\\d+\\.$"
                    //Identificadores no validos
                    + "|^\\d\\w*\\D+|^[#?]\\w+|\\w+[#?]+")) {

                throw new Errores("<html> <b>" + token + "</b> no es valido, linea: " + numeroLinea + "</html>");
            }
            //Envia el token, No.linea y No.columnda a los metodos
            Buscar_Palabras_Reservadas(token, numeroLinea, numeroColumna);
            Buscar_Simbolos_Operadores(token, numeroLinea, numeroColumna);
        }
    }
            // Método para llenar la tabla de palabras reservadas (tblRW)
        public void llenarTablaReservadas(int fila, int columna, String nombre, String simbolo, String tipo) {
         modeloReservadas.addRow(new Object[]{fila, columna, nombre, simbolo, tipo});
        }

            // Método para llenar la tabla de símbolos (tblSimbolos)
        public void llenarTablaSimbolos(int fila, int columna, String nombre, String simbolo, String tipo) {
            modeloSimbolos.addRow(new Object[]{fila, columna, nombre, simbolo, tipo});
        }

    public void Buscar_Simbolos_Operadores(String token, int numeroLinea, int numeroColumna) {
        //Leer archivo JSON
        JSONParser jsonParser = new JSONParser();
        try (FileReader read = new FileReader("signos.json");) {
            // Leer archivo JSON
            Object obj = jsonParser.parse(read);

            // Lista de objetos JSON
            JSONArray listaObjetos = (JSONArray) obj;
            //System.out.println("JSON: " + listaObjetos);

            // Iterar sobre cada objeto JSON en la lista
            for (Object item : listaObjetos) {
                JSONObject jsonObject = (JSONObject) item;
                // Obtiene el array de simbolos
                JSONArray signosList = (JSONArray) jsonObject.get("simbolos");
                for (Object signo : signosList) {
                    JSONObject simbolo = (JSONObject) signo;
                    //Obtiene el valor de la clave del objeto de simbolos: []
                    String nombre = (String) simbolo.get("nombre");
                    String simb = (String) simbolo.get("simbolo");
                    String tipo = (String) simbolo.get("tipo");
                    //Si encuentra el simbolo en el JSON lo agrega en la tabla
                    if (token.equals(simb)) {
                        agregarDtTabla(numeroLinea, numeroColumna, nombre, simb, tipo);
                    }
                }
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (org.json.simple.parser.ParseException ex) {
            Logger.getLogger(Tokens.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Buscar_Palabras_Reservadas(String token, int numeroLinea, int numeroColumna) throws Errores {
        boolean encontrado = false; // Bandera para marcar si se encontró el token
        // Convertir la reservada a minúscula
        String reservada_minuscula = token.toLowerCase();

        // Leer archivo JSON
        JSONParser jsonParser = new JSONParser();
        try (FileReader read = new FileReader("reservadas.json")) {
            // Leer archivo JSON
            Object obj = jsonParser.parse(read);
            JSONArray listaObjetos = (JSONArray) obj;

            // Iterar sobre cada objeto JSON en la lista
            for (Object item : listaObjetos) {
                JSONObject jsonObject = (JSONObject) item;
                JSONArray reservadasList = (JSONArray) jsonObject.get("reservadas");

                for (Object reservada : reservadasList) {
                    JSONObject palabra = (JSONObject) reservada;
                    String nombre = (String) palabra.get("nombre");
                    String palab = (String) palabra.get("palabra");
                    String tipo = (String) palabra.get("tipo");

                    // Convertir la palabra reservada a minúsculas para la comparación
                    String palb_minuscula = palab.toLowerCase();

                    if (reservada_minuscula.equals(palb_minuscula)) {
                        // Mensaje de JOptionPane sobre la corrección
                        if (!token.equals(palab)) {
                            JOptionPane.showMessageDialog(null, "Se ha corregido: '" + token + "' a '" + palab + "'", "Corrección", JOptionPane.INFORMATION_MESSAGE);
                        }

                        agregarDtTabla(numeroLinea, numeroColumna, nombre, palab, tipo);
                        encontrado = true; // Marca como encontrado

                        // Comprobar si la palabra reservada actual es consecutiva a otra en la misma línea
                        if (tokenAnterior != null && lineaTokenAnterior == numeroLinea && !token.equals("this")) {
                            if (tokenAnterior.equals("this") && token.equals("this")) {
                                // Permite 'this' seguido de 'this' en casos como this.x = this.y;
                                continue;
                            }
                            throw new Errores("No se permiten dos palabras reservadas consecutivas en la misma línea: " + numeroLinea);
                        }
                        tokenAnterior = token; // Actualiza el token anterior solo si no es 'this'
                        lineaTokenAnterior = numeroLinea; // Actualiza el número de línea del token anterior
                        break;
                    }
                }
                if (encontrado) {
                    break; // Sale del ciclo si se encuentra la palabra reservada
                }
            }

            // Si después el token no fue encontrado, se maneja como identificador, número o cadena de texto
            if (!encontrado) {
                // Números
                if (token.matches("-?\\b\\d+(\\.\\d+)?\\b")) {
                    agregarDtTabla(numeroLinea, numeroColumna, "Numero", token, "valor");
                } else if (token.matches("[a-zA-Z][a-zA-Z0-9_$]*")) {
                    // Identificadores
                    agregarDtTabla(numeroLinea, numeroColumna, "Identificador", token, "id");
                } else if (token.matches("\"[^\\\"]*\"")) {
                    // Cadena de texto
                    agregarDtTabla(numeroLinea, numeroColumna, "Cadena de texto", token, "String");
                }
            }

            // Resetea tokenAnterior si hay un salto de línea
            if (numeroLinea != lineaTokenAnterior) {
                tokenAnterior = null;
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (org.json.simple.parser.ParseException ex) {
            Logger.getLogger(Tokens.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
private boolean verificarSintaxisSemantica(String tipo, String valor) {
    switch (tipo) {
        case "int":
            return valor.isEmpty() || valor.matches("-?\\d+"); // Números enteros o sin asignación
        case "double":
            return valor.isEmpty() || valor.matches("-?\\d+(\\.\\d+)?"); // Números decimales o sin asignación
        case "char":
            return valor.isEmpty() || valor.matches("'.'"); // Caracteres o sin asignación
        case "boolean":
            return valor.isEmpty() || valor.equals("true") || valor.equals("false"); // Booleanos o sin asignación
        case "String":
            return valor.isEmpty() || valor.matches("\".*\""); // Cadenas de texto o sin asignación
        default:
            return false; // Regresa false para tipos no manejados
    }
}
    

    public String generarArchivoTokens() {
        String tokens = "";
        //Itera en cada fila de la tabla
        for (int i = 0; i < tabla.getRowCount(); i++) {
            //Obtiene el valor de cada columna en su respectiva fila
            String simbolo = tabla.getValueAt(i, 3).toString();
            String tipo = tabla.getValueAt(i, 4).toString();

            //Identifica ciertos simbolos para agregarle los '' para que no se confunda con el formato del token
            if (simbolo.matches("\\>|\\<")) {
                String newSimb = "'" + simbolo + "'";
                tokens += "<" + tipo + " , " + newSimb + ">";
            } else {
                tokens += "<" + tipo + " , " + simbolo + ">";
            }
        }
        //Retorna los token con su respectivo formato
        return tokens;
    }

    //Guarda el archivo de tokens generado
    private void guardarLex() {
        try {
            String contenido = generarArchivoTokens();

            // Cambia la ruta y el nombre del archivo
            File fileToSave = new File("Tokens.lex");

            try (FileWriter fileWriter = new FileWriter(fileToSave)) {
                fileWriter.write(contenido);
                //JOptionPane.showMessageDialog(null, "El archivo se ha guardado correctamente!");
            } catch (IOException e) {
                e.printStackTrace();
                //JOptionPane.showMessageDialog(null, "Ocurrió un error al guardar el archivo.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Leer el archivo generado de tokens
    private String leerLex() {
        StringBuilder contenido = new StringBuilder();
        File fileToRead = new File("Tokens.lex");

        try (FileReader fileReader = new FileReader(fileToRead); BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            String linea;
            while ((linea = bufferedReader.readLine()) != null) {
                contenido.append(linea).append("\n");
            }

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "El archivo no se encontró.");
            e.printStackTrace();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Ocurrió un error al leer el archivo.");
            e.printStackTrace();
        }

        return contenido.toString();
    }

    //metodo que extrae los token del archivo generado de tokens, solo apertura y cierre de simbolos
    public String extraerTokensSimbolos() {
        String contenido = leerLex();
        //expresion para identificar el for mato de los tokens <categoria , valor>
        Pattern pattern = Pattern.compile("<([^>]+) , (?:'([^']+)'|([^>]+))>");
        Matcher matcher = pattern.matcher(contenido);
        StringBuilder resultado = new StringBuilder();

        while (matcher.find()) {
            String categoria = matcher.group(1).trim(); // Extrae la categoría
            String valor = matcher.group(2) != null ? matcher.group(2).trim() : matcher.group(3).trim(); //Extrae el valor

            switch (categoria) {
                case "id", "simbolo", "valor", "reservada", "Tipo dato", "delimitador" -> //si coincide con la categoria
                    resultado.append(valor); // agrega el valor
            }
        }
        return resultado.toString();
    }

    public boolean validarExpresionSimbolos() {
        PilaSimbolos pila = new PilaSimbolos();
        String cadena = extraerTokensSimbolos();
        //validar apertura y cierre de simbolos
        for (int i = 0; i < cadena.length(); i++) {
            if (cadena.charAt(i) == '(' || cadena.charAt(i) == '[' || cadena.charAt(i) == '{') {
                pila.insertar(cadena.charAt(i));
            } else {
                if (cadena.charAt(i) == ')') {
                    if (pila.extraer() != '(') {
                        return false;
                    }
                } else {
                    if (cadena.charAt(i) == ']') {
                        if (pila.extraer() != '[') {
                            return false;
                        }
                    } else {
                        if (cadena.charAt(i) == '}') {
                            if (pila.extraer() != '{') {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return pila.pilaVacia();
    }

    //validaciones para las demas reglas que no sea apertura y cierre de simbolos
    public ArrayList<String[]> extraerTokens() {
        String contenido = leerLex();
        Pattern pattern = Pattern.compile("<([^>]+) , (?:'([^']+)'|([^>]+))>");
        Matcher matcher = pattern.matcher(contenido);
        ArrayList<String[]> resultado = new ArrayList<>();

        while (matcher.find()) {
            String categoria = matcher.group(1).trim(); // Extrae la categoría
            String valor = matcher.group(2) != null ? matcher.group(2).trim() : matcher.group(3).trim(); //Extrae el valor
            if (Arrays.asList("id", "simbolo", "valor", "reservada", "Tipo dato",
                    "delimitador").contains(categoria)) {
                resultado.add(new String[]{categoria, valor}); // agrega la categoría y el valor
            }
        }
        return resultado;
    }
    private void guardarContenidoTablaSintactica() {
    try (FileWriter fw = new FileWriter("BDSintactica.txt", true); // true para modo append
         BufferedWriter bw = new BufferedWriter(fw);
         PrintWriter out = new PrintWriter(bw)) {

        // Acceder al modelo de la tabla desde la instancia vistaSintactica
        DefaultTableModel modeloTablaSint = vistaSintactica.getModeloTablaSintactica();
        int rowCount = modeloTablaSint.getRowCount();
        int colCount = modeloTablaSint.getColumnCount();

        for (int i = 0; i < rowCount; i++) {
            StringBuilder rowContent = new StringBuilder();
            for (int j = 0; j < colCount; j++) {
                rowContent.append(modeloTablaSint.getValueAt(i, j)).append("\t");
            }
            out.println(rowContent.toString().trim());
        }

        out.println(); // Para agregar una línea vacía entre cada análisis

    } catch (IOException e) {
        e.printStackTrace();
    }
}

    public void analisisSemantico() {
        try {
            ArrayList<String[]> tokens = extraerTokens();
            // Imprime cada token (categoría y valor)
            for (String[] token : tokens) {
                System.out.println("Categoria: " + token[0] + ", Valor: " + token[1]);
            }
            Semantico semantico = new Semantico(vistaContextos);
            semantico.analizarTokens(tokens);
        } catch (Exception ex) {
            System.out.println("ERROR SEMANTICO");
        }
    }
   
    private void procesarTokens(String texto) {
    // Limpiar las tablas antes de comenzar
    DefaultTableModel modeloSimbolos = (DefaultTableModel) tblSimbolos.getModel();
    DefaultTableModel modeloReservadas = (DefaultTableModel) tblDatos.getModel();
    modeloSimbolos.setRowCount(0);
    modeloReservadas.setRowCount(0);

    // Procesar cada línea de texto
    String[] lineas = texto.split("\\n");
    for (int numeroLinea = 0; numeroLinea < lineas.length; numeroLinea++) {
        String linea = lineas[numeroLinea].trim(); // Elimina espacios innecesarios

        if (linea.isEmpty()) {
            continue; // Saltar líneas vacías
        }

        // Reiniciar el número de columna para cada nueva línea
        int numeroColumna = 1;

        // Dividir la línea en partes considerando operadores, números decimales, y otros tokens
        String[] partes = linea.split("(?<=\\W)|(?=\\W)");
        String tokenAcumulado = "";
        for (String parte : partes) {
            if (parte.matches("\\s")) {
                numeroColumna += parte.length(); // Incrementa el número de columna según los espacios en blanco
                continue; // Ignorar espacios en blanco entre tokens
            }

            // Acumular si es parte de un número decimal
            if (parte.matches("\\d") || parte.equals(".")) {
                tokenAcumulado += parte;
                numeroColumna += parte.length(); // Actualizar columna por longitud del token
                continue;
            } else if (!tokenAcumulado.isEmpty()) {
                procesarTokenAcumulado(tokenAcumulado, numeroLinea + 1, numeroColumna - tokenAcumulado.length());
                tokenAcumulado = "";
            }

            procesarTokenAcumulado(parte, numeroLinea + 1, numeroColumna);
            numeroColumna += parte.length(); // Incrementa la columna después de procesar el token
        }

        if (!tokenAcumulado.isEmpty()) {
            procesarTokenAcumulado(tokenAcumulado, numeroLinea + 1, numeroColumna - tokenAcumulado.length());
        }
    }
}

    private String preprocesarTexto(String texto) {
    StringBuilder textoFiltrado = new StringBuilder();
    String[] lineas = texto.split("\\n");

    for (String linea : lineas) {
        if (!linea.trim().isEmpty()) {
            textoFiltrado.append(linea.trim()).append("\n");
        }
    }

    return textoFiltrado.toString().trim();
}
private void procesarTokenAcumulado(String token, int numeroLinea, int numeroColumna) {
    if (esPalabraReservada(token)) {
        llenarTablaReservadas(numeroLinea, numeroColumna, "Palabra Reservada", token, "Tipo Dato");
    } else if (esDelimitador(token)) {
        llenarTablaReservadas(numeroLinea, numeroColumna, "Delimitador", token, "fin");
    } else if (esSimbolo(token)) {
        llenarTablaReservadas(numeroLinea, numeroColumna, "Operador", token, "Operador");
    } else if (esIdentificador(token)) {
        llenarTablaSimbolos(numeroLinea, numeroColumna, "Identificador", token, "id");
    } else if (esNumero(token)) {
        llenarTablaReservadas(numeroLinea, numeroColumna, "Número", token, "valor");
    }
}

// Actualización del método de verificación de números para incluir decimales
private boolean esNumero(String token) {
    return token.matches("-?\\d+(\\.\\d+)?");
}

private boolean esDelimitador(String token) {
    String[] delimitadores = {";", ",","{","}"};
    return Arrays.asList(delimitadores).contains(token);
}

private boolean esPalabraReservada(String token) {
    String[] palabrasReservadas = {"int", "double", "if", "else", "while", "for", "return", "void", "char", "bool", "boolean", "String", "main"};
    return Arrays.asList(palabrasReservadas).contains(token);
}

private boolean esSimbolo(String token) {
    String[] simbolos = {"+", "-", "*", "/", "=", "==", ">", "<", ">=", "<=", "&&", "||", "!", ","};
    return Arrays.asList(simbolos).contains(token);
}

private boolean esIdentificador(String token) {
    // Verifica si el token es un identificador (por ejemplo, variable o nombre de función)
    return token.matches("[a-zA-Z_][a-zA-Z0-9_]*") && !esPalabraReservada(token);
}

private String extraerTipo(String linea) {
    Pattern pattern = Pattern.compile("(\\b\\w+)\\s+\\w+\\s*=");
    Matcher matcher = pattern.matcher(linea);
    if (matcher.find()) {
        return matcher.group(1); // Devuelve el tipo de dato
    }
    return "";
}

private String extraerIdentificador(String linea) {
    Pattern pattern = Pattern.compile("\\b\\w+\\s+(\\w+)\\s*=");
    Matcher matcher = pattern.matcher(linea);
    if (matcher.find()) {
        return matcher.group(1); // Devuelve el identificador
    }
    return "";
}


    //validar las demas expresiones
    public boolean validarExpresion() {
        ArrayList<String[]> tokens = extraerTokens();
        boolean esperandoIdentificador = false;
        boolean esperandoValorIdentificador = false;
        boolean esperandoDelimitadorFinal = false;

        for (int i = 0; i < tokens.size(); i++) {
            String[] tokenActual = tokens.get(i);
            String categoria = tokenActual[0];
            String valor = tokenActual[1];

            if ("reservada".equals(categoria) || "Tipo dato".equals(categoria)) {
                esperandoIdentificador = true;
            } else if ("id".equals(categoria) && esperandoIdentificador) {
                esperandoIdentificador = false;
                esperandoValorIdentificador = true; // se puede asignar un valor inmediatamente después
            } else if ("=".equals(valor) && esperandoValorIdentificador) {
                esperandoDelimitadorFinal = true; // después de un '=', debe venir un valor/identificador y luego un ';'
            } else if (("valor".equals(categoria) || "id".equals(categoria)) && esperandoValorIdentificador) {
                esperandoValorIdentificador = false;
                // No se modifica esperandoDelimitadorFinal ya que ya está en true después de ver '='
            } else if (";".equals(valor) && esperandoDelimitadorFinal) {
                // Una secuencia válida termina aquí
                esperandoDelimitadorFinal = false; // Se reinicia el estado para permitir nuevas declaraciones después
            } else {
                // Si se encuentra cualquier otro caso es una expresión inválida, espera lo que obtenga al validar los simbolos de apertura y cierre
                return validarExpresionSimbolos();
            }
        }

        // Si al final no se está esperando un identificador, valor/identificador después de '=' o un delimitador final, es válido
        return !esperandoIdentificador && !esperandoValorIdentificador && !esperandoDelimitadorFinal;
    }


        private String verificarFin(String linea) {
            return linea.trim().endsWith(";") ? ";" : "Error";
        }

        private String extraerValor(String linea) {
             Pattern pattern = Pattern.compile("=\\s*(\\S+);");
             Matcher matcher = pattern.matcher(linea);
                 if (matcher.find()) {
                 return matcher.group(1);
                }
            return "";
            }
    
        
        private void guardarDatosEnArchivo(String datos) {
                try (FileWriter fw = new FileWriter("Datos.txt", true); // true para modo append
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {

                out.println(datos); // Escribe los datos en una nueva línea

                } catch (IOException e) {
                e.printStackTrace();
            }
        }




    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        btnAbrir = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtDatos = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblDatos = new javax.swing.JTable();
        btnAnalizar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtSintactico = new javax.swing.JTextArea();
        btnContextos = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblSimbolos = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnReglas = new javax.swing.JButton();
        btnconvertir = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        txtconvertido = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(15, 95, 97));

        jLabel1.setFont(new java.awt.Font("Poppins", 1, 50)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("COMPILADOR - CÓDIGO INTERMEDIO");

        btnAbrir.setFont(new java.awt.Font("Poppins", 0, 15)); // NOI18N
        btnAbrir.setForeground(new java.awt.Color(255, 255, 255));
        btnAbrir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-abrir-carpeta-40(1).png"))); // NOI18N
        btnAbrir.setText("Abrir");
        btnAbrir.setBorderPainted(false);
        btnAbrir.setContentAreaFilled(false);
        btnAbrir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAbrir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAbrir.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-abrir-carpeta-60.png"))); // NOI18N
        btnAbrir.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-abrir-carpeta-40(1).png"))); // NOI18N
        btnAbrir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAbrirActionPerformed(evt);
            }
        });

        btnGuardar.setFont(new java.awt.Font("Poppins", 0, 15)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-guardar-como-40.png"))); // NOI18N
        btnGuardar.setText("Guardar como");
        btnGuardar.setBorderPainted(false);
        btnGuardar.setContentAreaFilled(false);
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnGuardar.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-guardar-como-60.png"))); // NOI18N
        btnGuardar.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-guardar-como-40.png"))); // NOI18N
        btnGuardar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        txtDatos.setColumns(20);
        txtDatos.setFont(new java.awt.Font("Poppins", 0, 16)); // NOI18N
        txtDatos.setRows(5);
        jScrollPane1.setViewportView(txtDatos);

        jLabel2.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Datos Ingresados");

        jLabel3.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Tabla de Palabras Reservadas");

        tblDatos.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        tblDatos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "No.", "Símbolo", "Nombre"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(tblDatos);
        if (tblDatos.getColumnModel().getColumnCount() > 0) {
            tblDatos.getColumnModel().getColumn(0).setResizable(false);
            tblDatos.getColumnModel().getColumn(1).setResizable(false);
            tblDatos.getColumnModel().getColumn(2).setResizable(false);
        }

        btnAnalizar.setFont(new java.awt.Font("Poppins", 0, 15)); // NOI18N
        btnAnalizar.setForeground(new java.awt.Color(255, 255, 255));
        btnAnalizar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-ver-50.png"))); // NOI18N
        btnAnalizar.setText("Analizar");
        btnAnalizar.setBorderPainted(false);
        btnAnalizar.setContentAreaFilled(false);
        btnAnalizar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAnalizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAnalizar.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-ver-70.png"))); // NOI18N
        btnAnalizar.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-ver-50.png"))); // NOI18N
        btnAnalizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAnalizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnalizarActionPerformed(evt);
            }
        });

        btnLimpiar.setFont(new java.awt.Font("Poppins", 0, 15)); // NOI18N
        btnLimpiar.setForeground(new java.awt.Color(255, 255, 255));
        btnLimpiar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-limpiar-50.png"))); // NOI18N
        btnLimpiar.setText("Limpiar");
        btnLimpiar.setBorderPainted(false);
        btnLimpiar.setContentAreaFilled(false);
        btnLimpiar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLimpiar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLimpiar.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-limpiar-70.png"))); // NOI18N
        btnLimpiar.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-limpiar-50.png"))); // NOI18N
        btnLimpiar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        txtSintactico.setEditable(false);
        txtSintactico.setColumns(20);
        txtSintactico.setFont(new java.awt.Font("Poppins", 1, 15)); // NOI18N
        txtSintactico.setRows(5);
        jScrollPane2.setViewportView(txtSintactico);

        btnContextos.setBackground(new java.awt.Color(0, 93, 139));
        btnContextos.setFont(new java.awt.Font("Poppins", 0, 15)); // NOI18N
        btnContextos.setForeground(new java.awt.Color(255, 255, 255));
        btnContextos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-context-50.png"))); // NOI18N
        btnContextos.setText("Contextos");
        btnContextos.setBorderPainted(false);
        btnContextos.setContentAreaFilled(false);
        btnContextos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnContextos.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnContextos.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-context-70.png"))); // NOI18N
        btnContextos.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-context-50.png"))); // NOI18N
        btnContextos.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-context-50.png"))); // NOI18N
        btnContextos.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnContextos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContextosActionPerformed(evt);
            }
        });

        tblSimbolos.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        tblSimbolos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "No.", "Símbolo", "Nombre"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(tblSimbolos);
        if (tblSimbolos.getColumnModel().getColumnCount() > 0) {
            tblSimbolos.getColumnModel().getColumn(0).setResizable(false);
            tblSimbolos.getColumnModel().getColumn(1).setResizable(false);
            tblSimbolos.getColumnModel().getColumn(2).setResizable(false);
        }

        jLabel4.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Tabla de Símbolos");

        jLabel5.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Análisis Sintáctico");

        btnReglas.setFont(new java.awt.Font("Poppins", 0, 15)); // NOI18N
        btnReglas.setForeground(new java.awt.Color(255, 255, 255));
        btnReglas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-reglas-50.png"))); // NOI18N
        btnReglas.setText("Reglas");
        btnReglas.setBorderPainted(false);
        btnReglas.setContentAreaFilled(false);
        btnReglas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnReglas.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReglas.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-reglas-70.png"))); // NOI18N
        btnReglas.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-reglas-50.png"))); // NOI18N
        btnReglas.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-reglas-50.png"))); // NOI18N
        btnReglas.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnReglas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReglasActionPerformed(evt);
            }
        });

        btnconvertir.setFont(new java.awt.Font("Poppins", 0, 15)); // NOI18N
        btnconvertir.setForeground(new java.awt.Color(255, 255, 255));
        btnconvertir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-convertir-60.png"))); // NOI18N
        btnconvertir.setText("Cod Intermedio");
        btnconvertir.setBorderPainted(false);
        btnconvertir.setContentAreaFilled(false);
        btnconvertir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnconvertir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnconvertir.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-convertir-70.png"))); // NOI18N
        btnconvertir.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-convertir-60.png"))); // NOI18N
        btnconvertir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnconvertir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnconvertirActionPerformed(evt);
            }
        });

        txtconvertido.setColumns(20);
        txtconvertido.setFont(new java.awt.Font("Poppins", 0, 16)); // NOI18N
        txtconvertido.setRows(5);
        jScrollPane5.setViewportView(txtconvertido);

        jLabel6.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Código Intermedio");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(104, 104, 104)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(371, 371, 371)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(178, 178, 178))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel3)
                                .addGap(218, 218, 218)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(53, 53, 53)
                                .addComponent(btnAbrir)
                                .addGap(139, 139, 139)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnGuardar)))
                        .addGap(84, 84, 84))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(123, 123, 123)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(58, 58, 58)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnContextos)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(btnLimpiar))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(btnReglas)
                                .addComponent(btnAnalizar)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 64, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 468, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(90, 90, 90))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 468, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(99, 99, 99)))
                .addGap(90, 90, 90)
                .addComponent(btnconvertir)
                .addGap(179, 179, 179))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAbrir)
                    .addComponent(btnGuardar)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(jLabel6)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(89, 89, 89))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 435, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addComponent(btnconvertir)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(28, 28, 28)
                                .addComponent(jLabel5))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnAnalizar)
                                .addGap(32, 32, 32)
                                .addComponent(btnReglas, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(49, 49, 49)
                                .addComponent(btnContextos)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnLimpiar)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 10, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbrirActionPerformed
        EstiloJfilechooser();
        JFileChooser chooser = new JFileChooser();
        //Filtro para buscar archivos
        chooser.setFileFilter(new FiltroArchivos(".java", "Archivo java"));
        chooser.setFileFilter(new FiltroArchivos(".chalk", "CHALK"));

        chooser.showOpenDialog(null);
        File archivo = new File(chooser.getSelectedFile().getAbsolutePath());

        try {
            String ST = new String(Files.readAllBytes(archivo.toPath()));
            txtDatos.setText(ST);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Interfaz.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Interfaz.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAbrirActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        // Crea un JFileChooser para seleccionar dónde guardar el archivo
        EstiloJfilechooser();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar");
        //Filtro extensión propia
        fileChooser.setFileFilter(new FiltroArchivos(".chalk", "CHALK"));

        int selection = fileChooser.showSaveDialog(null);

        if (selection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            try (FileWriter fileWriter = new FileWriter(fileToSave + ".chalk")) {
                fileWriter.write(txtDatos.getText());
                JOptionPane.showMessageDialog(null, "El archivo se ha guardado correctamente!");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Ocurrió un error al guardar el archivo.");
            }
        }
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnAnalizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnalizarActionPerformed
    //Reset de variables
    String texto = txtDatos.getText();
    procesarTokens(texto);
    tokenAnterior = null;
    lineaTokenAnterior = -1;
    tabla.setRowCount(0); //resetea la tabla
    vistaContextos.tablaCtx.setRowCount(0); //resetea la tabla de contextos

    try {
        actualizarTabla();
        guardarLex();
        analisisSemantico();
        guardarDatosEnArchivo(texto); // Aquí se guarda el texto procesado

        boolean esValido = true;
        String[] lineas = texto.split("\\n");

        for (String linea : lineas) {
            if (!verificarSintaxisSemantica(linea)) {
                esValido = false;
                txtSintactico.setText("Error sintáctico en la línea: " + linea);
                txtSintactico.setForeground(Color.red);
                break;
            }
        }

        if (esValido) {
            txtSintactico.setText("Compilado");
            txtSintactico.setForeground(Color.green);
        }
    } catch (Errores ex) {
        Logger.getLogger(Interfaz.class.getName()).log(Level.SEVERE, null, ex);
        txtSintactico.setText(null);
    }
    }//GEN-LAST:event_btnAnalizarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiarTabla_Datos();
        extraerTokensSimbolos(); //dafa
        vistaContextos.tablaCtx.setRowCount(0);
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnContextosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContextosActionPerformed
        vistaContextos.setDefaultCloseOperation(tablaContextos.DISPOSE_ON_CLOSE);
        vistaContextos.setVisible(true);
    }//GEN-LAST:event_btnContextosActionPerformed

    private void btnReglasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReglasActionPerformed
     vistaSintactica.setDefaultCloseOperation(tablaSintactica.DISPOSE_ON_CLOSE);
    vistaSintactica.setVisible(true);

    DefaultTableModel modeloTablaSint = vistaSintactica.getModeloTablaSintactica();
    modeloTablaSint.setRowCount(0);

    String[] lineas = txtDatos.getText().split("\\n");

    for (String linea : lineas) {
        linea = linea.trim();

        if (linea.isEmpty()) {
            continue;
        }

        String tipo = extraerTipo(linea);
        String identificador = extraerIdentificador(linea);
        String asignacion = linea.contains("=") ? "=" : "";
        String valor = asignacion.equals("=") ? extraerValor(linea) : "";
        String fin = verificarFin(linea);
        String resultado = "";

        // Validación detallada
        boolean tipoValido = !tipo.isEmpty();
        boolean identificadorValido = !identificador.isEmpty();
        boolean finValido = fin.equals(";");

        // Verifica si el valor es correcto según el tipo solo si hay asignación
        boolean valorValido = asignacion.isEmpty() || verificarSintaxisSemantica(tipo, valor);

        // Evaluar el resultado final
        if (tipoValido && identificadorValido && finValido && valorValido) {
            resultado = "Cumple";
        } else {
            resultado = "No cumple";
        }

        // Llenar la tabla con los valores evaluados
        modeloTablaSint.addRow(new Object[]{tipo, identificador, asignacion, valor, fin, resultado});
    }
        // Guardar el contenido de la tabla en el archivo BDSintactica.txt
        guardarContenidoTablaSintactica();
    }//GEN-LAST:event_btnReglasActionPerformed

    private void btnconvertirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnconvertirActionPerformed
      // Obtener el texto ingresado
    String codigoFuente = txtDatos.getText().trim();  // Obtener el texto y eliminar espacios en blanco
    
    // Validar si el campo está vacío
    if (codigoFuente.isEmpty()) {
        // Mostrar mensaje de advertencia si no hay texto
        JOptionPane.showMessageDialog(this, "Por favor, ingrese una expresión válida.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        return; // Detener el proceso si no hay texto
    }
    
    // Continuar con la conversión si el campo no está vacío
    GeneradorTresDirecciones generador = new GeneradorTresDirecciones();
    String codigoIntermedio = generador.convertirA3Direcciones(codigoFuente);
    txtconvertido.setText(codigoIntermedio);
    }//GEN-LAST:event_btnconvertirActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Interfaz().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbrir;
    private javax.swing.JButton btnAnalizar;
    private javax.swing.JButton btnContextos;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnReglas;
    private javax.swing.JButton btnconvertir;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTable tblDatos;
    private javax.swing.JTable tblSimbolos;
    private javax.swing.JTextArea txtDatos;
    private javax.swing.JTextArea txtSintactico;
    private javax.swing.JTextArea txtconvertido;
    // End of variables declaration//GEN-END:variables
}
