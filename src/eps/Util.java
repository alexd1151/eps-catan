
package eps;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Utilidades varias para manipular ficheros y comparar strings
 * linea a linea.
 */
public class Util {

    static Random random;
    
    /**
     * Reads a resource to a string
     * (uses the classpath to find it)
     */
    public static String leeRecursoACadena(String name) throws IOException {
        InputStream is = Util.class.getClassLoader().getResourceAsStream(name);
        if (is == null) {
            throw new IOException("Recurso no encontrado: '"+name+"'");
        }
        return leeACadena(is);
    }
    
    /**
     * Reads a file to a string
     * (uses the file's path to find it)
     */
    public static String leeFicheroACadena(File f) throws IOException {
        return leeACadena(new FileInputStream(f));
    }
    
    /**
     * Reads an inputstream to a string
     */
    public static String leeACadena(InputStream is) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s+"\n");
            }
            return sb.toString();
        }
        finally {
            /*
             * Esto se ejecuta suceda o no una excepcion; si no se cierra
             * explicitamente, se acumulan 'filehandles' abiertos en la JVM,
             * que solo se liberan cuando pasa el recolector de basura...
             */
            try {
                if (br != null) br.close();
            }
            catch (Exception e) {
            }
        }
    }  

    /**
     * Writes a string to a file
     */
    public static void escribeCadenaAFichero(String s, File f) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(fos));            
            bw.write(s);
        }
        finally {
            try {
                if (bw != null) bw.close();
            }
            catch (Exception e) {                
            }
        }
    }             
    
    /**
     * Compara las primeras N lineas de dos 'Strings'. Devuelve 'false'
     * y muestra la primera linea donde no son identicas en caso de fallo.
     * (Si n > numero de lineas de ambas, y son iguales hasta ese punto, 
     * devuelve 'true').
     */
    public static boolean comienzanIgual(String a, String b, int n) {
        StringTokenizer ta = new StringTokenizer(a, "\n");
        StringTokenizer tb = new StringTokenizer(b, "\n");
        for (int i=0; i<n; i++) {
            if ( ! ta.hasMoreTokens() && tb.hasMoreTokens()) {
                System.err.println("Fin de cadena 1 en linea " + (i+1) + "\n");
                return false;
            }
            if ( ! tb.hasMoreTokens() && ta.hasMoreTokens()) {
                System.err.println("Fin de cadena 2 en linea " + (i+1) + "\n");
                return false;
            }
            if ( ! ta.hasMoreTokens() && ! tb.hasMoreTokens()) {
                break;
            }

            String sa = ta.nextToken();
            String sb = tb.nextToken();
            if ( ! sa.equals(sb)) {
                System.err.println("Cadenas distintas en linea " + (i+1) + ":\n" +
                        "\t"+sa+"\n\t"+sb+"\n");
                return false;
            }
        }
        return true;
    }    
    
    /**
     * Devuelve el generador de numeros aleatorios actual
     */
    public static Random getRandom() {
        return random;
    }
    
    /**
     * Lee una imagen, que debe estar dentro del classpath
     */
    public static Image getImage(String path) {
        URL imgUrl = Util.class.getClassLoader().getResource(path);
        return Toolkit.getDefaultToolkit().createImage(imgUrl);
    }

    /**
     * Compara dos 'Strings' linea a linea, esperando que
     * ambas acaben a la vez.
     */
    public static boolean comienzanIgual(String a, String b) {
        return comienzanIgual(a, b, Integer.MAX_VALUE);
    }
    
    /**
     * Inicializa el generador de numeros aleatorios con una semilla aleatoria
     * (el numero de milisegundos sacado del reloj del sistema)
     */
    public static void initRandom() {
        random = new Random(System.currentTimeMillis());
    }
    
    /**
     * Inicializa el generador de numeros aleatorios a un numero conocido
     * (util para hacer pruebas)
     */
    public static void initRandom(long semilla) {
        random = new Random(semilla);
    }
    
    /**
     * Tira un dado de N caras. Se garantiza que la secuencia de valores de 
     * dos dados es la misma si, antes de generar cada una, se ha llamado a 
     * "initRandom" en ambos dados con la misma semilla.
     * @param n el numero de caras
     * @return el valor obtenido, un numero entre 1 y n (ambos inclusive)
     */
    public static int tiraDado(int n) {
        if (random == null) initRandom();
        return random.nextInt(n) + 1;
    }
    
    /**
     * Baraja una lista de elementos, usando un dado.
     * Por tanto, esto es igual de predecible que usar "tiraDado":
     * conocida la semilla, es posible predecir la secuencia de
     * permutaciones resultante.
     */
    public static void baraja(List<?> lista) {
        if (random == null) initRandom();
        Collections.shuffle(lista, random);
    }
    
    /**
     * Predicado que indica si la cadena que se le pasa como parámetro es un
     * entero
     */
    public static boolean esEntero(String str)
    {
        boolean retorno = true;
        
        try {
            Integer.valueOf(str);
        }
        catch(NumberFormatException err) {
            retorno = false;
        }
        return(retorno);
    }
}
