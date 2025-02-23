package Codigo;

import Excepciones.Errores;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author juani
 */
public class Tokens {

    //método para descartar las palabras reservadas con caracteres incorrectos
    public void detectarCaracteresIncorrectosRW(String texto) throws Errores {
        // Leer archivo JSON
        JSONParser jsonParser = new JSONParser();
        try (FileReader read = new FileReader("reservadas.json")) {
            // Leer archivo JSON
            Object obj = jsonParser.parse(read);
            JSONArray listaObjetos = (JSONArray) obj;

            // Dividir el texto en líneas para detectar la reservada incorrecta
            String[] txtLineas = texto.split("\\r?\\n+");

            // Iterar sobre cada línea
            for (String linea : txtLineas) {
                // Iterar sobre cada objeto JSON en la lista
                for (Object item : listaObjetos) {
                    JSONObject jsonObject = (JSONObject) item;
                    JSONArray reservadasList = (JSONArray) jsonObject.get("reservadas");

                    for (Object reservada : reservadasList) {
                        JSONObject palabra = (JSONObject) reservada;
                        String palab = (String) palabra.get("palabra");
                        // Expresión para detectar los caracteres
                        Pattern pattern = Pattern.compile("\\b" + palab + "\\b\\s*[\\’\\$\\'\\#\\!\\/\\\"]+\\s*\\w+\\;");
                        Matcher matcher = pattern.matcher(linea);
                        // Si se encuentra la expresión
                        if (matcher.find()) {
                            // Reservada incorrecta
                            String token = matcher.group();
                            // Separación de reservadas segun sus caracteres
                            String[] partesReservada = token.split("[\\’\\$\\'\\#\\\"\\!\\/\\\"]");
                            // Reservada correcta
                            String resultado = String.join("", partesReservada);
                            //lanza el aviso
                            throw new Errores("<html><b>" + token + "</b> es incorrecto, sugerencia: <b>" + resultado + "</b>");
                        }
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

    public ArrayList<String> separacionTokens(String texto) throws Errores {
        //Crea una lista para almacenar los tokens
        ArrayList<String> tokens = new ArrayList<>();
        // Detecta los tokens establecidos a base de una expresión regular
        Pattern pattern = Pattern.compile(
                //comentarios y saltos de línea
                "//[^\\n]*|/\\*.*?\\*/|\\n+"
                //Cadenas de Texto
                + "|\"[^\\\"]*\""
                //combinación de caracteres
                + "|[#?]?\\d*[a-zA-Z_$][\\w$]*[#?]*"
                //combinación de números
                + "|-?\\d+(\\.\\d+)*\\.?"
                //símbolos
                + "|\\=\\=|\\;|\\+\\+|\\-\\-|\\+|\\-|\\)|\\(|\\>|\\<|\\,|\\=|\\}|\\{|\\]|\\[", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(texto);

        int numeroLinea = 1;
        int numeroColumna = 1; // Inicializa el contador de tokens por línea

        while (matcher.find()) {
            String token = matcher.group();
            // Ignora los comentarios para añadirlos al array
            if (token.startsWith("//") || token.startsWith("/*")) {
                // Ajusta el número de línea para comentarios de bloque que contienen saltos de línea
                if (token.startsWith("/*")) {
                    numeroLinea += token.split("\n", -1).length - 1;
                }
                continue; // No agregar comentarios al array
            }
            // Ajuste específico para saltos de línea
            if (token.matches("\\n+")) {
                // Incrementar el contador de líneas pero no agregar el token de salto de línea
                numeroLinea += token.length(); // Considerar la cantidad de saltos de línea
                numeroColumna = 1; // Restablecer el contador de columna al inicio de la nueva línea
                continue; // No añadir este token al ArrayList de tokens
}
            // Añade separedores entre el numero de linea y columna para despues procesarlos de manera correcta
            tokens.add(token + "°" + numeroLinea + "¬" + numeroColumna);
            numeroColumna++;
        }
        return tokens;
    }
}
